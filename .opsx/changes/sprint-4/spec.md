# Specification: Sprint 4 — Outputs & Platform Operations

**Sources:** FFG API Specification v1.0 (§22–28, §30), Database Design (Modules K6–K7, L, M–O), **IF2AF0203-export.pdf**, flow diagrams **`08`–`18`**, architecture **`01`–`02`**, ERD **`6`–`7`**, NGO Flyway **V6–V8**.  
**Contracts:** [api-contracts.md](./api-contracts.md)

> **Career admin CRUD** is Sprint 3 (EPIC-12). Sprint 4 owns **recommendation generate**, reports, ops modules.

> **NGO merge note:** `generated_reports` requires `recommendation_run_id`, `configuration_group_id`, `s3_key`, `file_name` (V7). `career_mappings` uses domain factor columns (V6). Map entities exactly — do not simplify to FFG-only DTO shapes if they omit required FKs.

## Requirements

### Career mapping & recommendations (EPIC-13 / FFG §22 / Module K6–K7)

* Career clusters/careers/mappings **admin APIs delivered in Sprint 3** — Sprint 4 consumes seeded data.
* **`POST /recommendations/generate/{studentId}`** (FFG §22.4):
  * Input optional `attemptId` — default latest SUBMITTED attempt with domain_scores for student
  * Use attempt snapshotted **`iar_logic_version_id`** (never "latest" if snapshot present)
  * Use **`configuration_group_id`** from attempt for **`recommendation_runs`** row (NGO schema)
  * Persist **`recommendation_runs`** + ranked **`recommendations`** with `rank_order`, `recommendation_score`, `recommendation_reason`, **`match_breakdown`** JSON (interest/aptitude/reality/aspiration labels per FFG)
* **`GET /students/{studentId}/recommendations`** — history / latest run (FFG §22.5).

### Reports (EPIC-14 / FFG §23 / Module L)

* Report templates: create template, **`POST /report-templates/{templateId}/versions`**, edit DRAFT version sections via **`report_sections`** or `template_config` JSON.
* Publish: **`POST /report-template-versions/{versionId}/publish`**.
* **`POST /reports/generate/{studentId}`** (FFG §23.4):
  * Requires completed scoring + **`recommendation_run`** (NGO FK on `generated_reports`)
  * Resolve template version from attempt snapshot or active group output
  * Language: request → user preferred → tenant default
  * Persist **`generated_reports`**: `student_id`, **`recommendation_run_id`**, `report_template_version_id`, **`configuration_group_id`**, `report_type`, `language_id`, **`s3_key`**, **`file_name`**, `generated_at`, `generated_by`
  * MVP storage: mock S3 key / placeholder URL — no real AWS required
* **`GET /students/{studentId}/reports`** — list (FFG §23.5).
* **`GET /reports/{reportId}`** — metadata.
* **`GET /reports/{reportId}/download`** — PDF stream or pre-signed URL (FFG §23.6); audit **Report download** + optional **activity_logs** entry.

### Counsellor feedback (EPIC-15 / FFG §24 / Module L5)

* Assigned counsellors (`counsellor_id`) SHALL add feedback: **`feedback_text`**, optional **`recommendation_notes`**, optional **`report_id`**, **`visibility`** (`INTERNAL`|`VISIBLE_TO_STUDENT`) — counsellor chooses visibility before save (diagram **`14`**).
* System SHALL record **author (`counsellor_id`)** and **timestamp** on create/update; write **audit_logs** on feedback mutations.
* **`GET /students/{studentId}/feedback`** — explorers see VISIBLE_TO_STUDENT only.
* Authors or admins SHALL **`PUT /feedback/{feedbackId}`** (FFG §24.3).
* Updates auditable.

### Dashboards (EPIC-16 / FFG §25)

* **Student:** `GET /students/{studentId}/dashboard` — own progress (assessments, attempts, reports, recommendations).
* **Facilitator:** `GET /facilitators/{facilitatorId}/dashboard` — assigned students metrics only.
* **Admin:** `GET /admin/dashboard` — tenant filters (`orgUnitId`, `clusterId`, `fromDate`, `toDate`).
* **Sub-Admin:** **`GET /sub-admin/dashboard`** — scoped to permitted org units/clusters (FFG §25.4 — **required**).
* **Sub-Admin output control (IF2AF0203 §4.4):** where permitted, Sub-Admin MAY select **approved output template / output language** on configuration assignments — admin creates templates; Sub-Admin selects within scope.
* MVP MAY implement simplified dashboard path aliases documented in api-contracts.

### Notifications (EPIC-17 / FFG §26 / Module N)

* Seed/manage **`notification_templates`** (code, channel EMAIL/SMS, subject/body, language_id).
* Internal **`NotificationService.enqueue`** on lifecycle events per IF2AF0203 §17: **`ACCOUNT_CREATED`**, **`PASSWORD_RESET`** required; **`ASSESSMENT_ASSIGNED`**, **`ASSESSMENT_COMPLETED`**, **`REPORT_PUBLISHED`** optional but recommended.
* Admin/system **`POST /notifications/send`** with templateCode, channel, variables (FFG §26.1).
* Write **`notification_logs`** (requires `template_id`, recipient, status PENDING/SENT/FAILED).
* Language resolution: user preferred platform language → tenant default.
* MVP: simulate email (log `[SIMULATED EMAIL]` like OTP); SMS Phase 2.

### Integrations (EPIC-18 / FFG §27 / Module M)

* **`POST /integration-clients`** — register client, store `api_key_hash`, `client_code`, `allowed_scopes`.
* External **`POST /integrations/students`** with API key: `externalSystem`, `externalStudentId`, profile fields, org/grade — upsert user + **`external_id_mappings`**.
* **`GET /integrations/students/{externalStudentId}/results`** — latest domain scores / attempt summary.
* **`GET /integrations/students/{externalStudentId}/reports/latest`** — latest report metadata + download path.
* Every call writes **`integration_events`** (direction INBOUND/OUTBOUND, status SUCCESS/FAILED).
* Admins **`GET /integration-events`** with filters (FFG §27.5).

### Audit & activity (EPIC-19 / FFG §28 / IF2AF0203 §18)

**Full IF2AF0203 audit matrix (implement across Sprint 1–4; Sprint 4 completes AOP + list APIs):**

| Event | Type |
|-------|------|
| User create/update/deactivate | Audit |
| Role assignment/change | Audit |
| Student-facilitator assignment | Audit |
| Consent capture/withdrawal | Audit (Sprint 1) |
| Questionnaire publish/retire | Audit |
| IAR logic publish/retire | Audit |
| Scoring rule publish/retire | Audit |
| Configuration group create/assign/activate | Audit |
| Assessment submission | **Activity** + traceability on attempt |
| Report generation/download | **Activity** |
| Counsellor feedback add/update | Audit |
| Integration calls | `integration_events` + optional audit |

* **`audit_logs`**: compliance mutations — `entity_name`, `entity_id`, `action`, old/new JSON, `performed_by`, ip (NGO O1).
* **`activity_logs`**: `LOGIN`, `LOGOUT`, `ASSESSMENT_SUBMIT`, `REPORT_DOWNLOAD` (NGO O2) — **never merge with audit_logs**.
* **`login_history`**: authentication activity (Sprint 1 table, separate from activity_logs).
* List APIs: `GET /audit-logs`, `GET /activity-logs` with filters per FFG §28.

## FFG alignment notes

| FFG § | Item | Status in spec |
|-------|------|----------------|
| 23.5 | List student reports | Required |
| 24.3 | Update feedback | Required |
| 25.4 | Sub-admin dashboard | Required |
| 26.1 | POST notifications/send | Required |
| 27.5 | GET integration-events | Required |
| 28.2 | GET activity-logs | Required |
| IF2AF0203 §18 | Full audit vs activity matrix | Required |
| IF2AF0203 §4.4 | Sub-Admin output template/language selection | Limited scope |

## Sprint 4 demo script (IF2AF0203 §113)

1. Student completes assessment → scores generated  
2. System applies IAR logic → ranked recommendations  
3. PDF report generated → S3 storage  
4. Student views/downloads report  
5. Facilitator views report + adds feedback  
6. Admin / Sub-Admin dashboards  
7. External API fetches result/report  
8. Audit + activity logs show traceability  

## EPIC-20 exit criteria (IF2AF0203 + diagram `18`)

* End-to-end smoke: admin config (steps 5–11 of diagram **`18`**) → student start/submit → scores → recommend → report → facilitator feedback → student download → logs verified  
* Critical UAT defects closed  
* Production deployment validated against **`02`** AWS topology (or documented local MVP equivalent)  
* Handover + training pack  
* See [tasks.md](./tasks.md) checklist  

## Scenarios

### Generate recommendations

**GIVEN** SUBMITTED attempt with domain_scores and career_mappings  
**WHEN** `POST /recommendations/generate/{studentId}`  
**THEN** recommendation_run + ranked recommendations with match_breakdown; uses IAR snapshot.

### Generate & list & download report

**GIVEN** recommendation_run exists and report template on snapshot/output  
**WHEN** generate → list reports → download  
**THEN** generated_reports row with s3_key; audit + activity entries.

### Counsellor feedback visibility

**GIVEN** INTERNAL and VISIBLE_TO_STUDENT feedback rows  
**WHEN** student lists feedback  
**THEN** only VISIBLE_TO_STUDENT returned.

### Update feedback

**GIVEN** feedback by counsellor  
**WHEN** `PUT /feedback/{feedbackId}`  
**THEN** student-visible listing updated; audit records change.

### External upsert + fetch

**GIVEN** valid integration API key  
**WHEN** upsert student then GET results  
**THEN** external_id_mapping + integration_event SUCCESS.

### Sub-admin scoped dashboard

**GIVEN** Sub-Admin with org-unit scope  
**WHEN** `GET /sub-admin/dashboard`  
**THEN** counts limited to scoped students/orgs.

### Notification language fallback

**GIVEN** user has no preferred language template  
**WHEN** ACCOUNT_CREATED sent  
**THEN** tenant default language template used; notification_log written.

### Notification manual send

**GIVEN** admin calls POST /notifications/send  
**THEN** notification_log row with template resolution.
