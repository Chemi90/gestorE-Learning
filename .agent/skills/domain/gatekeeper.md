# Skill: Pre-Commit Gatekeeper (Orquestador de Integridad)

## Proposito
Actuar como la autoridad máxima de integridad antes de cualquier confirmación de código, asegurando que el entorno local sea un espejo perfecto de CI (GitHub Actions).

## Mandato de Bloqueo Absoluto
**EL AGENTE TIENE PROHIBIDO EJECUTAR `git commit` O `git push` SI ESTA SKILL NO TERMINA EN ÉXITO.**

## Procedimiento de Verificación (Workflow)

### 1. Validación de la "Tríada Sagrada" (Modelo)
- Delegar en `domain/db-coherence.md`.
- Confirmar que `Entidad Java` <-> `init.sql` <-> `init-test.sql` están sincronizados.
- Si hay cambios en `common-dtos`, verificar impacto en todos los servicios.

### 2. Ejecución de Tests (Capa de Espejo CI)
Ejecutar obligatoriamente los comandos de Maven que emulan el entorno de producción:
- **Backend**: `mvn -B -ntp -f services/pom.xml -pl <modulo-afectado>,api-gateway -am test`
- **Frontend**: `cd apps/frontend-angular && npm test -- --watch=false`

### 3. Puertas de Calidad (Estándares)
- Indentación: Java (4 espacios), Web/TS/YAML (2 espacios).
- DTOs: Uso estricto de Java `record` con `@Valid`.
- JPA: `ddl-auto: none` en ficheros de configuración principal.
- Docker: Dockerfiles actualizados con los nuevos `pom.xml`.

## Manejo de Fallos
- Si un test falla: **ABORTAR OPERACIÓN**.
- Corregir código/test.
- Reiniciar el Gatekeeper desde el punto 1.

## Referencias
- Integridad DB: `domain/db-coherence.md`
- Tests: `domain/testing-suite.md`
- Git: `core/git-workflow.md`
