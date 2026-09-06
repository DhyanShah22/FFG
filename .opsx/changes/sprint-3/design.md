# System Design: Sprint 3 — Execution & Scoring

**Implement against:** [api-contracts.md](./api-contracts.md) · **Requirements:** [spec.md](./spec.md)

## Schema strategy (NGO monorepo)

Map to existing NGO migrations — **no new Flyway for Sprint 3 tables**:

| NGO migration | Tables |
|---------------|--------|
| **V4** (read) | `assessments`, `assessment_versions`, `assessment_configurations` |
| **V5** | `assessment_attempts`, `assessment_responses`, `assessment_response_options`, `assessment_question_timings`, `attempt_status_history` |
| **V6** | `scoring_rules`, `scoring_rule_versions`, `benchmarks`, `domain_scores`, **`career_clusters`**, **`careers`**, **`career_mappings`** |

## Key entities (NGO column names)

### `assessment_attempts` (V5 / Module I1)

| Column | Notes |
|--------|-------|
| tenant_id, student_id, assessment_id, assessment_version_id | FKs |
| configuration_group_id | resolved group |
| questionnaire_version_id, iar_logic_version_id, scoring_rule_version_id, report_template_version_id | snapshots |
| assessment_language_id, output_language_id | language snapshots |
| status | IN_PROGRESS, SUBMITTED, EVALUATED, EXPIRED, CANCELLED |
| timer_mode | COUNT_UP default MVP |
| started_at, submitted_at, evaluated_at, elapsed_seconds | |
| submission_reason | MANUAL_SUBMIT, AUTO_SUBMIT, ADMIN_FORCE_SUBMIT, SESSION_EXPIRED, RESTARTED |
| score_status | PENDING → COMPLETED (or FAILED) |
| is_admin_test_attempt, exclude_from_analytics | flags |

**Constraint:** at most one IN_PROGRESS per (student_id, assessment_id) — enforce in service.

### Responses & timings (V5)

**`assessment_responses`:** `response_text`, `response_numeric`, `response_json`, `is_skipped`, `answered_at`.

**`assessment_response_options`:** links response → selected `option_id`(s) for RADIO/CHECKBOX.

**`assessment_question_timings`:** `started_at`, `ended_at`, `elapsed_seconds` per question.

**`attempt_status_history`:** `old_status`, `new_status`, `changed_by`, `remarks`.

### Scoring (V6 / Module J)

**`scoring_rules`:** tenant, **assessment_id**, code, name, status, current_version_id.

**`scoring_rule_versions`:** `rule_definition` JSONB; status DRAFT/PUBLISHED/ARCHIVED.

Example `rule_definition`:

```json
{
  "domains": [
    {
      "domainCode": "ANALYTICAL",
      "questionCodes": ["INT_Q001", "INT_Q002"],
      "aggregation": "SUM"
    }
  ]
}
```

**`benchmarks`:** assessment_id, domain_code, grade_config_id, age_group_config_id, min_score, max_score, benchmark_label, interpretation.

**`domain_scores`:** attempt_id, student_id, assessment_id, domain_code, raw_score, normalized_score, benchmark_id.

## Services

```
AssessmentAttemptService     start / resume / restart / expire / submit
ResponseService              batch upsert responses + options
QuestionTimingService        PUT .../question-timings
QuestionRuleEvaluator        VISIBILITY/BRANCHING from Sprint 2 rules + condition_config
ConfigurationResolutionService  (from Sprint 2)
ScoringService                 sync on submit + admin calculate
ScoringRuleService             version CRUD + publish
BenchmarkService               CRUD + band matching
CareerService                  clusters, careers, mappings CRUD (EPIC-12)
StudentAccessGuard             self vs counsellor vs admin
ActivityLogService             ASSESSMENT_SUBMIT on submit (IF2AF0203 §18)
```

## Start algorithm

Aligned with **`05-student-assessment-start-sequence.png`** (React PWA → Spring Boot API orchestration):

1. Authorize student (self) / admin test / facilitator demo per RBAC.
2. **`ConfigurationResolutionService`**: resolve active configuration group for student → fetch group **item** metadata (questionnaire, scoring, IAR, assessment language, template version refs).
3. **`QuestionnaireService`**: load published questionnaire version — questions, options, translations, rules.
4. **`AssessmentAttemptService`**: create attempt; persist all version FK snapshots + timer/policy fields from `assessment_configurations`.
5. If IN_PROGRESS exists:
   - `allow_resume=true` → return existing + questions (**`07-count-up-timer-flow.png`** resume path)
   - else mark interrupted (`submission_reason=RESTARTED` / status history) → new attempt if policy allows (**restart path** when `restart_on_interruption=true`)
6. Validate `max_attempts`, `allow_reattempt`, `reattempt_after_days` against prior attempts.
7. Return **attemptId + localized questions + count-up timer config** (`show_timer_to_student`, `max_duration_minutes`, etc.).

## Execution loop (during attempt)

From **`06-student-assessment-execution-flow.png`**:

* Frontend displays count-up timer; backend records `started_at` on first start.
* Per question: timing start → student answers/skips → **response auto-saved** via `PUT .../responses` (and optional `PUT .../question-timings`).
* On submit click: validate mandatory → if incomplete return **`422`** with missing question ids (client re-shows questions) → else proceed.
* Backend sets `submitted_at`, calculates `elapsed_seconds`, `status=SUBMITTED`, triggers scoring.

## Submit + scoring pipeline (MVP boundary)

**Diagram `08` shows a full synchronous chain through report generation.** MVP **splits by sprint**:

| Step | Sprint |
|------|--------|
| Validate mandatory → SUBMITTED + `elapsed_seconds` | **3** |
| Scoring → `domain_scores` + benchmarks | **3** |
| IAR logic → recommendation run | **4** (`POST /recommendations/generate`) |
| Report → S3 + `generated_reports` | **4** (`POST /reports/generate`) |

Sprint 3 submit pipeline:

1. Validate IN_PROGRESS, not expired (if max_duration enforced).
2. Mandatory check against snapshotted questionnaire_questions unless ALLOW_PARTIAL.
3. Set SUBMITTED, timestamps, **`submission_reason`** (`MANUAL_SUBMIT` default per **`07-count-up-timer-flow.png`**), `score_status=PENDING`; history row.
4. `ScoringService.calculate(attemptId)`:
   - Load responses + selected options
   - Load snapshotted `rule_definition`
   - Aggregate per domain_code
   - Match benchmark (grade/age when present on student profile)
   - Upsert domain_scores
5. Set score_status=COMPLETED, evaluated_at; on failure score_status=FAILED.
6. Write **`activity_logs`** `ASSESSMENT_SUBMIT` with attempt + version metadata (IF2AF0203 §18 / diagram **`17`** — submit = Activity, not audit_logs).

## Scoring MVP algorithm

1. For each domain in `rule_definition.domains`, sum option score_values (or apply configured aggregation).
2. Find matching benchmark row: tenant + assessment_id + domain_code + optional grade/age configs.
3. Set normalized_score if formula defined (MVP may equal raw_score).
4. Upsert domain_scores with benchmark_id.

## ERD alignment

**`ERD 5`** confirms attempt snapshot columns (`configuration_group_id`, all version FKs, `submission_reason`, `score_status`) and child tables `assessment_responses`, `assessment_response_options`, `assessment_question_timings`, `attempt_status_history`.

## Packages

```
controller/ AssessmentController, AttemptController, ScoringController,
            BenchmarkController, ScoringRuleController, CareerController,
            CareerMappingController
service/    (see above)
security/   StudentAccessGuard, attempt ownership checks
```

## Error codes (Sprint 3)

| errorCode | When |
|-----------|------|
| ATTEMPT_ACTIVE | Conflicting IN_PROGRESS |
| ATTEMPT_NOT_IN_PROGRESS | Mutate/submit wrong status |
| ATTEMPT_EXPIRED | Over max duration |
| MAX_ATTEMPTS_EXCEEDED | Policy block |
| MANDATORY_RESPONSE_MISSING | Submit blocked (FFG §6) |
| CONFIGURATION_NOT_FOUND | Cannot start |
| SCORING_RULE_MISSING | Null/unpublished snapshot |
| INVALID_STATE | Wrong scoring version edit |

## Indexes (already in V5/V6)

Use existing indexes on `assessment_attempts(student_id, status)`, `domain_scores(attempt_id)`, etc.
