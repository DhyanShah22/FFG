# Specification: Sprint 3 — Assessment Lifecycle & Scoring

**Sources:** FFG API Specification v1.0 (§19–20, §30), Database Design (Modules I–J, K3–K5), **IF2AF0203-export.pdf**, flow diagrams **`05`–`08`** (MVP: submit stops at scoring; recommend/report = Sprint 4), ERD **`5`**, NGO Flyway **V5–V6**.  
**Contracts:** [api-contracts.md](./api-contracts.md)

> **NGO merge note:** Attempt/scoring tables exist in V5/V6. Align entities to NGO columns (`submission_reason`, `score_status`, `assessment_response_options`, etc.) — not simplified design-only names.

## Requirements

### Attempt lifecycle (EPIC-10 / FFG §19 / Module I)

* The system SHALL enforce at most one `IN_PROGRESS` attempt per `(student_id, assessment_id)` (service guard + index as applicable).
* Starting an assessment SHALL resolve the active configuration group **item** for the assessment master (Sprint 2 priority rules) and **snapshot** on the attempt:
  * `configuration_group_id`, `assessment_version_id`, `questionnaire_version_id`
  * `scoring_rule_version_id`, `iar_logic_version_id`, `report_template_version_id`
  * `assessment_language_id`, `output_language_id`
  * timer/policy fields from snapshotted `assessment_configurations` / group context
* **`POST /assessments/{assessmentId}/start`** request MAY include `studentId` (default JWT) and optional **`assessmentLanguageId`** override (FFG §19.2); default from resolved group item.
* Start response SHOULD return attempt metadata **and** localized **`questions[]`** (options, mandatory, `conditionConfig`) in one payload (FFG §19.2). Alternative: immediate `GET /attempts/{attemptId}` after start.
* **`GET /api/v1/students/{studentId}/assessments`** lists assessments with resolvable ACTIVE config (FFG §19.1).
* **`GET /api/v1/assessments`** lists assessment masters for admin/reference.
* Evaluate **`allow_resume`**: if true and `IN_PROGRESS` exists → return same attempt; if false → close prior attempt (`ABANDONED`/`RESTARTED` via `submission_reason` or status history) and create new if within **`max_attempts`**, **`allow_reattempt`**, **`reattempt_after_days`**.
* Timer MVP: **`COUNT_UP`** default; honor `show_timer_to_student`, `auto_submit_enabled`, `max_duration_minutes` from snapshotted config.
* Persist responses via **`PUT /attempts/{attemptId}/responses`** with `selectedOptionIds`, `responseText`, `responseNumeric`, `isSkipped` (FFG §19.3). Store in `assessment_responses` + `assessment_response_options`. Frontend SHOULD auto-save on each answer/skip (diagram **`06`**).
* Per-question timing: **`PUT /attempts/{attemptId}/question-timings`** (FFG §19.4) **and/or** embedded timing in responses batch — IF2AF0203 MVP cutline §96 lists question-level timing as **should-have** (implement if capacity; otherwise defer after core submit path).
* Submit (**FFG §19.5**):
  * Validate mandatory questions on snapshotted questionnaire version unless `completion_rule=ALLOW_PARTIAL`
  * Set `status=SUBMITTED`, `submitted_at`, `elapsed_seconds`, **`submission_reason`** (`MANUAL_SUBMIT`|`AUTO_SUBMIT`|`ADMIN_FORCE_SUBMIT`|`SESSION_EXPIRED`|`RESTARTED`)
  * Write **`attempt_status_history`**
  * Set **`score_status=PENDING`**, then **`COMPLETED`** (or `FAILED`) after scoring
  * Trigger scoring synchronously (MVP); write **`activity_logs`** `ASSESSMENT_SUBMIT` (IF2AF0203 §18 / diagram **`17`**)
  * **MVP boundary:** diagram **`08`** shows IAR → recommendations → report on same submit chain — **defer those to Sprint 4** explicit generate APIs; Sprint 3 submit ends at `score_status=COMPLETED`
* **`GET /students/{studentId}/attempt-history`** with optional `assessmentId` filter (FFG §19.6).
* Evaluate **`question_rules`** / questionnaire **`condition_config`** when rendering questions during attempt (depends on Sprint 2 rules).
* Support optional **`is_admin_test_attempt`** and **`exclude_from_analytics`** flags on attempts (NGO V5).

### Career masters & mappings (EPIC-12 / IF2AF0203 Sprint 3)

* Admin SHALL CRUD **`career_clusters`**, **`careers`**, **`career_mappings`** (NGO V6) — Antarang inputs items 14–15 target Sprint 3/4; **admin APIs in Sprint 3**, recommendation generate in Sprint 4.
* NGO mapping columns: `interest_domain`, `aptitude_domain`, `reality_factor`, `aspiration_factor`, `min_score`, `max_score`, `mapping_weight`, `mapping_config`.
* Audit **`scoring rule publish/retire`** and **`IAR publish/retire`** per IF2AF0203 §18.

### Scoring (EPIC-11 / FFG §20 / Module J)

* Maintain versioned **`scoring_rules`** (per `assessment_id`) and **`scoring_rule_versions`** with **`rule_definition`** JSONB (DRAFT/PUBLISHED).
* **`POST /scoring-rule-versions/{versionId}/publish`** (FFG §20.3) — published immutable.
* Score calculation SHALL use **`scoring_rule_version_id` snapshotted on attempt** — never "latest".
* **`rule_definition`** SHALL support domain aggregation (e.g. SUM by question codes / categories) per FFG §20.2.
* **`POST /scoring/calculate/{attemptId}`** for admin/system recalc; idempotent unless `force=true` (FFG §20.4).
* Persist **`domain_scores`**: `raw_score`, `normalized_score`, **`benchmark_id`**, per `(attempt_id, domain_code)` unique.
* **`benchmarks`**: tenant + **`assessment_id`** + **`domain_code`** + optional **`grade_config_id`**, **`age_group_config_id`**, **`benchmark_label`** (`LOW`|`MEDIUM`|`HIGH`), **`interpretation`** (FFG §20.5).
* **`GET /students/{studentId}/scores`** — latest or by `attemptId` (FFG §20.6).

### Traceability (FFG §30 / diagram 17)

* Every attempt SHALL permanently record all configuration version IDs used at start for audit, scoring, recommendation, and report pipelines.

### Authorization

* Explorers act only on own `studentId` unless Admin.
* Counsellors read assigned students' assessments/attempts/scores/history.
* Scoring-rule and benchmark mutations: `ADMINISTRATOR` only.
* Facilitator demo/test start per FFG auth matrix where enabled.

## FFG alignment notes

| FFG § | Item | Handling |
|-------|------|----------|
| 19.2 | Inline questions on start | Required in response or immediate GET |
| 19.4 | Question timings endpoint | Dedicated PUT required |
| 19.5 | submission_reason, score_status | Required on submit |
| 20.5 | Benchmark grade/age dimensions | Required |
| IF2AF0203 §18 | Assessment submit → activity_logs | Required |
| IF2AF0203 §96 | Question-level timing | Should-have if timeline tight |

## Sprint 3 demo script (IF2AF0203 §110)

1. Student login → sees assigned assessment  
2. Start → config group resolved → questionnaire in selected language  
3. Count-up timer → save responses → submit  
4. Mandatory validation → scoring → domain scores + benchmarks  
5. Admin verifies attempt traceability (questionnaire/scoring/IAR/template/group versions)  
6. Admin maintains career clusters/careers/mappings for upcoming recommendations  

## Scenarios

### Start — new attempt

**GIVEN** explorer has resolvable ACTIVE config item and no IN_PROGRESS attempt  
**WHEN** `POST /assessments/{id}/start`  
**THEN** attempt `IN_PROGRESS` with all version snapshots and optional inline questions.

### Start — resume

**GIVEN** `allow_resume=true` and IN_PROGRESS attempt  
**WHEN** start called  
**THEN** same `attemptId` returned.

### Start — restart

**GIVEN** `allow_resume=false`, IN_PROGRESS exists, attempts remaining  
**WHEN** start called  
**THEN** prior attempt closed; new IN_PROGRESS created; history row written.

### Save responses

**GIVEN** IN_PROGRESS attempt owned by caller  
**WHEN** `PUT /attempts/{id}/responses`  
**THEN** responses + options upserted.

### Save question timing

**GIVEN** IN_PROGRESS attempt  
**WHEN** `PUT /attempts/{id}/question-timings`  
**THEN** row in `assessment_question_timings`.

### Submit — missing mandatory

**GIVEN** mandatory unanswered  
**WHEN** submit  
**THEN** `422 MANDATORY_RESPONSE_MISSING` / `MANDATORY_UNANSWERED` with question ids; status stays IN_PROGRESS.

### Submit — success

**GIVEN** all mandatory answered  
**WHEN** submit  
**THEN** SUBMITTED, score_status PENDING→COMPLETED, domain_scores persisted.

### Scoring with grade band

**GIVEN** benchmark row with `grade_config_id` matching student profile  
**WHEN** calculate runs  
**THEN** correct `benchmark_id` linked on domain_score.

### Max attempts exceeded

**GIVEN** `max_attempts` reached and no reattempt window  
**WHEN** start  
**THEN** business error; no new attempt.
