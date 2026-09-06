# Specification: IF2AF0203 Account Auth Alignment

## Requirements

### Profile Types (Signup / Login)

* The system SHALL expose three public profile types: `CAREER_EXPLORER`, `CAREER_COUNSELLOR`, `ADMINISTRATOR`.
* Profile types SHALL be stored on `users.profile_type` and returned in auth/user API responses.
* Profile types SHALL NOT include `SUB_ADMIN`, `SUPER_ADMIN`, or `DATA_ANALYST` (deferred).

### Role Names (RBAC)

* The system SHALL enforce RBAC for `CAREER_EXPLORER`, `CAREER_COUNSELLOR`, `ADMINISTRATOR`, `SUB_ADMIN`, and `SUPER_ADMIN`.
* Public signup SHALL assign only the three profile-aligned roles via `ProfileRoleMapper`.
* `SUB_ADMIN` and `SUPER_ADMIN` SHALL be assignable only through administrative role assignment, not public signup.

### Authentication

* Login SHALL accept optional `profileType`; when provided, it MUST match the user's stored `profile_type` or return `PROFILE_MISMATCH`.
* Register SHALL accept optional `profileType` (default `CAREER_EXPLORER`) and assign the matching RBAC role.
* Register SHALL return `consentRequired: true` and `consentType: "GUARDIAN"` when DOB indicates age below 18.
* `SUPER_ADMIN` and tenant admin users SHALL login with `profileType: ADMINISTRATOR`.

### SUPER_ADMIN Add-On

* `SUPER_ADMIN` SHALL have permissions limited to `USER_WRITE` and `TENANT_WRITE` (registration/provisioning).
* `SUPER_ADMIN` SHALL access `POST /api/v1/users` but NOT other admin endpoints (org-units, config, clusters, assignment, etc.).

## Scenarios

### Successful Login with Profile Type

**GIVEN** an active user with `profile_type = CAREER_EXPLORER`  
**WHEN** they POST to `/api/v1/auth/login` with matching `profileType`  
**THEN** the system returns 200 with tokens and user data.

### Profile Mismatch on Login

**GIVEN** a user with `profile_type = CAREER_COUNSELLOR`  
**WHEN** they POST to `/api/v1/auth/login` with `profileType: CAREER_EXPLORER`  
**THEN** the system returns an error with code `PROFILE_MISMATCH`.

### Public Register

**GIVEN** a new email and valid tenant  
**WHEN** they POST to `/api/v1/auth/register` with `profileType: CAREER_COUNSELLOR`  
**THEN** a user is created with `profile_type = CAREER_COUNSELLOR` and role `CAREER_COUNSELLOR`.

### SUPER_ADMIN Provisioning

**GIVEN** an authenticated user with role `SUPER_ADMIN`  
**WHEN** they POST to `/api/v1/users`  
**THEN** the user is created successfully.

### SUPER_ADMIN Blocked from Admin APIs

**GIVEN** an authenticated user with role `SUPER_ADMIN`  
**WHEN** they POST to `/api/v1/org-units`  
**THEN** the system returns 403.

### SUB_ADMIN Scoped Access Retained

**GIVEN** an authenticated user with role `SUB_ADMIN`  
**WHEN** they GET `/api/v1/users`  
**THEN** the system returns 200 with paginated user list.

### Facilitator Assignment Profile Validation

**GIVEN** a user with `profile_type = CAREER_COUNSELLOR` as facilitator  
**WHEN** an admin assigns a `CAREER_EXPLORER` student  
**THEN** the assignment is created successfully.
