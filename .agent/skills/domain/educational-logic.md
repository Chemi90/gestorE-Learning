# Skill: LLM Orchestrator

## Proposito
Orquestar la generación, hidratación y validación de estructuras educativas (Cursos, Módulos, Unidades) utilizando modelos de lenguaje y persistencia incremental.

## Reglas Obligatorias

1. **Flujo de 3 Fases**: Seguir estrictamente el orden Macro (Fase 1) -> Meso (Fase 2) -> Micro (Fase 3).
2. **Patrón Skeleton-Hydration**: La Fase 1 usa `POST` (crear esqueleto) y las Fases 2/3 usan `PATCH` (hidratar con UUIDs existentes).
3. **Validación DTO**: Todo JSON generado por el LLM debe validarse contra los records de `common-dtos` antes de enviarse al `content-service`.
4. **Ubicación de Prompts**: Los prompts deben residir en `services/llm-orchestrator/src/main/resources/prompts/`.
5. **Zero Hallucination**: Los prompts deben restringirse al texto fuente proporcionado.

## Procedimiento

1. **Fase 1 (Macro)**: Extraer Curso y Módulos. Generar `CreateCourseRequest`.
2. **Fase 2 (Meso)**: Por cada módulo, extraer Unidades. Generar `CreateUnitRequest`.
3. **Fase 3 (Micro)**: Por cada unidad, extraer Objetivos y Detalles. Generar `CreateObjectiveRequest` y campos de contenido.
4. **Validación de Export/Import**: Al importar un temario JSON, verificar la integridad jerárquica y el `orderIndex` de todos los elementos.
5. **Versionado**: Incrementar la versión del curso (SemVer) en `app.json` o metadatos del curso tras cambios estructurales.

## Anti-patrones a Evitar

- Generar toda la estructura en un solo prompt (causa alucinaciones y pérdida de contexto).
- No validar el JSON del LLM antes de la persistencia (causa errores 500 en cascada).
- Hardcodear IDs en los prompts en lugar de usar placeholders `{courseId}`.
- Mezclar lógica de negocio del contenido en el orquestador (debe delegar en `content-service`).

## Referencias en el Repo

- `services/llm-orchestrator/src/main/resources/prompts/`: Librería de prompts.
- `libs/common-dtos/src/main/java/com/gestorelearning/common/dto/`: DTOs de validación.
- `.agent/specs/ai-index-prompt.md`: Especificación técnica del pipeline.
- `services/llm-orchestrator/src/main/java/com/gestorelearning/llmorchestrator/`: Código base del servicio.
