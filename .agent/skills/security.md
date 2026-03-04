# Skill: Seguridad JWT

## Proposito
Arquitectura de seguridad JWT, filtros de autenticacion, SecurityConfig y propagacion de headers entre servicios.

## Arquitectura de Seguridad

```
Frontend → api-gateway (JwtValidationFilter reactivo)
                ↓
    Agrega headers: X-User-Role, X-Organization-Id
                ↓
    Servicios downstream (JwtAuthenticationFilter Servlet)
```

Dos niveles de validacion:
1. **api-gateway**: `JwtValidationFilter` (Spring Cloud Gateway, reactivo WebFlux) — valida el token y propaga headers
2. **servicios individuales** (auth-service, content-service): `JwtAuthenticationFilter` (Spring MVC, Servlet) — valida el token y puebla `SecurityContext`

## Claims del JWT

- `sub`: email del usuario
- `role`: nombre del enum `UserRole` (ADMIN, TEACHER, STUDENT)
- `organizationId`: UUID como String

## JwtService — Parseo (content-service, gateway)

```java
@Component
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.signingKey = createSigningKey(secret);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey createSigningKey(String secret) {
        if (secret.length() >= 32) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        byte[] decoded = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(decoded);
    }
}
```

## JwtService — Generacion (solo auth-service)

Adicionalmente tiene:
```java
public String generateToken(String email, UserRole role, UUID organizationId) {
    Instant now = Instant.now();
    return Jwts.builder()
            .subject(email)
            .claim("role", role.name())
            .claim("organizationId", organizationId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
            .signWith(signingKey)
            .compact();
}
```

## JwtAuthenticationFilter — Servicios MVC (Servlet)

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/health") || path.equals("/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.parseClaims(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            String organizationId = claims.get("organizationId", String.class);

            if (email != null && role != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                if (organizationId != null) {
                    auth.setDetails(Map.of("organizationId", organizationId));
                }

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid JWT\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

## SecurityConfig — Servicios MVC

```java
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    (req, res, e) -> res.sendError(HttpStatus.UNAUTHORIZED.value())))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/health", "/info", "/api/v1/ping", "/error").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## SecurityConfig — API Gateway (WebFlux)

El gateway usa `ServerHttpSecurity` (reactivo):

```java
@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
            .build();
}
```

La logica de autorizacion real esta en `JwtValidationFilter` que implementa `GlobalFilter` con `@Order(-100)`.

## Propagacion de Headers por el Gateway

| Header              | Contenido                                  |
|---------------------|--------------------------------------------|
| `X-User-Role`       | Nombre del rol (ADMIN/TEACHER/STUDENT)     |
| `X-Organization-Id` | UUID de la organizacion del usuario        |

Los servicios downstream sin JWT propio acceden a estos headers directamente via `@RequestHeader`.

## Checklist — Agregar Seguridad a un Servicio Nuevo

1. Copiar `JwtService.java` (version parseo-only) al paquete `security/`
2. Copiar `JwtAuthenticationFilter.java` al paquete `security/`
3. Crear `SecurityConfig.java` con las rutas publicas del servicio
4. Agregar dependencias JJWT y spring-security al `pom.xml`
5. Agregar `security.jwt.secret` al `application.yml`
6. Verificar que `/health`, `/info`, `/api/v1/ping` y `/error` son publicos

## Referencias en el Repo

- `services/auth-service/src/main/java/com/gestorelearning/auth/service/JwtService.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/security/JwtAuthenticationFilter.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/security/SecurityConfig.java`
- `services/content-service/src/main/java/com/gestorelearning/content/security/`
- `services/api-gateway/src/main/java/com/gestorelearning/gateway/filter/JwtValidationFilter.java`
