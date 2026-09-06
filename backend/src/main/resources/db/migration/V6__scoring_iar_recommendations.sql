-- =====================================================================
-- V6__scoring_iar_recommendations.sql
-- Antarang CAP Platform — Scoring Rules, Benchmarks, Domain Scores,
-- IAR / A-I-A-R-P Logic, Careers & Recommendations
-- Scope: scoring_rules, scoring_rule_versions, benchmarks, domain_scores,
--        iar_logic_configs, iar_logic_versions, career_clusters,
--        careers, career_mappings, recommendation_runs, recommendations
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
--             V4__assessment_config_groups.sql
--             V5__assessment_execution.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module J1: scoring_rules
-- Logical scoring rule definition.
-- current_version_id is attached via ALTER TABLE below once
-- scoring_rule_versions is created.
-- ---------------------------------------------------------------------
CREATE TABLE scoring_rules (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    assessment_id       UUID NOT NULL REFERENCES assessments(id),
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),  -- DRAFT / ACTIVE / INACTIVE / ARCHIVED
    current_version_id  UUID,  -- FK to scoring_rule_versions(id) attached below
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_scoring_rules_tenant_code UNIQUE (tenant_id, code)
);

CREATE TRIGGER trigger_update_scoring_rules BEFORE UPDATE ON scoring_rules FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module J2: scoring_rule_versions
-- ---------------------------------------------------------------------
CREATE TABLE scoring_rule_versions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scoring_rule_id  UUID NOT NULL REFERENCES scoring_rules(id),
    version_number   INT NOT NULL,
    rule_definition  JSONB NOT NULL,
    status           VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),  -- DRAFT / PUBLISHED / ARCHIVED
    published_at     TIMESTAMP,
    published_by     UUID REFERENCES users(id),
    version_notes    TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_scoring_rule_versions_rule_number UNIQUE (scoring_rule_id, version_number)
);

-- Attach deferred FK on scoring_rules
ALTER TABLE scoring_rules
    ADD CONSTRAINT fk_scoring_rules_current_version
        FOREIGN KEY (current_version_id) REFERENCES scoring_rule_versions(id);

-- ---------------------------------------------------------------------
-- Module J3: benchmarks
-- Benchmarking remains language-independent for MVP.
-- ---------------------------------------------------------------------
CREATE TABLE benchmarks (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID NOT NULL REFERENCES tenants(id),
    assessment_id        UUID NOT NULL REFERENCES assessments(id),
    domain_code          VARCHAR(100) NOT NULL,
    grade_config_id      UUID REFERENCES configurations(id),       -- nullable
    age_group_config_id  UUID REFERENCES configurations(id),       -- nullable
    min_score            NUMERIC(10,2) NOT NULL,
    max_score            NUMERIC(10,2) NOT NULL,
    benchmark_label      VARCHAR(100) NOT NULL CHECK (benchmark_label IN ('LOW', 'MEDIUM', 'HIGH')),                    -- LOW / MEDIUM / HIGH
    interpretation       TEXT,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module J4: domain_scores
-- Stores computed domain scores per assessment attempt.
-- ---------------------------------------------------------------------
CREATE TABLE domain_scores (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id        UUID NOT NULL REFERENCES assessment_attempts(id),
    student_id        UUID NOT NULL REFERENCES users(id),
    assessment_id     UUID NOT NULL REFERENCES assessments(id),
    domain_code       VARCHAR(100) NOT NULL,
    raw_score         NUMERIC(10,2) NOT NULL,
    normalized_score  NUMERIC(10,2),
    benchmark_id      UUID REFERENCES benchmarks(id),
    calculated_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_domain_scores_attempt_domain UNIQUE (attempt_id, domain_code)
);

-- ---------------------------------------------------------------------
-- Module K1: iar_logic_configs
-- Logical recommendation/evaluation configuration.
-- current_version_id is attached via ALTER TABLE below once
-- iar_logic_versions is created.
-- ---------------------------------------------------------------------
CREATE TABLE iar_logic_configs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),  -- DRAFT / ACTIVE / INACTIVE / ARCHIVED
    current_version_id  UUID,  -- FK to iar_logic_versions(id) attached below
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_iar_logic_configs_tenant_code UNIQUE (tenant_id, code)
);

CREATE TRIGGER trigger_update_iar_logic_configs BEFORE UPDATE ON iar_logic_configs FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module K2: iar_logic_versions
-- Versioned IAR / A-I-A-R-P logic.
-- ---------------------------------------------------------------------
CREATE TABLE iar_logic_versions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    iar_logic_config_id  UUID NOT NULL REFERENCES iar_logic_configs(id),
    version_number       INT NOT NULL,
    logic_definition     JSONB NOT NULL,
    status               VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),  -- DRAFT / PUBLISHED / ARCHIVED
    published_at         TIMESTAMP,
    published_by         UUID REFERENCES users(id),
    version_notes        TEXT,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_iar_logic_versions_config_number UNIQUE (iar_logic_config_id, version_number)
);

-- Attach deferred FK on iar_logic_configs
ALTER TABLE iar_logic_configs
    ADD CONSTRAINT fk_iar_logic_configs_current_version
        FOREIGN KEY (current_version_id) REFERENCES iar_logic_versions(id);

-- ---------------------------------------------------------------------
-- Module K3: career_clusters
-- Master career clusters within a tenant scope.
-- ---------------------------------------------------------------------
CREATE TABLE career_clusters (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    code         VARCHAR(100) NOT NULL,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_career_clusters_tenant_code UNIQUE (tenant_id, code)
);

-- ---------------------------------------------------------------------
-- Module K4: careers
-- Master careers dictionary.
-- ---------------------------------------------------------------------
CREATE TABLE careers (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    career_cluster_id  UUID NOT NULL REFERENCES career_clusters(id),
    code               VARCHAR(100) NOT NULL,
    name               VARCHAR(200) NOT NULL,
    description        TEXT,
    education_pathway  TEXT,
    skills_required    JSONB,
    metadata           JSONB,
    is_active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_careers_cluster_code UNIQUE (career_cluster_id, code)
);

-- ---------------------------------------------------------------------
-- Module K5: career_mappings
-- Maps assessment domains/scores to careers.
-- ---------------------------------------------------------------------
CREATE TABLE career_mappings (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenants(id),
    career_id         UUID NOT NULL REFERENCES careers(id),
    interest_domain   VARCHAR(100),
    aptitude_domain   VARCHAR(100),
    reality_factor    VARCHAR(100),
    aspiration_factor VARCHAR(100),
    min_score         NUMERIC(10,2),
    max_score         NUMERIC(10,2),
    mapping_weight    NUMERIC(10,2) NOT NULL DEFAULT 1,
    mapping_config    JSONB,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- Module K6: recommendation_runs
-- Groups generated recommendations for a student.
-- ---------------------------------------------------------------------
CREATE TABLE recommendation_runs (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id              UUID NOT NULL REFERENCES users(id),
    configuration_group_id  UUID NOT NULL REFERENCES assessment_configuration_groups(id),
    iar_logic_version_id    UUID NOT NULL REFERENCES iar_logic_versions(id),
    status                  VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),  -- PENDING / COMPLETED / FAILED
    generated_at            TIMESTAMP NOT NULL DEFAULT now(),
    generated_by            UUID REFERENCES users(id)  -- nullable for system-generated
);

-- ---------------------------------------------------------------------
-- Module K7: recommendations
-- Recommendations generated per run.
-- ---------------------------------------------------------------------
CREATE TABLE recommendations (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recommendation_run_id  UUID NOT NULL REFERENCES recommendation_runs(id),
    career_id              UUID NOT NULL REFERENCES careers(id),
    rank_order             INT NOT NULL,
    recommendation_score   NUMERIC(10,2),
    recommendation_reason  TEXT,
    match_breakdown        JSONB,  -- Interest/Aptitude/Reality/Aspiration match detail
    created_at             TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_recommendations_run_career UNIQUE (recommendation_run_id, career_id)
);

-- ---------------------------------------------------------------------
-- Attach deferred FK constraints from V4 and V5 tables
-- ---------------------------------------------------------------------
ALTER TABLE assessment_configuration_group_items
    ADD CONSTRAINT fk_acg_items_iar_logic_version
        FOREIGN KEY (iar_logic_version_id) REFERENCES iar_logic_versions(id),
    ADD CONSTRAINT fk_acg_items_scoring_rule_version
        FOREIGN KEY (scoring_rule_version_id) REFERENCES scoring_rule_versions(id);

ALTER TABLE assessment_attempts
    ADD CONSTRAINT fk_attempts_iar_logic_version
        FOREIGN KEY (iar_logic_version_id) REFERENCES iar_logic_versions(id),
    ADD CONSTRAINT fk_attempts_scoring_rule_version
        FOREIGN KEY (scoring_rule_version_id) REFERENCES scoring_rule_versions(id);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V6 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_domain_scores_student ON domain_scores (student_id);
CREATE INDEX idx_domain_scores_attempt ON domain_scores (attempt_id);
CREATE INDEX idx_recommendation_runs_student ON recommendation_runs (student_id);
CREATE INDEX idx_recommendations_run ON recommendations (recommendation_run_id);

-- Supporting query indexes for lookups
CREATE INDEX idx_scoring_rules_tenant ON scoring_rules (tenant_id);
CREATE INDEX idx_scoring_rule_versions_rule ON scoring_rule_versions (scoring_rule_id);
CREATE INDEX idx_iar_logic_configs_tenant ON iar_logic_configs (tenant_id);
CREATE INDEX idx_iar_logic_versions_config ON iar_logic_versions (iar_logic_config_id);
CREATE INDEX idx_career_clusters_tenant ON career_clusters (tenant_id);
CREATE INDEX idx_careers_cluster ON careers (career_cluster_id);
CREATE INDEX idx_career_mappings_tenant_career ON career_mappings (tenant_id, career_id);

CREATE INDEX idx_scoring_rules_current_version ON scoring_rules(current_version_id);
CREATE INDEX idx_srv_published_by ON scoring_rule_versions(published_by);
CREATE INDEX idx_benchmarks_grade ON benchmarks(grade_config_id);
CREATE INDEX idx_benchmarks_age ON benchmarks(age_group_config_id);
CREATE INDEX idx_domain_scores_assessment ON domain_scores(assessment_id);
CREATE INDEX idx_domain_scores_benchmark ON domain_scores(benchmark_id);
CREATE INDEX idx_iar_logic_configs_current_version ON iar_logic_configs(current_version_id);
CREATE INDEX idx_ilv_published_by ON iar_logic_versions(published_by);
CREATE INDEX idx_rec_runs_config_group ON recommendation_runs(configuration_group_id);
CREATE INDEX idx_rec_runs_iar_logic_version ON recommendation_runs(iar_logic_version_id);
CREATE INDEX idx_rec_runs_generated_by ON recommendation_runs(generated_by);
CREATE INDEX idx_recs_career ON recommendations(career_id);
