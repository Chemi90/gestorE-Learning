# Especificación: Pipeline de Prompts para Estructuración de Cursos (Course Pipeline)

## 1. Objetivo
Implementar una pipeline de orquestación de prompts para extraer la estructura completa de un curso de alta densidad, dividiendo el proceso en fases lógicas para maximizar la precisión y evitar alucinaciones.

## 2. Microservicios Implicados
- **LLM Orchestrator**: Gestiona el flujo de llamadas al LLM y la lógica de reintentos.
- **Content Service**: Gestiona la persistencia incremental y la validación de IDs.

## 3. Pipeline de Prompts (3 Fases)

### Fase 1: Estructura Macro (The Course Architect)
- **Prompt**: `phase1-course-modules.md`
- **Input**: Texto Fuente Completo + Meta-datos (Nivel, Organización).
- **Output**: Datos del Curso + Lista de Módulos (Títulos y Summaries).
- **Acción**: 
    1. El LLM identifica los grandes bloques temáticos.
    2. El Orchestrator llama a `POST /courses` con estos datos.
    3. **Resultado**: Se obtiene el `courseId` y una lista de `moduleIds`.

### Fase 2: Estructura Meso (The Module Specialist)
- **Prompt**: `phase2-module-units.md`
- **Bucle**: Se ejecuta **una vez por cada Módulo** identificado en la Fase 1.
- **Input**: Fragmento de texto del Módulo X + Contexto del Curso.
- **Output**: Lista de Unidades para ese módulo (Títulos y Placeholders).
- **Acción**: 
    1. El Orchestrator llama a `PATCH /modules/{moduleId}` para inyectar las unidades.
    2. **Resultado**: Se obtienen los `unitIds` para cada unidad del módulo.

### Fase 3: Estructura Micro (The Unit Hydrator)
- **Prompt**: `phase3-unit-details.md`
- **Bucle**: Se ejecuta **una vez por cada Unidad** identificada en la Fase 2.
- **Input**: Fragmento de texto mínimo de la Unidad Y.
- **Output**: Objetivos de Aprendizaje + ResourceType + Contenido Refinado.
- **Acción**: 
    1. El Orchestrator llama a `PATCH /units/{unitId}` para completar la unidad.
    2. **Resultado**: El curso queda marcado como `READY`.

## 4. Gestión de Estado y Resiliencia
- **Persistencia Atómica**: Cada fase se guarda inmediatamente en disco (PostgreSQL).
- **Validación de Contrato**: Cada output del LLM se valida contra su respectivo DTO (`CreateModuleRequest`, `CreateUnitRequest`, etc.) antes de persistir.
- **Paralelismo**: Las Fases 2 y 3 pueden ejecutarse de forma concurrente para diferentes módulos/unidades.

## 5. Restricciones Críticas
- **Context Window Management**: Solo se envía al LLM la información necesaria para la fase actual.
- **ID Traceability**: Cada elemento generado debe estar vinculado al UUID de su padre (Course -> Module -> Unit).

---
**¿Esta segmentación en 3 fases te parece la más adecuada para asegurar la calidad del Máster en IA?**
