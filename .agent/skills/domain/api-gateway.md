# Skill: API Gateway

## Proposito
Configuracion de rutas en Spring Cloud Gateway, StripPrefix, JwtValidationFilter reactivo, CORS y agregar nuevas rutas.

## Reglas Obligatorias

1. Cada servicio downstream tiene una ruta dedicada en el gateway.
2. Rutas usan `StripPrefix=1` excepto `/api/v1/auth/**` (ruta directa al auth-service).
3. `JwtValidationFilter` es un `GlobalFilter` con `@Order(-100)` — se ejecuta antes de las rutas.
4. Rutas publicas (login, register, ping, health) deben estar en la whitelist del filtro.
5. URLs de servicios se leen de variables de entorno con defaults Docker.

## Configuracion de Rutas — application.yml

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service-api
          uri: ${AUTH_SERVICE_URL:http://auth-service:8081}
          predicates:
            - Path=/api/v1/auth/**

        - id: auth-service
          uri: ${AUTH_SERVICE_URL:http://auth-service:8081}
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1

        - id: content-service
          uri: ${CONTENT_SERVICE_URL:http://content-service:8082}
          predicates:
            - Path=/content/**
          filters:
            - StripPrefix=1

        - id: rag-service
          uri: ${RAG_SERVICE_URL:http://rag-service:8083}
          predicates:
            - Path=/rag/**
          filters:
            - StripPrefix=1

        # ... patron identico para exam, grading, integrity, llm
```

## Agregar una Ruta Nueva — Procedimiento

1. Agrega el bloque de ruta en `services/api-gateway/src/main/resources/application.yml`
2. Usa la variable de entorno `${NOMBRE_SERVICE_URL:http://<nombre>:<puerto>}` como URI
3. Define el `Path` predicate con el prefijo del servicio
4. Agrega `StripPrefix=1` en filters (salvo que el servicio necesite recibir el prefijo)
5. Agrega la variable de entorno al `docker-compose.yml` del api-gateway
6. Si hay endpoints publicos nuevos, agregalos a la whitelist del `JwtValidationFilter`

## JwtValidationFilter — GlobalFilter Reactivo

```java
@Component
@Order(-100)
public class JwtValidationFilter implements GlobalFilter {

    private final JwtService jwtService;
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/organizations",
            "/health", "/info"
    );

    public JwtValidationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Rutas publicas — no requieren JWT
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extraer y validar token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.parseClaims(token);
            String role = claims.get("role", String.class);
            String organizationId = claims.get("organizationId", String.class);

            // Propagar headers downstream
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Role", role)
                    .header("X-Organization-Id", organizationId)
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (JwtException ex) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.endsWith("/ping");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
```

## CORS — Configuracion en el Gateway

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "${CORS_ALLOWED_ORIGINS:http://localhost:4200}"
            allowedMethods: ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
            allowedHeaders: ["*"]
            allowCredentials: true
```

## Variables de Entorno del Gateway

| Variable                | Default                            |
|-------------------------|------------------------------------|
| `AUTH_SERVICE_URL`      | `http://auth-service:8081`         |
| `CONTENT_SERVICE_URL`   | `http://content-service:8082`      |
| `RAG_SERVICE_URL`       | `http://rag-service:8083`          |
| `EXAM_SERVICE_URL`      | `http://exam-service:8084`         |
| `GRADING_SERVICE_URL`   | `http://grading-service:8085`      |
| `INTEGRITY_SERVICE_URL` | `http://integrity-service:8086`    |
| `LLM_SERVICE_URL`       | `http://llm-orchestrator:8087`     |
| `JWT_SECRET`            | (obligatorio en produccion)        |

## Anti-patrones a Evitar

- Configurar CORS en cada servicio individual — solo en el gateway
- Omitir `StripPrefix` y que el servicio downstream reciba el prefijo inesperadamente
- Olvidar agregar endpoints publicos nuevos a la whitelist del filtro JWT
- Hardcodear URLs de servicios — siempre usar variables de entorno

## Referencias en el Repo

- `services/api-gateway/src/main/resources/application.yml`
- `services/api-gateway/src/main/java/com/gestorelearning/gateway/filter/JwtValidationFilter.java`
- `services/api-gateway/src/main/java/com/gestorelearning/gateway/config/SecurityConfig.java`
