# Implementation Tasks — Sprint 2

**Contract:** [api-contracts.md](./api-contracts.md) · **Design:** [design.md](./design.md) · **Spec:** [spec.md](./spec.md)

## 0. Preconditions

- [ ] Branch `feature/avinash-changes-ffg`; Flyway V1–V16 applied locally
- [ ] Hibernate `validate` passes for Sprint-1 entities
- [ ] Audit Sprint-1 `ConfigurationController` / `LanguageController` vs FFG §14 gaps (`POST /languages`, filtered configurations)

## 1. Domain layer (no new domain migrations)

- [ ] Map JPA entities to NGO V3/V4/V6/V7 columns (see design.md); `@Column(name="question_code")` etc.
- [ ] Enums mirroring DB checks: `QuestionType`, `ReviewStatus`, `QuestionnaireVersionStatus`, `AssessmentGroupStatus`, `AssignmentType`, `RuleType`, `TimerMode`, `CompletionRule`, `IarVersionStatus`, `ReportTemplateType`
- [ ] Repositories with tenant-scoped queries + soft-delete filters (`is_deleted=false` defaults)

## 2. Configuration & language (FFG §14)

- [ ] Complete `ConfigurationController` — groups CRUD, configurations CRUD/list by `groupCode`
- [ ] `POST /languages` if not present from Sprint 1
- [ ] Keep public GET configurations for signup where required

## 3. Question bank APIs (FFG §15 / EPIC-07)

- [ ] `QuestionCategoryController` — CRUD/list with `domain_code`
- [ ] `QuestionController` — create/update/list/get, soft-delete guards
- [ ] `POST /questions/{id}/options` (if not bundled in create)
- [ ] `PUT /questions/{id}/translations` — **question + optionTranslations**
- [ ] `POST /questions/{id}/rules`, `GET`, `DELETE /questions/{id}/rules/{ruleId}`
- [ ] `PATCH /questions/{id}/status` — `review_status` + `is_active`
- [ ] Filters: assessmentType, categoryId, questionType, reviewStatus, languageId, search, pagination

## 4. Questionnaire APIs (FFG §16 / EPIC-08)

- [ ] `QuestionnaireController` + service
- [ ] Create questionnaire + auto version 1 DRAFT
- [ ] `POST /questionnaires/{id}/versions`
- [ ] `POST /questionnaire-versions/{versionId}/questions` and/or consolidated `PUT /questionnaires/{id}`
- [ ] `PUT /questionnaire-versions/{versionId}/questions/reorder` (or consolidated)
- [ ] `GET /questionnaire-versions/{versionId}/preview`
- [ ] `POST /questionnaire-versions/{versionId}/publish` (+ MVP alias on questionnaire)
- [ ] **`POST /questionnaire-versions/{versionId}/suspend`** and **`POST .../retire`** (or status PATCH → ARCHIVED/INACTIVE) — IF2AF0203 EPIC-08
- [ ] `POST /questionnaires/{id}/clone`
- [ ] `GET /questionnaires/{id}/versions`
- [ ] Reject INACTIVE/unapproved questions on DRAFT attach

## 5. Assessment masters & execution config (FFG §17)

- [ ] `AssessmentController` — create/list assessments
- [ ] `POST /assessments/{assessmentId}/versions`
- [ ] `POST /assessment-versions/{versionId}/configuration` — full execution policy fields

## 6. Assessment configuration groups (FFG §18)

- [ ] `AssessmentConfigurationGroupController` + service
- [ ] Create/update group (`academic_year`, `assessment_cycle`, `status`)
- [ ] `POST .../items` — bind assessment components (batch or single per api-contracts)
- [ ] `POST .../outputs` — report template version + output_config
- [ ] Assignments CRUD with `effective_from`/`effective_to`
- [ ] `POST .../activate`
- [ ] `ConfigurationResolutionService` (USER > CLUSTER > ORG_UNIT)
- [ ] `GET /students/{studentId}/active-assessment-configuration`
- [ ] `GET /assessment-configuration-groups/resolve` (admin debug)

## 7. IAR + report skeleton (FFG §21, §23 lean)

- [ ] `IarLogicController` — config CRUD, version CRUD, **validate**, publish (V6)
- [ ] `ReportTemplateController` — template + version create, publish skeleton (V7)
- [ ] Ensure published versions satisfy FK on group items/outputs

## 8. Audit foundation (EPIC-19)

- [ ] Write `audit_logs` on questionnaire publish/retire, IAR publish, config group assign/activate
- [ ] Prepare `AuditLoggingAspect` interface for Sprint 3–4 expansion

## 9. Security

- [ ] Register all Sprint-2 paths in `SecurityConfig`
- [ ] `@PreAuthorize` per api-contracts matrix
- [ ] Sub-Admin scope on assignments where FFG §18.4 applies

## 10. Acceptance (manual / Bruno) — includes IF2AF0203 §107 demo script

- [ ] Category → question (+options/translations + rule) → questionnaire → preview → publish → clone
- [ ] INACTIVE/unapproved question rejected on DRAFT attach
- [ ] Assessment master + version + execution config created
- [ ] Config group with items + outputs + assignment; activate; resolve for student
- [ ] USER assignment overrides ORG_UNIT assignment
- [ ] IAR validate failure blocks publish; success allows bind on group item
- [ ] Report template publish; bind on group output
- [ ] Option translations round-trip on GET question
- [ ] **Flow `12`:** unapproved question blocked from DRAFT attach; published version immutable
- [ ] **Flow `13`:** resolution returns questionnaire/scoring/IAR/language/template refs on group item
- [ ] **Flow `04`:** full admin demo steps 1–11, 14–24 (scoring publish steps 12–13 = Sprint 3 APIs, FK bindable here)

## Package touch list

| Area | Packages |
|------|----------|
| Controllers | `com.antarang.cap.controller` |
| Services | `com.antarang.cap.service` |
| Repos | `com.antarang.cap.repository` |
| Entities | `com.antarang.cap.domain.entity` |
| DTOs | `com.antarang.cap.dto.request`, `...dto.response` |
