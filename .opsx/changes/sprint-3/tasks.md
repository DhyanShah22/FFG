# Implementation Tasks — Sprint 3

**Contract:** [api-contracts.md](./api-contracts.md) · **Design:** [design.md](./design.md) · **Spec:** [spec.md](./spec.md)

## 0. Preconditions

- [ ] Sprint 2 complete: ACTIVE config group + item + published questionnaire version (+ optional scoring/IAR versions on item)
- [ ] NGO V5–V6 applied; entities map to existing columns
- [ ] Seed via SQL or admin APIs: assessment masters, scoring rule version, sample benchmarks

## 1. Domain layer

- [ ] Entities/repos for V5 attempt/response/timing/history
- [ ] Entities/repos for V6 scoring_rules, scoring_rule_versions, benchmarks, domain_scores
- [ ] Enums: AttemptStatus, ScoreStatus, SubmissionReason, ScoringRuleVersionStatus, BenchmarkLabel

## 2. Services

- [ ] `AssessmentAttemptService` — start/resume/restart/expire/submit; snapshot all version IDs
- [ ] `ResponseService` — batch upsert with selectedOptionIds, text, numeric, isSkipped
- [ ] `QuestionTimingService` — `PUT /attempts/{id}/question-timings`
- [ ] `QuestionRuleEvaluator` — visibility/branching when loading questions
- [ ] Integrate `ConfigurationResolutionService` from Sprint 2
- [ ] `ScoringService`, `ScoringRuleService`, `BenchmarkService`
- [ ] `StudentAccessGuard`

## 3. Controllers

- [ ] `GET /assessments`
- [ ] `GET /students/{studentId}/assessments`
- [ ] `POST /assessments/{assessmentId}/start`
- [ ] `GET /attempts/{attemptId}`
- [ ] `PUT /attempts/{attemptId}/responses`
- [ ] `PUT /attempts/{attemptId}/question-timings`
- [ ] `POST /attempts/{attemptId}/submit`
- [ ] `GET /students/{studentId}/attempt-history`
- [ ] Scoring rules + versions + publish (FFG §20.1–20.3)
- [ ] `POST /scoring/calculate/{attemptId}`
- [ ] `GET /students/{studentId}/scores`
- [ ] Benchmarks GET/POST/PUT
- [ ] **Career clusters, careers, career_mappings CRUD** (EPIC-12)
- [ ] `activity_logs` on assessment submit
- [ ] Audit on scoring/IAR publish/retire

## 4. Security

- [ ] Attempt mutations owner-only (except admin)
- [ ] Path `{studentId}` matches JWT for CAREER_EXPLORER
- [ ] Counsellor read only for assigned students

## 5. Acceptance (Bruno)

- [ ] List assessments → start (questions inline or via GET) → save responses → save timings → submit
- [ ] Mandatory missing → error with questionIds
- [ ] allow_resume returns same attemptId
- [ ] allow_resume=false creates new attempt within policy
- [ ] max_attempts / reattempt_after_days enforced
- [ ] score_status PENDING→COMPLETED; GET scores works without manual calculate
- [ ] Benchmark with grade_config_id matches student grade
- [ ] Attempt history lists SUBMITTED and abandoned attempts
- [ ] Snapshotted scoring version used even if newer version published later
- [ ] **Flow `05`:** start returns attemptId + questions + timer config after config resolve
- [ ] **Flow `06`:** auto-save responses; submit blocked with missingQuestionIds until mandatory complete
- [ ] **Flow `07`:** MANUAL_SUBMIT + resume/restart paths per assessment_configurations
- [ ] **Flow `08` (partial):** submit ends at domain_scores — no recommendation/report in Sprint 3 response

## Packages

`com.antarang.cap.controller|service|repository|domain.entity|dto.*|security`
