# Sprint 2 API Contracts (Implementable)

Source: FFG *Antarang CAP Platform API Specification* v1.0 (§14–18, §21, §23.1–23.3) + **IF2AF0203-export.pdf** + flow diagrams **`04`**, **`12`**, **`13`**, ERD **`4`**.  
Base path: `/api/v1`.

> **NGO merge:** Persist NGO enum/column names (`question_code`, `RADIO`/`CHECKBOX`, `domain_code`, `review_status`, `assessment_configuration_group_items`, etc.). DTOs MAY expose aliases — see [spec.md](./spec.md).

## Conventions

### Role synonyms (FFG §7 → backend)

| FFG label | Backend role |
|-----------|--------------|
| Student | `CAREER_EXPLORER` |
| Facilitator / Counsellor | `CAREER_COUNSELLOR` |
| Administrator | `ADMINISTRATOR` |
| Sub Admin | `SUB_ADMIN` |
| Super Admin | `SUPER_ADMIN` |

### Auth & headers

* `Authorization: Bearer <accessToken>` (except public config reads)
* Tenant from JWT `tenantId`; optional `X-Tenant-Id` per FFG §3
* Optional `Accept-Language` for localized admin preview

### Response wrapper (FFG §4)

```json
{
  "success": true,
  "message": "string",
  "data": {},
  "timestamp": "2026-08-08T00:00:00Z"
}
```

### Pagination

`?page=0&size=20` → `data`: `{ "content": [], "page", "size", "totalElements", "totalPages", "last" }`

### Common errors (FFG §6)

| HTTP | errorCode | When |
|------|-----------|------|
| 400 | VALIDATION_ERROR | Bean validation / business field rules |
| 403 | ACCESS_DENIED | Role/tenant mismatch |
| 404 | RESOURCE_NOT_FOUND | Missing entity |
| 409 | DUPLICATE_RESOURCE | Unique code conflict |
| 422 | INVALID_STATE | Publish/edit on wrong status |

---

## 1. Configuration groups & values (FFG §14)

> Extend Sprint-1 controllers where present.

### `GET /api/v1/configuration-groups`

**AuthZ:** authenticated admin roles.  
**Success:** paginated `{ id, code, name, description, isSystemDefined? }`.

### `POST /api/v1/configuration-groups`

**AuthZ:** `ADMINISTRATOR`.  
**Body:** `{ "code": "GRADE", "name": "Grade", "description": "...", "isSystemDefined": false }`.

### `GET /api/v1/configurations`

**AuthZ:** authenticated (public for signup if Sprint 1 permits).  
**Query:** `groupCode` required for filtered list; optional `tenantId` for admin.  
**Success:** `[{ id, groupId, code, label/value, displayOrder, metadata, isActive }]`.

### `POST /api/v1/configurations`

**AuthZ:** `ADMINISTRATOR`.  
**Body:** `{ "configurationGroupId": "uuid", "code": "GRADE_10", "value": "Grade 10", "displayOrder": 1, "metadata": {} }`.

### `PUT /api/v1/configurations/{id}`

**AuthZ:** `ADMINISTRATOR`. Partial update label/value/order/active.

### `GET /api/v1/languages`

**AuthZ:** authenticated / public as today.

### `POST /api/v1/languages`

**AuthZ:** `ADMINISTRATOR`.  
**Body:** `{ "code": "hi", "name": "Hindi", "isActive": true }`.

---

## 2. Question categories (FFG §15.1)

### `GET /api/v1/question-categories`

**Query:** `assessmentType?`, `page`, `size`.  
**AuthZ:** `ADMINISTRATOR`, `SUB_ADMIN`, `CAREER_COUNSELLOR` (read).

### `POST /api/v1/question-categories`

**AuthZ:** `ADMINISTRATOR`.  
**Body:**

```json
{
  "assessmentType": "INTEREST",
  "code": "ANALYTICAL",
  "name": "Analytical Interest",
  "domainCode": "ANALYTICAL",
  "description": "Analytical interest domain"
}
```

### `PUT /api/v1/question-categories/{categoryId}`

**AuthZ:** `ADMINISTRATOR`. Code immutable; update name/description/domainCode/isActive.

---

## 3. Questions (FFG §15.2–15.6)

### `POST /api/v1/questions`

**AuthZ:** `ADMINISTRATOR`.  
**Body:**

```json
{
  "categoryId": "uuid",
  "questionCode": "INT_Q001",
  "questionType": "RADIO",
  "defaultText": "Which activity do you enjoy the most?",
  "helpText": "Select one option",
  "difficultyLevel": "EASY",
  "reviewStatus": "APPROVED",
  "isRequiredDefault": true,
  "scoringEnabled": true,
  "metadata": {},
  "options": [
    { "optionCode": "A", "defaultText": "Solving puzzles", "scoreValue": 5, "displayOrder": 1 }
  ]
}
```

**Errors:** `VALIDATION_ERROR` if RADIO has &lt;2 options. Accept DTO aliases `SINGLE_CHOICE`→`RADIO`.

### `GET /api/v1/questions`

**Query:** `assessmentType?`, `categoryId?`, `questionType?`, `reviewStatus?`, `languageId?`, `search?`, `page`, `size`.  
**Default list:** APPROVED + active unless filters override.

### `GET /api/v1/questions/{questionId}`

Full question + options + translations + rules summary.

### `PUT /api/v1/questions/{questionId}`

**AuthZ:** `ADMINISTRATOR`. Same shape as create (code immutable).  
**MVP rule:** allow translation text updates always; allow adding options; disallow deleting options on published versions → `QUESTION_IN_PUBLISHED_VERSION`.

### `DELETE /api/v1/questions/{questionId}`

Soft-delete (`is_deleted=true`) if not on PUBLISHED version; else `409 QUESTION_IN_USE`.

### `POST /api/v1/questions/{questionId}/options`

**Body:** `{ "optionCode", "defaultText", "scoreValue", "displayOrder", "metadata?" }`.

### `PUT /api/v1/questions/{questionId}/translations`

**Body (FFG §15.5):**

```json
{
  "translations": [
    { "languageId": "uuid", "questionText": "...", "helpText": "..." }
  ],
  "optionTranslations": [
    { "optionId": "uuid", "languageId": "uuid", "optionText": "..." }
  ]
}
```

### `POST /api/v1/questions/{questionId}/rules`

**Body:** `{ "ruleType": "VISIBILITY", "ruleConfig": { "dependsOnQuestionId", "operator", "value", "action" } }`.

### `GET /api/v1/questions/{questionId}/rules`

List active rules.

### `DELETE /api/v1/questions/{questionId}/rules/{ruleId}`

Deactivate or delete per NGO schema.

### `PATCH /api/v1/questions/{questionId}/status`

**Body:** `{ "reviewStatus": "RETIRED", "isActive": false }`.

---

## 4. Questionnaires (FFG §16)

### `POST /api/v1/questionnaires`

**Body:** `{ "assessmentType", "code", "name", "description?", "shuffleQuestions": false }`  
**Side effects:** Create questionnaire + version 1 DRAFT.

### `GET /api/v1/questionnaires`

**Query:** `assessmentType?`, `page`, `size`.

### `GET /api/v1/questionnaires/{questionnaireId}`

**Query:** `versionId?` (default latest). Returns ordered questions with summary.

### `POST /api/v1/questionnaires/{questionnaireId}/versions`

**Body:** `{ "versionNotes": "..." }`.

### `POST /api/v1/questionnaire-versions/{versionId}/questions`

**Body:**

```json
{
  "questions": [
    {
      "questionId": "uuid",
      "displayOrder": 1,
      "isMandatory": true,
      "sectionCode": "SECTION_1",
      "sectionName": "Interest Discovery",
      "conditionConfig": null
    }
  ]
}
```

### `PUT /api/v1/questionnaire-versions/{versionId}/questions/reorder`

**Body:** `{ "questionIds": ["uuid", "..."] }` in desired order.

### `GET /api/v1/questionnaire-versions/{versionId}/preview`

**Query:** `languageId?`. Localized preview for admin.

### `POST /api/v1/questionnaire-versions/{versionId}/publish`

**Body optional:** `{ "versionNotes": "Approved for MVP" }`.  
**Rules:** DRAFT, ≥1 question. Audit `PUBLISH`.

### `POST /api/v1/questionnaire-versions/{versionId}/suspend`

**AuthZ:** `ADMINISTRATOR`.  
**Purpose:** Suspend version from new assignments while preserving historical attempts (IF2AF0203 EPIC-08).  
**Side effects:** Status transition; audit.

### `POST /api/v1/questionnaire-versions/{versionId}/retire`

**AuthZ:** `ADMINISTRATOR`.  
**Purpose:** Retire/archive obsolete version (IF2AF0203 audit: publish/**retire**).  
**Side effects:** Status → `ARCHIVED` or questionnaire master `INACTIVE`; audit.

### `POST /api/v1/questionnaires/{questionnaireId}/clone`

**Body optional:** `{ "code", "name" }`.

### `GET /api/v1/questionnaires/{questionnaireId}/versions`

Returns `[{ id, versionNumber, status, publishedAt, questionCount }]`.

### `PUT /api/v1/questionnaires/{questionnaireId}`

**MVP consolidated edit:** when latest is DRAFT, replace questions/title; when latest PUBLISHED, auto-create next DRAFT version and apply (document in response message).

---

## 5. Assessment masters & execution (FFG §17)

### `POST /api/v1/assessments`

**Body:** `{ "code": "INTEREST", "name": "Interest Assessment", "description": "..." }`.

### `GET /api/v1/assessments`

Paginated list.

### `POST /api/v1/assessments/{assessmentId}/versions`

**Body:** `{ "questionnaireVersionId": "uuid", "versionNumber": 1 }` — questionnaire version MUST be PUBLISHED.

### `POST /api/v1/assessment-versions/{versionId}/configuration`

**Body:**

```json
{
  "timerMode": "COUNT_UP",
  "maxDurationMinutes": null,
  "autoSubmitEnabled": false,
  "showTimerToStudent": true,
  "allowResume": true,
  "restartOnInterruption": false,
  "allowReattempt": true,
  "reattemptAfterDays": 365,
  "maxAttempts": 1,
  "completionRule": "ALL_MANDATORY",
  "configJson": {}
}
```

---

## 6. Assessment configuration groups (FFG §18)

### `POST /api/v1/assessment-configuration-groups`

**Body:**

```json
{
  "code": "MH_GRADE10_2026",
  "name": "Maharashtra Grade 10 Assessment Group",
  "description": "...",
  "academicYear": "2026-27",
  "assessmentCycle": "Cycle 1"
}
```

Initial `status`: `DRAFT`.

### `GET /api/v1/assessment-configuration-groups`

**Query:** `status?`, `academicYear?`, `page`, `size`.

### `GET /api/v1/assessment-configuration-groups/{groupId}`

Full group + items + outputs + assignments.

### `PUT /api/v1/assessment-configuration-groups/{groupId}`

Metadata update (name, description, academicYear, assessmentCycle).

### `POST /api/v1/assessment-configuration-groups/{groupId}/items`

**Body (FFG batch shape):**

```json
{
  "items": [
    {
      "assessmentId": "uuid",
      "assessmentVersionId": "uuid",
      "questionnaireVersionId": "uuid",
      "iarLogicVersionId": "uuid-or-null",
      "scoringRuleVersionId": "uuid-or-null",
      "assessmentLanguageId": "uuid",
      "isRequired": true,
      "displayOrder": 1
    }
  ]
}
```

### `POST /api/v1/assessment-configuration-groups/{groupId}/outputs`

**Body:**

```json
{
  "reportTemplateVersionId": "uuid",
  "outputLanguageId": "uuid",
  "outputConfig": {
    "showScores": true,
    "showRecommendations": true,
    "showCounsellorFeedback": true
  }
}
```

### `POST /api/v1/assessment-configuration-groups/{groupId}/assignments`

**Body:**

```json
{
  "assignmentType": "ORG_UNIT",
  "assignmentId": "uuid",
  "effectiveFrom": "2026-07-01T00:00:00Z",
  "effectiveTo": "2027-03-31T23:59:59Z"
}
```

**AuthZ:** `ADMINISTRATOR`, scoped `SUB_ADMIN` where permitted.

### `DELETE /api/v1/assessment-configuration-groups/{groupId}/assignments/{assignmentId}`

### `POST /api/v1/assessment-configuration-groups/{groupId}/activate`

**Rules:** ≥1 item; referenced versions PUBLISHED/active; DRAFT→ACTIVE.

### `GET /api/v1/assessment-configuration-groups/resolve`

**Query:** `studentId`, `assessmentId`. Admin debug.  
**Success:** winning group + matched item + `matchedAssignmentType`.

### `GET /api/v1/students/{studentId}/active-assessment-configuration`

**AuthZ:** self, assigned counsellor, admin, scoped sub-admin.  
**Query:** optional `assessmentId`.  
**Success:** resolved ACTIVE group with items + outputs.

---

## 7. IAR logic (FFG §21)

### `POST /api/v1/iar-logic-configs`

**Body:** `{ "code", "name", "description" }`.

### `GET /api/v1/iar-logic-configs`

List with latest version status.

### `POST /api/v1/iar-logic-configs/{logicConfigId}/versions`

**Body:**

```json
{
  "versionNumber": 1,
  "logicDefinition": {
    "aspirationWeight": 0.25,
    "interestWeight": 0.25,
    "aptitudeWeight": 0.30,
    "realityWeight": 0.20,
    "rules": []
  },
  "versionNotes": "Initial"
}
```

### `PUT /api/v1/iar-logic-versions/{versionId}`

DRAFT only — update `logicDefinition`.

### `POST /api/v1/iar-logic-versions/{versionId}/validate`

**Success:** `{ "valid": true, "errors": [] }` or `{ "valid": false, "errors": [{ "path", "message" }] }`.

### `POST /api/v1/iar-logic-versions/{versionId}/publish`

DRAFT→PUBLISHED after validate. Audit `PUBLISH` on IAR entity.

---

## 8. Report template skeleton (FFG §23.1–23.3)

### `POST /api/v1/report-templates`

**Body:** `{ "code", "name", "templateType": "STUDENT" }` → template + DRAFT version v1.

### `GET /api/v1/report-templates`

### `POST /api/v1/report-templates/{templateId}/versions`

**Body:** `{ "templateConfig": {}, "versionNotes": "..." }`.

### `POST /api/v1/report-template-versions/{versionId}/publish`

**MVP alias:** `POST /report-templates/{templateId}/publish` on current DRAFT.

Full `report_sections` CRUD is Sprint 4.

---

## Endpoint inventory (Sprint 2)

| Method | Path |
|--------|------|
| GET/POST | `/configuration-groups` |
| GET/POST | `/configurations` |
| PUT | `/configurations/{id}` |
| GET/POST | `/languages` |
| GET/POST | `/question-categories` |
| PUT | `/question-categories/{categoryId}` |
| GET/POST | `/questions` |
| GET/PUT/DELETE | `/questions/{questionId}` |
| POST | `/questions/{questionId}/options` |
| PUT | `/questions/{questionId}/translations` |
| GET/POST/DELETE | `/questions/{questionId}/rules`, `.../rules/{ruleId}` |
| PATCH | `/questions/{questionId}/status` |
| GET/POST | `/questionnaires` |
| GET/PUT | `/questionnaires/{questionnaireId}` |
| POST | `/questionnaires/{questionnaireId}/versions` |
| POST | `/questionnaires/{questionnaireId}/clone` |
| GET | `/questionnaires/{questionnaireId}/versions` |
| POST | `/questionnaire-versions/{versionId}/questions` |
| PUT | `/questionnaire-versions/{versionId}/questions/reorder` |
| GET | `/questionnaire-versions/{versionId}/preview` |
| POST | `/questionnaire-versions/{versionId}/publish` |
| POST | `/questionnaire-versions/{versionId}/suspend` |
| POST | `/questionnaire-versions/{versionId}/retire` |
| GET/POST | `/assessments` |
| POST | `/assessments/{assessmentId}/versions` |
| POST | `/assessment-versions/{versionId}/configuration` |
| GET/POST | `/assessment-configuration-groups` |
| GET/PUT | `/assessment-configuration-groups/{groupId}` |
| POST | `/assessment-configuration-groups/{groupId}/items` |
| POST | `/assessment-configuration-groups/{groupId}/outputs` |
| POST | `/assessment-configuration-groups/{groupId}/activate` |
| POST/DELETE | `/assessment-configuration-groups/{groupId}/assignments`, `.../assignments/{assignmentId}` |
| GET | `/assessment-configuration-groups/resolve` |
| GET | `/students/{studentId}/active-assessment-configuration` |
| GET/POST | `/iar-logic-configs` |
| POST | `/iar-logic-configs/{logicConfigId}/versions` |
| PUT | `/iar-logic-versions/{versionId}` |
| POST | `/iar-logic-versions/{versionId}/validate` |
| POST | `/iar-logic-versions/{versionId}/publish` |
| GET/POST | `/report-templates` |
| POST | `/report-templates/{templateId}/versions` |
| POST | `/report-template-versions/{versionId}/publish` |

## FFG path aliases (optional forwards)

| FFG canonical | MVP alias |
|---------------|-----------|
| `POST /questionnaire-versions/{id}/publish` | `POST /questionnaires/{id}/publish` |
| IF2AF0203 API catalogue §133–136 | Simpler path list; **FFG API Spec + this file** are authoritative for payloads |
| Consolidated questionnaire PUT | May replace separate add/reorder for MVP if documented |

## Phase-2 deferrals

Data Analyst APIs, Guest demo APIs, advanced rule builder/simulation, approval workflows (FFG §31).
