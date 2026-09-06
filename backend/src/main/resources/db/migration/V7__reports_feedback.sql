-- =====================================================================
-- V7__reports_feedback.sql
-- Antarang CAP Platform — Report Templates, Versioned Templates,
-- Report Sections, Generated Reports & Counsellor Feedback
-- Scope: report_templates, report_template_versions, report_sections,
--        generated_reports, counsellor_feedback
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
--             V4__assessment_config_groups.sql
--             V5__assessment_execution.sql
--             V6__scoring_iar_recommendations.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module L1: report_templates
-- Logical report template.
-- current_version_id is attached via ALTER TABLE below once
-- report_template_versions is created.
-- ---------------------------------------------------------------------
CREATE TABLE report_templates (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    template_type       VARCHAR(50) NOT NULL CHECK (template_type IN ('STUDENT', 'FACILITATOR')),  -- STUDENT / FACILITATOR
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),  -- DRAFT / ACTIVE / INACTIVE / ARCHIVED
    current_version_id  UUID,  -- FK to report_template_versions(id) attached below
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_report_templates_tenant_code UNIQUE (tenant_id, code)
);

-- ---------------------------------------------------------------------
-- Module L2: report_template_versions
-- Versioned report template.
-- ---------------------------------------------------------------------
CREATE TABLE report_template_versions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_template_id  UUID NOT NULL REFERENCES report_templates(id),
    version_number      INT NOT NULL,
    template_config     JSONB NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),  -- DRAFT / PUBLISHED / ARCHIVED
    published_at        TIMESTAMP,
    published_by        UUID REFERENCES users(id),
    version_notes       TEXT,
    CONSTRAINT uq_report_template_versions_template_number UNIQUE (report_template_id, version_number)
);

-- Attach deferred FK on report_templates
ALTER TABLE report_templates
    ADD CONSTRAINT fk_report_templates_current_version
        FOREIGN KEY (current_version_id) REFERENCES report_template_versions(id);

-- ---------------------------------------------------------------------
-- Module L3: report_sections
-- Report template section configurations.
-- ---------------------------------------------------------------------
CREATE TABLE report_sections (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_template_version_id  UUID NOT NULL REFERENCES report_template_versions(id),
    section_code                VARCHAR(100) NOT NULL,
    section_title               VARCHAR(200) NOT NULL,
    display_order               INT NOT NULL,
    section_config              JSONB,
    CONSTRAINT uq_report_sections_version_code UNIQUE (report_template_version_id, section_code)
);

-- ---------------------------------------------------------------------
-- Module L4: generated_reports
-- Stores generated report metadata. Actual PDF files are stored in S3.
-- ---------------------------------------------------------------------
CREATE TABLE generated_reports (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id                  UUID NOT NULL REFERENCES users(id),
    recommendation_run_id       UUID NOT NULL REFERENCES recommendation_runs(id),
    report_template_version_id  UUID NOT NULL REFERENCES report_template_versions(id),
    configuration_group_id      UUID NOT NULL REFERENCES assessment_configuration_groups(id),
    report_type                 VARCHAR(50) NOT NULL CHECK (report_type IN ('STUDENT', 'FACILITATOR')),  -- STUDENT / FACILITATOR
    language_id                 UUID NOT NULL REFERENCES languages(id),
    s3_key                      TEXT NOT NULL,
    file_name                   VARCHAR(255) NOT NULL,
    generated_at                TIMESTAMP NOT NULL DEFAULT now(),
    generated_by                UUID REFERENCES users(id)
);

-- ---------------------------------------------------------------------
-- Module L5: counsellor_feedback
-- Stores qualitative feedback and guidance notes from counsellors.
-- ---------------------------------------------------------------------
CREATE TABLE counsellor_feedback (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL REFERENCES tenants(id),
    student_id            UUID NOT NULL REFERENCES users(id),
    counsellor_id         UUID NOT NULL REFERENCES users(id),
    report_id             UUID REFERENCES generated_reports(id),  -- nullable
    feedback_text         TEXT NOT NULL,
    recommendation_notes  TEXT,
    visibility            VARCHAR(30) NOT NULL DEFAULT 'INTERNAL' CHECK (visibility IN ('INTERNAL', 'VISIBLE_TO_STUDENT')),  -- INTERNAL / VISIBLE_TO_STUDENT
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_update_counsellor_feedback BEFORE UPDATE ON counsellor_feedback FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Attach deferred FK constraints from V4 and V5 tables
-- ---------------------------------------------------------------------
ALTER TABLE assessment_configuration_group_outputs
    ADD CONSTRAINT fk_acg_outputs_report_template_version
        FOREIGN KEY (report_template_version_id) REFERENCES report_template_versions(id);

ALTER TABLE assessment_attempts
    ADD CONSTRAINT fk_attempts_report_template_version
        FOREIGN KEY (report_template_version_id) REFERENCES report_template_versions(id);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V7 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_reports_student ON generated_reports (student_id);
CREATE INDEX idx_reports_config_group ON generated_reports (configuration_group_id);
CREATE INDEX idx_feedback_student ON counsellor_feedback (student_id);
CREATE INDEX idx_feedback_counsellor ON counsellor_feedback (counsellor_id);

-- Supporting query indexes for lookups
CREATE INDEX idx_report_templates_tenant ON report_templates (tenant_id);
CREATE INDEX idx_report_template_versions_template ON report_template_versions (report_template_id);
CREATE INDEX idx_report_sections_version ON report_sections (report_template_version_id);

CREATE INDEX idx_report_templates_current_version ON report_templates(current_version_id);
CREATE INDEX idx_rtv_published_by ON report_template_versions(published_by);
CREATE INDEX idx_gen_reports_rec_run ON generated_reports(recommendation_run_id);
CREATE INDEX idx_gen_reports_rtv ON generated_reports(report_template_version_id);
CREATE INDEX idx_gen_reports_lang ON generated_reports(language_id);
CREATE INDEX idx_gen_reports_generated_by ON generated_reports(generated_by);
CREATE INDEX idx_counsellor_feedback_report ON counsellor_feedback(report_id);
