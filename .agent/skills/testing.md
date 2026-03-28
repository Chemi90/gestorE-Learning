# Skill: Testing

## Proposito
Patrones para tests unitarios e integracion en backend (JUnit 5 + MockMvc + H2) y frontend (Vitest), y comandos de Build & CI Local.

## Reglas Obligatorias

1. Todo servicio nuevo tiene al menos `PingControllerTest`.
2. Tests de integracion usan `@ActiveProfiles("test")` y H2 con `MODE=PostgreSQL`.
3. Los tests son deterministas — no dependen de orden de ejecucion ni datos aleatorios.
4. `@Sql` para insertar datos maestros necesarios en tests de integracion.
5. Frontend usa Vitest, NO Karma/Jasmine.

## Test Unitario — PingControllerTest (obligatorio)

```java
package com.gestorelearning.<servicio>;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PingControllerTest {

    @Test
    void ping_whenCalled_returnsExpectedPayload() {
        PingController controller = new PingController();
        Map<String, String> payload = controller.ping();

        assertEquals("<nombre-servicio>", payload.get("service"));
        assertEquals("ok", payload.get("status"));
        assertNotNull(payload.get("time"));
    }
}
```

Nota: es un test unitario puro — sin Spring context, sin anotaciones `@SpringBootTest`.

## Test de Integracion — Patron Canonico

```java
package com.gestorelearning.<servicio>.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/seed-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class FeatureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createResource_withValidData_returns200() throws Exception {
        String json = objectMapper.writeValueAsString(new CrearRecursoRequest("nombre", ...));

        mockMvc.perform(post("/api/v1/recurso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("nombre"));
    }

    @Test
    void getResource_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/recurso/protegido"))
                .andExpect(status().isUnauthorized());
    }
}
```

## Configuracion H2 para Tests

`src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS <schema>
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        default_schema: <schema>

security:
  jwt:
    secret: 01234567890123456789012345678901
```

Gotcha: si el servicio usa enums PostgreSQL nativos, agregar `CREATE DOMAIN` en el `INIT`:
```
INIT=CREATE SCHEMA IF NOT EXISTS auth\;CREATE DOMAIN IF NOT EXISTS auth.user_role AS VARCHAR
```

## Test con JWT — MockMvc + Token

Para endpoints protegidos, generar un token valido en el test:

```java
@Autowired
private JwtService jwtService;

private String generateTestToken(String email, String role, UUID orgId) {
    // Usar el JwtService directamente si esta disponible
    return jwtService.generateToken(email, UserRole.valueOf(role), orgId);
}

@Test
void protectedEndpoint_withValidToken_returns200() throws Exception {
    String token = generateTestToken("test@test.com", "ADMIN", orgId);

    mockMvc.perform(get("/api/v1/recurso/protegido")
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
}
```

## Nomenclatura de Tests

Formato: `<metodo>_<condicion>_<resultado>`

Ejemplos:
- `register_withValidData_returnsUser()`
- `login_withInvalidPassword_returns401()`
- `getMe_withExpiredToken_returns401()`
- `createCourse_asStudent_returns403()`

## Frontend — Vitest

```typescript
import { describe, it, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { MiComponente } from './mi-componente.component';

describe('MiComponente', () => {
  it('should create', () => {
    const fixture = TestBed.createComponent(MiComponente);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
```

## Comandos de Build y CI Local

```bash
# Build y empaquetado de todos los servicios (sin ejecutar tests)
mvn -B -ntp -f services/pom.xml clean package -DskipTests

# Build de un servicio individual (sin tests)
mvn -B -ntp -f services/pom.xml -pl <servicio> -am clean package -DskipTests

# Frontend completo (Install + Build)
cd apps/frontend-angular && npm ci && npm run build
```

## Comandos de Test

```bash
# Todos los tests de todos los servicios
mvn -B -ntp -f services/pom.xml test

# Todos los tests de un servicio especifico
mvn -B -ntp -f services/pom.xml -pl <servicio> -am test

# Un test especifico
mvn -B -ntp -f services/pom.xml -pl <servicio> -am test -Dtest=<NombreTest>

# Frontend (Unit Tests)
cd apps/frontend-angular && npm test -- --watch=false
```

## Anti-patrones a Evitar

- `@SpringBootTest` para tests que no necesitan Spring context (usar test unitario puro)
- Tests que dependen de datos de otros tests (orden de ejecucion)
- Tests sin asserts significativos (solo verificar que "no falla")
- Hardcodear URLs en tests en lugar de usar constantes

## Referencias en el Repo

- `services/auth-service/src/test/java/com/gestorelearning/auth/PingControllerTest.java`
- `services/auth-service/src/test/java/com/gestorelearning/auth/integration/AuthFlowIntegrationTest.java`
- `services/auth-service/src/test/resources/application-test.yml`
- `services/content-service/src/test/java/com/gestorelearning/content/integration/ContentSecurityIntegrationTest.java`