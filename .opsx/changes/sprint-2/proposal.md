# Change Proposal: Sprint 2 — Content & Assessment Configuration

**Status:** Documentation complete for implementation; code not started.

## Intent

Implement the structural metadata layer for the Antarang CAP platform: question bank, multilingual content, version-controlled questionnaires, assessment masters with execution policy, deployable configuration groups (items + outputs + assignments), IAR logic versioning (admin/bind only), and report template skeletons so downstream FKs validate.

## Document sources

| Source | Sections |
|--------|----------|
| FFG *Antarang CAP Platform API Specification* v1.0 | §14–18, §21, §23.1–23.3, §30 (Sprint 2 scope) |
| FFG *Database Design* v1.0 | Modules D (config/language), E–H (question bank through config groups), K1–K2 (IAR config lean), L1–L2 (report template lean) |
| **IF2AF0203-export.pdf** (Confluence FFG pack) | Scope & Epics §77–94, Sprint 2 resource plan §105–107, Configuration lifecycle §20–21, Audit strategy §18, API catalogue §133–136, Antarang inputs §61–65 |
| Flow diagrams (reviewed) | **`04`** admin config-group pipeline (24 steps), **`12`** question bank → questionnaire builder, **`13`** config-group resolution (USER > CLUSTER > ORG_UNIT) |
| Architecture / ERD (reviewed) | **`03`** logical module map; **`ERD 1`** high-level; **`ERD 4`** config/language/question bank/questionnaire |
| Cross-sprint context (reviewed, not Sprint 2 build) | `01` system architecture, `02` AWS deployment, `05`–`18` execution/output flows — referenced for traceability |
| Excluded | `IF2AF0203-Account Creation…pdf`, `IF2AF0203-Organisational Hierarchy…pdf`, Sprint 1 docs |
| NGO schema (canonical) | Flyway **V3** (question bank/questionnaire), **V4** (assessments/config groups), **V6** (`iar_logic_configs`/`iar_logic_versions`), **V7** (`report_templates`/`report_template_versions` skeleton) |

## In-scope epics (IF2AF0203 sprint plan §94 + FFG §30)

| Epic | IF2AF0203 scope |
|------|-----------------|
| EPIC-06 (completion) | Configuration groups/values, languages, assessment language preference at config level |
| EPIC-07 | Question bank: categories, questions, options, translations, review status, rules |
| EPIC-08 | Questionnaires: create, version, preview, **publish / suspend / retire** |
| EPIC-09 | Assessment masters, execution config (count-up timer), configuration groups (items, outputs, activate, assign) |
| EPIC-12 (**foundation**) | IAR config + version CRUD, validate, publish — not full career mapping yet |
| EPIC-14 (**foundation**) | Report template + version create/publish — bindable on group outputs |
| EPIC-19 (**foundation**) | Audit on questionnaire/IAR/scoring publish, config group assign/activate (expand in Sprint 3–4) |

## Antarang inputs required before / during Sprint 2 (IF2AF0203 §61–75)

| Input | Needed by |
|-------|-----------|
| Question bank sample (questions, options, translations, domains) | Sprint 2 start |
| Questionnaire structure (A/I/A/R sections, ordering) | Sprint 2 |
| Conditional logic rules (skip/branching) | Sprint 2 |
| Timer guidance (count-up confirmed; max duration / reattempt) | Sprint 2 |
| IAR logic guide (weights, matching — may refine Sprint 3) | 22 Jul target |

## Out of scope (later sprints)

- Assessment start/submit/responses (Sprint 3)
- Scoring rule/benchmark admin beyond FK references on group items (Sprint 3 admin APIs)
- Career masters, recommendation generation (Sprint 4)
- Full report section editing and PDF generation (Sprint 4)
- Sprint 1 auth/RBAC (done)

## Dependencies

- Sprint 1: tenants, users, roles, org units, clusters, languages table, configuration groups from V2
- **NGO Flyway V3–V4, V6–V7 tables already exist** — implement JPA entities, repositories, services, controllers only
- Do **not** add duplicate Sprint-2 domain migrations; auth deltas remain V10–V16 only

## Primary contract

[spec.md](./spec.md) · [design.md](./design.md) · [tasks.md](./tasks.md) · [api-contracts.md](./api-contracts.md)
