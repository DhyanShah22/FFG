# Change Proposal: Sprint 4 — Recommendations, Reports, Integrations & Ops

**Status:** Documentation complete for implementation; code not started.

## Intent

Complete MVP delivery: career mapping and ranked recommendations, report generation (mock S3), counsellor feedback, lean notifications, audit + activity logging, basic external integration APIs, operational dashboards (including **Sub-Admin scope**), and UAT/release exit criteria.

## Document sources

| Source | Sections |
|--------|----------|
| FFG API Specification v1.0 | §22–28, §30 (Sprint 4 scope) |
| FFG Database Design v1.0 | Modules K6–K7 (recommendations), L (reports/feedback), M–O |
| **IF2AF0203-export.pdf** | Sprint 4 plan §111–114, EPIC-13–20, MVP cutline §96–97, audit §18, notification §17, Sub-Admin §4.4, demo §113 |
| Flow diagrams (reviewed) | **`08`** post-submit IAR → recommend → report chain, **`09`** recommendation logic (parallel I/A/R/A → rank), **`10`** RBAC + data-scope (401/403), **`11`** Sub-Admin scoped ops + blocks, **`14`** counsellor feedback visibility, **`15`** notification template→language→send→log, **`16`** integration auth + upsert + results, **`17`** audit vs activity split, **`18`** end-to-end MVP UAT path |
| Architecture (reviewed) | **`01`** platform module map (React PWA → Spring Boot → PostgreSQL/S3/email), **`02`** AWS deployment (CloudFront, EC2/Nginx, Docker Spring Boot, RDS, S3 reports, Secrets Manager, CloudWatch) |
| ERD (reviewed) | **`ERD 1`** logical overview; **`ERD 6`** recommendations/reports/feedback; **`ERD 7`** audit, notifications, integrations |
| Excluded | Sprint 1 auth/hierarchy PDFs |

## In-scope epics

| Epic | Priority | Notes |
|------|----------|-------|
| EPIC-12 | P0 | Career mapping **admin done Sprint 3**; IAR execution at recommend time |
| EPIC-13 | P0 | Recommendation engine (generate ranked runs) |
| EPIC-14 | P0 | Report template sections, generate/list/download reports |
| EPIC-15 | P1 | Counsellor feedback create/list/**update** |
| EPIC-16 | P1 | Student, facilitator, admin, **sub-admin** dashboards |
| EPIC-17 | P1 | Notification templates, send, logs (email-first / simulated) |
| EPIC-18 | P1 | Integration clients, upsert student, fetch results/report, **integration-events list** |
| EPIC-19 | P0 | Audit AOP + **audit_logs** and **activity_logs** APIs |
| EPIC-20 | P0 | UAT, hardening, training, go-live (tasks/checklist) |

## MVP priority cutline (IF2AF0203 §96–97)

**Must-have:** login, RBAC, config groups, question bank, questionnaire versioning, assessment execution, count-up timer, scoring, IAR versioning, recommendation generation, report generation, audit/traceability.

**Should-have (Sprint 4 lean — implement if capacity):** counsellor feedback, basic dashboards, basic email notifications, basic external APIs, question-level timing (may land Sprint 3).

**Phase 2 if timeline slips:** advanced dashboards, SMS, Salesforce/Glific, Guest, Data Analyst, approval workflows.

## Explicit Phase-2 deferrals (FFG §31)

| Area | Reason |
|------|--------|
| EPIC-21 Data Analyst role & analytics APIs | Phase 2 |
| EPIC-22 Guest user / demo | Phase 2 |
| EPIC-23 Salesforce / Glific / SMS depth | Phase 2 — MVP basic REST only |
| Advanced rule builder / approval workflows | Phase 2 |

## Dependencies

- Sprint 3: SUBMITTED attempts with `domain_scores`, `score_status=COMPLETED`, snapshotted IAR/scoring/report template versions on attempt
- Sprint 2: published report template versions on group outputs; IAR versions on group items
- **NGO V6–V8 tables exist** — implement services/APIs only; no duplicate domain migrations unless approved V17+ gap

## Primary contract

[spec.md](./spec.md) · [design.md](./design.md) · [tasks.md](./tasks.md) · [api-contracts.md](./api-contracts.md)
