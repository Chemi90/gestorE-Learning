# Skill: Estructura de Microservicios

## Proposito
Convenciones de paquetes, nomenclatura y checklist para crear o modificar servicios en el monorepo.

## Paquete Base

Todos los servicios usan el grupo `com.gestorelearning` y el subpaquete del servicio:

```
com.gestorelearning.<servicio>/
├── <Servicio>Application.java       # @SpringBootApplication
├── PingController.java               # GET /api/v1/ping — siempre presente
├── controller/                       # @RestController — solo HTTP in/out
├── domain/                           # @Entity, enums de dominio
├── dto/                              # Java records con @Valid
├── repository/                       # interfaces JpaRepository<E, UUID>
├── service/                          # @Service — logica de negocio
└── security/                         # JwtService, JwtAuthenticationFilter, SecurityConfig
```

El paquete `security/` SOLO existe en servicios que validan JWT directamente.
Servicios sin JWT propio (rag, exam, grading, integrity, llm-orchestrator) no tienen `security/`.

## Convenciones de Nomenclatura

| Elemento           | Convencion                           | Ejemplo                        |
|--------------------|--------------------------------------|--------------------------------|
| Entidad JPA        | `<Nombre>Entity`                     | `UserEntity`, `CourseEntity`   |
| Repositorio        | `<Nombre>Repository`                 | `UserRepository`               |
| Servicio           | `<Nombre>Service`                    | `AuthService`, `CourseService` |
| Controlador        | `<Nombre>Controller`                 | `AuthController`               |
| DTO request        | `<Nombre>Request`                    | `RegisterRequest`              |
| DTO response       | `<Nombre>Response`                   | `AuthUserResponse`             |
| Enum de dominio    | `<Nombre>` (sin sufijo)              | `UserRole`                     |
| Filtro JWT         | `JwtAuthenticationFilter`            | (mismo en todos los servicios) |
| Servicio JWT       | `JwtService`                         | (mismo en todos los servicios) |

## PingController — Obligatorio en Todo Servicio

```java
package com.gestorelearning.<servicio>;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PingController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
                "service", "<nombre-servicio>",
                "status", "ok",
                "time", Instant.now().toString()
        );
    }
}
```

## application.yml — Estructura Minima por Servicio

```yaml
server:
  port: <puerto>

spring:
  application:
    name: <nombre-servicio>
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/elearning}
    username: ${SPRING_DATASOURCE_USERNAME:elearning}
    password: ${SPRING_DATASOURCE_PASSWORD:elearning}
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        default_schema: <schema>

management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      base-path: /
      path-mapping:
        health: health
        info: info
      exposure:
        include: health,info

security:
  jwt:
    secret: ${JWT_SECRET:01234567890123456789012345678901}
    expiration-minutes: ${JWT_EXPIRATION_MINUTES:60}
```

## Crear un Servicio Nuevo — Checklist

1. Crea el directorio `services/<nombre-servicio>/src/main/java/com/gestorelearning/<nombre>/`
2. Crea `pom.xml` con parent `services-parent` (ver patron en `services/auth-service/pom.xml`)
3. Agrega el modulo en `services/pom.xml` dentro de `<modules>`
4. Crea la clase `<Nombre>Application.java` con `@SpringBootApplication`
5. Crea `PingController.java`
6. Crea `src/main/resources/application.yml`
7. Crea `src/test/resources/application-test.yml` con H2
8. Crea `PingControllerTest.java` (ver skill `testing.md`)
9. Crea el `Dockerfile` copiando TODOS los `pom.xml` (ver skill `docker.md`)
10. Agrega el servicio en `infra/docker-compose.yml`
11. Agrega la ruta en el gateway (ver skill `gateway.md`)
12. Crea el script SQL de init en `infra/postgres/init/`

## Modulos Maven

El POM padre esta en `services/pom.xml`. Cada modulo hijo declara:

```xml
<parent>
  <groupId>com.gestorelearning</groupId>
  <artifactId>services-parent</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <relativePath>../pom.xml</relativePath>
</parent>
```

Dependencias comunes disponibles del parent (no necesitan version):
- `spring-boot-starter-web`
- `spring-boot-starter-actuator`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-test`
- `spring-security-test`
- `com.h2database:h2` (scope test)
- `org.postgresql:postgresql` (scope runtime)

JJWT requiere version explicita `0.12.6`:
- `io.jsonwebtoken:jjwt-api`
- `io.jsonwebtoken:jjwt-impl` (scope runtime)
- `io.jsonwebtoken:jjwt-jackson` (scope runtime)

## Referencias en el Repo

- `services/auth-service/` — servicio completo con JWT, entidades, tests
- `services/content-service/` — servicio con JWT propio y SecurityConfig
- `services/exam-service/` — servicio esqueleto (solo PingController)
- `services/pom.xml` — POM padre con todos los modulos
