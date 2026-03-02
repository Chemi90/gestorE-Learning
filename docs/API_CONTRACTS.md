# API Contracts

## Base conventions

- Base path versioning: `/api/v1`
- Payload format: JSON
- Time values: ISO-8601 UTC
- Correlation: `X-Request-Id` (placeholder, to be enforced)

## Health endpoints

Every backend service exposes:

- `GET /health`
- `GET /api/v1/ping`

### Ping response shape

```json
{
  "service": "content-service",
  "status": "ok",
  "time": "2026-03-02T21:00:00Z"
}
```

## Gateway forwarding contracts

- `GET /auth/api/v1/ping` -> auth-service
- `GET /content/api/v1/ping` -> content-service
- `GET /rag/api/v1/ping` -> rag-service
- `GET /exam/api/v1/ping` -> exam-service
- `GET /grading/api/v1/ping` -> grading-service
- `GET /integrity/api/v1/ping` -> integrity-service
- `GET /llm/api/v1/ping` -> llm-orchestrator

## Error envelope (target)

All services should converge to this format:

```json
{
  "timestamp": "2026-03-02T21:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/resource",
  "requestId": "req-123"
}
```

## HTTP status guidelines

- `200`: success
- `201`: created
- `204`: success with no body
- `400`: validation/request errors
- `401`: unauthenticated
- `403`: unauthorized
- `404`: not found
- `409`: conflict
- `500`: unexpected error

## Pending API work

- define DTOs in `libs/common-dtos`
- add OpenAPI specs per service
- unify validation and error handlers
- enforce request-id propagation end-to-end