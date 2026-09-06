# Sprint 2 API Testing Guide

**Purpose:** Manually verify every Sprint 2 API implemented in the backend.  
**Contracts:** [api-contracts.md](./api-contracts.md) · [api-contracts-sprint-2-3-4.md](../api-contracts-sprint-2-3-4.md)  
**Base URL:** `http://localhost:8080/api/v1`  
**Auth:** `Authorization: Bearer <accessToken>` (ADMINISTRATOR unless noted)

Mark each `[ ]` when the call returns `success: true` with the expected shape.

---

## 0. Setup

### Prerequisites

- [ ] Backend running (`./mvnw spring-boot:run` from `backend/`)
- [ ] DB migrated (Flyway V1–V16+)
- [ ] At least one `ADMINISTRATOR` user exists
- [ ] Optional: one `CAREER_EXPLORER` student + org unit / cluster for resolution tests

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

Paginated `data` shape (where used):

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

### ID scratchpad (fill as you go)

| Variable | Value |
|----------|-------|
| `accessToken` | |
| `studentToken` | (explorer JWT for resolve self-test) |
| `tenantId` | |
| `languageId` (en/hi) | |
| `configurationGroupId` (platform) | |
| `configurationId` | |
| `categoryId` | |
| `questionId` | |
| `optionIdA` / `optionIdB` | |
| `ruleId` | |
| `questionnaireId` | |
| `questionnaireVersionId` (DRAFT) | |
| `publishedQuestionnaireVersionId` | |
| `clonedQuestionnaireId` | |
| `assessmentId` | |
| `assessmentVersionId` | |
| `iarLogicConfigId` | |
| `iarLogicVersionId` | |
| `reportTemplateId` | |
| `reportTemplateVersionId` | |
| `acgId` (assessment config group) | |
| `acgAssignmentId` | |
| `studentId` | |
| `orgUnitId` / `clusterId` | |

---

## 1. Auth (prerequisite — Sprint 1)

### 1.1 Login as Administrator

`POST /auth/login`

```json
{
  "loginId": "admin@example.com",
  "password": "YourPassword1!"
}
```

| Check | Pass |
|-------|------|
| `200` + `data.accessToken` present | [ ] |
| Save `accessToken`, `tenantId`, admin user id | [ ] |

---

## 2. Configuration & language (EPIC-06)

### 2.1 List languages

`GET /languages`  
**Auth:** authenticated

| Check | Pass |
|-------|------|
| `200` + array in `data` | [ ] |

### 2.2 Create language

`POST /languages`  
**Auth:** ADMINISTRATOR

```json
{
  "code": "mr",
  "name": "Marathi",
  "isActive": true
}
```

| Check | Pass |
|-------|------|
| `200` + language `id` returned | [ ] |
| Duplicate code → `DUPLICATE_RESOURCE` / 409 or validation error | [ ] |
| Save `languageId` | [ ] |

### 2.3 List configuration groups

`GET /configuration-groups`  
**Auth:** often public / authenticated

| Check | Pass |
|-------|------|
| `200` + list (may include system + tenant groups) | [ ] |

### 2.4 Create configuration group

`POST /configuration-groups`

```json
{
  "code": "GRADE_S2_TEST",
  "name": "Grade (Sprint2 Test)",
  "description": "Test group",
  "isSystemDefined": false
}
```

| Check | Pass |
|-------|------|
| `200` + `id` → save as `configurationGroupId` | [ ] |

### 2.5 Create configuration value

`POST /configurations`

```json
{
  "configurationGroupId": "{{configurationGroupId}}",
  "code": "GRADE_10",
  "value": "Grade 10",
  "displayOrder": 1,
  "metadata": {}
}
```

| Check | Pass |
|-------|------|
| `200` + save `configurationId` | [ ] |

### 2.6 List configurations by group

`GET /configurations?groupCode=GRADE_S2_TEST`

| Check | Pass |
|-------|------|
| `200` + includes `GRADE_10` | [ ] |

### 2.7 Update configuration

`PUT /configurations/{{configurationId}}`

```json
{
  "value": "Grade 10 Updated",
  "displayOrder": 2,
  "isActive": true,
  "metadata": { "band": "secondary" }
}
```

| Check | Pass |
|-------|------|
| `200` + updated `value` / `displayOrder` / `isActive` | [ ] |

---

## 3. Question categories (EPIC-07)

### 3.1 Create category

`POST /question-categories`  
**Auth:** ADMINISTRATOR

```json
{
  "assessmentType": "INTEREST",
  "code": "ANALYTICAL",
  "name": "Analytical Interest",
  "domainCode": "ANALYTICAL",
  "description": "Analytical domain"
}
```

| Check | Pass |
|-------|------|
| `200` + save `categoryId` | [ ] |
| Duplicate `(type, code)` → `DUPLICATE_RESOURCE` | [ ] |

### 3.2 List categories

`GET /question-categories?assessmentType=INTEREST&page=0&size=20`  
**Auth:** ADMINISTRATOR / SUB_ADMIN / CAREER_COUNSELLOR

| Check | Pass |
|-------|------|
| Paginated `content` includes created category | [ ] |

### 3.3 Update category

`PUT /question-categories/{{categoryId}}`

```json
{
  "name": "Analytical Interest Updated",
  "domainCode": "ANALYTICAL",
  "description": "Updated",
  "isActive": true
}
```

| Check | Pass |
|-------|------|
| `200`; `code` unchanged | [ ] |

---

## 4. Questions (EPIC-07)

### 4.1 Create question (with options)

`POST /questions`

```json
{
  "categoryId": "{{categoryId}}",
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
    {
      "optionCode": "A",
      "defaultText": "Solving puzzles",
      "scoreValue": 5,
      "displayOrder": 1
    },
    {
      "optionCode": "B",
      "defaultText": "Helping others",
      "scoreValue": 3,
      "displayOrder": 2
    }
  ]
}
```

| Check | Pass |
|-------|------|
| `200` + save `questionId`, `optionIdA`, `optionIdB` | [ ] |
| Alias `SINGLE_CHOICE` also accepted as RADIO | [ ] |
| RADIO with &lt;2 options → `VALIDATION_ERROR` | [ ] |

### 4.2 List questions

`GET /questions?assessmentType=INTEREST&reviewStatus=APPROVED&page=0&size=20`

| Check | Pass |
|-------|------|
| Includes `INT_Q001` | [ ] |
| Filters by `categoryId`, `questionType`, `search` work | [ ] |

### 4.3 Get question detail

`GET /questions/{{questionId}}`

| Check | Pass |
|-------|------|
| Options + empty/partial translations + rules present | [ ] |

### 4.4 Add option

`POST /questions/{{questionId}}/options`

```json
{
  "optionCode": "C",
  "defaultText": "Building things",
  "scoreValue": 4,
  "displayOrder": 3
}
```

| Check | Pass |
|-------|------|
| `200` + new option id | [ ] |

### 4.5 Upsert translations (question + options)

`PUT /questions/{{questionId}}/translations`

```json
{
  "translations": [
    {
      "languageId": "{{languageId}}",
      "questionText": "आपको कौन सी गतिविधि सबसे अधिक पसंद है?",
      "helpText": "एक विकल्प चुनें"
    }
  ],
  "optionTranslations": [
    {
      "optionId": "{{optionIdA}}",
      "languageId": "{{languageId}}",
      "optionText": "पहेली हल करना"
    },
    {
      "optionId": "{{optionIdB}}",
      "languageId": "{{languageId}}",
      "optionText": "दूसरों की मदद करना"
    }
  ]
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |
| `GET /questions/{{questionId}}` shows both question + option translations | [ ] |

### 4.6 Create rule

`POST /questions/{{questionId}}/rules`

```json
{
  "ruleType": "VISIBILITY",
  "ruleConfig": {
    "dependsOnQuestionId": "{{questionId}}",
    "operator": "EQUALS",
    "value": "A",
    "action": "SHOW"
  }
}
```

| Check | Pass |
|-------|------|
| `200` + save `ruleId` | [ ] |

### 4.7 List rules

`GET /questions/{{questionId}}/rules`

| Check | Pass |
|-------|------|
| Includes created rule | [ ] |

### 4.8 Delete / deactivate rule

`DELETE /questions/{{questionId}}/rules/{{ruleId}}`

| Check | Pass |
|-------|------|
| `200`; rule gone or `isActive=false` | [ ] |

### 4.9 Update question

`PUT /questions/{{questionId}}`

```json
{
  "categoryId": "{{categoryId}}",
  "questionType": "RADIO",
  "defaultText": "Which activity do you enjoy the most? (updated)",
  "helpText": "Select one",
  "difficultyLevel": "MEDIUM",
  "reviewStatus": "APPROVED",
  "isRequiredDefault": true,
  "scoringEnabled": true,
  "metadata": {},
  "options": null
}
```

| Check | Pass |
|-------|------|
| `200`; `questionCode` immutable | [ ] |

### 4.10 Patch status

`PATCH /questions/{{questionId}}/status`

```json
{
  "reviewStatus": "APPROVED",
  "isActive": true
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

> Keep at least one **APPROVED + active** question for questionnaire attach tests.  
> Soft-delete / retire negative cases are in §11.

---

## 5. Questionnaires (EPIC-08)

### 5.1 Create questionnaire

`POST /questionnaires`

```json
{
  "assessmentType": "INTEREST",
  "code": "INT_QN_V1",
  "name": "Interest Discovery",
  "description": "Sprint 2 demo questionnaire",
  "shuffleQuestions": false
}
```

| Check | Pass |
|-------|------|
| `200` + save `questionnaireId` | [ ] |
| Version 1 DRAFT created; save `questionnaireVersionId` (`currentVersionId`) | [ ] |

### 5.2 List questionnaires

`GET /questionnaires?assessmentType=INTEREST&page=0&size=20`

| Check | Pass |
|-------|------|
| Includes created questionnaire | [ ] |

### 5.3 Get questionnaire

`GET /questionnaires/{{questionnaireId}}`

| Check | Pass |
|-------|------|
| Returns metadata + version questions (may be empty) | [ ] |

### 5.4 Add questions to DRAFT version

`POST /questionnaire-versions/{{questionnaireVersionId}}/questions`

```json
{
  "questions": [
    {
      "questionId": "{{questionId}}",
      "displayOrder": 1,
      "isMandatory": true,
      "sectionCode": "SECTION_1",
      "sectionName": "Interest Discovery",
      "conditionConfig": null
    }
  ]
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |
| Non-APPROVED / inactive question → `VALIDATION_ERROR` | [ ] |

### 5.5 Reorder questions

`PUT /questionnaire-versions/{{questionnaireVersionId}}/questions/reorder`

```json
{
  "questionIds": ["{{questionId}}"]
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 5.6 Preview

`GET /questionnaire-versions/{{questionnaireVersionId}}/preview?languageId={{languageId}}`

| Check | Pass |
|-------|------|
| Ordered localized text/options returned | [ ] |
| Version status still DRAFT (no mutation) | [ ] |

### 5.7 List versions

`GET /questionnaires/{{questionnaireId}}/versions`

| Check | Pass |
|-------|------|
| Shows v1 DRAFT + `questionCount` ≥ 1 | [ ] |

### 5.8 Publish version

`POST /questionnaire-versions/{{questionnaireVersionId}}/publish`

```json
{
  "versionNotes": "Approved for Sprint 2 MVP"
}
```

| Check | Pass |
|-------|------|
| Status → `PUBLISHED`; `publishedAt` set | [ ] |
| Save as `publishedQuestionnaireVersionId` | [ ] |
| Audit recorded (publish) | [ ] |

### 5.9 Publish alias

`POST /questionnaires/{{questionnaireId}}/publish`

| Check | Pass |
|-------|------|
| Works for remaining DRAFT **or** returns `INVALID_STATE` if none | [ ] |

### 5.10 Structural edit on published version rejected

`POST /questionnaire-versions/{{publishedQuestionnaireVersionId}}/questions` (same body as 5.4)

| Check | Pass |
|-------|------|
| `422` / `QUESTIONNAIRE_PUBLISHED` or `INVALID_STATE` | [ ] |

### 5.11 Create new version

`POST /questionnaires/{{questionnaireId}}/versions`

```json
{
  "versionNotes": "v2 draft"
}
```

| Check | Pass |
|-------|------|
| New DRAFT version id returned | [ ] |

### 5.12 Consolidated PUT (optional)

`PUT /questionnaires/{{questionnaireId}}`

```json
{
  "name": "Interest Discovery Updated",
  "description": "Updated via consolidated PUT",
  "shuffleQuestions": false,
  "questions": [
    {
      "questionId": "{{questionId}}",
      "displayOrder": 1,
      "isMandatory": true,
      "sectionCode": "SECTION_1",
      "sectionName": "Interest Discovery",
      "conditionConfig": null
    }
  ]
}
```

| Check | Pass |
|-------|------|
| Applies to DRAFT or auto-creates next DRAFT if latest PUBLISHED | [ ] |

### 5.13 Clone

`POST /questionnaires/{{questionnaireId}}/clone`

```json
{
  "code": "INT_QN_CLONE",
  "name": "Interest Discovery Clone"
}
```

| Check | Pass |
|-------|------|
| New questionnaire + DRAFT v1; source unchanged | [ ] |
| Save `clonedQuestionnaireId` | [ ] |

### 5.14 Suspend

`POST /questionnaire-versions/{{publishedQuestionnaireVersionId}}/suspend`

| Check | Pass |
|-------|------|
| `200`; historical version preserved; audit | [ ] |

### 5.15 Retire

> Prefer retiring a **non-primary** published version, or clone’s published version, so you still have a PUBLISHED version for assessment binding.

`POST /questionnaire-versions/{{somePublishedVersionId}}/retire`

| Check | Pass |
|-------|------|
| Version → `ARCHIVED` (or equivalent); audit | [ ] |

---

## 6. Assessments & execution config (EPIC-09 / FFG §17)

### 6.1 Create assessment master

`POST /assessments`

```json
{
  "code": "INTEREST",
  "name": "Interest Assessment",
  "description": "Interest master"
}
```

| Check | Pass |
|-------|------|
| `200` + save `assessmentId` | [ ] |
| Duplicate tenant code → `DUPLICATE_RESOURCE` | [ ] |

### 6.2 List assessments

`GET /assessments?page=0&size=20`

| Check | Pass |
|-------|------|
| Includes INTEREST master | [ ] |

### 6.3 Create assessment version

`POST /assessments/{{assessmentId}}/versions`

```json
{
  "questionnaireVersionId": "{{publishedQuestionnaireVersionId}}",
  "versionNumber": 1
}
```

| Check | Pass |
|-------|------|
| `200` + save `assessmentVersionId` | [ ] |
| DRAFT questionnaire version rejected | [ ] |

### 6.4 Upsert execution configuration

`POST /assessment-versions/{{assessmentVersionId}}/configuration`

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

| Check | Pass |
|-------|------|
| `200`; default COUNT_UP honored | [ ] |
| Calling again upserts (no duplicate row) | [ ] |

---

## 7. IAR foundation (EPIC-12 lean)

### 7.1 Create IAR config

`POST /iar-logic-configs`

```json
{
  "code": "IAR_2026",
  "name": "IAR Logic 2026",
  "description": "Sprint 2 foundation"
}
```

| Check | Pass |
|-------|------|
| `200` + save `iarLogicConfigId` | [ ] |

### 7.2 List IAR configs

`GET /iar-logic-configs`

| Check | Pass |
|-------|------|
| Includes created config | [ ] |

### 7.3 Create IAR version

`POST /iar-logic-configs/{{iarLogicConfigId}}/versions`

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

| Check | Pass |
|-------|------|
| `200` + save `iarLogicVersionId` | [ ] |

### 7.4 Update DRAFT version

`PUT /iar-logic-versions/{{iarLogicVersionId}}`

```json
{
  "logicDefinition": {
    "aspirationWeight": 0.20,
    "interestWeight": 0.30,
    "aptitudeWeight": 0.30,
    "realityWeight": 0.20,
    "rules": []
  }
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 7.5 Validate — success

`POST /iar-logic-versions/{{iarLogicVersionId}}/validate`

| Check | Pass |
|-------|------|
| `data.valid == true`, `errors: []` | [ ] |

### 7.6 Validate — failure

Update weights so sum ≠ 1 (e.g. all `0.5`), then validate again.

| Check | Pass |
|-------|------|
| `data.valid == false` with errors | [ ] |
| Restore valid weights before publish | [ ] |

### 7.7 Publish IAR version

`POST /iar-logic-versions/{{iarLogicVersionId}}/publish`

| Check | Pass |
|-------|------|
| Status → `PUBLISHED`; audit | [ ] |
| Further PUT → `INVALID_STATE` | [ ] |
| Publish with invalid weights blocked | [ ] |

---

## 8. Report template skeleton (EPIC-14 lean)

### 8.1 Create report template

`POST /report-templates`

```json
{
  "code": "STUDENT_REPORT",
  "name": "Student Career Report",
  "templateType": "STUDENT"
}
```

| Check | Pass |
|-------|------|
| `200` + save `reportTemplateId` | [ ] |
| DRAFT version v1 created → save `reportTemplateVersionId` | [ ] |

### 8.2 List templates

`GET /report-templates`

| Check | Pass |
|-------|------|
| Includes created template | [ ] |

### 8.3 Create another version (optional)

`POST /report-templates/{{reportTemplateId}}/versions`

```json
{
  "templateConfig": {
    "sections": ["SUMMARY", "SCORES", "CAREERS"]
  },
  "versionNotes": "v2 draft"
}
```

| Check | Pass |
|-------|------|
| New DRAFT version id returned | [ ] |

### 8.4 Publish template version

`POST /report-template-versions/{{reportTemplateVersionId}}/publish`

| Check | Pass |
|-------|------|
| Status → `PUBLISHED` | [ ] |

---

## 9. Assessment configuration groups (EPIC-09 / FFG §18)

### 9.1 Create group

`POST /assessment-configuration-groups`

```json
{
  "code": "MH_GRADE10_2026",
  "name": "Maharashtra Grade 10 Assessment Group",
  "description": "Sprint 2 demo group",
  "academicYear": "2026-27",
  "assessmentCycle": "Cycle 1"
}
```

| Check | Pass |
|-------|------|
| Status `DRAFT`; save `acgId` | [ ] |
| Audit CREATE | [ ] |

### 9.2 List groups

`GET /assessment-configuration-groups?status=DRAFT&academicYear=2026-27&page=0&size=20`

| Check | Pass |
|-------|------|
| Includes group | [ ] |

### 9.3 Update group metadata

`PUT /assessment-configuration-groups/{{acgId}}`

```json
{
  "name": "MH Grade 10 2026 Updated",
  "description": "Updated",
  "academicYear": "2026-27",
  "assessmentCycle": "Cycle 1"
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 9.4 Add items

`POST /assessment-configuration-groups/{{acgId}}/items`

```json
{
  "items": [
    {
      "assessmentId": "{{assessmentId}}",
      "assessmentVersionId": "{{assessmentVersionId}}",
      "questionnaireVersionId": "{{publishedQuestionnaireVersionId}}",
      "iarLogicVersionId": "{{iarLogicVersionId}}",
      "scoringRuleVersionId": null,
      "assessmentLanguageId": "{{languageId}}",
      "isRequired": true,
      "displayOrder": 1
    }
  ]
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 9.5 Add outputs

`POST /assessment-configuration-groups/{{acgId}}/outputs`

```json
{
  "reportTemplateVersionId": "{{reportTemplateVersionId}}",
  "outputLanguageId": "{{languageId}}",
  "outputConfig": {
    "showScores": true,
    "showRecommendations": true,
    "showCounsellorFeedback": true
  }
}
```

| Check | Pass |
|-------|------|
| `200` | [ ] |

### 9.6 Assign to org unit (or cluster / user)

`POST /assessment-configuration-groups/{{acgId}}/assignments`  
**Auth:** ADMINISTRATOR (or scoped SUB_ADMIN)

```json
{
  "assignmentType": "ORG_UNIT",
  "assignmentId": "{{orgUnitId}}",
  "effectiveFrom": "2026-07-01T00:00:00Z",
  "effectiveTo": "2027-03-31T23:59:59Z"
}
```

Also test:

```json
{
  "assignmentType": "USER",
  "assignmentId": "{{studentId}}",
  "effectiveFrom": "2026-07-01T00:00:00Z",
  "effectiveTo": null
}
```

| Check | Pass |
|-------|------|
| `200` + save `acgAssignmentId` | [ ] |
| Audit ASSIGN | [ ] |

### 9.7 Get group detail

`GET /assessment-configuration-groups/{{acgId}}`

| Check | Pass |
|-------|------|
| Returns items + outputs + assignments | [ ] |

### 9.8 Activate

`POST /assessment-configuration-groups/{{acgId}}/activate`

| Check | Pass |
|-------|------|
| Status → `ACTIVE` | [ ] |
| Activate with 0 items → error | [ ] |
| Audit STATUS_CHANGE / activate | [ ] |

### 9.9 Resolve (admin debug)

`GET /assessment-configuration-groups/resolve?studentId={{studentId}}&assessmentId={{assessmentId}}`

| Check | Pass |
|-------|------|
| Returns winning group + item + `matchedAssignmentType` | [ ] |

### 9.10 Student active configuration

`GET /students/{{studentId}}/active-assessment-configuration?assessmentId={{assessmentId}}`  
**Auth:** self explorer / counsellor / admin / sub-admin

| Check | Pass |
|-------|------|
| Returns ACTIVE group with items + outputs | [ ] |
| Explorer accessing another student → `ACCESS_DENIED` | [ ] |

### 9.11 Resolution priority — USER beats ORG_UNIT

1. Assign same assessment group set: ORG_UNIT for student’s org **and** USER for student (two groups or same group assignments — use two ACTIVE groups with different codes if needed).
2. Resolve for student.

| Check | Pass |
|-------|------|
| `matchedAssignmentType` = `USER` when both match | [ ] |

### 9.12 Delete assignment

`DELETE /assessment-configuration-groups/{{acgId}}/assignments/{{acgAssignmentId}}`

| Check | Pass |
|-------|------|
| `200`; assignment removed / inactive | [ ] |

---

## 10. End-to-end demo script (IF2AF0203 §107)

Run in order; all should pass:

| # | Step | Pass |
|---|------|------|
| 1 | Admin creates questions → options → translations | [ ] |
| 2 | Admin creates questionnaire → version → adds questions | [ ] |
| 3 | Admin previews questionnaire | [ ] |
| 4 | Admin publishes questionnaire version | [ ] |
| 5 | Admin creates assessment version + COUNT_UP config | [ ] |
| 6 | Admin creates IAR + report template foundations and publishes | [ ] |
| 7 | Admin creates config group → items → outputs → assign → activate | [ ] |
| 8 | Resolve returns expected group for student | [ ] |

---

## 11. Negative / guardrail tests

| # | Scenario | Endpoint | Expected | Pass |
|---|----------|----------|----------|------|
| 1 | Unapproved question on DRAFT attach | `POST .../questionnaire-versions/{id}/questions` | `VALIDATION_ERROR` | [ ] |
| 2 | Soft-delete question on PUBLISHED questionnaire | `DELETE /questions/{id}` | `QUESTION_IN_USE` / 409 | [ ] |
| 3 | Delete option on published version question | `PUT /questions/{id}` removing options | `QUESTION_IN_PUBLISHED_VERSION` | [ ] |
| 4 | Edit published questionnaire structure | add/reorder on PUBLISHED version | `QUESTIONNAIRE_PUBLISHED` / `INVALID_STATE` | [ ] |
| 5 | Cross-tenant access | any tenant-scoped GET/PUT with foreign id | `ACCESS_DENIED` / 404 | [ ] |
| 6 | Counsellor/explorer mutating question bank | `POST /questions` as explorer | 403 | [ ] |
| 7 | Duplicate question code | `POST /questions` same code | `DUPLICATE_RESOURCE` | [ ] |
| 8 | Activate empty config group | `POST .../activate` with no items | business error | [ ] |
| 9 | IAR publish without valid weights | `POST .../publish` | blocked / `IAR_VALIDATION_FAILED` | [ ] |
| 10 | Assessment version with unpublished questionnaire | `POST .../versions` | `VALIDATION_ERROR` / `INVALID_STATE` | [ ] |

---

## 12. Full endpoint inventory checklist

Tick when happy-path tested at least once.

### Config / language

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/languages` |
| [ ] | POST | `/languages` |
| [ ] | GET | `/configuration-groups` |
| [ ] | POST | `/configuration-groups` |
| [ ] | GET | `/configurations` |
| [ ] | POST | `/configurations` |
| [ ] | PUT | `/configurations/{id}` |

### Question bank

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/question-categories` |
| [ ] | POST | `/question-categories` |
| [ ] | PUT | `/question-categories/{categoryId}` |
| [ ] | GET | `/questions` |
| [ ] | POST | `/questions` |
| [ ] | GET | `/questions/{questionId}` |
| [ ] | PUT | `/questions/{questionId}` |
| [ ] | DELETE | `/questions/{questionId}` |
| [ ] | POST | `/questions/{questionId}/options` |
| [ ] | PUT | `/questions/{questionId}/translations` |
| [ ] | GET | `/questions/{questionId}/rules` |
| [ ] | POST | `/questions/{questionId}/rules` |
| [ ] | DELETE | `/questions/{questionId}/rules/{ruleId}` |
| [ ] | PATCH | `/questions/{questionId}/status` |

### Questionnaires

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/questionnaires` |
| [ ] | POST | `/questionnaires` |
| [ ] | GET | `/questionnaires/{id}` |
| [ ] | PUT | `/questionnaires/{id}` |
| [ ] | POST | `/questionnaires/{id}/versions` |
| [ ] | GET | `/questionnaires/{id}/versions` |
| [ ] | POST | `/questionnaires/{id}/clone` |
| [ ] | POST | `/questionnaires/{id}/publish` |
| [ ] | POST | `/questionnaire-versions/{versionId}/questions` |
| [ ] | PUT | `/questionnaire-versions/{versionId}/questions/reorder` |
| [ ] | GET | `/questionnaire-versions/{versionId}/preview` |
| [ ] | POST | `/questionnaire-versions/{versionId}/publish` |
| [ ] | POST | `/questionnaire-versions/{versionId}/suspend` |
| [ ] | POST | `/questionnaire-versions/{versionId}/retire` |

### Assessments

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/assessments` |
| [ ] | POST | `/assessments` |
| [ ] | POST | `/assessments/{assessmentId}/versions` |
| [ ] | POST | `/assessment-versions/{versionId}/configuration` |

### Config groups + resolve

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/assessment-configuration-groups` |
| [ ] | POST | `/assessment-configuration-groups` |
| [ ] | GET | `/assessment-configuration-groups/{groupId}` |
| [ ] | PUT | `/assessment-configuration-groups/{groupId}` |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/items` |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/outputs` |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/assignments` |
| [ ] | DELETE | `/assessment-configuration-groups/{groupId}/assignments/{assignmentId}` |
| [ ] | POST | `/assessment-configuration-groups/{groupId}/activate` |
| [ ] | GET | `/assessment-configuration-groups/resolve` |
| [ ] | GET | `/students/{studentId}/active-assessment-configuration` |

### IAR + report templates

| Done | Method | Path |
|------|--------|------|
| [ ] | GET | `/iar-logic-configs` |
| [ ] | POST | `/iar-logic-configs` |
| [ ] | POST | `/iar-logic-configs/{id}/versions` |
| [ ] | PUT | `/iar-logic-versions/{versionId}` |
| [ ] | POST | `/iar-logic-versions/{versionId}/validate` |
| [ ] | POST | `/iar-logic-versions/{versionId}/publish` |
| [ ] | GET | `/report-templates` |
| [ ] | POST | `/report-templates` |
| [ ] | POST | `/report-templates/{id}/versions` |
| [ ] | POST | `/report-template-versions/{versionId}/publish` |

---

## 13. Sample curl template

```bash
BASE=http://localhost:8080/api/v1
TOKEN='paste-access-token'

curl -sS -X POST "$BASE/question-categories" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "assessmentType": "INTEREST",
    "code": "ANALYTICAL",
    "name": "Analytical Interest",
    "domainCode": "ANALYTICAL",
    "description": "Analytical domain"
  }' | jq .
```

---

## Sign-off

| Item | Status |
|------|--------|
| Happy-path inventory (§12) complete | [ ] |
| Demo script (§10) complete | [ ] |
| Negative tests (§11) complete | [ ] |
| Tester | |
| Date | |
| Notes / defects | |
