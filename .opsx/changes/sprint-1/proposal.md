# Change Proposal: Sprint 1 - Foundation, Identity, Org & Config (FFG Aligned)

## Intent

Build the foundational Spring Boot and PostgreSQL backend for the Antarang Career Assessment Platform (CAP) MVP, aligned with the official FFG API Specification §30 and Database Design V1–V2 modules. This establishes multitenancy, identity, RBAC, consent, organization hierarchy/clusters, student–facilitator assignment, configuration values, and languages before assessment logic (Sprints 2–4).

## Scope

- Base project setup (Spring Boot, PostgreSQL, Flyway).
- Standardized API response wrappers, paginated list responses, and global error handling (FFG-compatible error fields).
- Core database: tenants, org_units, users (+ profile fields), roles, permissions, user_roles (scoped), verification_tokens, refresh_tokens, user_profiles, consent_records, organizational_clusters/members, facilitator_student_assignments, assignment_history, configuration_groups, configurations, languages.
- Flyway V1 (core RBAC), V2 (verification tokens + soft-delete audit + forward-looking career translation stubs), V3 (profiles, consent, assignment, config, languages, refresh_tokens, enrichments).
- JWT authentication: register, login, logout, refresh (persisted refresh tokens), password reset, `GET /auth/me` (with aliases for existing paths/fields).
- User management: CRUD, status, language preferences, pagination/filters.
- RBAC APIs: list roles/permissions, assign/remove user roles with scope.
- Consent APIs: capture, list, withdraw.
- Organization: org unit CRUD, tree, clusters + members.
- Student–facilitator assignment APIs + history.
- Configuration groups/values and language listing.

## Non-Goals (Strictly Out of Scope)

- Question Bank, Questionnaires, Assessment Execution, Scoring (Sprints 2–3).
- Recommendations, Reports, Counsellor Feedback, Dashboards (Sprint 4).
- Data Analyst and Guest roles (Phase 2).
- Full notification providers and external Salesforce/Glific connectors.
- Wiring FK constraints from premature `career_*_translations` to careers masters (later sprint).
