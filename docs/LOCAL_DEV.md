# Local Development

## Prerequisites

- Docker Desktop
- Java 25
- Maven 3.9+
- Node.js 24+

## 1. Start infrastructure

```powershell
Copy-Item infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d
```

Expected services:

- Postgres: `localhost:5432`
- Redis: `localhost:6379`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

## 2. Validate docker compose status

```powershell
docker compose --env-file infra/.env -f infra/docker-compose.yml ps
```

All containers (`postgres`, `redis`, `minio`) should be in `Up` status and marked `healthy`.

To inspect logs:

```powershell
docker compose --env-file infra/.env -f infra/docker-compose.yml logs -f
```

## 3. Check Postgres connectivity

```powershell
docker exec -it elearning-postgres psql -U elearning -d elearning -c "select current_database(), now();"
```

Optional schema check:

```powershell
docker exec -it elearning-postgres psql -U elearning -d elearning -c "select schema_name from information_schema.schemata where schema_name in ('auth','content','rag','exam','grading','integrity');"
```

## 4. Check MinIO

Liveness endpoint:

```powershell
curl http://localhost:9000/minio/health/live
```

Readiness endpoint:

```powershell
curl http://localhost:9000/minio/health/ready
```

Console:

- Open `http://localhost:9001`
- Login with `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` from `infra/.env`

## 5. Backend smoke test (gateway + auth + content)

Terminal 1:

```powershell
mvn -B -ntp -f services/pom.xml -pl auth-service spring-boot:run
```

Terminal 2:

```powershell
mvn -B -ntp -f services/pom.xml -pl content-service spring-boot:run
```

Terminal 3:

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

Health checks:

```powershell
curl http://localhost:8081/health
curl http://localhost:8082/health
curl http://localhost:8080/health
```

Ping checks:

```powershell
curl http://localhost:8081/api/v1/ping
curl http://localhost:8082/api/v1/ping
curl http://localhost:8080/api/v1/ping
```

## 6. Frontend

```powershell
cd apps/frontend-angular
npm ci
npm start
```

Open `http://localhost:4200`.

## 7. Build and tests

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

## Smoke Test Checklist

- [ ] `docker compose up` starts `postgres`, `redis`, `minio` without restart loop
- [ ] `docker compose ps` shows healthy containers
- [ ] Postgres accepts SQL query from `docker exec`
- [ ] MinIO liveness and readiness endpoints respond successfully
- [ ] `auth-service`, `content-service`, and `api-gateway` respond on `/health`
- [ ] `/api/v1/ping` responds in each service and through gateway
- [ ] Frontend starts and can reach gateway

