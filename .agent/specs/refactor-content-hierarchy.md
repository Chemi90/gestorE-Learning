# Spec: Refactor Content Hierarchy & Coherence

## Objective
Refactor the entire application (Backend, Frontend, and DTOs) to strictly adhere to the database hierarchy and data types defined in `infra/postgres/init/03_content_tables.sql`.

## Hierarchy Reference (SQL)
- `courses` (PK: UUID, level: ENUM, version: INT)
    - `modules` (FK: course_id, order_index: INT)
        - `units` (FK: module_id, order_index: INT)
            - `elements` (FK: unit_id, order_index: INT, resource_type: ENUM, generation_status: ENUM)
            - `objectives` (FK: unit_id, order_index: INT)

**Key Correction**: Objectives must be direct children of `Units`, not `Elements`. Both `Elements` and `Objectives` are siblings under the same `Unit`.

## Phase 1: Common DTOs (libs/common-dtos)
### Data Types
- Change `version` from `String` to `int` in:
    - `CourseResponse`
    - `CourseTreeResponse`
    - `CreateCourseRequest`
    - `CreateCourseBulkRequest`

### Hierarchy & Structure
- **UnitResponse**:
    - REMOVE `ElementResponse element`.
    - ADD `List<ElementResponse> elements`.
    - ADD `List<ObjectiveResponse> objectives`.
- **ElementResponse**:
    - REMOVE `List<ObjectiveResponse> objectives`.
    - ADD `Integer orderIndex`.
    - ADD `boolean active`.
- **ObjectiveResponse**:
    - ADD `Integer orderIndex`.
    - ADD `boolean active`.
- **CreateUnitRequest**:
    - REMOVE `CreateElementRequest element`.
    - ADD `List<CreateElementRequest> elements`.
    - ADD `List<CreateObjectiveRequest> objectives`.
- **CreateElementRequest**:
    - REMOVE `List<CreateObjectiveRequest> objectives`.
    - ADD `Integer orderIndex`.
- **CreateObjectiveRequest**:
    - ADD `Integer orderIndex`.

## Phase 2: Content Service (services/content-service)
### Entities (domain)
- **CourseEntity**: Change `version` to `int`.
- **ElementEntity**:
    - REMOVE `organizationId`.
    - ADD `Integer orderIndex`.
- **ObjectiveEntity**:
    - CHANGE parent from `ElementEntity element` to `UnitEntity unit`.
    - ADD `Integer orderIndex`.
    - ADD `boolean active` (default true).

### Logic (service/mapper)
- Refactor `CourseMapper` or similar to handle the new DTO hierarchy.
- Ensure `CourseService` saves elements and objectives as siblings under units.

## Phase 3: LLM Orchestrator (services/llm-orchestrator)
### Prompts (resources/prompts)
- **phase3-unit-details.md**: Update output schema to include a list of `elements` and a list of `objectives` under the unit, rather than nesting objectives inside elements.
- **Other prompts**: Ensure `version` is treated as an integer.

### Tests
- **CourseOrchestrationIntegrationTest.java**: Update all mock JSONs and assertions to match the new hierarchy and `int` versioning.

## Phase 4: Frontend (apps/frontend-angular)
### Models (core/models/course.model.ts)
- Update interfaces:
    - `CourseResponse`, `CourseTreeResponse`, `CreateCourseBulkRequest`: `version` -> `number`.
    - `UnitResponse`: `element` -> `elements: ElementResponse[]`, add `objectives: ObjectiveResponse[]`.
    - `ElementResponse`: remove `objectives`, add `orderIndex`, `active`.
    - `ObjectiveResponse`: add `orderIndex`, `active`.
    - Mirror changes in `CreateUnitRequest`, `CreateElementRequest`, `CreateObjectiveRequest`.

### Components
- **course-tree.component.ts**: Update template to render both elements and objectives under units.
- **course-editor-page.component.ts**: Update logic to handle multiple elements and objectives per unit.

## Verification Plan
1.  **Build Common**: `mvn clean install -pl libs/common-dtos`
2.  **Build Services**: `mvn clean compile` in `content-service` and `llm-orchestrator`.
3.  **Run Tests**:
    - `mvn test -pl services/content-service`
    - `mvn test -pl services/llm-orchestrator`
4.  **Frontend Build**: `npm run build` in `apps/frontend-angular`.
