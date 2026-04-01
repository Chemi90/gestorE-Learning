# Spec: Reestructuración de Inteligencia (Core vs Domain)

## 1. Objetivo
Dividir las "skills" del proyecto para permitir la reutilización de estándares de ingeniería en otros proyectos (Core) y mantener la lógica específica de `gestorE-Learning` aislada (Domain).

## 2. Nueva Estructura de Directorios
Se crearán dos subdirectorios dentro de `.agent/skills/`:
- `core/`: Skills agnósticas al proyecto (Git, patrones genéricos de código, creación de inteligencia).
- `domain/`: Skills vinculadas a la arquitectura, negocio y stack específico de este monorepo.

## 3. Mapeo de Archivos

| Archivo Original | Destino Final | Categoría | Acción Requerida |
| :--- | :--- | :--- | :--- |
| `git-workflow.md` | `core/git-workflow.md` | Core | Eliminar referencias a `docs/BRANCHING.md`. |
| `skill-creator.md` | `core/skill-creator.md` | Core | Ninguna (ya es genérica). |
| `api.md` | `domain/api-rest.md` | Domain | Mantener prefijos `/api/v1/` y DTOs locales. |
| `db.md` | `domain/persistence-jpa.md` | Domain | Mantener nombres de schemas (`auth`, `content`). |
| `db-coherence.md` | `domain/db-coherence.md` | Domain | Mantener la "Tríada Sagrada" específica del proyecto. |
| `docker.md` | `domain/infrastructure-docker.md` | Domain | Mantener los `COPY pom.xml` del monorepo. |
| `frontend.md` | `domain/frontend-angular.md` | Domain | Mantener patrones de Angular 21 y Vitest. |
| `gateway.md` | `domain/api-gateway.md` | Domain | Mantener rutas y filtros del gateway. |
| `llm-orchestrator.md` | `domain/educational-logic.md` | Domain | Lógica central del negocio. |
| `security.md` | `domain/security-jwt.md` | Domain | Mantener claims de `organizationId`. |
| `storage.md` | `domain/storage-minio.md` | Domain | Mantener convenciones de buckets locales. |
| `structure.md` | `domain/project-structure.md` | Domain | Mantener paquetes `com.gestorelearning.*`. |
| `testing.md` | `domain/testing-suite.md` | Domain | Mantener integraciones con H2/Postgres. |
| `pre-commit-gatekeeper.md`| `domain/gatekeeper.md` | Domain | Orquestador de validaciones locales. |

## 4. Cambios en Orquestación
- **`.agent/orchestrator.md`**: Se actualizará la sección de carga de skills para que primero busque en `core/` y luego en `domain/`.
- **`GEMINI.md`**: Se actualizará la sección de "Skill Routing" para reflejar los nuevos nombres de archivos.

## 5. Verificación
- Confirmar que `mcp_engram_mem_save` sigue funcionando tras el cambio de rutas.
- Validar que el agente puede cargar `core/git-workflow.md` sin fallar por falta de contexto local.

---
**¿Aprueba esta especificación para proceder con la reestructuración?**