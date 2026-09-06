# IF2AF0203-export.pdf — Summary (Sprint 2–4 focus)

**Source:** `/Users/khushi/Downloads/IF2AF0203-export.pdf` (Confluence FFG pack, 165 pages)  
**Cross-check date:** 2026-08-10  
**Verdict:** Sprint 2 / 3 / 4 folders align with this export for MVP scope. Minor PDF-internal ambiguities are resolved correctly in the sprint docs (see [Cross-check notes](#cross-check-notes)).

> Sprint 1 (auth, org, RBAC, consent) is out of scope for this summary.

---

## Document map (relevant sections)

| PDF area | Pages (approx) | Use for |
|----------|----------------|---------|
| Data model & configuration strategy | 19–35 | Tables, resolution, lifecycle, audit matrix |
| Functional architecture modules | 37–56 | Module responsibilities |
| Business requirements & Antarang inputs | 58–66 | Inputs by sprint |
| Compressed MVP roadmap | 67–77 | Timeline, risks |
| Scope & Epics | 78–93 | EPIC-00–23 definitions |
| Sprint plan as per Epics | 94–95 | Which epic lands in which sprint |
| MVP Priority Cutline | 96–98 | Must / should / Phase 2 |
| Resource planning Sprint 2–4 | 106–115 | Deliverables + demo scripts |
| API catalogue (high-level) | 133–138 | Path list only — incomplete |
| ERDs / architecture pointers | 122–149 | Diagrams 01–18, ERD 1–7 |

**Authoritative for request/response bodies:** FFG API Spec v1.0 (not the PDF catalogue).  
**Implementable contracts:** [api-contracts-sprint-2-3-4.md](./api-contracts-sprint-2-3-4.md) and per-sprint `api-contracts.md`.

---

## Sprint plan (PDF §94–95)

| Sprint | Timeline | Epics |
|--------|----------|-------|
| **2** | 22 Jul – 04 Aug 2026 | EPIC-06, 07, 08, 09; EPIC-12 **foundation**; EPIC-14 **foundation** (+ EPIC-19 foundation in resource plan) |
| **3** | 05 Aug – 18 Aug 2026 | EPIC-10, 11, EPIC-12, EPIC-19 traceability |
| **4** | 19 Aug – 31 Aug 2026 | EPIC-13, 14, 15, 16, 17, 18, EPIC-20 (+ EPIC-19 completion in resource plan) |

Phase 2: EPIC-21 Data Analyst, EPIC-22 Guest, EPIC-23 Advanced integrations/analytics.

---

## Epic cheat sheet (Sprint 2–4)

| Epic | Title | Sprint home |
|------|-------|-------------|
| EPIC-06 | Configuration framework & language | Sprint 2 (complete Sprint-1 gaps) |
| EPIC-07 | Question bank | Sprint 2 |
| EPIC-08 | Questionnaire versioning (publish / **suspend** / **retire**) | Sprint 2 |
| EPIC-09 | Assessment config + configuration groups | Sprint 2 |
| EPIC-10 | Assessment execution, count-up timer, attempts | Sprint 3 |
| EPIC-11 | Scoring, benchmarks, evaluation versioning | Sprint 3 |
| EPIC-12 | IAR logic + career mapping | S2 foundation (IAR versions); S3 career CRUD; S4 IAR execution at recommend |
| EPIC-13 | Recommendation engine | Sprint 4 |
| EPIC-14 | Report templates & generation | S2 skeleton; S4 full generate/download |
| EPIC-15 | Counsellor feedback | Sprint 4 (should-have) |
| EPIC-16 | Dashboards (student / facilitator / admin / **sub-admin**) | Sprint 4 (should-have) |
| EPIC-17 | Notification foundation (email-first) | Sprint 4 (should-have) |
| EPIC-18 | Basic external integration APIs | Sprint 4 (should-have) |
| EPIC-19 | Audit, activity, traceability | Foundation S2 → submit activity S3 → list APIs S4 |
| EPIC-20 | UAT, hardening, training, go-live | Sprint 4 |

---

## MVP priority cutline (PDF §96–97)

**Must-have:** login/RBAC/org (S1), config groups, languages, question bank, questionnaire versioning, assessment execution, count-up timer, response capture, scoring/benchmarks, IAR versioning, recommendation generation, report generation, audit/traceability.

**Should-have:** counsellor feedback, basic dashboards, basic email notifications, basic external APIs, question-level timing.

**Phase 2 if slips:** advanced dashboards, visual rule builder, SMS, Salesforce/Glific, Guest, Data Analyst, approval workflows, advanced template builder, exports.

---

## Configuration & data rules (PDF §19–35)

### Assessment types
`ASPIRATION` | `INTEREST` | `APTITUDE` | `REALITY`

### Timer (MVP)
Default **COUNT_UP**; manual submit; store `started_at`, `submitted_at`, `elapsed_seconds`. Countdown/auto-submit configurable but not default.

### Configuration group (deployable package)
Binds: assessment version, questionnaire version, scoring rule version, IAR logic version, assessment language, report template version, output language, assignment scope.

### Resolution priority (PDF §11)
1. Direct **USER** assignment  
2. **CLUSTER**  
3. **ORG_UNIT** (then parent org / tenant default in full PDF flow)

Sprint docs implement MVP as **USER > CLUSTER > ORG_UNIT** — acceptable lean cut.

### Attempt traceability (non-negotiable)
Every attempt stores: `configuration_group_id`, `assessment_version_id`, `questionnaire_version_id`, `scoring_rule_version_id`, `iar_logic_version_id`, `report_template_version_id`, `assessment_language_id`, `output_language_id`.

### Config lifecycle (PDF §20)
Draft → Configure → Validate → Preview → Publish → Assign → Activate → Use → Archive.

### Audit vs activity (PDF §18)

| Event | Type |
|-------|------|
| User/role/assignment/consent changes | Audit |
| Questionnaire / IAR / scoring publish/retire | Audit |
| Config group create/assign/activate | Audit |
| Counsellor feedback add/update | Audit |
| Assessment submission | **Activity** + attempt traceability |
| Report generation/download | **Activity** |

Keep `audit_logs`, `activity_logs`, and `login_history` separate.

### Notifications (MVP)
Required: Account created, Password reset.  
Optional: Assessment assigned/completed, Report published.  
SMS → Phase 2.

### Integrations (MVP)
Upsert student, fetch results, fetch latest report + `integration_events`. Full Salesforce/Glific → Phase 2.

---

## Demo scripts (PDF resource plan)

### Sprint 2 (§107 / p108)
Admin: create questions → options/translations → questionnaire → version → add questions (mandatory/order/branching) → preview → publish → assessment version + count-up config → config group items/outputs → assign → activate. IAR + report template foundations.

### Sprint 3 (§110 / p111)
Student: login → see assigned assessment → start (resolve config) → language applied → count-up → save responses → submit → mandatory validation → scoring → domain scores/benchmarks. Admin verifies version traceability. Career masters/mappings for upcoming recommend.

### Sprint 4 (§113 / p114)
Scores → IAR → ranked recommendations → PDF report (S3) → student download → facilitator feedback → dashboards (incl. Sub-Admin) → basic email + external APIs → audit/activity → UAT/release.

---

## PDF API catalogue (p133–138) — Sprint 2–4 paths only

High-level path list from the export (incomplete vs FFG). Sprint contracts expand these.

**Config / language:** `GET|POST /configuration-groups`, `GET|POST /configurations`, `PUT /configurations/{id}`, `GET|POST /languages`

**Questionnaires:** `GET|POST /questionnaires`, `GET|PUT /questionnaires/{id}`, `POST .../publish`, `POST .../clone`, `GET .../versions`

**Questions:** `POST|PUT|DELETE /questions`, `POST .../options`, `PUT .../translations`, `PATCH .../status`

**Assessment execution:** `GET /assessments`, `GET /students/{id}/assessments`, `POST /assessments/{id}/start`, `PUT /attempts/{id}/responses`, `POST /attempts/{id}/submit`, `GET /attempts/{id}`, `GET /students/{id}/attempt-history`

**Scoring:** `POST /scoring/calculate/{attemptId}`, `GET /students/{id}/scores`, `GET|POST /benchmarks`, `PUT /benchmarks/{id}`

**Recommend / report:** `POST /recommendations/generate/{studentId}`, `GET /students/{id}/recommendations`, `POST /reports/generate/{studentId}`, `GET /reports/{id}`, `GET /reports/{id}/download`

**Missing from catalogue but required by epics / FFG (covered in sprint contracts):** assessment-configuration-groups (items/outputs/assign/activate/resolve), questionnaire-versions preview/suspend/retire, question categories/rules, scoring-rules versioning, IAR APIs, career CRUD, report template versions, feedback, dashboards, notifications, integrations, audit/activity list APIs.

---

## Cross-check notes

| Topic | PDF | Sprint folders | Status |
|-------|-----|----------------|--------|
| Epic → sprint mapping | §94–95 | proposal/spec per sprint | Aligned |
| EPIC-08 suspend/retire | Epic definition | Sprint 2 contracts | Aligned (catalogue omits; docs correct) |
| EPIC-12 career CRUD | Sprint 3 epic; S4 resource also lists careers | Career CRUD in **Sprint 3**; generate in **Sprint 4** | Aligned (resolves PDF ambiguity) |
| EPIC-19 spread | Resource plans S2–S4 | Foundation → submit → list APIs | Aligned |
| Question-level timing | Should-have §96 | Sprint 3 | Aligned |
| Submit → recommend/report | Flow can imply one chain | S3 ends at scores; S4 generate APIs | Aligned intentional MVP split |
| API payloads | Catalogue path-only | FFG + `api-contracts.md` | Correct (docs state FFG authoritative) |
| Resolution depth | USER→CLUSTER→ORG→parent→tenant | USER→CLUSTER→ORG_UNIT | Acceptable MVP lean |

**No blocking gaps found for Sprint 2–4 documentation.**
