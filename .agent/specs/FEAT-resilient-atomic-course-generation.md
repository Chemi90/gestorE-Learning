# Feature: Resilient Atomic Course Generation

## 1. Objetivo
Implementar un flujo de generación de cursos por IA que sea resiliente a fallos mediante una estrategia de dos fases:
1.  **Skeleton**: Creación de la estructura jerárquica inicial (Curso/Módulos/Unidades).
2.  **Atomic Hydration**: Generación de contenido por Unidad y persistencia individual de cada **Elemento** y **Objetivo** en disco.

## 2. Flujo de Trabajo (Pipeline)

### Paso 1: Esqueleto (Estructura)
- **Llamada LLM**: Una única petición para extraer el **Curso**, sus **Módulos** y sus **Unidades** (incluyendo metadatos y un `summary` por cada unidad).
- **Persistencia**: Se guarda el árbol completo en `content-service` (esqueleto sin elementos).

### Paso 2: Hidratación Incremental (Contenido)
- **Iteración**: El orquestador recorre cada **Unidad** del esqueleto.
- **Llamada LLM (por Unidad)**: Se genera la lista de **Objetivos** y **Elementos** (redacción del `body`) usando el `summary` de la unidad.
- **Persistencia Atómica**:
    - Se itera sobre la lista de **Objetivos** -> Se persiste **uno por uno** en disco.
    - Se itera sobre la lista de **Elementos** -> Se persiste **uno por uno** en disco.

## 3. Cambios Técnicos

### A. `libs/common-dtos`
- **`CreateUnitRequest`**: Eliminar `@NotEmpty` en `elements` para permitir persistir la unidad vacía inicialmente.

### B. `services/content-service`
- **Nuevos Endpoints Singulares**:
    - `POST /api/v1/units/{unitId}/elements`: Persiste un único `CreateElementRequest`.
    - `POST /api/v1/units/{unitId}/objectives`: Persiste un único `CreateObjectiveRequest`.

### C. `services/llm-orchestrator`
- **Lógica de Checkpoints**: Antes de hidratar una unidad, el orquestador verifica si ya existen elementos en la DB para evitar duplicados en caso de reanudación tras fallo.
- **Refactor de Prompts**: Consolidar Phase 1+2 (Estructura) y Phase 3+4 (Hidratación Unidad).

## 4. Plan de Implementación
1.  **Paso 1**: Ajustar DTOs y crear endpoints de "Append" singular en `content-service`.
2.  **Paso 2**: Implementar la lógica del orquestador (Skeleton -> Atomic Hydration Loop).
3.  **Paso 3**: Tests de integración de reanudación ante fallos.
