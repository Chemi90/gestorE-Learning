# gestorE-Learning

Monorepo bootstrap para una plataforma e-learning basada en microservicios.

Estado actual: **skeleton MVP** listo para comenzar desarrollo hoy.

## Stack

- Backend: Java 21 + Spring Boot (microservices)
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
| auth-service | 8081 | autenticacion/autorizacion (skeleton) |
| content-service | 8082 | gestion de contenidos (skeleton) |
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
- Java 21
- Maven 3.9+
- Node.js 22+ y npm

## Quick start local

### 1) Levantar infra con un comando

```powershell
Copy-Item infra/.env.example infra/.env

docker compose --env-file infra/.env -f infra/docker-compose.yml up -d
```

### 2) Backend (manual por terminal)

Cada microservicio expone:

- `GET /health`
- `GET /api/v1/ping`

Ejemplo (minimo para smoke test):

Terminal 1:

```powershell
mvn -B -ntp -f services/pom.xml -pl content-service spring-boot:run
```

Terminal 2 (gateway en modo localhost):

```powershell
$env:CONTENT_SERVICE_URL="http://localhost:8082"
$env:AUTH_SERVICE_URL="http://localhost:8081"
$env:RAG_SERVICE_URL="http://localhost:8083"
$env:EXAM_SERVICE_URL="http://localhost:8084"
$env:GRADING_SERVICE_URL="http://localhost:8085"
$env:INTEGRITY_SERVICE_URL="http://localhost:8086"
$env:LLM_ORCHESTRATOR_URL="http://localhost:8087"
$env:GATEWAY_ALLOWED_ORIGINS="http://localhost:4200"
mvn -B -ntp -f services/pom.xml -pl api-gateway spring-boot:run
```

Comprobacion:

```powershell
curl http://localhost:8080/content/api/v1/ping
curl http://localhost:8080/api/v1/ping
```

### 3) Frontend Angular

```powershell
cd apps/frontend-angular
npm ci
npm start
```

Abrir: `http://localhost:4200`

Boton **Ping Gateway** llama a `GET http://localhost:8080/content/api/v1/ping`.

## Gateway routes

- `/auth/**` -> `auth-service:8081`
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
