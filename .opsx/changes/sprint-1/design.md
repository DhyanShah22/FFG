# System Design: Sprint 1 Foundation (FFG Aligned)

## Tech Stack

* **Framework:** Spring Boot 3.x / 4.x (Java 17+)
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate
* **Security:** Spring Security + JWT
* **Migrations:** Flyway (V1, V2, V3)

## Contract Strategy (Aliases)

* Prefer FFG request/response shapes.
* Keep aliases: `email` ↔ `loginId`; `GET /users/me` and `GET /auth/me`; `POST /auth/refresh` and `POST /auth/refresh-token`.
* Tenant from JWT `tenantId`; optional `X-Tenant-Id` must match when present.
* Refresh tokens persisted in `refresh_tokens`; access-token logout also uses in-memory denylist.

## Database Schema

### V1 — Core

* `tenants`, `org_units`, `users`, `roles`, `permissions`, `role_permissions`, `user_roles`, `auth_attempts`

### V2 — Tokens & audit

* `verification_tokens`
* Soft-delete: `deleted_at`, `deleted_by` on business tables sharing `BaseEntity`
* `career_cluster_translations`, `career_translations` (stubs; FKs deferred until careers/languages masters exist)

### V3 — Profile, consent, assignment, config

* Enrich `tenants` (`description`, `logo_url`), `org_units` (`description`, `address`, `metadata`), `users` (names, mobile, DOB, language FKs, etc.)
* `user_profiles`, `consent_records`, `refresh_tokens`
* `organizational_clusters`, `organizational_cluster_members`
* `facilitator_student_assignments`, `assignment_history`
* `configuration_groups`, `configurations`, `languages` (+ seeds)

**Status:** V1–V3 applied; Hibernate `ddl-auto=validate` passes.

## API Surface (Sprint 1)

| Area | Endpoints |
|------|-----------|
| Auth | register, login, logout, refresh(/refresh-token), me, forgot/reset-password |
| Users | CRUD, me, status, platform/assessment language, paginated list |
| RBAC | GET roles, GET permissions, assign/remove user roles |
| Consent | capture, list by user, withdraw |
| Org | org-units CRUD, tree; clusters create/members |
| Assignment | assign/list/unassign students; assignment history |
| Config | configuration groups/values |
| Languages | list languages |

## Standard Response Wrapper

```json
{
  "success": true,
  "message": "String",
  "data": {},
  "timestamp": "ISO-8601"
}
```

Paginated `data`: `{ "content", "page", "size", "totalElements", "totalPages", "last" }`.
