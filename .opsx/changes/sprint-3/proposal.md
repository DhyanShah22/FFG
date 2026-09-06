# Change Proposal: Sprint 3 — Assessment Execution & Scoring

**Status:** Documentation complete for implementation; code not started.

## Intent

Enable Career Explorers to start assigned assessments, save responses with count-up timing, resume or restart per policy, submit with mandatory validation, and compute versioned domain scores against benchmarks — with full configuration version traceability on each attempt.

## Document sources

| Source | Sections |
|--------|----------|
| FFG API Specification v1.0 | §19–20, §30 (Sprint 3 scope) |
| FFG Database Design v1.0 | Modules I (execution), J (scoring/benchmarks), K3–K5 (career masters — EPIC-12 continuation) |
| **IF2AF0203-export.pdf** | Sprint 3 plan §108–110, EPIC-10/11/12/19, MVP cutline §96 (question-level timing = should-have), Antarang inputs (scoring/benchmark by 29 Jul) |
| Flow diagrams (reviewed) | **`05`** start sequence (resolve config → questionnaire → create attempt), **`06`** execution loop (auto-save, mandatory submit validation), **`07`** count-up timer + resume/restart, **`08`** submit→scoring (MVP stops at scoring; IAR/recommend/report = Sprint 4) |
| ERD (reviewed) | **`ERD 5`** config groups + attempts/responses/timings; **`ERD 6`** scoring/benchmarks (career tables admin in this sprint) |
| Cross-sprint context | `04` admin pipeline (scoring-rule publish steps are Sprint 3 APIs, bindable on group items from Sprint 2), `13` resolution |
| Excluded | Sprint 1 auth/hierarchy PDFs |

## In-scope epics (IF2AF0203 §94)

| Epic | Scope |
|------|--------|
| EPIC-10 | Assessment execution: list, start, save, timings, submit, attempt history |
| EPIC-11 | Scoring rules, versions, calculate, benchmarks |
| EPIC-12 (**continuation**) | **Career clusters, careers, career_mappings admin CRUD** — recommendation *generation* remains Sprint 4 |
| EPIC-19 | Traceability on attempts; audit for scoring publish/retire; activity log on assessment submit |

## Out of scope

- Recommendation ranking, PDF reports, counsellor feedback (Sprint 4)
- Question bank / questionnaire authoring (Sprint 2)
- Career **recommendation engine** (generate ranked runs — Sprint 4)
- Phase 2 analytics exports

## Dependencies

- Sprint 2: ACTIVE configuration groups with valid items; published questionnaire versions; optional published scoring/IAR versions bound on items
- **NGO Flyway V5–V6 tables exist** — implement services/APIs only; no duplicate migrations
- Scoring rule admin binds `scoring_rule_version_id` on group items (Sprint 2) and snapshots on attempt (this sprint)

## Primary contract

[spec.md](./spec.md) · [design.md](./design.md) · [tasks.md](./tasks.md) · [api-contracts.md](./api-contracts.md)
