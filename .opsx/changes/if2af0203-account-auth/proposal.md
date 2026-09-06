# Change Proposal: IF2AF0203 Account Auth Alignment

## Intent

Align Sprint 1 authentication and identity with the IF2AF0203 Account Creation (Sign Up) and Log In Logic document. Introduce FFG **profile types** for signup/login UX, rename core RBAC roles to match product terminology, decouple signup profiles from internal admin roles, and preserve **SUPER_ADMIN** as a registration-only add-on not defined in the IF2AF0203 doc.

## Scope

- Revert incomplete Sprint-1 refactor that removed `SUB_ADMIN`/`SUPER_ADMIN` and rewrote V1 in place.
- Flyway **V4** migration: `user_type` → `profile_type`, role renames, scoped `SUPER_ADMIN` permissions.
- New `ProfileType` enum (3 public signup profiles) decoupled from `RoleName` (5 RBAC roles).
- Auth API: optional `profileType` on login (mismatch → `PROFILE_MISMATCH`); `profileType` on register.
- Role renames: `STUDENT` → `CAREER_EXPLORER`, `FACILITATOR` → `CAREER_COUNSELLOR`, `ADMIN` → `ADMINISTRATOR`.
- `SUPER_ADMIN` and tenant `ADMINISTRATOR` both login with `profileType: ADMINISTRATOR`.
- `SUPER_ADMIN` restricted to user provisioning (`POST /users`); excluded from other admin endpoints.
- Response field rename: `userType` → `profileType` (breaking change for API clients).

## Non-Goals

- OTP verification flows, SSO, username auto-generation, 13-char password rules.
- Role-specific signup form fields (gender, country, education stage, etc.).
- `DATA_ANALYST` profile (Phase 2 per Sprint 1 proposal).
- 1-hour signup session persistence.

## Relationship to Sprint 1

Builds on the committed Sprint 1 foundation (V1–V3, auth stack, RBAC). Does not replace Sprint 1 infrastructure — only realigns identity terminology and auth contract to IF2AF0203.
