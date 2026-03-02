# Architecture

## Context

This repository is a monorepo for an e-learning platform built with:

- Spring Boot microservices
- Angular frontend
- PostgreSQL + Redis + MinIO

The current phase is MVP skeleton (no LMS integration yet).

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

## Service responsibilities (MVP skeleton)

- `api-gateway`: central HTTP entrypoint, CORS and route forwarding.
- `auth-service`: auth/authz domain placeholder.
- `content-service`: content management placeholder.
- `rag-service`: retrieval and vector-search placeholder.
- `exam-service`: exam lifecycle placeholder.
- `grading-service`: grading workflows placeholder.
- `integrity-service`: integrity/proctoring placeholder.
- `llm-orchestrator`: orchestration placeholder for future AI workflows.

## Data strategy

Single PostgreSQL instance for local MVP with schema-per-service:

- `auth`
- `content`
- `rag`
- `exam`
- `grading`
- `integrity`

This keeps setup simple while preserving separation for future extraction.

## Communication

Current:

- synchronous HTTP through API gateway

Planned:

- async event-driven integration (placeholder) for grading, notifications, integrity alerts, and analytics.

## Security baseline

- Security dependency enabled in gateway and auth-service.
- Current skeleton allows all requests (for local bootstrap only).
- Future: JWT, role model, service-to-service auth, audit trails.

## Deployment target

Planned deployment target is Railway.

Pending work before production readiness:

- container orchestration strategy
- secrets management
- CI/CD release gates
- observability (metrics/tracing/log correlation)