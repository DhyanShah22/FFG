-- =====================================================================
-- V3__question_bank_questionnaire.sql
-- Antarang CAP Platform — Question Bank, Question Translations,
-- Question Rules & Questionnaire Versioning
-- Scope: question_categories, questions, question_options,
--        question_translations, option_translations, question_rules,
--        questionnaires, questionnaire_versions, questionnaire_questions
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module E1: question_categories
-- ---------------------------------------------------------------------
CREATE TABLE question_categories (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    assessment_type  VARCHAR(50) NOT NULL CHECK (assessment_type IN ('INTEREST', 'APTITUDE', 'REALITY', 'ASPIRATION')),
    code             VARCHAR(100) NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    domain_code      VARCHAR(100) NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_question_categories_tenant_type_code UNIQUE (tenant_id, assessment_type, code)
);

-- ---------------------------------------------------------------------
-- Module E2: questions
-- ---------------------------------------------------------------------
CREATE TABLE questions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL REFERENCES tenants(id),
    assessment_type       VARCHAR(50) NOT NULL CHECK (assessment_type IN ('INTEREST', 'APTITUDE', 'REALITY', 'ASPIRATION')),
    category_id           UUID NOT NULL REFERENCES question_categories(id),
    question_code         VARCHAR(100) NOT NULL,
    question_type         VARCHAR(50) NOT NULL CHECK (question_type IN ('RADIO', 'CHECKBOX', 'RATING', 'SHORT_TEXT', 'LONG_TEXT')),
    default_text          TEXT NOT NULL,
    help_text             TEXT,
    difficulty_level      VARCHAR(50) CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD')),
    review_status         VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (review_status IN ('DRAFT', 'REVIEWED', 'APPROVED', 'RETIRED')),
    source_reference       TEXT,
    is_required_default   BOOLEAN NOT NULL DEFAULT TRUE,
    scoring_enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    metadata              JSONB,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted            BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- Module E3: question_options
-- ---------------------------------------------------------------------
CREATE TABLE question_options (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id    UUID NOT NULL REFERENCES questions(id),
    option_code    VARCHAR(100) NOT NULL,
    default_text   TEXT NOT NULL,
    score_value    NUMERIC(10,2),
    display_order  INT NOT NULL DEFAULT 0,
    metadata       JSONB,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_question_options_question_code UNIQUE (question_id, option_code)
);

-- ---------------------------------------------------------------------
-- Module E4: question_translations
-- ---------------------------------------------------------------------
CREATE TABLE question_translations (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id    UUID NOT NULL REFERENCES questions(id),
    language_id    UUID NOT NULL REFERENCES languages(id),
    question_text  TEXT NOT NULL,
    help_text      TEXT,
    CONSTRAINT uq_question_translations_question_lang UNIQUE (question_id, language_id)
);

-- ---------------------------------------------------------------------
-- Module E5: option_translations
-- ---------------------------------------------------------------------
CREATE TABLE option_translations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_id    UUID NOT NULL REFERENCES question_options(id),
    language_id  UUID NOT NULL REFERENCES languages(id),
    option_text  TEXT NOT NULL,
    CONSTRAINT uq_option_translations_option_lang UNIQUE (option_id, language_id)
);

-- ---------------------------------------------------------------------
-- Module E6: question_rules
-- Supports validation, branching, skip logic, conditional display,
-- and scoring conditions.
-- Example rule_config:
-- {
--   "dependsOnQuestionId": "uuid",
--   "operator": "EQUALS",
--   "value": "YES",
--   "action": "SHOW"
-- }
-- ---------------------------------------------------------------------
CREATE TABLE question_rules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id  UUID NOT NULL REFERENCES questions(id),
    rule_type    VARCHAR(50) NOT NULL CHECK (rule_type IN ('VALIDATION', 'VISIBILITY', 'BRANCHING', 'SCORING')),
    rule_config  JSONB NOT NULL,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module F1: questionnaires
-- current_version_id is nullable initially; FK to questionnaire_versions
-- is attached below once that table exists (same deferred-FK pattern
-- used for organizational_cluster_members.added_by in V1).
-- ---------------------------------------------------------------------
CREATE TABLE questionnaires (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    assessment_type     VARCHAR(50) NOT NULL CHECK (assessment_type IN ('INTEREST', 'APTITUDE', 'REALITY', 'ASPIRATION')),
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),
    current_version_id  UUID,  -- FK to questionnaire_versions.id, attached below
    shuffle_questions   BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- Module F2: questionnaire_versions
-- ---------------------------------------------------------------------
CREATE TABLE questionnaire_versions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    questionnaire_id  UUID NOT NULL REFERENCES questionnaires(id),
    version_number    INT NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    published_at      TIMESTAMP,
    published_by      UUID REFERENCES users(id),
    version_notes     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_questionnaire_versions_questionnaire_number UNIQUE (questionnaire_id, version_number)
);

-- Now that questionnaire_versions exists, attach the deferred FK from questionnaires
ALTER TABLE questionnaires
    ADD CONSTRAINT fk_questionnaires_current_version FOREIGN KEY (current_version_id) REFERENCES questionnaire_versions(id);

-- ---------------------------------------------------------------------
-- Module F3: questionnaire_questions
-- Maps selected question bank questions to questionnaire versions.
-- ---------------------------------------------------------------------
CREATE TABLE questionnaire_questions (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    questionnaire_version_id  UUID NOT NULL REFERENCES questionnaire_versions(id),
    question_id               UUID NOT NULL REFERENCES questions(id),
    display_order             INT NOT NULL,
    is_mandatory              BOOLEAN NOT NULL DEFAULT TRUE,
    section_code              VARCHAR(100),
    section_name              VARCHAR(150),
    condition_config          JSONB,  -- optional questionnaire-level condition
    CONSTRAINT uq_questionnaire_questions_version_question UNIQUE (questionnaire_version_id, question_id)
);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V3 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_questions_tenant_type ON questions (tenant_id, assessment_type);
CREATE INDEX idx_questions_category ON questions (category_id);
CREATE INDEX idx_questionnaire_versions_questionnaire ON questionnaire_versions (questionnaire_id);
CREATE INDEX idx_questionnaire_questions_version ON questionnaire_questions (questionnaire_version_id);

-- Not in doc's explicit list, but needed given the obvious query patterns
-- (lookup by question, by category tenant scope, and by option/language
-- for translation fetches) — same rationale used for the extra indexes
-- added in V2.
CREATE INDEX idx_question_categories_tenant ON question_categories (tenant_id, assessment_type);
CREATE INDEX idx_question_options_question ON question_options (question_id);
CREATE INDEX idx_question_translations_question ON question_translations (question_id);
CREATE INDEX idx_question_translations_language ON question_translations (language_id);
CREATE INDEX idx_option_translations_option ON option_translations (option_id);
CREATE INDEX idx_option_translations_language ON option_translations (language_id);
CREATE INDEX idx_question_rules_question ON question_rules (question_id);
CREATE INDEX idx_questionnaires_tenant_status ON questionnaires (tenant_id, status);
CREATE INDEX idx_questionnaire_questions_question ON questionnaire_questions (question_id);

CREATE UNIQUE INDEX uq_questions_tenant_code_active ON questions (tenant_id, question_code) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uq_questionnaires_tenant_code_active ON questionnaires (tenant_id, code) WHERE is_deleted = FALSE;

CREATE INDEX idx_questionnaires_current_version ON questionnaires(current_version_id);
CREATE INDEX idx_qv_published_by ON questionnaire_versions(published_by);