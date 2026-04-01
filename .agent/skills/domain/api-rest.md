# Skill: Endpoints REST

## Proposito
Convenciones para controladores, DTOs, validaciones y manejo de errores en endpoints REST.

## Reglas Obligatorias

1. Todos los endpoints usan el prefijo `/api/v1/<recurso>`.
2. Inyeccion por constructor — nunca `@Autowired` en campo.
3. DTOs son Java `record` con anotaciones de `jakarta.validation`.
4. Nunca exponer entidades JPA directamente en la respuesta.
5. Logica de negocio en `@Service`, nunca en el controlador.

## Convencion General de Controladores

```java
@RestController
@RequestMapping("/api/v1/<recurso>")
public class RecursoController {

    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping("/{id}")
    public RecursoResponse getById(@PathVariable UUID id) {
        return recursoService.findById(id);
    }

    @PostMapping
    public RecursoResponse create(@Valid @RequestBody CrearRecursoRequest request) {
        return recursoService.create(request);
    }
}
```

## DTOs — Usar Java Records

```java
// Request
public record CrearRecursoRequest(
        @NotBlank String nombre,
        @NotNull UUID organizationId,
        @Email @NotBlank String email,
        @Size(min = 8, max = 100) String password
) {}

// Response
public record RecursoResponse(
        UUID id,
        String nombre,
        Instant createdAt
) {}
```

## Manejo de Errores

Usar `ResponseStatusException` de Spring Web:

```java
throw new ResponseStatusException(HttpStatus.CONFLICT, "El recurso ya existe");
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado");
throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permisos suficientes");
```

No crear `@ControllerAdvice` global salvo que sea transversal a todo el servicio.

## Rutas Publicas vs. Protegidas

Rutas SIEMPRE publicas (sin JWT):
- `GET /health`, `GET /info`, `GET /api/v1/ping`, `/error`

Rutas publicas especificas del auth-service:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/auth/organizations`

Todo lo demas requiere JWT valido.

## Acceso al Usuario Autenticado

En servicios con JWT propio (auth-service, content-service):

```java
@GetMapping("/me")
public RecursoResponse me(Authentication authentication) {
    String email = authentication.getName();
    Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
    UUID organizationId = UUID.fromString((String) details.get("organizationId"));
    return recursoService.findByEmailAndOrg(email, organizationId);
}
```

En servicios sin JWT propio (rag, exam, etc.), datos por headers:

```java
@GetMapping("/mis-recursos")
public List<RecursoResponse> getMios(
        @RequestHeader("X-User-Role") String role,
        @RequestHeader("X-Organization-Id") UUID organizationId) {
    return recursoService.findByOrg(organizationId);
}
```

## Seguridad por Rol en SecurityConfig

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/health", "/info", "/api/v1/ping", "/error").permitAll()
    .requestMatchers("/api/v1/recurso/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/recurso/docente/**").hasAnyRole("ADMIN", "TEACHER")
    .anyRequest().authenticated()
)
```

Los roles tienen prefijo `ROLE_` porque `JwtAuthenticationFilter` los registra como
`new SimpleGrantedAuthority("ROLE_" + role)`. Por eso `hasRole("ADMIN")` busca `ROLE_ADMIN`.

## Referencias en el Repo

- `services/auth-service/src/main/java/com/gestorelearning/auth/controller/AuthController.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/dto/RegisterRequest.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/dto/LoginRequest.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/dto/AuthUserResponse.java`
