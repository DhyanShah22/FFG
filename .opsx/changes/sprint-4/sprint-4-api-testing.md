# Sprint 4 API Testing Guide

**Purpose:** Manually verify Sprint 4 APIs — recommendations, reports, feedback, dashboards, notifications, integrations, audit & activity.  
**Contracts:** [api-contracts.md](./api-contracts.md) · [api-contracts-sprint-2-3-4.md](../api-contracts-sprint-2-3-4.md)  
**Bruno collection:** `docs/bruno/cap-sprint-4-outputs-platform/`  
**Base URL:** `http://localhost:8080/api/v1`

Mark each `[ ]` when the call returns `success: true` with the expected shape (or the documented error for negative tests).

---

## 0. Setup

### Prerequisites

- [ ] Backend running: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
- [ ] Flyway through **V17** (`V17__seed_notification_templates.sql`)
- [ ] **Sprint 1–3 complete** — users, ACTIVE config group, **SUBMITTED attempt with domain scores**
- [ ] **Career mappings seeded** (Sprint 3 folder `03-career-admin`)
- [ ] Counsellor assigned to explorer (Sprint 1) — required for feedback tests
- [ ] Bruno **`local`** environment (shared with Sprint 1–3)

### Run order (Bruno)

1. **`00-env-refresh`** (01→07) when tokens/`attemptId` are missing  
2. **`01-recommendations`** → **`08-e2e-smoke`**  
3. **`00-setup`** for token-only refresh

### Dependency chain

```
Sprint 3 submit + scores
  → generate recommendations (01)
  → generate report (02)
  → download report → audit/activity rows (07)
  → feedback (03)
```

### Common headers (JWT routes)

```http
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

### Integration routes

```http
Content-Type: application/json
X-Api-Key: {{integrationApiKey}}
```

No JWT on `/integrations/**` — API key filter only.

### Expected success wrapper

```json
{
  "success": true,
  "message": "...",
  "data": {},
  "timestamp": "..."
}
```

### ID scratchpad

| Variable | Value |
|----------|-------|
| `adminAccessToken` | |
| `explorerAccessToken` | |
| `counsellorAccessToken` | |
| `explorerUserId` | |
| `counsellorUserId` | |
| `attemptId` | (SUBMITTED, with scores) |
| `languageId` | |
| `recommendationRunId` | |
| `reportId` | |
| `feedbackId` | |
| `integrationClientId` | |
| `integrationApiKey` | (shown once on create) |
| `externalSystem` | e.g. `SALESFORCE` |
| `externalStudentId` | e.g. `SF_DEMO_001` |

---

## 1. Auth (prerequisite — Sprint 1)

### 1.1 Login as Administrator

`POST /auth/login`

```json
{
  "loginId": "admin.ngo.demo@test.com",
  "password": "SecurePass123!",
  "profileType": "ADMINISTRATOR"
}
```

| Check | Pass |
|-------|------|
| `200` + save `adminAccessToken` | [ ] |

### 1.2 Login as Career Explorer

```json
{
  "loginId": "explorer.ngo.demo@test.com",
  "password": "SecurePass123!",
  "profileType": "CAREER_EXPLORER"
}
```

| Check | Pass |
|-------|------|
| `200` + save `explorerAccessToken`, `explorerUserId` | [ ] |

### 1.3 Login as Career Counsellor

```json
{
  "loginId": "counsellor.ngo.demo@test.com",
  "password": "SecurePass123!",
  "profileType": "CAREER_COUNSELLOR"
}
```

| Check | Pass |
|-------|------|
| `200` + save `counsellorAccessToken` | [ ] |

---

## 2. Env refresh (optional)

| # | Request | Purpose | Pass |
|---|---------|---------|------|
| 1 | Admin login | Tokens | [ ] |
| 2 | Explorer login | Tokens + user id | [ ] |
| 3 | Counsellor login | Counsellor token | [ ] |
| 4 | `GET /languages` | `languageId` | [ ] |
| 5 | `GET /students/{id}/scores?attemptId=…` | Confirm scores exist | [ ] |
| 6 | `GET /students/{id}/attempt-history` | Resolve latest `attemptId` if blank | [ ] |
| 7 | `GET /students/{id}/active-assessment-configuration` | Config refs | [ ] |

---

## 3. Recommendations (EPIC-13)

### 3.1 Generate recommendations

`POST /recommendations/generate/{studentId}` · **Auth:** admin / counsellor / self

```json
{
  "attemptId": "{{attemptId}}"
}
```

| Check | Pass |
|-------|------|
| `200` + `recommendationRunId` | [ ] |
| `recommendations[]` with `rankOrder`, `matchBreakdown` | [ ] |
| Empty mappings → empty list or documented error (not 500) | [ ] |

### 3.2 List recommendations (latest)

`GET /students/{studentId}/recommendations`

| Check | Pass |
|-------|------|
| `200` + ranked careers | [ ] |

### 3.3 List by run id

`GET /students/{studentId}/recommendations?runId={{recommendationRunId}}`

| Check | Pass |
|-------|------|
| Filters to specific run | [ ] |

---

## 4. Reports (EPIC-14)

### 4.1 Generate report

`POST /reports/generate/{studentId}` · **Auth:** admin / counsellor / self

```json
{
  "attemptId": "{{attemptId}}",
  "recommendationRunId": "{{recommendationRunId}}",
  "languageId": "{{languageId}}"
}
```

| Check | Pass |
|-------|------|
| `200` + save `reportId`, `fileName`, `downloadUrl` | [ ] |
| Without recommendation run → `NO_RECOMMENDATION_RUN` | [ ] |

### 4.2 List student reports

`GET /students/{studentId}/reports?page=0&size=20`

| Check | Pass |
|-------|------|
| Paginated metadata includes generated report | [ ] |

### 4.3 List reports filtered by attempt

`GET /students/{studentId}/reports?attemptId={{attemptId}}`

| Check | Pass |
|-------|------|
| Filtered list | [ ] |

### 4.4 Get report metadata

`GET /reports/{reportId}`

| Check | Pass |
|-------|------|
| Includes `recommendationRunId`, template version refs | [ ] |

### 4.5 Download report

`GET /reports/{reportId}/download` · **Auth:** owner / assigned counsellor / admin

| Check | Pass |
|-------|------|
| `200` — PDF stream or mock `downloadUrl` | [ ] |
| Audit log row for report download | [ ] |
| Activity log `REPORT_DOWNLOAD` | [ ] |

---

## 5. Counsellor feedback (EPIC-15)

### 5.1 Create feedback

`POST /students/{studentId}/feedback` · **Auth:** assigned CAREER_COUNSELLOR

```json
{
  "reportId": "{{reportId}}",
  "feedbackText": "Student shows strong analytical inclination based on assessment results.",
  "recommendationNotes": "Discuss data analytics pathway in the next session.",
  "visibility": "VISIBLE_TO_STUDENT"
}
```

| Check | Pass |
|-------|------|
| `200` + save `feedbackId` | [ ] |
| Unassigned counsellor → `ACCESS_DENIED` | [ ] |

### 5.2 List feedback (explorer — student view)

`GET /students/{studentId}/feedback` · **Auth:** CAREER_EXPLORER (self)

| Check | Pass |
|-------|------|
| Only `VISIBLE_TO_STUDENT` entries | [ ] |

### 5.3 List feedback (counsellor — includes INTERNAL)

`GET /students/{studentId}/feedback` · **Auth:** counsellor

| Check | Pass |
|-------|------|
| Sees internal + visible entries | [ ] |

### 5.4 Update feedback

`PUT /feedback/{feedbackId}` · **Auth:** original counsellor or admin

```json
{
  "feedbackText": "Updated session notes.",
  "visibility": "INTERNAL"
}
```

| Check | Pass |
|-------|------|
| `200`; explorer no longer sees entry after INTERNAL | [ ] |

---

## 6. Dashboards (EPIC-16)

### 6.1 Student dashboard

`GET /students/{studentId}/dashboard` · **Auth:** self

| Check | Pass |
|-------|------|
| Counts: assessments, attempts, reports, recommendations | [ ] |

### 6.2 Student dashboard alias

`GET /dashboards/student` · **Auth:** CAREER_EXPLORER

| Check | Pass |
|-------|------|
| Same shape as §6.1 | [ ] |

### 6.3 Counsellor dashboard

`GET /facilitators/{counsellorUserId}/dashboard` or `GET /dashboards/counsellor`

| Check | Pass |
|-------|------|
| Assigned-student aggregates only | [ ] |

### 6.4 Admin dashboard

`GET /admin/dashboard` or `GET /dashboards/admin`

Optional query: `orgUnitId`, `clusterId`, `fromDate`, `toDate`

| Check | Pass |
|-------|------|
| Tenant-wide counts | [ ] |

### 6.5 Sub-admin dashboard (if SUB_ADMIN user seeded)

`GET /sub-admin/dashboard`

| Check | Pass |
|-------|------|
| Scoped counts ≤ admin dashboard | [ ] |

---

## 7. Notifications (EPIC-17)

### 7.1 Send notification (manual)

`POST /notifications/send` · **Auth:** ADMINISTRATOR

```json
{
  "userId": "{{explorerUserId}}",
  "templateCode": "ACCOUNT_CREATED",
  "channel": "EMAIL",
  "variables": {
    "firstName": "Explorer",
    "loginUrl": "https://cap.antarang.org"
  }
}
```

| Check | Pass |
|-------|------|
| `200`; log row created | [ ] |
| Unknown template → error | [ ] |

### 7.2 List notification logs

`GET /notification-logs?page=0&size=20` · **Auth:** ADMINISTRATOR

Optional: `userId`, `templateCode`, `status`

| Check | Pass |
|-------|------|
| Includes send from §7.1 | [ ] |

### 7.3 Automatic triggers (verify via logs after actions)

| Event | Trigger action | Pass |
|-------|------------------|------|
| ACCOUNT_CREATED | Register / integration upsert | [ ] |
| PASSWORD_RESET | Forgot-password flow | [ ] |
| ASSESSMENT_COMPLETED | Attempt submit | [ ] |
| REPORT_PUBLISHED | Report generate | [ ] |

---

## 8. Integrations (EPIC-18)

### 8.1 Create integration client

`POST /integration-clients` · **Auth:** ADMINISTRATOR

```json
{
  "clientCode": "SALESFORCE_DEMO",
  "clientName": "Salesforce Demo Client",
  "clientType": "API",
  "allowedScopes": {
    "students": ["read", "write"],
    "results": ["read"]
  }
}
```

| Check | Pass |
|-------|------|
| `200` + save `integrationClientId`, **`apiKey` (once)** | [ ] |

### 8.2 Upsert external student

`POST /integrations/students` · **Auth:** `X-Api-Key`

```json
{
  "externalSystem": "SALESFORCE",
  "externalStudentId": "SF_DEMO_001",
  "firstName": "Riya",
  "lastName": "Sharma",
  "email": "riya.integration.demo@test.com",
  "mobileNumber": "9876543210",
  "grade": "GRADE_10"
}
```

| Check | Pass |
|-------|------|
| `200`; user + external mapping created | [ ] |
| `integration_events` row (STUDENT_CREATE) | [ ] |

### 8.3 Get student results (integration)

`GET /integrations/students/{externalStudentId}/results?externalSystem=SALESFORCE`

| Check | Pass |
|-------|------|
| Latest domain scores when student mapped | [ ] |

### 8.4 Get latest report (integration)

`GET /integrations/students/{externalStudentId}/reports/latest?externalSystem=SALESFORCE`

| Check | Pass |
|-------|------|
| Report metadata + downloadUrl | [ ] |

### 8.5 List integration events

`GET /integration-events?page=0&size=20` · **Auth:** ADMINISTRATOR

| Check | Pass |
|-------|------|
| Events from §8.2+ visible | [ ] |

---

## 9. Audit & activity (EPIC-19)

### 9.1 List audit logs

`GET /audit-logs?page=0&size=20` · **Auth:** ADMINISTRATOR

Optional: `entityName`, `entityId`, `performedBy`, date range

| Check | Pass |
|-------|------|
| Rows for report download, role assign, etc. | [ ] |

### 9.2 List activity logs

`GET /activity-logs?page=0&size=20` · **Auth:** ADMINISTRATOR

Optional: `userId`, `activityType`, date range

| Check | Pass |
|-------|------|
| LOGIN / SUBMIT / REPORT_DOWNLOAD entries | [ ] |

---

## 10. End-to-end demo script (diagram 18)

| # | Step | Pass |
|---|------|------|
| 1 | Env refresh — confirm `attemptId` + scores | [ ] |
| 2 | Generate recommendations | [ ] |
| 3 | Generate report | [ ] |
| 4 | List + download report | [ ] |
| 5 | Counsellor creates feedback | [ ] |
| 6 | Student dashboard shows reports/recommendations | [ ] |
| 7 | Send manual notification | [ ] |
| 8 | Create integration client + upsert student | [ ] |
| 9 | Fetch results via API key | [ ] |
| 10 | Verify audit + activity logs | [ ] |

**Bruno shortcut:** run folder **`08-e2e-smoke`** (steps 2–5 + activity verify).

---

## 11. Negative / guardrail tests

| # | Scenario | Endpoint | Expected | Pass |
|---|----------|----------|----------|------|
| 1 | Generate recommendations without scores | `POST /recommendations/generate/{id}` | `NO_SCORES` | [ ] |
| 2 | Generate report without recommendation run | `POST /reports/generate/{id}` | `NO_RECOMMENDATION_RUN` | [ ] |
| 3 | Download report as unassigned counsellor | `GET /reports/{id}/download` | `ACCESS_DENIED` | [ ] |
| 4 | Feedback by unassigned counsellor | `POST /students/{id}/feedback` | `ACCESS_DENIED` | [ ] |
| 5 | Integration call without API key | `POST /integrations/students` | `401` / `INTEGRATION_UNAUTHORIZED` | [ ] |
| 6 | Integration call with invalid key | same | `INTEGRATION_UNAUTHORIZED` | [ ] |
| 7 | Explorer views another student's dashboard | `GET /students/{otherId}/dashboard` | `ACCESS_DENIED` | [ ] |
| 8 | Duplicate integration client code | `POST /integration-clients` | `DUPLICATE_RESOURCE` | [ ] |
| 9 | Sub-admin lists audit outside scope | `GET /audit-logs` | filtered / denied | [ ] |

---

## 12. Full endpoint inventory checklist

| Done | Method | Path |
|------|--------|------|
| [ ] | POST | `/recommendations/generate/{studentId}` |
| [ ] | GET | `/students/{studentId}/recommendations` |
| [ ] | POST | `/reports/generate/{studentId}` |
| [ ] | GET | `/students/{studentId}/reports` |
| [ ] | GET | `/reports/{reportId}` |
| [ ] | GET | `/reports/{reportId}/download` |
| [ ] | POST | `/students/{studentId}/feedback` |
| [ ] | GET | `/students/{studentId}/feedback` |
| [ ] | PUT | `/feedback/{feedbackId}` |
| [ ] | GET | `/students/{studentId}/dashboard` |
| [ ] | GET | `/dashboards/student` |
| [ ] | GET | `/facilitators/{facilitatorId}/dashboard` |
| [ ] | GET | `/dashboards/counsellor` |
| [ ] | GET | `/admin/dashboard` |
| [ ] | GET | `/dashboards/admin` |
| [ ] | GET | `/sub-admin/dashboard` |
| [ ] | POST | `/notifications/send` |
| [ ] | GET | `/notification-logs` |
| [ ] | POST | `/integration-clients` |
| [ ] | POST | `/integrations/students` |
| [ ] | GET | `/integrations/students/{externalStudentId}/results` |
| [ ] | GET | `/integrations/students/{externalStudentId}/reports/latest` |
| [ ] | GET | `/integration-events` |
| [ ] | GET | `/audit-logs` |
| [ ] | GET | `/activity-logs` |

---

## 13. Sample curl template

```bash
BASE=http://localhost:8080/api/v1
ADMIN_TOKEN='paste-admin-token'
STUDENT_ID='paste-explorer-user-id'
ATTEMPT_ID='paste-submitted-attempt-id'

curl -sS -X POST "$BASE/recommendations/generate/$STUDENT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"attemptId\": \"$ATTEMPT_ID\"}" | jq .
```

Integration example:

```bash
curl -sS -X POST "$BASE/integrations/students" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: paste-api-key-once" \
  -d '{
    "externalSystem": "SALESFORCE",
    "externalStudentId": "SF_DEMO_001",
    "firstName": "Riya",
    "lastName": "Sharma",
    "email": "riya.integration.demo@test.com"
  }' | jq .
```

---

## Sign-off

| Item | Status |
|------|--------|
| Happy-path inventory (§12) complete | [ ] |
| Demo script (§10) complete | [ ] |
| Negative tests (§11) complete | [ ] |
| Integration API key stored securely (not committed) | [ ] |
| Tester | |
| Date | |
| Notes / defects | |
