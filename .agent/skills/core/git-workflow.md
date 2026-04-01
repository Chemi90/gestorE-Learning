# Skill: Git Workflow

## Proposito
Garantizar la integridad del repositorio siguiendo las politicas de branching y commits estándar, optimizado para entornos Windows/PowerShell.

## Reglas Obligatorias

1. **Entorno Windows/PowerShell**: NUNCA usar `&&` para concatenar comandos. Usar siempre el punto y coma `;` (ej. `git add . ; git status`).
2. **Conventional Commits**: Los mensajes deben empezar con un prefijo minusculo seguido de dos puntos y espacio:
   - `feat:` (nueva funcionalidad)
   - `fix:` (correccion de error)
   - `chore:` (mantenimiento, dependencias, refactor sin cambio logico)
   - `docs:` (documentacion)
   - `ci:` (cambios en pipelines/docker)
3. **Branching**:
   - `main`: Solo para produccion (no tocar directamente).
   - `develop`: Rama de integracion.
   - `feature/*`: Desarrollo de tareas. Naming: `feature/descripcion-breve`.
4. **Validacion Pre-Commit**: Antes de cada commit, verificar:
   - `git status` para confirmar archivos trackeados.
   - Si el proyecto usa Maven, verificar con `mvn compile` (o el equivalente del stack).

## Procedimiento

1. **Estado**: Ejecutar `git status ; git diff --staged` para revisar que se va a commitear.
2. **Contexto**: Revisar los ultimos 3 mensajes con `git log -n 3` para mantener la consistencia de estilo.
3. **Mensaje**: Proponer un mensaje que explique el "que" y el "porque", no solo el "como".
4. **Commit**: Ejecutar el comando de commit y verificar con un ultimo `git status`.

## Anti-patrones a Evitar

- Usar `&&` en la terminal (falla en PowerShell).
- Commitear directamente en `main` o `develop`.
- Mensajes de commit vagos como "cambios", "fix" o "update".
- Mezclar refactorizacion y nuevas funcionalidades en el mismo commit.

## Checklist

- [ ] ¿He usado `;` en vez de `&&`?
- [ ] ¿El mensaje sigue `feat:`, `fix:`, etc.?
- [ ] ¿He verificado el `git status` antes de confirmar?
- [ ] ¿La rama actual es una `feature/*`?
