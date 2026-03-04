# Skill: Docker y Compose

## Proposito
Patrones para Dockerfiles multi-stage, docker-compose, healthchecks e infraestructura local.

## Reglas Obligatorias

1. Cada Dockerfile DEBE copiar TODOS los `pom.xml` hermanos (requisito del reactor Maven).
2. Build multi-stage: `maven` para compilar, `eclipse-temurin-jre` para ejecutar.
3. Healthchecks usan TCP check via bash (`/dev/tcp/localhost/<puerto>`).
4. No exponer puertos de BD/Redis/MinIO al host en produccion.

## Dockerfile — Patron Canonico (Backend)

```dockerfile
FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /workspace

# 1. Copiar TODOS los pom.xml del reactor (obligatorio)
COPY services/pom.xml services/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/content-service/pom.xml services/content-service/pom.xml
COPY services/exam-service/pom.xml services/exam-service/pom.xml
COPY services/grading-service/pom.xml services/grading-service/pom.xml
COPY services/integrity-service/pom.xml services/integrity-service/pom.xml
COPY services/llm-orchestrator/pom.xml services/llm-orchestrator/pom.xml
COPY services/rag-service/pom.xml services/rag-service/pom.xml

# 2. Descargar dependencias (cache de Docker layer)
RUN mvn -B -ntp -f services/pom.xml -pl <nombre-servicio> -am dependency:go-offline

# 3. Copiar codigo fuente y compilar
COPY services services
RUN mvn -B -ntp -f services/pom.xml -pl <nombre-servicio> -am clean package -DskipTests

# 4. Imagen de ejecucion
FROM eclipse-temurin:25-jre
COPY --from=build /workspace/services/<nombre-servicio>/target/*.jar /app/app.jar
EXPOSE <puerto>
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**CRITICO**: Si agregas un servicio nuevo al monorepo, TODOS los Dockerfiles existentes
deben actualizarse para incluir el nuevo `COPY services/<nuevo>/pom.xml`.

## Dockerfile — Frontend (Angular + nginx)

```dockerfile
FROM node:24-alpine AS build
WORKDIR /app
COPY apps/frontend-angular/package*.json ./
RUN npm ci
COPY apps/frontend-angular/ .
RUN npm run build

FROM nginx:alpine
COPY apps/frontend-angular/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend-angular/browser /usr/share/nginx/html
EXPOSE 80
```

## docker-compose.yml — Servicio Backend

```yaml
<nombre-servicio>:
  build:
    context: ../
    dockerfile: services/<nombre-servicio>/Dockerfile
  ports:
    - "<puerto>:<puerto>"
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/elearning
    SPRING_DATASOURCE_USERNAME: elearning
    SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
  depends_on:
    postgres:
      condition: service_healthy
  healthcheck:
    test: ["CMD-SHELL", "bash -c '</dev/tcp/localhost/<puerto>'"]
    interval: 10s
    timeout: 5s
    retries: 10
    start_period: 30s
```

## Infraestructura — Servicios Base

```yaml
postgres:
  image: pgvector/pgvector:pg16
  environment:
    POSTGRES_DB: elearning
    POSTGRES_USER: elearning
    POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./postgres/init:/docker-entrypoint-initdb.d
  ports:
    - "5432:5432"
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U elearning"]
    interval: 5s
    timeout: 5s
    retries: 5

redis:
  image: redis:7.2-alpine
  ports:
    - "6379:6379"

minio:
  image: minio/minio
  command: server /data --console-address ":9001"
  environment:
    MINIO_ROOT_USER: ${MINIO_ROOT_USER:-minioadmin}
    MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD:-minioadmin}
  ports:
    - "9000:9000"
    - "9001:9001"
```

## Comandos Utiles

```bash
# Stack completo
cp infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d --build

# Solo infraestructura (para desarrollo con IDE)
docker compose -f infra/docker-compose.yml up -d postgres redis minio

# Rebuild de un servicio especifico
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d --build <servicio>

# Ver logs
docker compose -f infra/docker-compose.yml logs -f <servicio>

# Limpiar todo
docker compose -f infra/docker-compose.yml down -v
```

## Checklist — Agregar Servicio al Compose

1. Agregar bloque del servicio en `infra/docker-compose.yml`
2. Agregar `depends_on: postgres: condition: service_healthy` (si usa BD)
3. Agregar healthcheck TCP en el puerto del servicio
4. Agregar como dependencia del `api-gateway` (si el gateway lo necesita)
5. Actualizar TODOS los Dockerfiles existentes con el nuevo `COPY pom.xml`
6. Verificar que `.env.example` tiene las variables necesarias

## Referencias en el Repo

- `services/auth-service/Dockerfile`
- `services/api-gateway/Dockerfile`
- `apps/frontend-angular/Dockerfile`
- `apps/frontend-angular/nginx.conf`
- `infra/docker-compose.yml`
- `infra/.env.example`
