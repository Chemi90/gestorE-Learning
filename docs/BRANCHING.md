# Branching and PR Workflow

## Branch model

- `main`: production branch
- `develop`: integration branch
- `feature/*`: task development branches

Recommended naming examples:

- `feature/auth-jwt-phase1`
- `feature/content-role-guards`

## Pull request policy

- Every feature branch must open PR to `develop`.
- Direct commits to `main` are not allowed.
- Minimum code review: 1 approver.
- PR author cannot self-approve.

## Merge policy

- `main` receives changes only from `develop` (release merge).
- `develop` receives changes from `feature/*` via PR.
- Keep PRs small and focused.

## Commit policy

Use Conventional Commits:

- `feat: ...`
- `fix: ...`
- `chore: ...`
- `docs: ...`
- `ci: ...`

## PR checklist

- Scope is clear and limited.
- Local build and tests are green.
- API/documentation changes are included when behavior changes.
- No hardcoded secrets.
- Rollback impact is identified for risky changes.

