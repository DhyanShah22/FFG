# Sprint 4 API Contracts (Implementable)

Source: FFG API Specification v1.0 (§22–28) + **IF2AF0203-export.pdf** + flow diagrams **`08`–`18`**, architecture **`01`–`02`**, ERD **`6`–`7`**.  
Depends on Sprint 3 scores, career mappings, and Sprint 2 IAR/report/config skeletons.

> **NGO merge:** V6 careers/recommendations, V7 reports/feedback, V8 integrations/notifications/audit/activity. `generated_reports.recommendation_run_id` is required.

## Conventions

Same wrapper, pagination, JWT, role synonyms as [Sprint 2](../sprint-2/api-contracts.md#conventions).  
Integration endpoints: `X-Api-Key: <key>` or `Authorization: ApiKey <key>` instead of user JWT.

| errorCode | Meaning |
|-----------|---------|
| NO_SCORES | Cannot recommend without domain_scores |
| NO_RECOMMENDATION_RUN | Report generate blocked (NGO FK) |
| TEMPLATE_NOT_FOUND | Missing report template version |
| INTEGRATION_UNAUTHORIZED | Bad API key |
| DUPLICATE_RESOURCE | External id conflict |
| ACCESS_DENIED | RBAC/visibility |

---

## 1. Recommendations (FFG §22.4–22.5)

> **Career admin CRUD** (`/career-clusters`, `/careers`, `/career-mappings`) — implemented in [Sprint 3](../sprint-3/api-contracts.md#7-career-masters--mappings-epic-12--if2af0203-sprint-3).

### `POST /api/v1/recommendations/generate/{studentId}`

**AuthZ:** self / assigned counsellor / admin / system.  
**Body optional:** `{ "attemptId": "uuid" }` — default latest SUBMITTED with scores.

**Success `data` (FFG §22.4):**

```json
{
  "recommendationRunId": "uuid",
  "studentId": "uuid",
  "recommendations": [
    {
      "careerId": "uuid",
      "careerName": "Data Analyst",
      "rankOrder": 1,
      "recommendationScore": 88,
      "reason": "Strong analytical interest and numerical aptitude",
      "matchBreakdown": {
        "interest": "HIGH",
        "aptitude": "HIGH",
        "reality": "MEDIUM",
        "aspiration": "MATCH"
      }
    }
  ]
}
```

**Side effects:** Insert `recommendation_runs` (configuration_group_id + iar_logic_version_id from attempt) + `recommendations`.

### `GET /api/v1/students/{studentId}/recommendations`

**Query:** `runId?`, `page`, `size`. Latest run or paginated history.

---

## 3. Report templates (FFG §23.1–23.3)

### `GET /api/v1/report-templates`

**AuthZ:** `ADMINISTRATOR`.

### `POST /api/v1/report-templates`

**Body:** `{ "code", "name", "templateType": "STUDENT" }`.

### `POST /api/v1/report-templates/{templateId}/versions`

**Body:** `{ "templateConfig": {}, "versionNotes": "..." }`.

### `PUT /api/v1/report-template-versions/{versionId}`

DRAFT only — update templateConfig and/or sections:

```json
{
  "sections": [
    { "sectionCode": "SUMMARY", "sectionTitle": "Summary", "displayOrder": 1, "sectionConfig": { "includeScores": true } },
    { "sectionCode": "CAREERS", "sectionTitle": "Recommendations", "displayOrder": 2, "sectionConfig": { "includeRecommendations": true } }
  ]
}
```

### `POST /api/v1/report-template-versions/{versionId}/publish`

DRAFT→PUBLISHED.

---

## 4. Reports (FFG §23.4–23.6)

### `POST /api/v1/reports/generate/{studentId}`

**AuthZ:** self / counsellor / admin / system.  
**Body optional:** `{ "attemptId": "uuid", "languageId": "uuid", "recommendationRunId": "uuid" }`.

**Preconditions:** domain_scores exist; recommendation_run exists (or generate inline if product allows).

**Success `data`:**

```json
{
  "reportId": "uuid",
  "studentId": "uuid",
  "fileName": "career-report-riya-sharma.pdf",
  "downloadUrl": "/api/v1/reports/uuid/download",
  "languageId": "uuid",
  "generatedAt": "ISO-8601"
}
```

**Side effects:** Insert `generated_reports` (s3_key, file_name, recommendation_run_id, configuration_group_id); notification REPORT_PUBLISHED.

### `GET /api/v1/students/{studentId}/reports`

**AuthZ:** self / counsellor / admin.  
**Query:** `page`, `size`, `attemptId?`.  
**Success:** paginated report metadata list.

### `GET /api/v1/reports/{reportId}`

Metadata including templateVersionId, recommendationRunId, configurationGroupId.

### `GET /api/v1/reports/{reportId}/download`

**AuthZ:** owner student / assigned counsellor / admin.  
**Response:** PDF stream **or**:

```json
{
  "downloadUrl": "https://example.invalid/mock/{reportId}.pdf",
  "expiresInSeconds": 300
}
```

**Side effects:** audit_logs (Report download); activity_logs REPORT_DOWNLOAD.

---

## 5. Counsellor feedback (FFG §24)

### `POST /api/v1/students/{studentId}/feedback`

**AuthZ:** assigned `CAREER_COUNSELLOR` or admin.  
**Body:**

```json
{
  "reportId": "uuid-or-null",
  "feedbackText": "Student shows strong analytical inclination.",
  "recommendationNotes": "Discuss data analytics pathway in next session.",
  "visibility": "VISIBLE_TO_STUDENT"
}
```

`visibility`: `VISIBLE_TO_STUDENT` | `INTERNAL`.

### `GET /api/v1/students/{studentId}/feedback`

**Query:** `page`, `size`. Student sees VISIBLE_TO_STUDENT only.

### `PUT /api/v1/feedback/{feedbackId}`

**AuthZ:** original counsellor or admin.  
**Body:** partial `{ feedbackText, recommendationNotes, visibility, reportId }`.

---

## 6. Dashboards (FFG §25)

### `GET /api/v1/students/{studentId}/dashboard`

**AuthZ:** self.  
**MVP alias:** `GET /dashboards/student`.  
**Success:**

```json
{
  "assessmentsAvailable": 2,
  "attemptsInProgress": 0,
  "attemptsSubmitted": 1,
  "reportsReady": 1,
  "recommendationsReady": true
}
```

### `GET /api/v1/facilitators/{facilitatorId}/dashboard`

**AuthZ:** self facilitator or admin.  
**MVP alias:** `GET /dashboards/counsellor`.  
**Success:** `{ assignedStudents, submittedCount, inProgressCount, reportsGenerated }` — assigned cohort only.

### `GET /api/v1/admin/dashboard`

**AuthZ:** `ADMINISTRATOR`.  
**MVP alias:** `GET /dashboards/admin`.  
**Query:** `orgUnitId?`, `clusterId?`, `fromDate?`, `toDate?`.  
**Success:** `{ usersActive, attemptsSubmitted, reportsGenerated, configGroupsActive }`.

### `GET /api/v1/sub-admin/dashboard`

**AuthZ:** `SUB_ADMIN` only.  
**Success:** same shape as admin dashboard filtered to scoped org units/clusters.

---

## 7. Notifications (FFG §26)

### `POST /api/v1/notifications/send`

**AuthZ:** `ADMINISTRATOR` or internal system.  
**Body:**

```json
{
  "userId": "uuid",
  "templateCode": "ACCOUNT_CREATED",
  "channel": "EMAIL",
  "variables": {
    "firstName": "Riya",
    "loginUrl": "https://cap.antarang.org"
  }
}
```

**Side effects:** Resolve template by code + user language fallback; insert notification_logs.

### `GET /api/v1/notification-logs`

**AuthZ:** `ADMINISTRATOR`.  
**Query:** `userId?`, `templateCode?`, `status?`, `page`, `size`.

### Internal `NotificationService.enqueue` (required)

| Event | Trigger |
|-------|---------|
| ACCOUNT_CREATED | register / integration create |
| PASSWORD_RESET | forgot-password |
| ASSESSMENT_ASSIGNED | config group USER assignment (optional MVP) |
| ASSESSMENT_COMPLETED | attempt submit |
| REPORT_PUBLISHED | report generate |

Language: user preferred platform language → tenant default.

---

## 8. Integrations (FFG §27)

### `POST /api/v1/integration-clients`

**AuthZ:** `ADMINISTRATOR`.  
**Body:** `{ "clientCode", "clientName", "clientType": "API", "allowedScopes": {} }`.  
**Response:** `{ id, clientCode, apiKey: "<plaintext-once>" }` — store hash only.

### `POST /api/v1/integrations/students`

**AuthZ:** API key.  
**Body:**

```json
{
  "externalSystem": "SALESFORCE",
  "externalStudentId": "SF_12345",
  "firstName": "Riya",
  "lastName": "Sharma",
  "email": "riya@example.com",
  "mobileNumber": "9876543210",
  "primaryOrgUnitId": "uuid",
  "grade": "GRADE_10"
}
```

**Side effects:** Upsert user; external_id_mappings; integration_events STUDENT_CREATE.

### `GET /api/v1/integrations/students/{externalStudentId}/results`

**AuthZ:** API key. Latest domain scores + attempt status.

### `GET /api/v1/integrations/students/{externalStudentId}/reports/latest`

**AuthZ:** API key. Latest generated report metadata + downloadUrl.

### `GET /api/v1/integration-events`

**AuthZ:** `ADMINISTRATOR`, scoped `SUB_ADMIN`.  
**Query:** `integrationClientId?`, `eventType?`, `status?`, `fromDate?`, `toDate?`, `page`, `size`.

---

## 9. Audit & activity (FFG §28)

### Required audit captures (map to audit_logs)

1. User update  
2. Role assignment  
3. Questionnaire publish  
4. IAR/scoring publish  
5. Configuration group assignment  
6. Assessment submit  
7. Report download  

### `GET /api/v1/audit-logs`

**AuthZ:** `ADMINISTRATOR`, limited scoped `SUB_ADMIN`.  
**Query:** `entityName?`, `entityId?`, `performedBy?`, `fromDate?`, `toDate?`, `page`, `size`.  
**Success:** `{ id, entityName, entityId, action, oldValue, newValue, performedBy, performedAt, ipAddress }`.

### `GET /api/v1/activity-logs`

**AuthZ:** `ADMINISTRATOR`.  
**Query:** `userId?`, `activityType?`, `fromDate?`, `toDate?`, `page`, `size`.  
**Success:** `{ id, userId, activityType, description, metadata, createdAt }`.  
**Note:** Separate from audit_logs — user activity stream only.

---

## Endpoint inventory (Sprint 4)

| Method | Path |
|--------|------|
| POST | `/recommendations/generate/{studentId}` |
| GET | `/students/{studentId}/recommendations` |
| GET/POST | `/report-templates` |
| POST | `/report-templates/{templateId}/versions` |
| PUT | `/report-template-versions/{versionId}` |
| POST | `/report-template-versions/{versionId}/publish` |
| POST | `/reports/generate/{studentId}` |
| GET | `/students/{studentId}/reports` |
| GET | `/reports/{reportId}` |
| GET | `/reports/{reportId}/download` |
| POST/GET | `/students/{studentId}/feedback` |
| PUT | `/feedback/{feedbackId}` |
| GET | `/students/{studentId}/dashboard` |
| GET | `/facilitators/{facilitatorId}/dashboard` |
| GET | `/admin/dashboard` |
| GET | `/sub-admin/dashboard` |
| POST | `/notifications/send` |
| GET | `/notification-logs` |
| POST | `/integration-clients` |
| POST | `/integrations/students` |
| GET | `/integrations/students/{externalStudentId}/results` |
| GET | `/integrations/students/{externalStudentId}/reports/latest` |
| GET | `/integration-events` |
| GET | `/audit-logs` |
| GET | `/activity-logs` |

## Phase-2 (do not implement)

Data Analyst APIs, Guest demo APIs, Salesforce/Glific/SMS connectors, advanced formula builders, approval workflows, scheduled report exports (FFG §31).
