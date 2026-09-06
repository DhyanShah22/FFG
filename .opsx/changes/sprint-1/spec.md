# Specification: Foundation & Identity (FFG Sprint 1)

## Requirements

### System & Architecture

* The system SHALL use PostgreSQL with UUIDs as primary keys for all tables.
* The system SHALL implement standard audit columns (`created_at`, `updated_at`, `created_by`, `updated_by`, `is_active`, `is_deleted`, `deleted_at`, `deleted_by`) for applicable business tables.
* The system SHALL return API responses in a standardized JSON format containing `success`, `message`, `data`, and `timestamp`.
* List APIs SHALL support pagination (`page`, `size`) where specified.

### Authentication & Authorization

* The system SHALL authenticate users using JWT Bearer Tokens.
* The system SHALL persist refresh tokens and support logout revocation.
* The system SHALL manage single-use, time-bound password-reset tokens via `verification_tokens`.
* The system SHALL enforce RBAC for STUDENT, FACILITATOR, ADMIN, SUB_ADMIN, and SUPER_ADMIN.
* The system SHALL support scoped Sub-Admin access via `user_roles.scope_type` / `scope_id`.
* The system SHALL log authentication attempts (success and failure).

### Consent, Org, Assignment, Config

* The system SHALL capture and withdraw consent records.
* The system SHALL manage org unit hierarchy and organizational clusters.
* The system SHALL assign students to facilitators and retain assignment history.
* The system SHALL expose configuration groups/values and languages.

## Scenarios

### Successful Login

**GIVEN** a registered user with valid credentials  
**WHEN** they POST to `/api/v1/auth/login` with `loginId` or `email`  
**THEN** the system returns 200 with `accessToken`, `refreshToken`, `expiresIn`, and nested `user` data.

### Token Refresh & Logout

**GIVEN** a valid refresh token  
**WHEN** they POST to `/api/v1/auth/refresh` or `/api/v1/auth/refresh-token`  
**THEN** new tokens are issued and the old refresh token is revoked.  
**WHEN** they POST to `/api/v1/auth/logout`  
**THEN** access and refresh tokens are invalidated.

### Consent Capture

**GIVEN** an authenticated user  
**WHEN** they POST to `/api/v1/consents`  
**THEN** a consent record is stored and returned.

### Role Assignment

**GIVEN** an Admin  
**WHEN** they POST roles to `/api/v1/users/{userId}/roles` with optional scope  
**THEN** `user_roles` rows are created and authorities reflect the assignment.

### Facilitator Assignment

**GIVEN** an Admin  
**WHEN** they POST student IDs to `/api/v1/facilitators/{facilitatorId}/students`  
**THEN** active assignments are created and history is recorded.

### Unauthorized Access

**GIVEN** a STUDENT  
**WHEN** they POST to `/api/v1/org-units`  
**THEN** the system returns 403 with error code `ACCESS_DENIED`.
