# Skill: Pre-Commit Gatekeeper (Orquestador Maestro)

## Proposito
Actuar como la autoridad máxima de integridad antes de una confirmación de código, orquestando secuencialmente las Puertas de Calidad, validaciones de base de datos, el pipeline de pruebas y el protocolo de Git.

## Reglas Obligatorias

1. **Jerarquía de Ejecución**: El Maestro DEBE seguir estrictamente este orden:
   0. **Puertas de Calidad (Quality Gates)**
   1. **Estructura** (db-coherence.md)
   2. **Calidad** (testing.md)
   3. **Versionado** (git-workflow.md)
2. **Fallo Crítico**: Cualquier error en un subdominio detiene la ejecución del Maestro. No hay "bypass".

## Procedimiento Maestro (Workflow de Integridad)

### Fase 0: Puertas de Calidad (Quality Gates)
Antes de proceder con validaciones técnicas, verifica obligatoriamente:
- [ ] Indentación Java: 4 espacios. Indentación YAML/JSON/TS: 2 espacios.
- [ ] Todos los DTOs usan `record` de Java con anotaciones `@Valid` / `@NotBlank` / `@NotNull` donde corresponda.
- [ ] Ninguna entidad JPA tiene `ddl-auto: create` o `update` en producción (solo `none`).
- [ ] El Dockerfile del servicio nuevo copia TODOS los `pom.xml` hermanos (ver `docker.md`).
- [ ] Cada servicio nuevo tiene `PingController` y `PingControllerTest`.
- [ ] Los endpoints protegidos por rol usan `hasRole()` o `hasAnyRole()` en `SecurityConfig`.
- [ ] Los commits siguen Conventional Commits: `feat:`, `fix:`, `chore:`, `docs:`, `ci:`.
- [ ] El script SQL de init está en `infra/postgres/init/` con prefijo numérico.
- [ ] La ruta nueva en el gateway está en `application.yml` del api-gateway y registrada en `JwtValidationFilter` si es pública.

### Fase 1: Validar Modelo (Delegar en db-coherence.md)
- Verificar la Tríada Sagrada: Entidad Java <-> SQL Infra <-> SQL Test.
- Asegurar unicidad de nombres, tipos y restricciones.

### Fase 2: Ejecutar CI Local (Delegar en testing.md)
- Ejecutar suite completa de Backend (Maven).
- Ejecutar suite completa de Frontend (Angular Build + Test).

### Fase 3: Confirmar Cambios (Delegar en git-workflow.md)
- Si las fases anteriores son exitosas:
- Preparar commit siguiendo Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`, `ci:`).
- Usar sintaxis con punto y coma `;` para PowerShell (no usar `&&`).

## Anti-patrones a Evitar

- Saltarse la validación de base de datos pensando que "es un cambio pequeño".
- Intentar el commit si los tests de Angular fallan pero los de Java pasan (integridad total).
- No leer las instrucciones específicas de cada sub-skill.

## Referencias de Delegación

- Modelo: `.agent/skills/db-coherence.md`
- Pruebas: `.agent/skills/testing.md`
- Git: `.agent/skills/git-workflow.md`