# Implementation Tasks - Sprint 1 (FFG Aligned)

## 1. Project Setup & Architecture

- [x] **1.1. Project Initialization:** Bootstrap Spring Boot; configure DB connectivity.
- [x] **1.2. Core Architecture:** `ApiResponse<T>` + paginated data shape.
- [x] **1.3. Exception Handling:** `@RestControllerAdvice` with FFG-compatible error fields.

## 2. Database Migrations (Flyway)

- [x] **2.1. V1:** `V1__core_tenant_org_user_rbac.sql`
- [x] **2.2. V2:** `V2__add_auth_tokens_translations_and_audit.sql`
- [x] **2.3. V3:** `V3__profile_consent_assignment_config.sql` (profiles, consent, refresh_tokens, clusters, assignment, config, languages + enrichments)

## 3. Data Access Layer

- [x] **3.1. Base Entity:** audit + soft-delete fields
- [x] **3.2. Core Entities:** Tenant, OrgUnit, User, Role, Permission, UserRole
- [x] **3.3. V2 Entities:** VerificationToken, CareerClusterTranslation, CareerTranslation
- [x] **3.4. V3 Entities:** UserProfile, ConsentRecord, RefreshToken, OrganizationalCluster(+Member), FacilitatorStudentAssignment, AssignmentHistory, ConfigurationGroup, Configuration, Language
- [x] **3.5. Repositories:** for all V3 entities; enrich User/Tenant/OrgUnit mappings

## 4. Security & Core Services

- [x] **4.1. JWT Utility**
- [x] **4.2. Security Filter Chain**
- [x] **4.3. UserService / OrgUnitService (foundation)**
- [x] **4.4. AuthService (login, password reset)**
- [x] **4.5. Persisted refresh tokens + FFG auth contract aliases**
- [x] **4.6. Expand User/Org services (status, languages, pagination, PUT, /tree)**

## 5. API Endpoints

### 5.1. Authentication `/api/v1/auth`

- [x] `POST /login`
- [x] `POST /register`
- [x] `POST /logout`
- [x] `POST /refresh`
- [x] `POST /refresh-token` (alias)
- [x] `GET /me` (FFG profile)
- [x] `POST /forgot-password`
- [x] `POST /reset-password`

### 5.2. Users `/api/v1/users`

- [x] `GET /me`, `PUT /me`
- [x] `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`
- [x] `PATCH /{id}/status`
- [x] `PUT /{id}/platform-language`
- [x] `PUT /{id}/assessment-language`
- [x] Paginated/filtered list

### 5.3. Organization `/api/v1/org-units`

- [x] `POST /`, `GET /`, `GET /{id}`
- [x] `PUT /{id}`
- [x] `GET /tree`

### 5.4. RBAC

- [x] `GET /api/v1/roles`
- [x] `GET /api/v1/permissions`
- [x] `POST /api/v1/users/{userId}/roles`
- [x] `DELETE /api/v1/users/{userId}/roles/{roleId}`

### 5.5. Consent

- [x] `POST /api/v1/consents`
- [x] `GET /api/v1/users/{userId}/consents`
- [x] `POST /api/v1/consents/{consentId}/withdraw`

### 5.6. Clusters

- [x] `POST /api/v1/organizational-clusters`
- [x] `POST /api/v1/organizational-clusters/{clusterId}/members`
- [x] `GET /api/v1/organizational-clusters/{clusterId}/members`

### 5.7. Assignment

- [x] `POST /api/v1/facilitators/{facilitatorId}/students`
- [x] `GET /api/v1/facilitators/{facilitatorId}/students`
- [x] `DELETE /api/v1/facilitators/{facilitatorId}/students/{studentId}`
- [x] `GET /api/v1/students/{studentId}/assignment-history`

### 5.8. Config & Languages

- [x] `POST /api/v1/configuration-groups`
- [x] `GET /api/v1/configuration-groups`
- [x] `POST /api/v1/configurations`
- [x] `GET /api/v1/configurations`
- [x] `GET /api/v1/languages`
