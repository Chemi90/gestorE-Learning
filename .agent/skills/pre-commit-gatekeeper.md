# Skill: Pre-Commit Gatekeeper (Orquestador Maestro)

## Proposito
Actuar como la autoridad máxima de integridad antes de una confirmación de código, orquestando secuencialmente las validaciones de base de datos, el pipeline de pruebas y el protocolo de Git.

## Reglas Obligatorias

1. **Jerarquía de Ejecución**: El Maestro DEBE seguir estrictamente este orden:
   1. **Estructura** (db-coherence.md)
   2. **Calidad** (testing.md)
   3. **Versionado** (git-workflow.md)
2. **Fallo Crítico**: Cualquier error en un subdominio detiene la ejecución del Maestro. No hay "bypass".

## Procedimiento Maestro (Workflow de Integridad)

### Fase 1: Validar Modelo (Delegar en db-coherence.md)
- Verificar la Tríada Sagrada: Entidad Java <-> SQL Infra <-> SQL Test.
- Asegurar unicidad de nombres, tipos y restricciones.

### Fase 2: Ejecutar CI Local (Delegar en testing.md)
- Ejecutar suite completa de Backend (Maven).
- Ejecutar suite completa de Frontend (Angular Build + Test).

### Fase 3: Confirmar Cambios (Delegar en git-workflow.md)
- Si 1 y 2 son exitosos:
- Preparar commit con Conventional Commits.
- Usar sintaxis `;` para PowerShell.

## Anti-patrones a Evitar

- Saltarse la validación de base de datos pensando que "es un cambio pequeño".
- Intentar el commit si los tests de Angular fallan pero los de Java pasan (integridad total).
- No leer las instrucciones específicas de cada sub-skill.

## Referencias de Delegación

- Modelo: `.agent/skills/db-coherence.md`
- Pruebas: `.agent/skills/testing.md`
- Git: `.agent/skills/git-workflow.md`
