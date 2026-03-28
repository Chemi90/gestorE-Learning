# Spec: Resilient Atomic Course Generation (User Flow v2)

## 1. Objetivo
Implementar un sistema de generación de cursos resiliente que separa la **Arquitectura del Temario** (Skeleton) de la **Redacción de Contenido** (Atomic Body Generation). El objetivo es tener una estructura completa en DB desde el primer minuto y luego "rellenar" los huecos del contenido uno a uno.

## 2. Flujo de Trabajo (Pipeline)

### Paso 1: Generación del Esqueleto (Macro-Estructura)
- **Llamada LLM 1**: Se envía el texto fuente y se pide el objeto completo:
    - **Curso**: Título, descripción, nivel.
    - **Módulos**: Título, sumario.
    - **Unidades**: Título, sumario.
    - **Objetivos**: Lista completa de objetivos por unidad (ya redactados).
    - **Elementos**: Lista de elementos por unidad conteniendo **solo** `title`, `resourceType`, `orderIndex` y un `placeholder` descriptivo (el campo `body` debe viajar nulo o vacío).
- **Persistencia**: Se guarda en el `content-service` mediante el endpoint `/bulk`. La base de datos queda poblada con toda la jerarquía. El estado de los elementos será `PENDING`.

### Paso 2: Hidratación Atómica de Cuerpos (Bucle de Redacción)
- **Iteración**: El orquestador recorre todas las **Unidades** y, dentro de cada una, todos sus **Elementos**.
- **Bucle Secundario (Por cada Elemento)**:
    - **Llamada LLM 2**: Se le pasa el título del elemento, su tipo y su `placeholder` (y el summary de la unidad para contexto).
    - **Respuesta**: El LLM devuelve **exclusivamente el texto del `body`** del elemento (formatos Markdown/HTML permitidos).
    - **Persistencia**: El orquestador realiza un `PATCH` al `content-service` para actualizar el `body` de ese elemento específico y marcarlo como `COMPLETED`.

## 3. Contratos de API (content-service)

### Endpoints
1.  **POST `/api/v1/courses/bulk`**: Ya existe. Se usará para el Skeleton (los `body` de los elementos irán vacíos).
2.  **PATCH `/api/v1/elements/{id}/body`**: (Nuevo) Actualiza solo el cuerpo y el estado de un elemento.
    - Body: `{ "body": "..." }`

## 4. Prompts (llm-orchestrator)

### Prompt 1: `skeleton-generator.md`
- **Output**: JSON `CreateCourseBulkRequest`.
- **Foco**: Coherencia estructural y planificación de recursos.

### Prompt 2: `element-body-writer.md`
- **Output**: String (o JSON con campo único `body`).
- **Foco**: Redacción profunda y educativa del contenido del elemento.

## 5. Plan de Implementación
1.  **Paso 1 (content-service)**: Crear el endpoint de actualización parcial del `body` en `ElementController` (o `UnitController`).
2.  **Paso 2 (llm-orchestrator)**: Refactorizar los prompts para alinearlos con este flujo de 2 niveles.
3.  **Paso 3 (llm-orchestrator)**: Implementar el servicio de orquestación que ejecute el bucle y gestione la persistencia tras cada respuesta del LLM.
