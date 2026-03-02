# Local Development

## Prerequisites

- Docker Desktop
- Java 21
- Maven 3.9+
- Node.js 22+

## 1. Start infrastructure

```powershell
Copy-Item infra/.env.example infra/.env

docker compose --env-file infra/.env -f infra/docker-compose.yml up -d
```

Services started:

- Postgres: `localhost:5432`
- Redis: `localhost:6379`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

## 2. Backend smoke test (gateway + one service)

Terminal 1:

```powershell
mvn -B -ntp -f services/pom.xml -pl content-service spring-boot:run
```

Terminal 2:

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

Checks:

```powershell
curl http://localhost:8082/api/v1/ping
curl http://localhost:8080/content/api/v1/ping
curl http://localhost:8080/api/v1/ping
curl http://localhost:8080/health
```

## 3. Frontend

```powershell
cd apps/frontend-angular
npm ci
npm start
```

Open `http://localhost:4200` and click **Ping Gateway**.

## 4. Build and tests

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

## Optional: run a different service

Change `-pl <module>` in Maven command:

- `auth-service`
- `content-service`
- `rag-service`
- `exam-service`
- `grading-service`
- `integrity-service`
- `llm-orchestrator`
- `api-gateway`