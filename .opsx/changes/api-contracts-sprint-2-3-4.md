# API Contracts Checklist — Sprint 2 + 3 + 4

**Purpose:** Single place to verify implementation coverage against sprint contracts and IF2AF0203 / FFG scope.  
**Sources:** [sprint-2/api-contracts.md](./sprint-2/api-contracts.md) · [sprint-3/api-contracts.md](./sprint-3/api-contracts.md) · [sprint-4/api-contracts.md](./sprint-4/api-contracts.md) · [IF2AF0203-export-summary.md](./IF2AF0203-export-summary.md)  
**Base path:** `/api/v1`  
**Cross-check:** PDF vs sprint folders OK (2026-08-10). Sprint 1 auth/org endpoints omitted.

Mark each row when coded + Bruno/manual verified.

---

## Shared conventions

| Rule | Expectation |
|------|-------------|
| Response wrapper | `{ success, message, data, timestamp }` |
| List pagination | `?page&size` → `{ content, page, size, totalElements, totalPages, last }` |
| Auth | `Authorization: Bearer <jwt>` (except public config/language reads; integrations use API key) |
| Tenant | JWT `tenantId` (optional `X-Tenant-Id`) |
| Roles | `CAREER_EXPLORER` / `CAREER_COUNSELLOR` / `ADMINISTRATOR` / `SUB_ADMIN` / `SUPER_ADMIN` |

### Common error codes

`VALIDATION_ERROR` · `ACCESS_DENIED` · `RESOURCE_NOT_FOUND` · `DUPLICATE_RESOURCE` · `INVALID_STATE`  
Sprint 3+: `ATTEMPT_ACTIVE` · `ATTEMPT_NOT_IN_PROGRESS` · `ATTEMPT_EXPIRED` · `MAX_ATTEMPTS_EXCEEDED` · `MANDATORY_RESPONSE_MISSING` · `CONFIGURATION_NOT_FOUND` · `SCORING_RULE_MISSING` · `NO_SCORES` · `NO_RECOMMENDATION_RUN` · `INTEGRATION_UNAUTHORIZED`

---

## Sprint 2 — Content & Assessment Configuration

### Configuration & language (EPIC-06 / FFG §14)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET | `/configuration-groups` | Admin list |
| [ ] | POST | `/configuration-groups` | Admin create |
| [ ] | GET | `/configurations` | `groupCode` filter; public OK for signup |
| [ ] | POST | `/configurations` | Admin |
| [ ] | PUT | `/configurations/{id}` | Admin |
| [ ] | GET | `/languages` | |
| [ ] | POST | `/languages` | Admin |

### Question categories (FFG §15.1)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET | `/question-categories` | `assessmentType?` |
| [ ] | POST | `/question-categories` | `domainCode` required |
| [ ] | PUT | `/question-categories/{categoryId}` | Code immutable |

### Questions (FFG §15.2–15.6)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/questions` | Persist `RADIO`/`CHECKBOX` (DTO aliases OK) |
| [ ] | GET | `/questions` | Filters: assessmentType, categoryId, questionType, reviewStatus, languageId, search |
| [ ] | GET | `/questions/{questionId}` | Options + translations + rules |
| [ ] | PUT | `/questions/{questionId}` | Guard published version option delete |
| [ ] | DELETE | `/questions/{questionId}` | Soft-delete; `QUESTION_IN_USE` if published |
| [ ] | POST | `/questions/{questionId}/options` | |
| [ ] | PUT | `/questions/{questionId}/translations` | **Both** `translations[]` + `optionTranslations[]` |
| [ ] | POST | `/questions/{questionId}/rules` | |
| [ ] | GET | `/questions/{questionId}/rules` | |
| [ ] | DELETE | `/questions/{questionId}/rules/{ruleId}` | |
| [ ] | PATCH | `/questions/{questionId}/status` | `reviewStatus` / `isActive` |

### Questionnaires (FFG §16 / EPIC-08)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/questionnaires` | Creates version 1 DRAFT |
| [ ] | GET | `/questionnaires` | |
| [ ] | GET | `/questionnaires/{questionnaireId}` | `versionId?` |
| [ ] | PUT | `/questionnaires/{questionnaireId}` | MVP consolidated DRAFT edit OK |
| [ ] | POST | `/questionnaires/{questionnaireId}/versions` | |
| [ ] | GET | `/questionnaires/{questionnaireId}/versions` | |
| [ ] | POST | `/questionnaires/{questionnaireId}/clone` | |
| [ ] | POST | `/questionnaire-versions/{versionId}/questions` | displayOrder, isMandatory, section*, conditionConfig |
| [ ] | PUT | `/questionnaire-versions/{versionId}/questions/reorder` | |
| [ ] | GET | `/questionnaire-versions/{versionId}/preview` | `languageId?` |
| [ ] | POST | `/questionnaire-versions/{versionId}/publish` | Audit; alias `POST /questionnaires/{id}/publish` OK |
| [ ] | POST | `/questionnaire-versions/{versionId}/suspend` | Preserve historical attempts |
| [ ] | POST | `/questionnaire-versions/{versionId}/retire` | → ARCHIVED / inactive |

### Assessments & execution config (FFG §17)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/assessments` | Master per type |
| [ ] | GET | `/assessments` | |
| [ ] | POST | `/assessments/{assessmentId}/versions` | Questionnaire version must be PUBLISHED |
| [ ] | POST | `/assessment-versions/{versionId}/configuration` | Default `timerMode=COUNT_UP` |

### Assessment configuration groups (FFG §18)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/assessment-configuration-groups` | status DRAFT; academicYear, assessmentCycle |
| [ ] | GET | `/assessment-configuration-groups` | |
| [ ] | GET | `/assessment-configuration-groups/{groupId}` | Items + outputs + assignments |
| [ ] | PUT | `/assessment-configuration-groups/{groupId}` | Metadata |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/items` | Batch items |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/outputs` | Template + outputConfig |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/assignments` | USER \| CLUSTER \| ORG_UNIT |
| [ ] | DELETE | `/assessment-configuration-groups/{groupId}/assignments/{assignmentId}` | |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/activate` | ≥1 valid item |
| [ ] | GET | `/assessment-configuration-groups/resolve` | Admin debug: studentId + assessmentId |
| [ ] | GET | `/students/{studentId}/active-assessment-configuration` | Resolution: USER > CLUSTER > ORG_UNIT |

### IAR foundation (FFG §21)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/iar-logic-configs` | |
| [ ] | GET | `/iar-logic-configs` | |
| [ ] | POST | `/iar-logic-configs/{logicConfigId}/versions` | logicDefinition weights |
| [ ] | PUT | `/iar-logic-versions/{versionId}` | DRAFT only |
| [ ] | POST | `/iar-logic-versions/{versionId}/validate` | `{ valid, errors }` |
| [ ] | POST | `/iar-logic-versions/{versionId}/publish` | After validate; audit |

### Report template skeleton (FFG §23.1–23.3)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/report-templates` | + DRAFT version v1 |
| [ ] | GET | `/report-templates` | |
| [ ] | POST | `/report-templates/{templateId}/versions` | |
| [ ] | POST | `/report-template-versions/{versionId}/publish` | Full sections in Sprint 4 |

### Sprint 2 behavioral checks

| Done | Check |
|------|-------|
| [ ] | Only APPROVED + active questions addable to new DRAFT versions |
| [ ] | Published version structural edit → `INVALID_STATE` / `QUESTIONNAIRE_PUBLISHED` |
| [ ] | Option delete on published version blocked |
| [ ] | Config group activate requires valid published refs |
| [ ] | Audit on questionnaire publish/retire, IAR publish, group assign/activate |

---

## Sprint 3 — Assessment Lifecycle & Scoring

### Execution (FFG §19)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET | `/assessments` | Admin masters |
| [ ] | GET | `/students/{studentId}/assessments` | Resolvable ACTIVE config only |
| [ ] | POST | `/assessments/{assessmentId}/start` | Snapshot all version IDs; inline `questions[]` preferred |
| [ ] | GET | `/attempts/{attemptId}` | Metadata + questions + saved responses |
| [ ] | PUT | `/attempts/{attemptId}/responses` | selectedOptionIds, text, numeric, isSkipped |
| [ ] | PUT | `/attempts/{attemptId}/question-timings` | Should-have; batch OK |
| [ ] | POST | `/attempts/{attemptId}/submit` | submissionReason; score_status PENDING→COMPLETED |
| [ ] | GET | `/students/{studentId}/attempt-history` | `assessmentId?` |

### Scoring rules & execution (FFG §20)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/scoring-rules` | |
| [ ] | GET | `/scoring-rules` | |
| [ ] | POST | `/scoring-rules/{scoringRuleId}/versions` | ruleDefinition domains/aggregation |
| [ ] | PUT | `/scoring-rule-versions/{versionId}` | DRAFT only |
| [ ] | POST | `/scoring-rule-versions/{versionId}/publish` | Audit |
| [ ] | POST | `/scoring/calculate/{attemptId}` | Admin/system; `force?` |
| [ ] | GET | `/students/{studentId}/scores` | `attemptId?` |

### Benchmarks (FFG §20.5)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET | `/benchmarks` | Filters: assessmentId, domainCode, grade, age |
| [ ] | POST | `/benchmarks` | LOW\|MEDIUM\|HIGH + interpretation |
| [ ] | PUT | `/benchmarks/{id}` | |

### Career masters (EPIC-12 admin — generate is Sprint 4)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET/POST/PUT | `/career-clusters` | |
| [ ] | GET/POST | `/careers` | clusterId, code, name, … |
| [ ] | GET/POST/PUT | `/career-mappings` | NGO domain factor columns |

### Sprint 3 behavioral checks

| Done | Check |
|------|-------|
| [ ] | Start snapshots config/questionnaire/scoring/IAR/template/language IDs |
| [ ] | One IN_PROGRESS per (student, assessment) |
| [ ] | `allow_resume` returns same attempt; false → close prior + new within max_attempts |
| [ ] | Mandatory missing → 422 with `missingQuestionIds`; status stays IN_PROGRESS |
| [ ] | Scoring uses **snapshotted** scoring_rule_version_id (not latest) |
| [ ] | Submit writes `activity_logs` ASSESSMENT_SUBMIT |
| [ ] | MVP: submit response may include domainScores; **no** recommend/report yet |
| [ ] | Question rules / conditionConfig evaluated at render |

---

## Sprint 4 — Outputs & Platform Operations

### Recommendations (FFG §22)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/recommendations/generate/{studentId}` | Uses IAR snapshot + mappings; persist run |
| [ ] | GET | `/students/{studentId}/recommendations` | `runId?` |

### Report templates & reports (FFG §23)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET/POST | `/report-templates` | |
| [ ] | POST | `/report-templates/{templateId}/versions` | |
| [ ] | PUT | `/report-template-versions/{versionId}` | DRAFT sections / templateConfig |
| [ ] | POST | `/report-template-versions/{versionId}/publish` | |
| [ ] | POST | `/reports/generate/{studentId}` | Requires recommendation_run (NGO FK) |
| [ ] | GET | `/students/{studentId}/reports` | List |
| [ ] | GET | `/reports/{reportId}` | Metadata |
| [ ] | GET | `/reports/{reportId}/download` | PDF or pre-signed URL; audit + activity |

### Counsellor feedback (FFG §24)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/students/{studentId}/feedback` | visibility INTERNAL \| VISIBLE_TO_STUDENT |
| [ ] | GET | `/students/{studentId}/feedback` | Student sees VISIBLE_TO_STUDENT only |
| [ ] | PUT | `/feedback/{feedbackId}` | Author or admin |

### Dashboards (FFG §25)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET | `/students/{studentId}/dashboard` | Alias `/dashboards/student` OK |
| [ ] | GET | `/facilitators/{facilitatorId}/dashboard` | Assigned cohort only |
| [ ] | GET | `/admin/dashboard` | orgUnitId, clusterId, date filters |
| [ ] | GET | `/sub-admin/dashboard` | Scoped org/cluster only |

### Notifications (FFG §26)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | POST | `/notifications/send` | templateCode + channel + variables |
| [ ] | GET | `/notification-logs` | Admin |
| [ ] | (internal) | `NotificationService.enqueue` | ACCOUNT_CREATED, PASSWORD_RESET required; ASSESSMENT_* / REPORT_PUBLISHED recommended |

### Integrations (FFG §27)

| Done | Method | Path | Auth |
|------|--------|------|------|
| [ ] | POST | `/integration-clients` | JWT admin — return apiKey once |
| [ ] | POST | `/integrations/students` | API key — upsert + mapping |
| [ ] | GET | `/integrations/students/{externalStudentId}/results` | API key |
| [ ] | GET | `/integrations/students/{externalStudentId}/reports/latest` | API key |
| [ ] | GET | `/integration-events` | Admin / scoped sub-admin |

### Audit & activity (FFG §28)

| Done | Method | Path | Notes |
|------|--------|------|-------|
| [ ] | GET | `/audit-logs` | entity/action/date filters |
| [ ] | GET | `/activity-logs` | Separate stream (LOGIN, ASSESSMENT_SUBMIT, REPORT_DOWNLOAD, …) |

### Sprint 4 behavioral checks

| Done | Check |
|------|-------|
| [ ] | Recommend uses attempt `iar_logic_version_id` + `configuration_group_id` |
| [ ] | `generated_reports` has recommendation_run_id, configuration_group_id, s3_key, file_name |
| [ ] | Report download → audit + activity |
| [ ] | Feedback visibility filter for students |
| [ ] | Sub-Admin dashboard scoped |
| [ ] | Every integration call writes `integration_events` |
| [ ] | Notification language: user preferred → tenant default |

---

## End-to-end smoke (EPIC-20 / diagram 18)

| Done | Step |
|------|------|
| [ ] | Admin: question bank → questionnaire publish → assessment version + config → group items/outputs → assign → activate |
| [ ] | Student: start → save → submit → domain scores |
| [ ] | Generate recommendations → generate report → download |
| [ ] | Facilitator: view report → add feedback |
| [ ] | Admin / Sub-Admin dashboards |
| [ ] | External: upsert student → fetch results/report |
| [ ] | Verify audit_logs + activity_logs + attempt version snapshots |

---

## Phase-2 (do not implement in S2–4)

Data Analyst APIs · Guest demo · Salesforce/Glific/SMS depth · Advanced rule builder / approval workflows · Scheduled exports (FFG §31 / EPIC-21–23)
