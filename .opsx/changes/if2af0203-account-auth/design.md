# System Design: IF2AF0203 Account Auth Alignment

## ProfileType vs RoleName

| Layer | Enum | Values | Purpose |
|-------|------|--------|---------|
| Signup/login identity | `ProfileType` | `CAREER_EXPLORER`, `CAREER_COUNSELLOR`, `ADMINISTRATOR` | IF2AF0203 profile picker; stored on `users.profile_type` |
| Authorization | `RoleName` | above 3 + `SUB_ADMIN`, `SUPER_ADMIN` | RBAC; not selectable at public signup |

`ProfileRoleMapper` maps the three public profile types to their corresponding RBAC roles at register/provision time.

## Database Migration Strategy

* **V1–V3 unchanged** — preserve Flyway checksum integrity for existing environments.
* **V4** (`V4__ffg_profile_types_and_role_renames.sql`):
  - Rename `users.user_type` → `users.profile_type`
  - Remap legacy values to FFG profile names
  - Rename role rows: `STUDENT`/`FACILITATOR`/`ADMIN` → FFG names
  - Retain `SUB_ADMIN`, `SUPER_ADMIN` role rows
  - Reseed `SUPER_ADMIN` permissions to `USER_WRITE` + `TENANT_WRITE` only

## API Field Changes

| Endpoint | Request | Response |
|----------|---------|----------|
| `POST /auth/login` | + `profileType` (optional) | `user.profileType` |
| `POST /auth/register` | `userType` → `profileType` | `profileType` |
| `GET /auth/me` | — | `profileType` |
| User CRUD | `profileType` filter/param | `profileType` |

## PreAuthorize Matrix

| Endpoint | Roles |
|----------|-------|
| `POST /auth/register` | Public |
| `POST /users` | `ADMINISTRATOR`, `SUPER_ADMIN` |
| `GET /users`, `GET /users/{id}` | `ADMINISTRATOR`, `SUB_ADMIN` |
| `PUT/PATCH /users/*`, role assign/remove | `ADMINISTRATOR` |
| Org-units write, config write, clusters | `ADMINISTRATOR` |
| Facilitator assignment, permissions list | `ADMINISTRATOR`, `SUB_ADMIN` |
| Consent admin access | `ADMINISTRATOR`, `SUB_ADMIN` |

## Login Flow for Admin Tiers

Users with RBAC role `SUPER_ADMIN` or `ADMINISTRATOR` store `profile_type = ADMINISTRATOR`. Both authenticate via:

```json
{ "loginId": "admin@example.com", "password": "...", "profileType": "ADMINISTRATOR" }
```

JWT authorities include the actual RBAC role (`ROLE_SUPER_ADMIN` or `ROLE_ADMINISTRATOR`).

## Deferred (IF2AF0203 Gaps)

- OTP verification (4-digit, 150s validity, 30s resend)
- Role-specific signup forms and configurable profile fields
- Google SSO
- `DATA_ANALYST` profile (Phase 2)
