# gestorE-Learning

Monorepo bootstrap para una plataforma e-learning basada en microservicios.

Estado actual: **skeleton MVP** listo para comenzar desarrollo hoy.

## Stack

- Backend: Java 25 + Spring Boot (microservices)
- Gateway: Spring Cloud Gateway
- Frontend: Angular 21
- Data: PostgreSQL 16 + pgvector, Redis, MinIO
- Infra local: Docker Compose

## Branching model

- `main`: rama estable
- `develop`: integracion
- ramas por integrante (desde `develop`):
  - `jmruiz`
  - `acamacho`
  - `ldemicheli`

Para trabajo por tarea (recomendado):

- `feature/<servicio>-<breve-descripcion>`
- `svc/<servicio>/<tarea>`

Mas detalle: [docs/BRANCHING.md](docs/BRANCHING.md)

## Arquitectura (resumen)

```text
[Angular Frontend]
        |
        v
   [API Gateway]
      |   |   |----> auth-service
      |   |--------> content-service
      |------------> rag/exam/grading/integrity/llm

shared infra:
- PostgreSQL (schemas por servicio + pgvector)
- Redis
- MinIO
```

Mas detalle: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

## Servicios y puertos

| Componente | Puerto | Responsabilidad MVP |
|---|---:|---|
| api-gateway | 8080 | entrada unica HTTP, routing y CORS |
| auth-service | 8081 | autenticacion JWT (register/login/me) |
| content-service | 8082 | contenido + endpoint protegido por rol |
| rag-service | 8083 | retrieval/RAG (skeleton) |
| exam-service | 8084 | examenes (skeleton) |
| grading-service | 8085 | calificacion (skeleton) |
| integrity-service | 8086 | integridad/proctoring (skeleton) |
| llm-orchestrator | 8087 | orquestacion LLM (placeholder) |
| postgres | 5432 | base de datos principal |
| redis | 6379 | cache/broker ligero |
| minio api | 9000 | objetos/archivos |
| minio console | 9001 | consola minio |
| frontend-angular | 4200 | app web |

## Estructura del repo

```text
.
|-- apps/
|   |-- frontend-angular/
|-- services/
|   |-- api-gateway/
|   |-- auth-service/
|   |-- content-service/
|   |-- rag-service/
|   |-- exam-service/
|   |-- grading-service/
|   |-- integrity-service/
|   |-- llm-orchestrator/
|   `-- pom.xml
|-- infra/
|   |-- docker-compose.yml
|   |-- .env.example
|   |-- postgres/init/
|   `-- minio/
|-- libs/
|   |-- common-dtos/
|   |-- common-security/
|   `-- common-logging/
`-- docs/
    |-- ARCHITECTURE.md
    |-- API_CONTRACTS.md
    |-- BRANCHING.md
    `-- LOCAL_DEV.md
```

## Requisitos

- Docker Desktop + Docker Compose
- Java 25
- Maven 3.9+
- Node.js 24+ y npm

## Quick start local

### 1) Levantar infra con un comando

```powershell
Copy-Item infra/.env.example infra/.env

docker compose --env-file infra/.env -f infra/docker-compose.yml up -d
```

### 2) Backend (manual por terminal)

Cada microservicio expone:

- `GET /health`
- `GET /api/v1/ping` (protegido en `content-service`)

Ejemplo (minimo para smoke test):

Terminal 1:

```powershell
mvn -B -ntp -f services/pom.xml -pl auth-service spring-boot:run
```

Terminal 2:

```powershell
mvn -B -ntp -f services/pom.xml -pl content-service spring-boot:run
```

Terminal 3 (gateway en modo localhost):

```powershell
$env:CONTENT_SERVICE_URL="http://localhost:8082"
$env:AUTH_SERVICE_URL="http://localhost:8081"
$env:RAG_SERVICE_URL="http://localhost:8083"
$env:EXAM_SERVICE_URL="http://localhost:8084"
$env:GRADING_SERVICE_URL="http://localhost:8085"
$env:INTEGRITY_SERVICE_URL="http://localhost:8086"
$env:LLM_ORCHESTRATOR_URL="http://localhost:8087"
$env:GATEWAY_ALLOWED_ORIGINS="http://localhost:4200"
$env:JWT_SECRET="01234567890123456789012345678901"
mvn -B -ntp -f services/pom.xml -pl api-gateway spring-boot:run
```

Comprobacion basica:

```powershell
curl http://localhost:8080/api/v1/ping
```

### 3) Frontend Angular

```powershell
cd apps/frontend-angular
npm ci
npm start
```

Abrir: `http://localhost:4200`

Flujo frontend:

- login contra `POST http://localhost:8080/api/v1/auth/login`
- guarda token en memoria
- boton **Probar endpoint protegido** llama `POST http://localhost:8080/content/api/v1/temarios/test`

## Fase 1 auth JWT (como probar)

### 1) Levantar Postgres

```powershell
Copy-Item infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d postgres
```

### 2) Registrar usuario

```powershell
curl -X POST http://localhost:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{"email":"teacher@example.com","password":"password123","role":"TEACHER"}'
```

### 3) Login y obtener JWT

```powershell
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"teacher@example.com","password":"password123"}'
```

Respuesta esperada:

```json
{
  "accessToken": "<jwt>",
  "role": "TEACHER"
}
```

### 4) Endpoint protegido sin token (debe fallar 401)

```powershell
curl -i http://localhost:8080/content/api/v1/ping
```

### 5) Endpoint protegido con token

```powershell
$token="<jwt devuelto en login>"
curl -X POST http://localhost:8080/content/api/v1/temarios/test `
  -H "Authorization: Bearer $token"
```

### 6) Endpoint `me`

```powershell
curl http://localhost:8080/auth/api/v1/auth/me `
  -H "Authorization: Bearer $token"
```

## Diagrama flujo JWT

```text
[Frontend] --login--> [API Gateway] --proxy--> [auth-service]
    ^                         |                    |
    |----- JWT ---------------|<-- signed token ---|
    |
    | Authorization: Bearer <jwt>
    v
[API Gateway] -- valida JWT + anade X-User-Role --> [content-service]
                                                    |
                                                    `--> autoriza por rol (ADMIN/TEACHER)
```

## Gateway routes

- `/auth/**` -> `auth-service:8081`
- `/api/v1/auth/**` -> `auth-service:8081`
- `/content/**` -> `content-service:8082`
- `/rag/**` -> `rag-service:8083`
- `/exam/**` -> `exam-service:8084`
- `/grading/**` -> `grading-service:8085`
- `/integrity/**` -> `integrity-service:8086`
- `/llm/**` -> `llm-orchestrator:8087`

Si el gateway corre fuera de Docker, usa variables de entorno `*_SERVICE_URL` con `localhost`.

## Convenciones de API

- Versionado: `/api/v1`
- JSON para respuestas
- Error format base (placeholder):

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

Mas detalle: [docs/API_CONTRACTS.md](docs/API_CONTRACTS.md)

## Convenciones de commits y PR

### Commits

Usar Conventional Commits:

- `feat: ...`
- `fix: ...`
- `chore: ...`
- `docs: ...`
- `ci: ...`

### Pull Requests

- PRs desde rama personal o feature branch hacia `develop`
- `main` solo recibe merge desde `develop` en releases
- incluir:
  - objetivo del cambio
  - evidencia de tests/build
  - impacto en arquitectura/API

Definition of Done minima para PR:

- build y tests verdes (backend + frontend)
- README/docs actualizados si cambia comportamiento
- sin secretos hardcodeados
- endpoints documentados cuando aplique

## CI

Workflow base: `.github/workflows/ci.yml`

Valida:

- build/test backend Maven
- build/test frontend Angular

## Comandos utiles

Backend:

```powershell
mvn -B -ntp -f services/pom.xml test
```

Frontend:

```powershell
cd apps/frontend-angular
npm run build
npm test -- --watch=false
```

## Next steps

- Implementar modelos de dominio y persistencia real por servicio.
- Definir auth real (JWT/OAuth2, roles, permisos).
- Incorporar mensajeria/eventos (Redis Streams o RabbitMQ/Kafka) segun necesidades.
- Implementar RAG real y pipeline LLM (ingesta, embeddings, retrieval, observabilidad).
- Integracion LMS (ultima fase del roadmap MVP).
- Endurecer seguridad, trazabilidad (`request-id`) y manejo de errores estandar.
- Agregar despliegue automatizado a Railway.

