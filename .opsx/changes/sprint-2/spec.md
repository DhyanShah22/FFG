# Specification: Sprint 2 — Content & Assessment Configuration

**Sources:** FFG API Specification v1.0 (§14–18, §21, §23.1–23.3, §30), Database Design (Modules D, E–H, K1–K2, L1–L2), **IF2AF0203-export.pdf**, flow diagrams **`04`**, **`12`**, **`13`**, ERD **`4`**, NGO Flyway **V3–V4**, **V6**, **V7**.  
**Implementable contracts:** [api-contracts.md](./api-contracts.md)

> **NGO merge note:** Do **not** add duplicate Flyway for Sprint-2 domain tables. Map JPA to existing NGO columns (`question_code`, `RADIO`/`CHECKBOX`, `domain_code`, `review_status`, `assessment_configuration_group_items`, `assessment_configuration_group_outputs`, etc.). Hibernate `ddl-auto: validate`.

## Requirements

### Shared platform rules

* The system SHALL use PostgreSQL UUIDs as primary keys and audit/soft-delete columns per NGO conventions.
* The system SHALL return `{ success, message, data, timestamp }` and paginate list endpoints with `{ content, page, size, totalElements, totalPages, last }` (FFG §4).
* The system SHALL authorize with JWT Bearer tokens and enforce RBAC. Role mapping: FFG `STUDENT` → `CAREER_EXPLORER`, `FACILITATOR` → `CAREER_COUNSELLOR`, `ADMIN` → `ADMINISTRATOR`, `SUB_ADMIN` → `SUB_ADMIN`.
* Tenant scope SHALL come from JWT `tenantId` (or `X-Tenant-Id` where applicable). Cross-tenant access SHALL return `ACCESS_DENIED`.

### Configuration & language (EPIC-06 / FFG §14)

* Complete Sprint-1 gaps: `POST /configuration-groups`, `GET /configuration-groups`, `POST /configurations`, `GET /configurations?groupCode=`, `POST /languages`.
* Configuration values SHALL support `code`, display label/value, `displayOrder`, optional `metadata` JSONB (map to NGO `configurations` table from V2).
* `GET /configurations` MAY remain public for signup flows where Sprint 1 already permits it.

### Question bank (EPIC-07 / FFG §15 / Module E)

* Categories SHALL be tenant-scoped with `assessment_type` (`INTEREST`|`APTITUDE`|`REALITY`|`ASPIRATION`), `code`, `name`, `description`, **`domain_code`**, `is_active`.
* Questions SHALL use NGO column names: `question_code`, `question_type` (`RADIO`|`CHECKBOX`|`RATING`|`SHORT_TEXT`|`LONG_TEXT`), `default_text`, `help_text`, `difficulty_level` (`EASY`|`MEDIUM`|`HARD`), **`review_status`** (`DRAFT`|`REVIEWED`|`APPROVED`|`RETIRED`), `source_reference`, `is_required_default`, `scoring_enabled`, `metadata`, `is_active`, `is_deleted`.
* API DTOs MAY expose aliases (`SINGLE_CHOICE`↔`RADIO`, `MULTI_CHOICE`↔`CHECKBOX`) but persistence MUST use NGO enums.
* Options SHALL use `option_code`, `default_text`, `score_value`, `display_order`, `metadata`, `is_active`.
* Translations: `question_translations` (`question_text`, `help_text`) and `option_translations` (`option_text`).
* **`PUT /questions/{id}/translations`** SHALL accept **both** `translations[]` and **`optionTranslations[]`** (FFG §15.5).
* Question rules (`question_rules`): `rule_type` (`VALIDATION`|`VISIBILITY`|`BRANCHING`|`SCORING`), `rule_config` JSONB — admin create/list/delete required (FFG §15.6). Runtime evaluation at attempt render is Sprint 3.
* List questions SHALL support filters: `assessmentType`, `categoryId`, `questionType`, `reviewStatus`, `languageId`, text/code `search`, pagination (FFG §15.3).
* Only **`APPROVED`** + active questions SHALL be addable to new DRAFT questionnaire versions unless admin override is explicitly deferred (diagram **`12`**: approve before questionnaire builder attach).
* Soft-deleted or retired/inactive questions SHALL NOT be addable to new DRAFT versions.
* Updating/deleting options referenced by **PUBLISHED** questionnaire versions SHALL be rejected or soft-blocked (`QUESTION_IN_PUBLISHED_VERSION` / `QUESTION_IN_USE`).

### Questionnaire versioning (EPIC-08 / FFG §16 / Module F)

* Questionnaires: `code`, `name`, `description`, `assessment_type`, `shuffle_questions`, `status`, `current_version_id`.
* Versions: `version_number`, `status` (`DRAFT`|`PUBLISHED`|`ARCHIVED`), `published_at`, `published_by`, `version_notes`.
* Create questionnaire SHALL create initial `questionnaire_versions` row (version 1 DRAFT) OR require explicit `POST /questionnaires/{id}/versions` (FFG §16.2) — implement at least one; document alias.
* Add questions: `POST /questionnaire-versions/{versionId}/questions` with `displayOrder`, `isMandatory`, `sectionCode`, `sectionName`, `conditionConfig` — OR consolidated `PUT /questionnaires/{id}` on current DRAFT (FFG §16.3–16.4).
* Reorder: `PUT /questionnaire-versions/{versionId}/questions/reorder` or consolidated PUT.
* **Preview:** `GET /questionnaire-versions/{versionId}/preview` with optional `languageId` (FFG §16.5).
* **Publish:** `POST /questionnaire-versions/{versionId}/publish` (FFG §16.6 canonical). MVP MAY alias `POST /questionnaires/{id}/publish` forwarding to current DRAFT version.
* **Suspend / retire:** Administrators SHALL be able to suspend or retire questionnaires/versions without breaking historical attempt traceability (IF2AF0203 EPIC-08: "publish, suspend, and retire"; audit type **publish/retire** per IF2AF0203 §18).
* Once **PUBLISHED**, structural edits to that version SHALL be rejected (`QUESTIONNAIRE_PUBLISHED` / `INVALID_STATE`) unless a new DRAFT version is created.
* Configuration lifecycle (IF2AF0203 §20): **Draft → Configure → Validate → Preview → Publish → Assign → Activate → Archive** when obsolete.
* **Clone** questionnaire copies latest (or specified) version into new questionnaire or new DRAFT version without mutating source.
* List versions: `GET /questionnaires/{questionnaireId}/versions` (FFG §16.7).

### Assessment masters & execution config (EPIC-09 / FFG §17 / Module G)

* **Assessment masters** (`assessments`): one row per type code (`INTEREST`|`APTITUDE`|`REALITY`|`ASPIRATION`) per tenant.
* **Assessment versions** (`assessment_versions`): link `assessment_id` → `questionnaire_version_id` (PUBLISHED questionnaire required).
* **Execution policy** (`assessment_configurations`) per assessment version (FFG §17.3):
  * `timer_mode` — MVP default **`COUNT_UP`** (also `NONE`, `COUNT_DOWN`)
  * `max_duration_minutes`, `auto_submit_enabled`, `show_timer_to_student`
  * `allow_resume`, `restart_on_interruption`, `allow_reattempt`, `reattempt_after_days`, `max_attempts`
  * `completion_rule` (`ALL_MANDATORY`|`ALLOW_PARTIAL`), optional `config_json`
* Admin APIs: create/list assessments, create assessment version, `POST /assessment-versions/{versionId}/configuration`.

### Assessment configuration groups (EPIC-09 / FFG §18 / Module H)

* Groups (`assessment_configuration_groups`): `code`, `name`, `description`, **`academic_year`**, **`assessment_cycle`**, **`status`** (`DRAFT`|`ACTIVE`|`INACTIVE`|`ARCHIVED`), audit columns.
* **Items** (`assessment_configuration_group_items`): per group, bind `assessment_id`, `assessment_version_id`, `questionnaire_version_id`, optional `iar_logic_version_id`, optional `scoring_rule_version_id`, `assessment_language_id`, `is_required`, `display_order` (FFG §18.2).
* **Outputs** (`assessment_configuration_group_outputs`): `report_template_version_id`, `output_language_id`, `output_config` JSON (`showScores`, `showRecommendations`, `showCounsellorFeedback`) (FFG §18.3).
* **Assignments** (`assessment_configuration_group_assignments`): `assignment_type` (`USER`|`CLUSTER`|`ORG_UNIT`), `assignment_id`, `effective_from`, `effective_to`, `is_active`, `assigned_by` (FFG §18.4). Sub-Admin scoped assignment where permitted.
* **Activate:** `POST /assessment-configuration-groups/{groupId}/activate` — group must have ≥1 valid item; status → `ACTIVE` (FFG §18.5).
* **Resolution priority:** **User assignment > Cluster assignment > Org Unit assignment**, respecting effective dates and `ACTIVE` status (diagram 13).
* Expose resolution via:
  * `GET /students/{studentId}/active-assessment-configuration` (FFG §18.6 — student/counsellor/admin/scoped sub-admin)
  * `GET /assessment-configuration-groups/resolve` (admin debug; query by student + assessment)
* Sprint 3 start SHALL consume resolved group **item** and snapshot all version IDs on the attempt.

### IAR foundation (EPIC-12 lean / FFG §21 / Module K1–K2)

* **`iar_logic_configs`** parent + **`iar_logic_versions`** with `logic_definition` JSONB (aspiration/interest/aptitude/reality weights, rules array).
* Create config, create version, update DRAFT version, **`POST /iar-logic-versions/{versionId}/validate`**, **`POST /iar-logic-versions/{versionId}/publish`**.
* Published versions immutable. Group items MAY reference published IAR version IDs.
* Full career mapping CRUD and recommendation engine are Sprint 4; Sprint 2 only needs version records for FK binding.

### Report template skeleton (EPIC-14 lean / FFG §23.1–23.3 / Module L1–L2)

* Create `report_templates` (`code`, `name`, `template_type` `STUDENT`|`FACILITATOR`).
* Create `report_template_versions` with `template_config` JSONB; publish version for FK use on group outputs.
* Full `report_sections` editing and PDF generation are Sprint 4.

### Audit foundation (EPIC-19 lean / IF2AF0203 §18)

Sprint 2 SHALL begin **`audit_logs`** capture for (full matrix completed Sprint 3–4):

| Event | Audit type (IF2AF0203) |
|-------|----------------------|
| Questionnaire publish / retire | Audit (`PUBLISH` / status change) |
| IAR logic publish / retire | Audit |
| Scoring rule publish / retire | Audit (when scoring admin touched in Sprint 3) |
| Configuration group create / assign / activate | Audit (`ASSIGN` / `CREATE`) |

* Use NGO `audit_logs`: `entity_name`, `entity_id`, `action`, `old_value`, `new_value`, `performed_by`.
* **`activity_logs`** and **`login_history`** remain Sprint 1 / Sprint 3+ (assessment submit = Activity per IF2AF0203).

## FFG alignment notes

| FFG area | MVP handling |
|----------|----------------|
| §15 Question types | Persist NGO enums; optional API aliases in DTOs |
| §15.6 Question rules | Admin APIs in Sprint 2; runtime branching in Sprint 3 |
| §16 Version-centric URLs | Prefer FFG paths; allow consolidated MVP aliases documented in api-contracts |
| §17 Assessment masters | **Required** — not optional flattening |
| §18 Group items/outputs/activate | **Required** — maps to NGO V4 H2/H3/H4 |
| §21 IAR validate | Required before publish |
| §23 Report template | Skeleton only in Sprint 2 |
| IF2AF0203 API catalogue §133–136 | High-level paths; **FFG API Spec is authoritative** for request bodies |
| IF2AF0203 §20 config lifecycle | Validate + preview + activate required in workflow |
| Sub-Admin output control | Admin creates templates; Sub-Admin may select approved output template/language where permitted (Sprint 4 dashboards) |

## Sprint 2 demo script (IF2AF0203 §107)

1. Admin creates questions → options → translations  
2. Admin creates questionnaire → version → adds questions (mandatory/order/branching)  
3. Admin previews questionnaire  
4. Admin publishes questionnaire version  
5. Admin creates assessment version + count-up timer config  
6. Admin creates configuration group → maps items (questionnaire, IAR placeholder, language, template) → assigns to org/cluster → activates  

## Scenarios

### Publish questionnaire version

**GIVEN** a questionnaire with a `DRAFT` version containing ≥1 approved question  
**WHEN** `ADMINISTRATOR` calls publish on that version  
**THEN** status becomes `PUBLISHED`, `published_at` set  
**AND** structural PUT on that version returns `QUESTIONNAIRE_PUBLISHED` / `INVALID_STATE`.

### Preview draft version

**GIVEN** a DRAFT version with questions and translations  
**WHEN** admin calls preview with `languageId`  
**THEN** ordered localized questions/options returned without mutating version.

### Clone questionnaire

**GIVEN** a published questionnaire  
**WHEN** admin clones  
**THEN** new questionnaire (or new DRAFT version) copies order/mandatory/sections; source unchanged.

### Activate configuration group

**GIVEN** a DRAFT group with valid items (published questionnaire + assessment version + language)  
**WHEN** admin activates  
**THEN** status → `ACTIVE` and group participates in resolution.

### Resolve configuration — user beats org unit

**GIVEN** USER-scoped and ORG_UNIT-scoped assignments for same student  
**WHEN** resolution runs  
**THEN** USER assignment group wins.

### IAR validate before publish

**GIVEN** DRAFT IAR version with invalid weights (sum ≠ 1 or missing domains)  
**WHEN** validate is called  
**THEN** `valid: false` with errors; publish rejected.

### Deactivate / retire question

**GIVEN** question on a published version  
**WHEN** admin sets `review_status=RETIRED` or `is_active=false`  
**THEN** question remains on historical published version  
**AND** cannot be added to new DRAFT versions.

### Add option translations

**GIVEN** question with options  
**WHEN** `PUT /questions/{id}/translations` includes `optionTranslations`  
**THEN** both question and option localized text persisted and returned on GET.
