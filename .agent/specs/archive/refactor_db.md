# Spec: Refactor DB — Separacion Estructura / Contenido (patron Moodle)

**Estado:** BORRADOR — pendiente aprobacion del equipo
**Fecha:** 2026-03-20
**Motivacion:** Tarjeta todo.md #1 y #2 — definir estructura de Temario y CRUD con versionado.

---

## 1. Problema

La tabla `content.units` mezcla dos responsabilidades:

| Responsabilidad | Campos actuales |
|---|---|
| Posicion en la jerarquia | `module_id`, `order_index` |
| Contenido/comportamiento | `resource_type`, `content_placeholder`, `status` |

Consecuencias:
- Un elemento (ej. un quiz sobre "derivadas") no puede reutilizarse en otro modulo o curso — existe solo en esa posicion del arbol.
- `exam-service` y `grading-service` tendrian que referenciar `unit_id`, un nodo de posicion, no de contenido.
- El RAG tendria que atravesar el arbol (course → module → unit) para indexar contenido.
- El versionado de contenido requiere duplicar registros de `units` enteros.

El `todo.md` establece explicitamente que los `objectives` actuan como **"prompts quirurgicos"** para la fase de redaccion de contenido. Eso implica que el contenido es una entidad propia, separable de su posicion en el temario.

---

## 2. Objetivo

Separar la **posicion en el arbol** del **contenido reutilizable**, conservando:
- El patron hibrido ya documentado en `todo.md`: FK dura en SQL, UUID simple en JPA.
- `ddl-auto: none` — ningun cambio automatico de esquema.
- Compatibilidad con el endpoint `/api/v1/courses/bulk` existente (mismo contrato externo).

---

## 3. Nuevo esquema conceptual

```
content.courses (sin cambios)
    └── content.modules (sin cambios)
            └── content.units  [solo posicion]
                    └── content.elements  [contenido real]
                            └── content.objectives (objectives se mueven aqui)
```

### Regla de negocio clave
- Una `unit` es un **slot de posicion** en el temario. Tiene titulo y orden.
- Un `element` es el **contenido real** de ese slot. Tiene tipo, cuerpo, estado de generacion.
- Relacion: cada `unit` tiene exactamente **1 element** activo (1-a-1 inicialmente; se extendera a N para versionado).
- Los `objectives` pasan de depender de `unit` a depender de `element` — son los prompts quirurgicos del LLM.

---

## 4. Schema SQL — cambios en `03_content_tables.sql`

### 4a. Tabla `content.units` — simplificada

```sql
-- ANTES (campos a eliminar): resource_type, content_placeholder, status
-- DESPUES:
CREATE TABLE IF NOT EXISTS content.units (
    id          UUID PRIMARY KEY,
    module_id   UUID NOT NULL REFERENCES content.modules(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_unit_module_order UNIQUE (module_id, order_index)
);
-- Eliminado: summary (queda en module), resource_type, content_placeholder, status
```

### 4b. Tabla nueva `content.elements`

```sql
CREATE TABLE IF NOT EXISTS content.elements (
    id             UUID PRIMARY KEY,
    unit_id        UUID NOT NULL REFERENCES content.units(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    resource_type  content.resource_type NOT NULL,
    title          VARCHAR(255) NOT NULL,
    body           TEXT,
    status         content.generation_status NOT NULL DEFAULT 'PENDING',
    version        INT NOT NULL DEFAULT 1,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_elements_organization
        FOREIGN KEY (organization_id) REFERENCES auth.organizations(id)
);
```

**Nota sobre organization_id:** Patron hibrido identico al de `content.courses` — FK dura en SQL, UUID simple en JPA. No se importa `OrganizationEntity`.

### 4c. Tabla `content.objectives` — FK cambia de `unit_id` a `element_id`

```sql
-- ANTES: unit_id UUID NOT NULL REFERENCES content.units(id)
-- DESPUES:
CREATE TABLE IF NOT EXISTS content.objectives (
    id          UUID PRIMARY KEY,
    element_id  UUID NOT NULL REFERENCES content.elements(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 5. Archivos a crear / modificar

### SQL (infra)
| Archivo | Accion | Descripcion |
|---|---|---|
| `infra/postgres/init/03_content_tables.sql` | Modificar | Simplificar `units`, agregar `elements`, cambiar FK de `objectives` |

### Entidades JPA (content-service)
| Archivo | Accion | Descripcion |
|---|---|---|
| `services/content-service/.../domain/UnitEntity.java` | Modificar | Eliminar `resourceType`, `contentPlaceholder`, `status` |
| `services/content-service/.../domain/ElementEntity.java` | Crear | Nueva entidad con `unitId` (UUID), `organizationId` (UUID), `resourceType`, `body`, `status`, `version` |
| `services/content-service/.../domain/ObjectiveEntity.java` | Modificar | Cambiar FK de `UnitEntity` a `ElementEntity` |

### Repositorios (content-service)
| Archivo | Accion | Descripcion |
|---|---|---|
| `services/content-service/.../repository/ElementRepository.java` | Crear | `findByUnitIdOrderByVersionDesc`, `findByUnitIdAndActiveTrue` |
| `services/content-service/.../repository/ObjectiveRepository.java` | Modificar | `findByElementId` en lugar de `findByUnitId` |

### DTOs (libs/common-dtos)
| Archivo | Accion | Descripcion |
|---|---|---|
| `libs/common-dtos/.../dto/CreateUnitRequest.java` | Modificar | Eliminar `resourceType`, `contentPlaceholder`; agregar `CreateElementRequest` anidado |
| `libs/common-dtos/.../dto/CreateElementRequest.java` | Crear | `resourceType`, `body`, `List<CreateObjectiveRequest> objectives` |
| `libs/common-dtos/.../dto/UnitResponse.java` | Modificar | Eliminar campos de contenido; agregar `ElementResponse element` |
| `libs/common-dtos/.../dto/ElementResponse.java` | Crear | `id`, `resourceType`, `title`, `body`, `status`, `version`, `createdAt`, `List<ObjectiveResponse>` |

### Servicio (content-service)
| Archivo | Accion | Descripcion |
|---|---|---|
| `services/content-service/.../service/CourseService.java` | Modificar | `saveModules` crea `ElementEntity` tras guardar `UnitEntity`; `mapToUnitResponse` consulta element |

---

## 6. Contrato API — sin cambios externos

El endpoint `POST /api/v1/courses/bulk` mantiene el mismo contrato de cara al frontend y al llm-orchestrator. Solo cambia la estructura interna del request:

### CreateUnitRequest — antes vs despues

```java
// ANTES
record CreateUnitRequest(
    String title,
    String contentPlaceholder,
    ResourceType resourceType,
    Integer orderIndex,
    List<CreateObjectiveRequest> objectives
)

// DESPUES
record CreateUnitRequest(
    String title,
    Integer orderIndex,
    CreateElementRequest element   // obligatorio, exactamente 1
)

record CreateElementRequest(
    ResourceType resourceType,
    String body,                   // era contentPlaceholder
    List<CreateObjectiveRequest> objectives
)
```

### UnitResponse — antes vs despues

```java
// ANTES
record UnitResponse(
    UUID id, String title, String contentPlaceholder,
    ResourceType resourceType, Integer orderIndex,
    GenerationStatus status, Instant createdAt, boolean active,
    List<ObjectiveResponse> objectives
)

// DESPUES
record UnitResponse(
    UUID id, String title, Integer orderIndex,
    Instant createdAt, boolean active,
    ElementResponse element         // siempre presente
)

record ElementResponse(
    UUID id, ResourceType resourceType, String title,
    String body, GenerationStatus status, int version,
    Instant createdAt, List<ObjectiveResponse> objectives
)
```

---

## 7. Impacto en otros servicios

| Servicio | Impacto |
|---|---|
| `auth-service` | Ninguno |
| `rag-service` | Ninguno (referencia `organization_id`, no `unit_id`) |
| `llm-orchestrator` | Cambia el DTO que recibe del content-service; hay que actualizar `CourseOrchestrationIntegrationTest` |
| `exam-service` | Positivo: podra referenciar `element_id` en lugar de `unit_id` cuando se implemente |
| `grading-service` | Positivo: mismo beneficio que exam-service |
| `integrity-service` | Ninguno por ahora |
| `api-gateway` | Ninguno (proxy puro) |
| `frontend-angular` | Debera actualizar el modelo de `UnitResponse` y el formulario de creacion |

---

## 8. Plan de implementacion por fases

### Fase A — SQL (prerequisito de todo lo demas)
1. Reescribir `infra/postgres/init/03_content_tables.sql` con el nuevo esquema.
2. Si hay datos de desarrollo en el contenedor local: `docker compose down -v && docker compose up -d postgres`.

### Fase B — Dominio JPA
1. Simplificar `UnitEntity`.
2. Crear `ElementEntity`.
3. Modificar `ObjectiveEntity` (FK → element_id).
4. Crear `ElementRepository`.
5. Modificar `ObjectiveRepository`.

### Fase C — DTOs (libs/common-dtos)
1. Crear `CreateElementRequest` y `ElementResponse`.
2. Actualizar `CreateUnitRequest` y `UnitResponse`.

### Fase D — Servicio
1. Actualizar `CourseService.saveModules` para crear `ElementEntity` tras `UnitEntity`.
2. Actualizar `mapToUnitResponse` para incluir `ElementResponse`.

### Fase E — Tests
1. Actualizar `CourseOrchestrationIntegrationTest` en llm-orchestrator.
2. Agregar test de integracion en content-service para el flujo bulk con element.

---

## 9. Decisiones de diseno

| Decision | Razon |
|---|---|
| `element.title` duplica `unit.title` inicialmente | Permite que el titulo del elemento evolucione independientemente (versionado futuro) sin afectar la posicion en el arbol |
| Relacion 1-a-1 unit → element en v1 | Simplifica la implementacion; la tabla soporta N elementos por unit desde el inicio (versionado) pero la logica de negocio solo expone el activo |
| `organization_id` en `elements` | Patron hibrido del `todo.md`; permite queries directas sobre elementos sin traversar el arbol |
| Objectives en `elements`, no en `units` | Los objectives son "prompts quirurgicos" (terminologia del todo.md) del contenido, no de la posicion |

---

## 10. Criterios de aceptacion

- [ ] `POST /api/v1/courses/bulk` crea curso completo con modules → units → elements → objectives.
- [ ] `GET /api/v1/courses/{id}/tree` devuelve el arbol completo con `element` anidado en cada unit.
- [ ] `PUT /api/v1/courses/{id}` reemplaza el arbol completo (comportamiento identico al actual).
- [ ] `DELETE /api/v1/courses/{id}` borrado logico sin errores.
- [ ] Tests de integracion pasan con H2 (requiere `CREATE DOMAIN` para enums PostgreSQL).
- [ ] `CourseOrchestrationIntegrationTest` en llm-orchestrator pasa.
- [ ] Ningun endpoint publico sin JWT involuntario.
