# System Design: Sprint 4 — Advanced Modules

**Implement against:** [api-contracts.md](./api-contracts.md) · **Requirements:** [spec.md](./spec.md)

## Schema strategy (NGO monorepo)

Map to existing NGO migrations — **no new Flyway for Sprint 4 tables** unless gap review finds missing columns:

| NGO migration | Tables |
|---------------|--------|
| **V6** | `career_clusters`, `careers`, `career_mappings`, `recommendation_runs`, `recommendations` (+ IAR/scoring from prior sprints) |
| **V7** | `report_templates`, `report_template_versions`, `report_sections`, `generated_reports`, `counsellor_feedback` |
| **V8** | `integration_clients`, `external_id_mappings`, `integration_events`, `notification_templates`, `notification_logs`, `audit_logs`, `activity_logs` |

## Entity notes (NGO column names)

### Careers (V6 / K3–K5)

* **`career_clusters`:** tenant, code, name, description, is_active
* **`careers`:** career_cluster_id, code, name, description, education_pathway, skills_required JSONB, metadata, is_active
* **`career_mappings`:** tenant, career_id, **interest_domain**, **aptitude_domain**, **reality_factor**, **aspiration_factor**, min_score, max_score, **mapping_weight**, mapping_config JSONB, is_active

### Recommendations (V6 / K6–K7)

* **`recommendation_runs`:** student_id, **configuration_group_id**, **iar_logic_version_id**, status (PENDING/COMPLETED/FAILED), generated_at, generated_by
* **`recommendations`:** recommendation_run_id, career_id, **rank_order**, **recommendation_score**, **recommendation_reason**, **match_breakdown** JSONB

**Note:** NGO schema links runs to configuration_group + IAR version (not attempt_id directly). Service layer SHALL derive group/IAR from latest relevant SUBMITTED attempt when generating.

### Reports (V7 / L1–L4)

* **`report_template_versions`:** template_config JSONB; **`report_sections`:** section_code, section_title, display_order, section_config
* **`generated_reports`:** student_id, **recommendation_run_id** (required FK), report_template_version_id, **configuration_group_id**, report_type, language_id, **s3_key**, **file_name**, generated_at, generated_by

### Feedback (V7 / L5)

* **`counsellor_feedback`:** tenant_id, student_id, **counsellor_id**, report_id optional, **feedback_text**, **recommendation_notes**, **visibility** (INTERNAL | VISIBLE_TO_STUDENT), is_active, created_at, updated_at

### Integrations (V8 / M1–M3)

* **`integration_clients`:** tenant, client_code, client_name, client_type, api_key_hash, allowed_scopes JSONB
* **`external_id_mappings`:** external_system, external_entity_type, external_entity_id, internal_entity_type, internal_entity_id
* **`integration_events`:** integration_client_id, event_type, direction, request/response payload, status, error_message

### Notifications (V8 / N1–N2)

* **`notification_templates`:** code (ACCOUNT_CREATED, etc.), channel, subject_template, body_template, language_id
* **`notification_logs`:** user_id, **template_id**, channel, recipient, status, provider_response, sent_at

### Audit vs activity (V8 / O1–O2 / IF2AF0203 §18)

Full matrix in [spec.md](./spec.md). Implement `@Audited` AOP in Sprint 4; foundation events from Sprint 1–3.

* **`audit_logs`**: compliance/configuration changes
* **`activity_logs`**: LOGIN, LOGOUT, ASSESSMENT_SUBMIT, REPORT_DOWNLOAD
* **`login_history`**: Sprint 1 — separate from activity_logs

## Recommendation MVP algorithm

Aligned with **`09-recommendation-logic-flow.png`** and **`08-scoring-recommendation-report-sequence.png`** (post-scoring stages):

1. Load latest SUBMITTED attempt (or specified attemptId) with `domain_scores` + version snapshots.
2. **Parallel inputs:** interest scores, aptitude scores, reality responses/factors, aspiration preferences (from attempt responses + domain_scores).
3. Apply benchmarks already linked on `domain_scores`.
4. Load **`iar_logic_version.logic_definition`** from attempt snapshot (never "latest" if snapshot present).
5. Apply configured weights/rules from IAR definition.
6. Match **`career_mappings`** (interest_domain, aptitude_domain, reality_factor, aspiration_factor, min/max score bands, mapping_weight).
7. Calculate recommendation_score per career; rank descending.
8. Generate recommendation_reason + **match_breakdown** JSON per row.
9. Insert **`recommendation_run`** + **`recommendations`** (status COMPLETED/FAILED).

## Report generation MVP

Per **`08`** sequence (Report Service → S3 → PostgreSQL metadata):

1. Require existing **`recommendation_run`** for student (generate recommendations first if missing).
2. Resolve **`report_template_version_id`** from attempt snapshot or group output.
3. Resolve language (request → user pref → tenant default).
4. Load recommendations, scores, template sections / `template_config`.
5. Assemble PDF placeholder (MVP: no real renderer — mock file).
6. Store file in **S3** (mock `s3_key` for MVP; real bucket per **`02-updated-aws-deployment.png`** in production).
7. Insert **`generated_reports`** with required NGO FKs.
8. Enqueue **REPORT_PUBLISHED** notification (**`15-notification-flow.png`**).

## RBAC and scoped access

From **`10-rbac-and-scoped-access-flow.png`** (applies all sprints; enforce in Sprint 4 `SecurityConfig` review):

| Check | Result |
|-------|--------|
| No/invalid JWT | **401** |
| Authenticated but missing permission | **403** `ACCESS_DENIED` |
| **ADMINISTRATOR** | Tenant-wide access |
| **SUB_ADMIN** | Org unit / cluster scope only → **403** if out of scope |
| **CAREER_COUNSELLOR** | Assigned students only |
| **CAREER_EXPLORER** | Own records only |

JWT claims: `userId`, `tenantId`, `roles` (+ scope from `user_roles` for Sub-Admin).

## Sub-Admin scoped operations

From **`11-sub-admin-scoped-operations-flow.png`**:

**Allowed (in scope):** view students/facilitators; manage student–facilitator assignments (**write `assignment_history`** + audit); scoped analytics dashboard; select **approved output template / output language** where permitted (**IF2AF0203 §4.4**).

**Blocked:** global configuration; questionnaire/scoring/IAR authoring or publish.

## Integration sequence

From **`16-external-integration-flow.png`**:

1. External app → **`IntegrationApiKeyFilter`** validates API key / integration token against `integration_clients.api_key_hash`.
2. **`POST /integrations/students`** → UserService upsert user/profile → store **`external_id_mappings`** → log **`integration_events`** SUCCESS/FAILED.
3. **`GET /integrations/students/{externalStudentId}/results`** → resolve external ID → fetch latest scores/recommendations/report metadata → return payload.

## Notification sequence

From **`15-notification-flow.png`**:

Event (ACCOUNT_CREATED, PASSWORD_RESET, ASSESSMENT_ASSIGNED, ASSESSMENT_COMPLETED, REPORT_PUBLISHED) → select template by code → resolve user language → render variables → send (simulated email MVP) → **`notification_logs`** status **SENT** or **FAILED**.

## Audit vs activity routing

From **`17-audit-and-traceability-flow.png`**:

| Action | Log table |
|--------|-----------|
| User update, role assignment, questionnaire publish, IAR/scoring update, config group assignment | **`audit_logs`** (entity, old/new values, actor) |
| Assessment submit, report download | **`activity_logs`** (+ attempt version refs in metadata) |

Do **not** route submit/download to `audit_logs` unless product explicitly requires duplicate compliance entry.

## Platform architecture (reference)

**`01-updated-system-architecture.png`:** React PWA → Spring Boot REST → module services → PostgreSQL; S3 for reports; email for notifications; external REST for integrations.

**`02-updated-aws-deployment.png`:** CloudFront + S3 (frontend), EC2/Nginx + Docker Spring Boot, RDS PostgreSQL, S3 reports bucket, Secrets Manager, CloudWatch Logs, email provider. MVP backend may run locally; production targets this topology.

## ERD alignment

**`ERD 6`:** `recommendation_runs` ↔ `configuration_group_id` + `iar_logic_version_id`; `generated_reports` requires `recommendation_run_id`; `counsellor_feedback.visibility`.

**`ERD 7`:** `integration_clients`, `external_id_mappings`, `integration_events`, `notification_templates`, `notification_logs`, `audit_logs`, `activity_logs` — column names match NGO V8.

## End-to-end MVP UAT path

From **`18-end-to-end-mvp-flow.png`** (Sprint 4 EPIC-20 smoke test — steps 5–22 are Sprint 2–4 backend):

1. Admin: question bank → questionnaire versions → scoring/IAR/report template versions → config group → assign → activate  
2. Student: login → list assessments → start (resolve config) → complete → submit  
3. System: scores → recommendations → report  
4. Facilitator: review report → add feedback (visibility choice)  
5. Student: view/download report  
6. Verify audit + activity + integration logs  

## AOP / audit design

`@Audited` aspect on service methods after successful commit (audit_logs targets only):

| Action label | Trigger | Log |
|--------------|---------|-----|
| User update | UserService.update | audit_logs |
| Role assignment | UserRoleService.assign | audit_logs |
| Questionnaire publish | QuestionnaireService.publish | audit_logs |
| IAR/scoring publish | IAR/scoring publish | audit_logs |
| Configuration group assignment | Group assignment create | audit_logs |
| Counsellor feedback add/update | FeedbackService | audit_logs |
| Assessment submit | AttemptService.submit | **activity_logs** (diagram 17) |
| Report download | ReportController.download | **activity_logs** |

Map audit **`action`**: PUBLISH, ASSIGN, UPDATE as appropriate.

## Integration security

* **`IntegrationApiKeyFilter`** or Spring Security filter: match `X-Api-Key` / `Authorization: ApiKey` against `integration_clients.api_key_hash`.
* Integration routes bypass JWT but require valid client + tenant scope.

## Dashboard aggregation

* **Student:** count available assessments, in-progress, submitted, reports ready, recommendations ready.
* **Facilitator:** assigned students, submitted/in-progress, reports generated — **only assigned cohort**.
* **Admin:** tenant-wide users active, attempts submitted, reports generated, active config groups; filters orgUnitId, clusterId, dates.
* **Sub-Admin:** same metrics as admin but SQL/RBAC scoped to permitted org units/clusters.

## Mock S3

`storage_url` / download response may return:

* `s3://antarang-cap-mock/reports/{reportId}.pdf`
* or `GET /reports/{id}/download` → stream stub PDF / `{ downloadUrl, expiresInSeconds }`

## Packages

```
controller/ RecommendationController, ReportController, ReportTemplateController,
            CareerController, CareerMappingController, FeedbackController,
            IntegrationController, NotificationController, AuditController,
            ActivityLogController, DashboardController
service/    RecommendationService, ReportService, CareerService, FeedbackService,
            IntegrationService, NotificationService, AuditService, ActivityLogService,
            DashboardService
aspect/     AuditLoggingAspect
security/   IntegrationApiKeyFilter
```

## FFG path aliases

| FFG canonical | MVP alias (optional) |
|---------------|----------------------|
| `GET /students/{id}/dashboard` | `GET /dashboards/student` |
| `GET /facilitators/{id}/dashboard` | `GET /dashboards/counsellor` |
| `GET /admin/dashboard` | `GET /dashboards/admin` |
| `GET /sub-admin/dashboard` | same path (required) |

## Phase-2 deferrals

Salesforce/Glific connectors, SMS delivery, Data Analyst/Guest APIs, scheduled exports (FFG §31).
