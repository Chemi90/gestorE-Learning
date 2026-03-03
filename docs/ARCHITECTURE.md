# Architecture

## Context

Monorepo for an e-learning platform with:

- Spring Boot microservices
- Angular frontend
- PostgreSQL + Redis + MinIO

## High-level view

```text
frontend-angular (4200)
        |
        v
api-gateway (8080)
  |  |  |  |  |  |
  |  |  |  |  |  +--> integrity-service (8086)
  |  |  |  |  +-----> grading-service (8085)
  |  |  |  +--------> exam-service (8084)
  |  |  +-----------> rag-service (8083)
  |  +--------------> content-service (8082)
  +-----------------> auth-service (8081)

shared infra
- postgres (5432): schemas auth/content/rag/exam/grading/integrity
- redis (6379)
- minio (9000/9001)
```

## API Gateway routing

Gateway is the only external HTTP entrypoint.

Configured route prefixes:

- `/api/v1/auth/**` -> `auth-service`
- `/auth/**` -> `auth-service` (legacy prefix with `StripPrefix=1`)
- `/content/**` -> `content-service`
- `/rag/**` -> `rag-service`
- `/exam/**` -> `exam-service`
- `/grading/**` -> `grading-service`
- `/integrity/**` -> `integrity-service`
- `/llm/**` -> `llm-orchestrator`

Global CORS is enabled for `/**` and uses `GATEWAY_ALLOWED_ORIGINS` (default `http://localhost:4200`).

## Security at gateway

JWT validation is implemented in `JwtValidationFilter`.

Public routes (no JWT required):

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/auth/api/v1/auth/login`
- `/auth/api/v1/auth/register`
- `/health`
- `/info`
- `/api/v1/ping` (gateway ping)

Protected routes (JWT required):

- All other routes passing through gateway.

When JWT is valid, gateway propagates user role as internal header:

- `X-User-Role: <ADMIN|TEACHER|STUDENT>`

## Service-level authorization

- `auth-service`: exposes register/login/me and issues JWT.
- `content-service`:
  - `GET /api/v1/ping` requires valid JWT.
  - `POST /api/v1/temarios/test` requires role `ADMIN` or `TEACHER`.

## Request flow examples

Public auth flow:

```text
Client -> API Gateway -> auth-service (/api/v1/auth/login)
auth-service -> JWT
JWT -> Client
```

Protected flow:

```text
Client + Bearer JWT -> API Gateway
API Gateway -> validates JWT -> forwards to service
API Gateway -> adds X-User-Role header
Service -> applies endpoint auth rules -> response
```

