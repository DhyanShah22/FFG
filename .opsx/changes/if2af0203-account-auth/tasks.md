# Implementation Tasks — IF2AF0203 Account Auth Alignment

## 0. Reset Incomplete Refactor

- [x] Revert working-tree changes to HEAD (except local `application.yaml`)
- [x] Delete bad `V4__replace_user_types_with_profile_types.sql`

## 1. Database Migration

- [x] Create `V4__ffg_profile_types_and_role_renames.sql`
- [x] Rename `user_type` → `profile_type` with value remapping
- [x] Rename core roles; retain `SUB_ADMIN` / `SUPER_ADMIN`
- [x] Scope `SUPER_ADMIN` to `USER_WRITE` + `TENANT_WRITE`

## 2. Domain & DTOs

- [x] Add `ProfileType` enum (3 values)
- [x] Add `ProfileRoleMapper`
- [x] Update `RoleName` (5 values with renames)
- [x] Delete `UserType`; update `User` entity to `profileType`
- [x] Update auth/user request and response DTOs

## 3. Services & Security

- [x] `AuthService`: login profile mismatch check; register via `ProfileRoleMapper`
- [x] `UserPrincipal`: derive `primaryRole` from `profileType`
- [x] `UserService` / `UserRoleService`: `profileType` throughout
- [x] `FacilitatorAssignmentService`: counsellor/explorer profile checks
- [x] `ConsentService`: admin roles `ADMINISTRATOR`, `SUB_ADMIN`

## 4. Controllers — Renamed Roles & SUPER_ADMIN Scope

- [x] `UserController`: SUPER_ADMIN on `POST /users` only
- [x] `OrgUnitController`, `ConfigurationController`, `OrganizationalClusterController`: `ADMINISTRATOR` only
- [x] `FacilitatorAssignmentController`, `PermissionController`: `ADMINISTRATOR`, `SUB_ADMIN`

## 5. Documentation

- [x] `.opsx/changes/if2af0203-account-auth/` (proposal, spec, design, tasks)

## 6. Verification (Sprint-1 scope)

- [x] Flyway V4 applies on existing DB
- [x] `./mvnw clean test` passes
- [ ] Login profile mismatch returns `PROFILE_MISMATCH` (manual smoke test)
- [ ] SUPER_ADMIN can `POST /users`, blocked on org-units (manual smoke test)

## 7. Bruno manual testing (Sprint-1)

See [docs/IF2AF0203-Bruno-API-Testing.md](../../docs/IF2AF0203-Bruno-API-Testing.md) for endpoints, payloads, and step-by-step Bruno checklist.

## 8. PDF multi-step signup / login (IF2AF0203 §II–III) — **not complete**

Do not mark signup/login as done until Bruno E2E passes for all profile types.

### OTP signup verification

- [x] Signup session OTP send / verify / resend (`V7`, `SignupSessionController`)
- [x] OTP expire endpoint (`POST .../otp/expire`)
- [x] 4-digit OTP, 150s validity, 30s resend (`OtpService`, `app.otp.*`)
- [ ] Bruno E2E: Explorer email-only, mobile-only, and both channels

### Consent before account creation

- [x] `consent` block required in session data; validated at `complete` (`RegisterSignupValidator`)
- [x] `consent_records` row created on account creation (`AuthService.registerFromSession`)
- [ ] Bruno E2E: guardian vs self consent paths

### Password policy (13 characters)

- [x] Signup session password min 13 (`RegisterRequest`)
- [x] Reset-password min 13 (`ResetPasswordRequest`)
- [x] Admin `POST /users` min 13 (`CreateUserRequest`)
- [x] Profile password change min 13 (`UpdateProfileRequest`)

### Google SSO

- [x] `POST /api/v1/auth/google` (`GoogleAuthService`)
- [ ] Configure `GOOGLE_CLIENT_ID` in target environment
- [ ] Bruno E2E with real Google ID token

### Explorer username login

- [x] `findByLoginIdWithRoles` matches email or username
- [ ] Bruno smoke: `loginId` = username (not email) for Explorer account

### Password reset (OTP, not opaque token)

- [x] `forgot-password` sends 4-digit OTP (`OtpService`)
- [x] `forgot-password/resend` with `challengeId`
- [x] `reset-password` with `challengeId` + `otp`
- [x] Explorer mobile reset via `mobileNumber` + `profileType`
- [ ] Bruno E2E: email reset and Explorer mobile reset

### Signup session lifecycle (1h, abandon, back-navigation)

- [x] 1-hour TTL extended on activity (`SignupSessionService.touchSession`)
- [x] `DELETE /signup/sessions/{id}` abandon — clears data, expires OTPs (window close)
- [x] Back-navigation: changing email/mobile after OTP clears verification (`applyBackNavigationVerificationRules`)
- [ ] Bruno E2E: abandon, timeout, and back-button re-verify scenarios

### Full PDF workflow sign-off

- [ ] Career Explorer full signup (session → consent → OTP → complete → activate → login)
- [ ] Career Counsellor, Administrator, Data Analyst full signup
- [ ] Forgot-password OTP + reset for email and Explorer mobile
- [ ] Google SSO login (when configured)
