# Sprint 3 API Testing Guide

**Purpose:** Manually verify every Sprint 3 API (assessment execution, scoring, benchmarks, career admin).  
**Contracts:** [api-contracts.md](./api-contracts.md) · [api-contracts-sprint-2-3-4.md](../api-contracts-sprint-2-3-4.md)  
**Bruno collection:** `docs/bruno/cap-sprint-3-assessment-execution/`  
**Base URL:** `http://localhost:8080/api/v1`

Mark each `[ ]` when the call returns `success: true` with the expected shape (or the documented error for negative tests).

---

## 0. Setup

### Prerequisites

- [ ] Backend running: `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
- [ ] DB migrated (Flyway V1–V16+)
- [ ] **Sprint 1 complete** — `ADMINISTRATOR` + `CAREER_EXPLORER` users, org structure, facilitator assignment
- [ ] **Sprint 2 complete** — published questionnaire, assessment, IAR, report template, **ACTIVE** config group assigned to explorer
- [ ] Optional: `docs/sql/local-test-seed.sql` applied (GENDER/GRADE, tenant `T-001`)
- [ ] Bruno **`local`** environment selected (shared with Sprint 1–2)

### Run order (Bruno)

1. **`00-env-refresh`** (01→10) if env vars are empty — reads existing DB, no duplicate creates  
2. **`01-scoring-rules`** → **`09-negative`**  
3. **`00-setup`** only when refreshing tokens

> **Scoring rule attachment:** Run folder **04** after publishing a scoring rule. Without `scoringRuleVersionId` on the config group item, submit succeeds but scoring returns `SCORING_RULE_MISSING`.

### Common headers

```http
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

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
| `adminUserId` | |
| `explorerUserId` | |
| `gradeConfigId` | |
| `languageId` | |
| `assessmentId` | |
| `acgId` | |
| `assessmentVersionId` | |
| `publishedQuestionnaireVersionId` | |
| `iarLogicVersionId` | |
| `questionId` (INT_Q001) | |
| `optionIdA` / `optionIdB` / `optionIdC` | |
| `scoringRuleId` | |
| `scoringRuleVersionId` | |
| `benchmarkId` | |
| `careerClusterId` | |
| `careerId` | |
| `careerMappingId` | |
| `attemptId` | |
| `negativeAttemptId` | |

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
| `200` + `data.accessToken` | [ ] |
| Save `adminAccessToken`, `adminUserId` | [ ] |

### 1.2 Login as Career Explorer

`POST /auth/login`

```json
{
  "loginId": "explorer.ngo.demo@test.com",
  "password": "SecurePass123!",
  "profileType": "CAREER_EXPLORER"
}
```

| Check | Pass |
|-------|------|
| `200` + `data.accessToken` | [ ] |
| Save `explorerAccessToken`, `explorerUserId` | [ ] |

---

## 2. Env refresh (optional — reload IDs from DB)

Run when Bruno `local` vars are blank but Sprint 1–2 data already exists.

| # | Request | Sets | Pass |
|---|---------|------|------|
| 1 | Admin login | `adminAccessToken`, `adminUserId` | [ ] |
| 2 | Explorer login | `explorerAccessToken`, `explorerUserId` | [ ] |
| 3 | `GET /configurations?groupCode=GRADE` | `gradeConfigId` | [ ] |
| 4 | `GET /languages` | `languageId` | [ ] |
| 5 | `GET /assessments?assessmentType=INTEREST` | `assessmentId` | [ ] |
| 6 | `GET /students/{studentId}/active-assessment-configuration` | `acgId`, version IDs | [ ] |
| 7 | `GET /questions?search=INT_Q001` | `questionId` | [ ] |
| 8 | `GET /questions/{questionId}` | `optionIdA/B/C` | [ ] |
| 9 | `GET /scoring-rules` | `scoringRuleId` (if exists) | [ ] |
| 10 | `GET /students/{studentId}/assessments` (explorer) | sanity — non-empty list | [ ] |

---

## 3. Scoring rules (EPIC-11)

### 3.1 Create scoring rule

`POST /scoring-rules` · **Auth:** ADMINISTRATOR

```json
{
  "assessmentId": "{{assessmentId}}",
  "code": "SR-INTEREST",
  "name": "Interest sum scoring",
  "description": "Sprint 3 demo scoring rule"
}
```

| Check | Pass |
|-------|------|
| `200` + save `scoringRuleId` | [ ] |

### 3.2 List scoring rules

`GET /scoring-rules` · **Auth:** ADMINISTRATOR

| Check | Pass |
|-------|------|
| `200` + array in `data` | [ ] |

### 3.3 Create scoring rule version

`POST /scoring-rules/{scoringRuleId}/versions`

```json
{
  "versionNumber": 1,
  "ruleDefinition": {
    "domains": [
      {
        "domainCode": "ANALYTICAL",
        "questionCodes": ["INT_Q001"],
        "aggregation": "SUM"
      }
    ]
  },
  "versionNotes": "Initial Sprint 3 scoring version"
}
```

| Check | Pass |
|-------|------|
| `200` + save `scoringRuleVersionId` | [ ] |

### 3.4 Update scoring rule version (DRAFT only)

`PUT /scoring-rule-versions/{scoringRuleVersionId}`

| Check | Pass |
|-------|------|
| `200` while DRAFT | [ ] |
| After publish → skip or expect error | [ ] |

### 3.5 Publish scoring rule version

`POST /scoring-rule-versions/{scoringRuleVersionId}/publish`

| Check | Pass |
|-------|------|
| Status → PUBLISHED | [ ] |

---

## 4. Benchmarks (EPIC-11)

### 4.1 Create benchmark

`POST /benchmarks`

```json
{
  "assessmentId": "{{assessmentId}}",
  "domainCode": "ANALYTICAL",
  "gradeConfigId": "{{gradeConfigId}}",
  "ageGroupConfigId": null,
  "minScore": 0,
  "maxScore": 100,
  "benchmarkLabel": "HIGH",
  "interpretation": "Strong analytical interest"
}
```

| Check | Pass |
|-------|------|
| `200` + save `benchmarkId` | [ ] |

### 4.2 List benchmarks

`GET /benchmarks?assessmentId={{assessmentId}}&domainCode=ANALYTICAL`

| Check | Pass |
|-------|------|
| `200` + includes created band | [ ] |

### 4.3 Update benchmark

`PUT /benchmarks/{benchmarkId}`

```json
{
  "interpretation": "Strong analytical inclination (updated)",
  "isActive": true
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

---

## 5. Career admin (seeds Sprint 4 recommendations)

### 5.1 Create career cluster

`POST /career-clusters`

```json
{
  "code": "STEM_CLUSTER",
  "name": "STEM Careers",
  "description": "Science, technology, engineering, mathematics"
}
```

| Check | Pass |
|-------|------|
| `200` + save `careerClusterId` | [ ] |

### 5.2 List / update career cluster

| Check | Pass |
|-------|------|
| `GET /career-clusters` → `200` | [ ] |
| `PUT /career-clusters/{id}` → `200` | [ ] |

### 5.3 Create career

`POST /careers`

```json
{
  "clusterId": "{{careerClusterId}}",
  "code": "DATA_ANALYST",
  "name": "Data Analyst",
  "description": "Analyze data to support decisions",
  "educationPathway": "Bachelor's in Statistics or CS",
  "skillsRequired": { "analytical": "high", "numerical": "high" }
}
```

| Check | Pass |
|-------|------|
| `200` + save `careerId` | [ ] |

### 5.4 List careers

`GET /careers`

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 5.5 Create career mapping

`POST /career-mappings`

```json
{
  "careerId": "{{careerId}}",
  "interestDomain": "ANALYTICAL",
  "aptitudeDomain": "NUMERICAL",
  "realityFactor": "MEDIUM",
  "aspirationFactor": "MATCH",
  "minScore": 0,
  "maxScore": 100,
  "mappingWeight": 1.0,
  "mappingConfig": {}
}
```

| Check | Pass |
|-------|------|
| `200` + save `careerMappingId` | [ ] |

### 5.6 List / update career mapping

| Check | Pass |
|-------|------|
| `GET /career-mappings` → `200` | [ ] |
| `PUT /career-mappings/{id}` → `200` | [ ] |

---

## 6. Config prep — attach scoring rule

### 6.1 Upsert config group item with scoring rule

`POST /assessment-configuration-groups/{acgId}/items` · **Auth:** ADMINISTRATOR

```json
{
  "items": [
    {
      "assessmentId": "{{assessmentId}}",
      "assessmentVersionId": "{{assessmentVersionId}}",
      "questionnaireVersionId": "{{publishedQuestionnaireVersionId}}",
      "iarLogicVersionId": "{{iarLogicVersionId}}",
      "scoringRuleVersionId": "{{scoringRuleVersionId}}",
      "assessmentLanguageId": "{{languageId}}",
      "isRequired": true,
      "displayOrder": 1
    }
  ]
}
```

| Check | Pass |
|-------|------|
| `200`; item includes `scoringRuleVersionId` | [ ] |

---

## 7. Student assessments (EPIC-10)

### 7.1 List assessments (admin)

`GET /assessments?page=0&size=20&assessmentType=INTEREST` · **Auth:** ADMINISTRATOR

| Check | Pass |
|-------|------|
| Paginated list with INTEREST assessment | [ ] |

### 7.2 List student assessments (explorer)

`GET /students/{explorerUserId}/assessments` · **Auth:** CAREER_EXPLORER (self)

| Check | Pass |
|-------|------|
| Non-empty when ACTIVE config assigned | [ ] |
| Empty array (not 500) when no assignment | [ ] |

---

## 8. Attempt flow (EPIC-10)

### 8.1 Start assessment

`POST /assessments/{assessmentId}/start` · **Auth:** CAREER_EXPLORER

```json
{
  "studentId": "{{explorerUserId}}",
  "assessmentLanguageId": "{{languageId}}"
}
```

| Check | Pass |
|-------|------|
| `200` + `attemptId`, `questions[]`, timer config | [ ] |
| Save `attemptId`; optional `attemptQuestionId` / `attemptOptionId` | [ ] |

### 8.2 Get attempt

`GET /attempts/{attemptId}` · **Auth:** owner

| Check | Pass |
|-------|------|
| IN_PROGRESS status + questions | [ ] |

### 8.3 Save responses

`PUT /attempts/{attemptId}/responses`

```json
{
  "responses": [
    {
      "questionId": "{{questionId}}",
      "selectedOptionIds": ["{{optionIdA}}"],
      "responseText": null,
      "responseNumeric": null,
      "isSkipped": false
    }
  ]
}
```

| Check | Pass |
|-------|------|
| `200`; use `questionId`/`optionIdA` from env refresh if start returned empty questions | [ ] |

### 8.4 Save question timings

`PUT /attempts/{attemptId}/question-timings`

```json
{
  "questionId": "{{questionId}}",
  "startedAt": "2026-07-01T10:02:00Z",
  "endedAt": "2026-07-01T10:03:30Z",
  "elapsedSeconds": 90
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 8.5 Submit attempt

`POST /attempts/{attemptId}/submit`

```json
{
  "clientElapsedSeconds": 2550,
  "submissionReason": "MANUAL_SUBMIT"
}
```

| Check | Pass |
|-------|------|
| `status` = SUBMITTED | [ ] |
| `scoreStatus` = COMPLETED (when scoring rule attached) | [ ] |
| `domainScores` includes ANALYTICAL `rawScore` 5 (option A on INT_Q001) | [ ] |
| `benchmarkLabel` = HIGH when benchmark matches grade | [ ] |

---

## 9. Scoring (EPIC-11)

### 9.1 Admin recalculate scoring

`POST /scoring/calculate/{attemptId}` · **Auth:** ADMINISTRATOR

```json
{ "force": true }
```

| Check | Pass |
|-------|------|
| `200` + scores array | [ ] |

### 9.2 Get student scores

`GET /students/{explorerUserId}/scores?attemptId={{attemptId}}` · **Auth:** self or admin

| Check | Pass |
|-------|------|
| Domain scores match submit output | [ ] |

---

## 10. Attempt history

### 10.1 Get attempt history

`GET /students/{explorerUserId}/attempt-history?assessmentId={{assessmentId}}&page=0&size=20`

| Check | Pass |
|-------|------|
| Paginated list includes submitted attempt | [ ] |

---

## 11. End-to-end demo script

Run after Sprint 2 is complete:

| # | Step | Pass |
|---|------|------|
| 1 | Env refresh or login + load IDs | [ ] |
| 2 | Create + publish scoring rule (SR-INTEREST) | [ ] |
| 3 | Create benchmark for ANALYTICAL domain | [ ] |
| 4 | Seed career cluster + career + mapping | [ ] |
| 5 | Attach `scoringRuleVersionId` to config group item | [ ] |
| 6 | Explorer lists available assessments | [ ] |
| 7 | Start → save responses → timings → submit | [ ] |
| 8 | Verify domain scores + benchmark label | [ ] |
| 9 | Attempt history shows SUBMITTED row | [ ] |

---

## 12. Negative / guardrail tests

| # | Scenario | Endpoint | Expected | Pass |
|---|----------|----------|----------|------|
| 1 | Submit without mandatory responses | `POST /attempts/{id}/submit` | `422` `MANDATORY_RESPONSE_MISSING` + `missingQuestionIds` | [ ] |
| 2 | Second start when maxAttempts=1 | `POST /assessments/{id}/start` | `MAX_ATTEMPTS_EXCEEDED` | [ ] |
| 3 | Save responses on SUBMITTED attempt | `PUT /attempts/{id}/responses` | `ATTEMPT_NOT_IN_PROGRESS` | [ ] |
| 4 | Explorer starts for another student | `POST .../start` wrong studentId | `ACCESS_DENIED` | [ ] |
| 5 | Submit without scoring rule on config | `POST .../submit` | submit OK; `SCORING_RULE_MISSING` or empty scores | [ ] |
| 6 | Duplicate scoring rule code | `POST /scoring-rules` same code | `DUPLICATE_RESOURCE` | [ ] |
| 7 | Duplicate career cluster code | `POST /career-clusters` | `DUPLICATE_RESOURCE` | [ ] |
| 8 | Counsellor mutating scoring rules | `POST /scoring-rules` as counsellor | `403` | [ ] |

---

## 13. Full endpoint inventory checklist

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/assessments` |
| [ ] | GET | `/students/{studentId}/assessments` |
| [ ] | POST | `/assessments/{assessmentId}/start` |
| [ ] | GET | `/attempts/{attemptId}` |
| [ ] | PUT | `/attempts/{attemptId}/responses` |
| [ ] | PUT | `/attempts/{attemptId}/question-timings` |
| [ ] | POST | `/attempts/{attemptId}/submit` |
| [ ] | GET | `/students/{studentId}/attempt-history` |
| [ ] | GET | `/scoring-rules` |
| [ ] | POST | `/scoring-rules` |
| [ ] | POST | `/scoring-rules/{scoringRuleId}/versions` |
| [ ] | PUT | `/scoring-rule-versions/{versionId}` |
| [ ] | POST | `/scoring-rule-versions/{versionId}/publish` |
| [ ] | POST | `/scoring/calculate/{attemptId}` |
| [ ] | GET | `/students/{studentId}/scores` |
| [ ] | GET | `/benchmarks` |
| [ ] | POST | `/benchmarks` |
| [ ] | PUT | `/benchmarks/{id}` |
| [ ] | GET/POST/PUT | `/career-clusters` |
| [ ] | GET/POST | `/careers` |
| [ ] | GET/POST/PUT | `/career-mappings` |
| [ ] | POST | `/assessment-configuration-groups/{acgId}/items` (scoring attach) |

---

## 14. Sample curl template

```bash
BASE=http://localhost:8080/api/v1
EXPLORER_TOKEN='paste-explorer-token'
ASSESSMENT_ID='paste-assessment-id'
STUDENT_ID='paste-explorer-user-id'

curl -sS -X POST "$BASE/assessments/$ASSESSMENT_ID/start" \
  -H "Authorization: Bearer $EXPLORER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"studentId\": \"$STUDENT_ID\",
    \"assessmentLanguageId\": \"paste-language-id\"
  }" | jq .
```

---

## Sign-off

| Item | Status |
|------|--------|
| Happy-path inventory (§13) complete | [ ] |
| Demo script (§11) complete | [ ] |
| Negative tests (§12) complete | [ ] |
| Tester | |
| Date | |
| Notes / defects | |
