# Branching and PR Rules

## Main branches

- `main`: stable/releasable code only
- `develop`: integration branch for upcoming release

## Personal branches

Created from `develop`:

- `jmruiz`
- `acamacho`
- `ldemicheli`

Use personal branches for daily work and draft integration.

## Feature branches (recommended)

For medium/large tasks, create branches from `develop` (or from your personal branch if team agrees):

- `feature/<servicio>-<breve-descripcion>`
- `svc/<servicio>/<tarea>`

Examples:

- `feature/content-catalog-crud`
- `svc/rag/vector-index-bootstrap`

## Merge policy

- PR target should be `develop`
- `main` accepts merges from `develop` only (release flow)
- avoid long-lived branches per microservice as mainline branches

## Commit convention

Use Conventional Commits:

- `feat: ...`
- `fix: ...`
- `chore: ...`
- `docs: ...`
- `ci: ...`

## PR checklist

- clear scope and motivation
- linked issue/task
- local build/tests pass
- docs updated when behavior changes
- no secrets committed

## Definition of Done (baseline)

- code compiles
- tests added/updated and green
- API changes documented
- reviewer can run locally with documented steps