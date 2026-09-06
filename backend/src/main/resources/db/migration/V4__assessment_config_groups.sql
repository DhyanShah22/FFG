-- =====================================================================
-- V4__assessment_config_groups.sql
-- Antarang CAP Platform — Assessments, Assessment Versions,
-- Assessment Configurations, Configuration Groups & Assignments
-- Scope: assessments, assessment_versions, assessment_configurations,
--        assessment_configuration_groups, assessment_configuration_group_items,
--        assessment_configuration_group_outputs,
--        assessment_configuration_group_assignments
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
--             V3__question_bank_questionnaire.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module G1: assessments (Assessment Master)
-- ---------------------------------------------------------------------
CREATE TABLE assessments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    code         VARCHAR(50) NOT NULL CHECK (code IN ('INTEREST', 'APTITUDE', 'REALITY', 'ASPIRATION')),
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- Module G2: assessment_versions
-- Links an assessment to a questionnaire version.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_versions (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id             UUID NOT NULL REFERENCES assessments(id),
    questionnaire_version_id  UUID NOT NULL REFERENCES questionnaire_versions(id),
    version_number            INT NOT NULL,
    status                    VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    published_at              TIMESTAMP,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_assessment_versions_assessment_number UNIQUE (assessment_id, version_number)
);

-- ---------------------------------------------------------------------
-- Module G3: assessment_configurations
-- Execution configuration for assessment versions.
-- MVP default: COUNT_UP by default, manual submit, elapsed time recorded.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_configurations (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_version_id   UUID NOT NULL REFERENCES assessment_versions(id),
    timer_mode              VARCHAR(30) NOT NULL DEFAULT 'COUNT_UP' CHECK (timer_mode IN ('NONE', 'COUNT_UP', 'COUNT_DOWN')),
    max_duration_minutes    INT,
    auto_submit_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    show_timer_to_student   BOOLEAN NOT NULL DEFAULT TRUE,
    allow_resume            BOOLEAN NOT NULL DEFAULT TRUE,
    restart_on_interruption BOOLEAN NOT NULL DEFAULT FALSE,
    allow_reattempt         BOOLEAN NOT NULL DEFAULT FALSE,
    reattempt_after_days    INT,
    max_attempts            INT NOT NULL DEFAULT 1,
    completion_rule         VARCHAR(50) NOT NULL DEFAULT 'ALL_MANDATORY' CHECK (completion_rule IN ('ALL_MANDATORY', 'ALLOW_PARTIAL')),
    config_json             JSONB,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_assessment_config_version UNIQUE (assessment_version_id)
);

-- ---------------------------------------------------------------------
-- Module H1: assessment_configuration_groups
-- Deployable group that binds questionnaire version, IAR logic version,
-- scoring version, language, output template, and assignment scope.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_configuration_groups (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenants(id),
    code              VARCHAR(100) NOT NULL,
    name              VARCHAR(200) NOT NULL,
    description       TEXT,
    academic_year     VARCHAR(20),
    assessment_cycle  VARCHAR(100),
    status            VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    created_by        UUID REFERENCES users(id),
    updated_by        UUID REFERENCES users(id)
);

CREATE TRIGGER trigger_update_assessment_configuration_groups BEFORE UPDATE ON assessment_configuration_groups FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module H2: assessment_configuration_group_items
-- Assessment-specific components inside a configuration group.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_configuration_group_items (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuration_group_id   UUID NOT NULL REFERENCES assessment_configuration_groups(id),
    assessment_id             UUID NOT NULL REFERENCES assessments(id),
    questionnaire_version_id  UUID NOT NULL REFERENCES questionnaire_versions(id),
    assessment_version_id     UUID NOT NULL REFERENCES assessment_versions(id),
    iar_logic_version_id      UUID,  -- FK to iar_logic_versions(id) attached in V6
    scoring_rule_version_id   UUID,  -- FK to scoring_rule_versions(id) attached in V6
    assessment_language_id    UUID NOT NULL REFERENCES languages(id),
    is_required               BOOLEAN NOT NULL DEFAULT TRUE,
    display_order             INT NOT NULL DEFAULT 0,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_acg_items_group_assessment UNIQUE (configuration_group_id, assessment_id)
);

-- ---------------------------------------------------------------------
-- Module H3: assessment_configuration_group_outputs
-- Output/report configuration for a configuration group.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_configuration_group_outputs (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuration_group_id     UUID NOT NULL REFERENCES assessment_configuration_groups(id),
    report_template_version_id  UUID,  -- FK to report_template_versions(id) attached in V7
    output_language_id          UUID NOT NULL REFERENCES languages(id),
    output_config               JSONB,
    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module H4: assessment_configuration_group_assignments
-- Assigns configuration groups to org units, clusters, or users.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_configuration_group_assignments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuration_group_id  UUID NOT NULL REFERENCES assessment_configuration_groups(id),
    assignment_type         VARCHAR(50) NOT NULL CHECK (assignment_type IN ('ORG_UNIT', 'CLUSTER', 'USER')),
    assignment_id           UUID NOT NULL,  -- ID of assigned object
    effective_from          TIMESTAMP,
    effective_to            TIMESTAMP,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_by             UUID REFERENCES users(id),
    assigned_at             TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V4 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_acg_tenant_status ON assessment_configuration_groups (tenant_id, status);
CREATE INDEX idx_acg_assignments_type_id ON assessment_configuration_group_assignments (assignment_type, assignment_id);

-- Supporting query indexes for lookups
CREATE INDEX idx_assessments_tenant ON assessments (tenant_id);
CREATE INDEX idx_assessment_versions_assessment ON assessment_versions (assessment_id);
CREATE INDEX idx_acg_items_group ON assessment_configuration_group_items (configuration_group_id);
CREATE INDEX idx_acg_outputs_group ON assessment_configuration_group_outputs (configuration_group_id);
CREATE INDEX idx_acg_assignments_group ON assessment_configuration_group_assignments (configuration_group_id);

CREATE UNIQUE INDEX uq_assessments_tenant_code_active ON assessments (tenant_id, code) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uq_acg_tenant_code_active ON assessment_configuration_groups (tenant_id, code) WHERE is_deleted = FALSE;

CREATE INDEX idx_av_questionnaire_version ON assessment_versions(questionnaire_version_id);
CREATE INDEX idx_acg_created_by ON assessment_configuration_groups(created_by);
CREATE INDEX idx_acg_updated_by ON assessment_configuration_groups(updated_by);
CREATE INDEX idx_acgi_questionnaire_version ON assessment_configuration_group_items(questionnaire_version_id);
CREATE INDEX idx_acgi_assessment_version ON assessment_configuration_group_items(assessment_version_id);
CREATE INDEX idx_acgi_iar_logic_version ON assessment_configuration_group_items(iar_logic_version_id);
CREATE INDEX idx_acgi_scoring_rule_version ON assessment_configuration_group_items(scoring_rule_version_id);
CREATE INDEX idx_acgi_assessment_lang ON assessment_configuration_group_items(assessment_language_id);
CREATE INDEX idx_acgo_report_template_version ON assessment_configuration_group_outputs(report_template_version_id);
CREATE INDEX idx_acgo_output_lang ON assessment_configuration_group_outputs(output_language_id);
CREATE INDEX idx_acga_assigned_by ON assessment_configuration_group_assignments(assigned_by);
