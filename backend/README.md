# CAP Backend (Spring Boot)

Spring Boot API for the Antarang Career Assessment Platform.

**Full documentation** (git workflow, Flyway/AWS runbook, monorepo layout): see [../README.md](../README.md).

## Quick start

**Prerequisites:** Java 21, PostgreSQL, database `antarang_cap` (or update `src/main/resources/application.yaml`).

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- Migrations: `src/main/resources/db/migration/` (Flyway runs on startup)
- Tests: `./mvnw clean test`
- API smoke script: `./scripts/smoke-signup-and-status.sh`

`POST /api/v1/auth/signup` (start a signup session), `POST /api/v1/auth/login`,
and the rest of the `/api/v1/auth/**` entry points listed in `SecurityConfig`
are anonymous by design (stateless JWT API, CSRF disabled). Only
`/api/v1/auth/me`, `/api/v1/auth/logout`, and other non-listed endpoints
require a valid access token supplied via the `Authorization` request header.

### Local H2 development

Run the API without PostgreSQL:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile uses an in-memory H2 database and Hibernate creates its
schema directly from the JPA entities. PostgreSQL Flyway migrations are
intentionally disabled for this profile because they use PostgreSQL-only SQL.
It seeds a `demo` tenant, all application roles, and `GENDER` configuration
values needed for the signup API. Data is reset whenever the application stops.

## Key paths

| Path | Purpose |
|------|---------|
| `src/main/java/com/antarang/cap/` | Application code |
| `src/main/resources/application.yaml` | Local DB & app config |
| `src/main/resources/db/migration/` | Flyway SQL (V1–V21) |
| `scripts/` | Dev helper scripts (not production deploy) |
| `.mvn/wrapper/` + `mvnw` | Maven Wrapper (pinned Maven version) |

## Docs elsewhere in the monorepo

- [docs/IF2AF0203-Bruno-API-Testing.md](../docs/IF2AF0203-Bruno-API-Testing.md) — signup/login API checklist
- [.opsx/changes/](../.opsx/changes/) — sprint & IF2AF0203 change proposals
