# Spec: RAG Service - Subida de documentos a MinIO + metadatos

## Objetivo
Implementar la subida de documentos (PDF, TXT, etc.) a MinIO desde el `rag-service` y almacenar sus metadatos en la base de datos PostgreSQL (`schema rag`). Todo debe apegarse estrictamente a los contratos de API definidos en `docs/API_CONTRACTS.md`.

## Archivos a Modificar / Crear

### Configuración y Dependencias
1. **`services/rag-service/pom.xml`**:
   - Agregar dependencia de `io.minio:minio`.
   - Asegurar la dependencia al módulo `common-dtos`.
   - Agregar dependencia de OpenAPI (`springdoc-openapi-starter-webmvc-ui`) para cumplir con "add OpenAPI specs per service".
2. **`services/rag-service/src/main/resources/application.yml`**:
   - Agregar configuración de MinIO (endpoint, access key, secret key, bucket name).
3. **`infra/docker-compose.yml`**:
   - Inyectar las variables de entorno de MinIO (`MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`) al contenedor `rag-service`.

### Base de Datos
4. **`infra/postgres/init/04_rag_tables.sql`**:
   - Crear script para el esquema `rag` y la tabla `document` con el estándar definido.

### Código Fuente (DTOs Globales) - ¡OBLIGATORIO por API_CONTRACTS!
5. **`libs/common-dtos/src/main/java/com/gestorelearning/common/dto/rag/UploadDocumentResponse.java`**:
   - Record DTO con `id`, `filename`, `minioObjectName`, `contentType`, `size`, `createdAt` (en UTC ISO-8601).
   - "define DTOs in libs/common-dtos".

### Código Fuente (`services/rag-service/src/main/java/com/gestorelearning/rag/`)
6. **`domain/DocumentEntity.java`**: Entidad JPA para la tabla `document`.
7. **`repository/DocumentRepository.java`**: Repositorio Spring Data JPA.
8. **`service/MinioService.java`**: Lógica de interacción con el SDK de MinIO.
9. **`service/DocumentService.java`**: Orquestación de guardado en base de datos y en MinIO.
10. **`controller/DocumentController.java`**: Endpoint REST `POST /api/v1/documents/upload` con especificaciones OpenAPI.
11. **`exception/GlobalExceptionHandler.java`** (o similar): Asegurar la convergencia al "Error envelope (target)" de `API_CONTRACTS.md` incluyendo el `requestId`.

## Contratos API

### Endpoint: Subir Documento

`POST /rag/api/v1/documents/upload` (Internamente mapeado a `/api/v1/documents/upload` en `rag-service`)

**Headers (Obligatorios)**:
- `X-Organization-Id`: `UUID`
- `X-Request-Id`: `String` (Correlación end-to-end, propagar).
- `Content-Type`: `multipart/form-data`

**Request Body**:
- `file`: Archivo binario (`MultipartFile`).

**Response** (Status 201 Created):
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "filename": "temario_java.pdf",
  "minioObjectName": "123e4567-e89b-12d3-a456-426614174000.pdf",
  "contentType": "application/pdf",
  "size": 1048576,
  "createdAt": "2026-03-18T10:00:00Z"
}
```

*Nota: Los valores de tiempo DEBEN ser `ISO-8601 UTC` como establece el contrato de API.*

### Respuesta de Error (Target Envelope)
Si hay un fallo (e.g., archivo vacío o error de MinIO), se retorna un error estandarizado:

```json
{
  "timestamp": "2026-03-18T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/rag/api/v1/documents/upload",
  "requestId": "req-123"
}
```

## Esquema de Base de Datos

**Schema**: `rag`
**Table**: `document`

| Columna | Tipo | Restricciones |
| :--- | :--- | :--- |
| `id` | `UUID` | PRIMARY KEY |
| `filename` | `VARCHAR(255)` | NOT NULL |
| `minio_object_name` | `VARCHAR(255)` | NOT NULL, UNIQUE |
| `content_type` | `VARCHAR(100)` | NOT NULL |
| `size` | `BIGINT` | NOT NULL |
| `organization_id` | `UUID` | NOT NULL |
| `active` | `BOOLEAN` | NOT NULL DEFAULT TRUE |
| `created_at` | `TIMESTAMP` | NOT NULL |
