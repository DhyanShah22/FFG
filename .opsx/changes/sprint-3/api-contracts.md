# Sprint 3 API Contracts (Implementable)

Source: FFG API Specification v1.0 (§19–20) + **IF2AF0203-export.pdf** + flow diagrams **`05`–`08`**, ERD **`5`**.  
Depends on Sprint 2 resolution + published questionnaire/scoring versions.

> **NGO merge:** Map to V5 attempt/response/timing and V6 scoring/benchmark columns.

## Conventions

Same wrapper, pagination, JWT, and role synonyms as [Sprint 2](../sprint-2/api-contracts.md#conventions).

| errorCode | Meaning |
|-----------|---------|
| ATTEMPT_ACTIVE | Conflicting IN_PROGRESS |
| ATTEMPT_NOT_IN_PROGRESS | Mutate/submit wrong status |
| ATTEMPT_EXPIRED | Over max duration |
| MAX_ATTEMPTS_EXCEEDED | max_attempts / reattempt policy |
| MANDATORY_RESPONSE_MISSING | FFG §6 — submit blocked |
| MANDATORY_UNANSWERED | Alias with missingQuestionIds in data |
| CONFIGURATION_NOT_FOUND | Cannot start |
| ASSESSMENT_ALREADY_SUBMITTED | Resubmit blocked |
| SCORING_RULE_MISSING | Snapshot null / invalid |
| INVALID_STATE | Edit frozen scoring version |

Ownership: path `{studentId}` must equal JWT user for `CAREER_EXPLORER`, or caller is Admin/Counsellor-with-assignment.

---

## 1. Assessments (FFG §19.1)

### `GET /api/v1/assessments`

**AuthZ:** authenticated admin roles.  
**Query:** `page`, `size`, `assessmentType?`.  
**Success:** paginated `{ id, code, name, assessmentType, isActive }`.

### `GET /api/v1/students/{studentId}/assessments`

**Purpose:** List assessments available to student (resolvable ACTIVE config).  
**AuthZ:** self / assigned counsellor / admin / scoped sub-admin.  
**Success `data`:**

```json
[
  {
    "assessmentId": "uuid",
    "assessmentCode": "INTEREST",
    "assessmentName": "Interest Assessment",
    "configurationGroupId": "uuid",
    "status": "NOT_STARTED",
    "timerMode": "COUNT_UP",
    "allowResume": true,
    "maxAttempts": 1,
    "attemptsUsed": 0,
    "activeAttemptId": null
  }
]
```

**Acceptance:** No assignment → empty list (not 500).

---

## 2. Start assessment (FFG §19.2)

### `POST /api/v1/assessments/{assessmentId}/start`

**Orchestration (diagram `05`):** resolve config group item → fetch questionnaire payload → create attempt with version snapshots → return questions + timer config.

**AuthZ:** `CAREER_EXPLORER` (self); facilitator/admin for demo/test per matrix.  
**Body:**

```json
{
  "studentId": "uuid",
  "assessmentLanguageId": "uuid"
}
```

**Success `data` (FFG shape):**

```json
{
  "attemptId": "uuid",
  "assessmentId": "uuid",
  "configurationGroupId": "uuid",
  "questionnaireVersionId": "uuid",
  "iarLogicVersionId": "uuid",
  "scoringRuleVersionId": "uuid",
  "reportTemplateVersionId": "uuid",
  "assessmentLanguageId": "uuid",
  "timer": {
    "timerMode": "COUNT_UP",
    "startedAt": "2026-07-01T10:00:00Z",
    "showTimerToStudent": true,
    "autoSubmitEnabled": false,
    "maxDurationMinutes": null,
    "allowResume": true
  },
  "questions": [
    {
      "questionId": "uuid",
      "questionType": "RADIO",
      "questionText": "Which activity do you enjoy the most?",
      "isMandatory": true,
      "displayOrder": 1,
      "sectionCode": "SECTION_1",
      "options": [
        { "optionId": "uuid", "optionText": "Solving puzzles", "displayOrder": 1 }
      ],
      "conditionConfig": null
    }
  ],
  "resumed": false
}
```

**Errors:** `CONFIGURATION_NOT_FOUND`, `MAX_ATTEMPTS_EXCEEDED`, `ACCESS_DENIED`.

---

## 3. Attempts & responses (FFG §19.3–19.6)

### `GET /api/v1/attempts/{attemptId}`

**AuthZ:** owner / assigned counsellor / admin.  
**Success:** attempt metadata + questions + saved responses (localized via snapshotted assessment_language_id).

### `PUT /api/v1/attempts/{attemptId}/responses`

**AuthZ:** attempt owner.  
**Body:**

```json
{
  "responses": [
    {
      "questionId": "uuid",
      "selectedOptionIds": ["uuid"],
      "responseText": null,
      "responseNumeric": null,
      "isSkipped": false
    }
  ]
}
```

**Rules:** IN_PROGRESS; questions belong to snapshotted version; RADIO max 1 option; CHECKBOX multiple.

**Optional MVP extension:** embed `questionElapsedSeconds` per row (also accept dedicated timings endpoint).

### `PUT /api/v1/attempts/{attemptId}/question-timings`

**AuthZ:** attempt owner (FFG §19.4).  
**Body:**

```json
{
  "questionId": "uuid",
  "startedAt": "2026-07-01T10:02:00Z",
  "endedAt": "2026-07-01T10:03:30Z",
  "elapsedSeconds": 90
}
```

**Batch variant (implementer choice):** `{ "timings": [ {...}, {...} ] }`.

### `POST /api/v1/attempts/{attemptId}/submit`

**AuthZ:** owner.  
**Body optional:** `{ "clientElapsedSeconds": 2550, "submissionReason": "MANUAL_SUBMIT" }`.

**Success `data`:**

```json
{
  "attemptId": "uuid",
  "status": "SUBMITTED",
  "startedAt": "2026-07-01T10:00:00Z",
  "submittedAt": "2026-07-01T10:42:30Z",
  "elapsedSeconds": 2550,
  "submissionReason": "MANUAL_SUBMIT",
  "scoreStatus": "COMPLETED",
  "domainScores": [
    {
      "domainCode": "ANALYTICAL",
      "rawScore": 72,
      "normalizedScore": 72,
      "benchmarkLabel": "HIGH",
      "interpretation": "Strong analytical interest"
    }
  ]
}
```

**Errors:** `MANDATORY_RESPONSE_MISSING` with `data.missingQuestionIds` — client re-prompts unanswered mandatory questions (diagram **`06`**).

**MVP note:** Response includes `domainScores` after synchronous scoring only. IAR/recommendations/report generation are **Sprint 4** (`POST /recommendations/generate`, `POST /reports/generate`) — diagram **`08`** full chain is split across sprints.

### `GET /api/v1/students/{studentId}/attempt-history`

**Query:** `assessmentId?`, `page`, `size`.  
**Success:** paginated attempts with status, attemptNumber, timestamps, elapsedSeconds.

---

## 4. Scoring rules (FFG §20.1–20.3)

### `POST /api/v1/scoring-rules`

**Body:** `{ "assessmentId": "uuid", "code": "SR-INTEREST", "name": "Interest sum" }`.

### `GET /api/v1/scoring-rules`

List with latest version status.

### `POST /api/v1/scoring-rules/{scoringRuleId}/versions`

**Body:**

```json
{
  "versionNumber": 1,
  "ruleDefinition": {
    "domains": [
      {
        "domainCode": "ANALYTICAL",
        "questionCodes": ["INT_Q001", "INT_Q002"],
        "aggregation": "SUM"
      }
    ]
  },
  "versionNotes": "Initial scoring version"
}
```

### `PUT /api/v1/scoring-rule-versions/{versionId}`

DRAFT only.

### `POST /api/v1/scoring-rule-versions/{versionId}/publish`

DRAFT→PUBLISHED. Audit IAR/scoring update.

---

## 5. Scoring execution (FFG §20.4–20.6)

### `POST /api/v1/scoring/calculate/{attemptId}`

**AuthZ:** `ADMINISTRATOR` or submit pipeline (not explorer).  
**Body optional:** `{ "force": true }`.  
**Success:** `{ attemptId, scores: [{ domainCode, rawScore, normalizedScore, benchmarkLabel, interpretation }] }`.

### `GET /api/v1/students/{studentId}/scores`

**Query:** `attemptId?` (default latest SUBMITTED with scores).  
**AuthZ:** self / counsellor / admin.

---

## 6. Benchmarks (FFG §20.5)

### `GET /api/v1/benchmarks`

**Query:** `assessmentId?`, `domainCode?`, `gradeConfigId?`, `ageGroupConfigId?`.

### `POST /api/v1/benchmarks`

**Body:**

```json
{
  "assessmentId": "uuid",
  "domainCode": "ANALYTICAL",
  "gradeConfigId": "uuid-or-null",
  "ageGroupConfigId": "uuid-or-null",
  "minScore": 71,
  "maxScore": 100,
  "benchmarkLabel": "HIGH",
  "interpretation": "Strong analytical inclination"
}
```

### `PUT /api/v1/benchmarks/{id}`

Partial update; recalc with force picks up new bands.

---

## 7. Career masters & mappings (EPIC-12 / IF2AF0203 Sprint 3)

> Recommendation **generation** (`POST /recommendations/generate`) is Sprint 4. Admin CRUD here seeds Sprint 4 engine.

### `POST /api/v1/career-clusters` · `GET` · `PUT /career-clusters/{id}`

**AuthZ:** `ADMINISTRATOR`.

### `POST /api/v1/careers` · `GET`

**Body:** `{ clusterId, code, name, description, educationPathway?, skillsRequired? }`.

### `POST /api/v1/career-mappings` · `GET` · `PUT /career-mappings/{id}`

**Body (NGO columns):**

```json
{
  "careerId": "uuid",
  "interestDomain": "ANALYTICAL",
  "aptitudeDomain": "NUMERICAL",
  "realityFactor": "MEDIUM",
  "aspirationFactor": "MATCH",
  "minScore": 60,
  "maxScore": 100,
  "mappingWeight": 1.0,
  "mappingConfig": {}
}
```

---

## Endpoint inventory (Sprint 3)

| Method | Path |
|--------|------|
| GET | `/assessments` |
| GET | `/students/{studentId}/assessments` |
| POST | `/assessments/{assessmentId}/start` |
| GET | `/attempts/{attemptId}` |
| PUT | `/attempts/{attemptId}/responses` |
| PUT | `/attempts/{attemptId}/question-timings` |
| POST | `/attempts/{attemptId}/submit` |
| GET | `/students/{studentId}/attempt-history` |
| GET/POST | `/scoring-rules` |
| POST | `/scoring-rules/{scoringRuleId}/versions` |
| PUT | `/scoring-rule-versions/{versionId}` |
| POST | `/scoring-rule-versions/{versionId}/publish` |
| POST | `/scoring/calculate/{attemptId}` |
| GET | `/students/{studentId}/scores` |
| GET/POST | `/benchmarks` |
| PUT | `/benchmarks/{id}` |
| GET/POST/PUT | `/career-clusters`, `/careers`, `/career-mappings` |

## Phase-2 deferrals

Advanced rule builder, simulation, approval workflows (FFG §31).
