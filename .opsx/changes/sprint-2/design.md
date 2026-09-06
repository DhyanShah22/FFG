# System Design: Sprint 2 — Metadata & Configuration Layers

**Implement against:** [api-contracts.md](./api-contracts.md) · **Requirements:** [spec.md](./spec.md)

## Tech stack

Spring Boot + JPA + PostgreSQL + Flyway (`validate`) + Spring Security JWT. Package root: `com.antarang.cap`.

## Schema strategy (NGO monorepo)

**Do not create new Flyway scripts for Sprint 2 domain tables.** Map JPA entities to existing NGO migrations:

| NGO migration | Modules / tables |
|---------------|------------------|
| **V2** (read) | `configuration_groups`, `configurations`, `languages` |
| **V3** | `question_categories`, `questions`, `question_options`, `question_translations`, `option_translations`, `question_rules`, `questionnaires`, `questionnaire_versions`, `questionnaire_questions` |
| **V4** | `assessments`, `assessment_versions`, `assessment_configurations`, `assessment_configuration_groups`, `assessment_configuration_group_items`, `assessment_configuration_group_outputs`, `assessment_configuration_group_assignments` |
| **V6** | `iar_logic_configs`, `iar_logic_versions` (Sprint 2 admin APIs only) |
| **V7** | `report_templates`, `report_template_versions`, `report_sections` (skeleton; section CRUD lean until Sprint 4) |

Hibernate `ddl-auto: validate` — entity `@Column(name=...)` must match NGO snake_case exactly.

## Entity / column mapping (authoritative = NGO SQL)

### Question bank (V3 / Module E)

**`question_categories`:** `tenant_id`, `assessment_type`, `code`, `name`, `description`, **`domain_code`**, `is_active`.

**`questions`:** `tenant_id`, `assessment_type`, `category_id`, **`question_code`**, **`question_type`**, `default_text`, `help_text`, `difficulty_level`, **`review_status`**, `source_reference`, `is_required_default`, `scoring_enabled`, `metadata`, `is_active`, `is_deleted`.

**DTO alias layer (optional):** map `SINGLE_CHOICE`↔`RADIO`, `MULTI_CHOICE`↔`CHECKBOX` in request/response only.

**`question_options`:** `question_id`, **`option_code`**, `default_text`, `score_value`, **`display_order`**, `metadata`, `is_active`.

**`question_translations`:** `question_id`, `language_id`, **`question_text`**, `help_text`.

**`option_translations`:** `option_id`, `language_id`, **`option_text`**.

**`question_rules`:** `question_id`, `rule_type`, `rule_config` JSONB, `is_active`, `created_at`.

### Questionnaires (V3 / Module F)

**`questionnaires`:** `tenant_id`, `assessment_type`, `code`, `name`, `description`, `status`, `current_version_id`, `shuffle_questions`, `is_active`, `is_deleted`.

**`questionnaire_versions`:** `questionnaire_id`, `version_number`, `status`, `published_at`, `published_by`, `version_notes`, `created_at`.

**`questionnaire_questions`:** `questionnaire_version_id`, `question_id`, **`display_order`**, `is_mandatory`, **`section_code`**, **`section_name`**, **`condition_config`** JSONB.

### Assessment masters & execution (V4 / Module G)

**`assessments`:** `tenant_id`, `code` (INTEREST/APTITUDE/REALITY/ASPIRATION), `name`, `description`, `is_active`, `is_deleted`.

**`assessment_versions`:** `assessment_id`, `questionnaire_version_id`, `version_number`, `status`, `published_at`, `created_at`.

**`assessment_configurations`:** per `assessment_version_id` — all timer/resume/attempt/completion fields per FFG §17.3.

### Configuration groups (V4 / Module H)

**`assessment_configuration_groups`:** `tenant_id`, `code`, `name`, `description`, **`academic_year`**, **`assessment_cycle`**, **`status`**, `is_active`, `is_deleted`, audit columns.

**`assessment_configuration_group_items`:** `configuration_group_id`, `assessment_id`, `questionnaire_version_id`, `assessment_version_id`, `iar_logic_version_id`, `scoring_rule_version_id`, `assessment_language_id`, `is_required`, `display_order`.

**`assessment_configuration_group_outputs`:** `configuration_group_id`, `report_template_version_id`, `output_language_id`, **`output_config`** JSONB, `is_active`.

**`assessment_configuration_group_assignments`:** `configuration_group_id`, **`assignment_type`**, **`assignment_id`**, **`effective_from`**, **`effective_to`**, `is_active`, `assigned_by`, `assigned_at`.

### IAR (V6 / Module K1–K2)

**`iar_logic_configs`:** `tenant_id`, `code`, `name`, `description`, `status`, `current_version_id`.

**`iar_logic_versions`:** `iar_logic_config_id`, `version_number`, **`logic_definition`** JSONB, `status`, `published_at`, `published_by`, `version_notes`.

Example `logic_definition`:

```json
{
  "aspirationWeight": 0.25,
  "interestWeight": 0.25,
  "aptitudeWeight": 0.30,
  "realityWeight": 0.20,
  "rules": []
}
```

### Report template skeleton (V7 / Module L1–L2)

**`report_templates`:** `tenant_id`, `code`, `name`, **`template_type`** (`STUDENT`|`FACILITATOR`), `status`, `current_version_id`, `is_active`.

**`report_template_versions`:** `report_template_id`, `version_number`, **`template_config`** JSONB, `status`, `published_at`, `published_by`, `version_notes`.

## Services

```
QuestionCategoryService
QuestionService              CRUD, translations (incl. optionTranslations), rules, review workflow
QuestionnaireService         versions, add/reorder, preview, publish, clone
AssessmentService            masters + versions
AssessmentConfigurationService  execution policy per version
AssessmentConfigurationGroupService  group CRUD, items, outputs, assignments, activate
ConfigurationResolutionService     priority: USER > CLUSTER > ORG_UNIT; effective dates
IarLogicService              config + version CRUD, validate, publish
ReportTemplateService        skeleton create version + publish
ConfigurationService         Sprint-1 completion (groups/values)
LanguageService              POST /languages if missing
```

## Configuration resolution algorithm

Aligned with **`13-assessment-configuration-group-resolution-flow.png`**:

1. Identify student → fetch org unit + cluster memberships.
2. Load all **ACTIVE** groups with assignments matching student context (tenant, effective dates).
3. Check assignments at three levels: **USER** (direct), **CLUSTER** (membership), **ORG_UNIT** (hierarchy).
4. If multiple groups match, apply priority: **USER > CLUSTER > ORG_UNIT**; tie-break by most specific scope or latest `assigned_at`.
5. Return winning group with **items[]** (questionnaire version, scoring version, IAR version, assessment language) and **outputs[]** (report template version, output language, output_config).

## Flow diagram alignment (FFG Documents)

### `12-question-bank-to-questionnaire-flow.png`

Admin path: create/edit question → options → translations → category/domain → scoring values → **`review_status=APPROVED`** → questionnaire DRAFT → search/filter bank → add questions → order/mandatory/section → branching → preview → publish → **published version immutable for new attempts**.

### `04-admin-configuration-group-flow.png` (24-step pipeline)

Full admin demo spans Sprint 2–4. **Sprint 2 owns steps 1–11, 14–24** (through group activate); steps **12–13** (scoring rule version create/publish) are **Sprint 3 admin APIs** but group **items** MAY reference `scoring_rule_version_id` once published. Steps after activate (student receives assessment) are Sprint 3+.

### `03-updated-erd-logical-view.png` / `ERD 4`

Confirms module boundaries: config/language, question bank, questionnaire versioning, assessment masters, configuration groups — all map to NGO V2–V4 columns documented above.

## Publish / immutability rules

| Entity | Rule |
|--------|------|
| `questionnaire_versions` | DRAFT → PUBLISHED; no structural mutation after publish |
| `iar_logic_versions` | DRAFT → PUBLISHED after validate; no payload edit after publish |
| `report_template_versions` | DRAFT → PUBLISHED; section/template_config edit DRAFT only |
| `assessment_configuration_groups` | Must be ACTIVE to resolve; metadata edit while ACTIVE allowed per api-contracts |

## Security (`SecurityConfig` + `@PreAuthorize`)

| Area | Roles |
|------|-------|
| Question bank / questionnaires / assessments / config groups | `ADMINISTRATOR`; read `SUB_ADMIN` where scoped |
| IAR / report template admin | `ADMINISTRATOR` |
| Active config for student | self `CAREER_EXPLORER`, assigned `CAREER_COUNSELLOR`, admin, scoped `SUB_ADMIN` |
| Configurations/languages GET | authenticated or public per Sprint 1 |

## Packages

```
controller/ QuestionCategoryController, QuestionController, QuestionnaireController,
            AssessmentController, AssessmentVersionController,
            AssessmentConfigurationGroupController, IarLogicController,
            ReportTemplateController, ConfigurationController, LanguageController
service/    (see above)
repository/ tenant-scoped queries + soft-delete filters
domain/     entities + enums mirroring CHECK constraints
dto/        request/response; optional type alias mappers
```

## Error codes (Sprint 2)

| errorCode | When |
|-----------|------|
| `VALIDATION_ERROR` | Bean/field rules |
| `DUPLICATE_RESOURCE` | Unique code conflicts |
| `RESOURCE_NOT_FOUND` | Missing entity |
| `ACCESS_DENIED` | RBAC/tenant |
| `QUESTIONNAIRE_PUBLISHED` / `INVALID_STATE` | Structural edit on published version |
| `QUESTION_IN_PUBLISHED_VERSION` | Destructive option change |
| `QUESTION_IN_USE` | Delete blocked |
| `CONFIG_GROUP_NOT_FOUND` | Resolution miss |
| `IAR_VALIDATION_FAILED` | Validate endpoint failures |

## Deferred to Sprint 3+

* Scoring rule version admin APIs (tables in V6; consumed on attempts in Sprint 3)
* Question rule **evaluation** at attempt render (Sprint 3)
* Career/recommendation/report PDF (Sprint 4)
