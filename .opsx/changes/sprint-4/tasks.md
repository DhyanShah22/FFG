# Implementation Tasks — Sprint 4

**Contract:** [api-contracts.md](./api-contracts.md) · **Design:** [design.md](./design.md) · **Spec:** [spec.md](./spec.md)

## 0. Preconditions

- [ ] Sprint 3: SUBMITTED attempts with `domain_scores`, `score_status=COMPLETED`
- [ ] Sprint 2: published IAR + report template versions on ACTIVE config group
- [ ] NGO V6–V8 applied; entities map to existing columns
- [ ] Seed sample careers, career_mappings, notification_templates for UAT

## 1. Domain layer (no duplicate migrations)

- [ ] Entities/repos: V6 recommendation_runs, recommendations (career admin repos from Sprint 3)
- [ ] Entities/repos: V7 generated_reports, report_sections, counsellor_feedback
- [ ] Entities/repos: V8 integration_*, notification_templates, notification_logs, audit_logs, activity_logs

## 2. Career + recommendation

- [ ] `RecommendationService` — rank using domain_scores + mappings + IAR snapshot
- [ ] `POST /recommendations/generate/{studentId}`, `GET /students/{studentId}/recommendations`
- [ ] Populate match_breakdown JSON on recommendations

## 3. Reports

- [ ] Expand `ReportTemplateController` — version create, section edit on DRAFT, publish
- [ ] `ReportService` — require recommendation_run; mock s3_key/file_name
- [ ] `POST /reports/generate/{studentId}`
- [ ] **`GET /students/{studentId}/reports`**
- [ ] `GET /reports/{reportId}`, `GET /reports/{reportId}/download`
- [ ] Audit report download + activity_logs REPORT_DOWNLOAD

## 4. Feedback

- [ ] `FeedbackController` — POST/GET with visibility filter
- [ ] **`PUT /feedback/{feedbackId}`** — author or admin
- [ ] Map visibility INTERNAL | VISIBLE_TO_STUDENT

## 5. Integrations

- [ ] `IntegrationClient` bootstrap — `POST /integration-clients`
- [ ] `IntegrationApiKeyFilter` in SecurityConfig
- [ ] `POST /integrations/students` — externalSystem, externalStudentId, upsert user + mapping
- [ ] `GET .../results`, `GET .../reports/latest`
- [ ] Persist **`integration_events`** on every call
- [ ] **`GET /integration-events`** admin list

## 6. Notifications

- [ ] Seed `notification_templates` per event type + language
- [ ] `NotificationService.enqueue` + language fallback
- [ ] **`POST /notifications/send`**
- [ ] Wire triggers: register, submit, report generate, assignment (optional)
- [ ] `GET /notification-logs`

## 7. Audit + activity

- [ ] `AuditLoggingAspect` — 7 compliance actions → audit_logs
- [ ] **`GET /audit-logs`** with entity/action/date filters
- [ ] `ActivityLogService` — login, submit, download
- [ ] **`GET /activity-logs`**

## 8. Dashboards

- [ ] `GET /students/{studentId}/dashboard` (or `/dashboards/student`)
- [ ] `GET /facilitators/{facilitatorId}/dashboard` (or `/dashboards/counsellor`)
- [ ] `GET /admin/dashboard` (or `/dashboards/admin`) with query filters
- [ ] **`GET /sub-admin/dashboard`** — scoped aggregates

## 9. Security

- [ ] Integration routes: API key filter
- [ ] User routes: JWT + RBAC
- [ ] Sub-Admin scope on dashboard, audit list, assignments where applicable

## 10. Bruno / UAT acceptance

- [ ] Generate recommendations → student GET with ranks + match_breakdown
- [ ] Generate report → appears in list → download → audit + activity rows
- [ ] Feedback create + PUT update + visibility rules
- [ ] Integration upsert + results fetch + integration-events row
- [ ] Notification on report + manual send
- [ ] Dashboard counts coherent; sub-admin scoped
- [ ] **Flow `09`:** recommendation uses parallel I/A/R/A inputs + career_mappings rank
- [ ] **Flow `10`:** 401 unauthenticated; 403 out-of-scope Sub-Admin/counsellor/student
- [ ] **Flow `11`:** Sub-Admin blocked from questionnaire/scoring/IAR admin; assignment history + audit on assign
- [ ] **Flow `14`–`17`:** feedback visibility; notification SENT/FAILED log; integration auth sequence; submit=activity, publish=audit
- [ ] **Flow `18`:** full E2E MVP smoke (admin config → student → scores → recommend → report → feedback → logs)

---

## EPIC-20 Exit criteria checklist (release)

- [ ] Critical UAT defects closed
- [ ] Smoke: login → start → submit → recommend → report → download (diagram **`18`** full path)
- [ ] Flyway clean apply through V16 on fresh DB
- [ ] OpenAPI export optional
- [ ] Handover: env vars (`JWT_SECRET`, `DB_*`, `GOOGLE_CLIENT_ID`, integration keys)
- [ ] Rollback plan documented (DB snapshot restore)

## Packages

`com.antarang.cap.controller|service|repository|domain|dto|aspect|security`
