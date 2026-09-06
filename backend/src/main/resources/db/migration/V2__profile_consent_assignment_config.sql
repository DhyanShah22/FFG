-- =====================================================================
-- V2__profile_consent_assignment_config.sql
-- Antarang CAP Platform — Profiles, Consent, Facilitator-Student
-- Assignment, Configuration Groups & Localization
-- Scope: user_profiles, consent_records, facilitator_student_assignments,
--        assignment_history, configuration_groups, configurations,
--        languages, translations, tenant_configurations
-- Depends on: V1__core_tenant_org_user_rbac.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module D1: configuration_groups
-- (created before user_profiles / users FKs since both depend on it)
-- ---------------------------------------------------------------------
CREATE TABLE configuration_groups (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID REFERENCES tenants(id),  -- nullable = global config
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(150) NOT NULL,
    description         TEXT,
    is_system_defined   BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_update_configuration_groups BEFORE UPDATE ON configuration_groups FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- Partial unique index for global configuration groups (where tenant_id IS NULL)
CREATE UNIQUE INDEX uq_config_groups_global_code_active
    ON configuration_groups (code) WHERE tenant_id IS NULL AND is_deleted = FALSE;


-- ---------------------------------------------------------------------
-- Module D2: configurations
-- ---------------------------------------------------------------------
CREATE TABLE configurations (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuration_group_id   UUID NOT NULL REFERENCES configuration_groups(id),
    code                     VARCHAR(100) NOT NULL,
    value                    VARCHAR(200) NOT NULL,
    display_order            INT NOT NULL DEFAULT 0,
    metadata                 JSONB,
    is_active                BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted               BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- Module D3: languages
-- ---------------------------------------------------------------------
CREATE TABLE languages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(10) NOT NULL UNIQUE,   -- en, hi, mr
    name         VARCHAR(100) NOT NULL,
    native_name  VARCHAR(100),
    is_default   BOOLEAN NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- Module D4: translations (generic platform/UI translations)
-- ---------------------------------------------------------------------
CREATE TABLE translations (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_id       UUID NOT NULL REFERENCES languages(id),
    translation_key   VARCHAR(200) NOT NULL,
    translation_value TEXT NOT NULL,
    module            VARCHAR(100),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_translations_lang_key UNIQUE (language_id, translation_key)
);

-- ---------------------------------------------------------------------
-- Module D5: tenant_configurations
-- ---------------------------------------------------------------------
CREATE TABLE tenant_configurations (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL REFERENCES tenants(id),
    configuration_key     VARCHAR(150) NOT NULL,
    configuration_value   JSONB NOT NULL,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_config_key UNIQUE (tenant_id, configuration_key)
);

CREATE TRIGGER trigger_update_tenant_configurations BEFORE UPDATE ON tenant_configurations FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Deferred FKs on users (from V1) — now that configurations/languages exist
-- ---------------------------------------------------------------------
ALTER TABLE users
    ADD CONSTRAINT fk_users_gender_config
        FOREIGN KEY (gender_config_id) REFERENCES configurations(id),
    ADD CONSTRAINT fk_users_platform_language
        FOREIGN KEY (preferred_platform_language_id) REFERENCES languages(id),
    ADD CONSTRAINT fk_users_assessment_language
        FOREIGN KEY (preferred_assessment_language_id) REFERENCES languages(id);

-- ---------------------------------------------------------------------
-- Module B2: user_profiles
-- ---------------------------------------------------------------------
CREATE TABLE user_profiles (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL UNIQUE REFERENCES users(id),
    grade_config_id       UUID REFERENCES configurations(id),
    age_group_config_id   UUID REFERENCES configurations(id),
    guardian_name         VARCHAR(150),
    guardian_mobile       VARCHAR(20),
    guardian_email        VARCHAR(255),
    address_line_1        TEXT,
    address_line_2        TEXT,
    city                  VARCHAR(100),
    state                 VARCHAR(100),
    pincode               VARCHAR(20),
    profile_data          JSONB,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_update_user_profiles BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module B9: consent_records
-- ---------------------------------------------------------------------
CREATE TABLE consent_records (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id),
    consent_type            VARCHAR(50) NOT NULL CHECK (consent_type IN ('SELF', 'GUARDIAN')),
    consent_text_version    VARCHAR(50) NOT NULL,
    guardian_name           VARCHAR(150),
    guardian_contact        VARCHAR(50),
    consent_given           BOOLEAN NOT NULL,
    consent_given_at        TIMESTAMP,
    consent_withdrawn_at    TIMESTAMP,
    metadata                JSONB,
    created_at              TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module C1: facilitator_student_assignments
-- ---------------------------------------------------------------------
CREATE TABLE facilitator_student_assignments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    facilitator_id  UUID NOT NULL REFERENCES users(id),
    student_id      UUID NOT NULL REFERENCES users(id),
    org_unit_id     UUID REFERENCES org_units(id),
    assigned_by     UUID REFERENCES users(id),
    assigned_at     TIMESTAMP NOT NULL DEFAULT now(),
    unassigned_at   TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_fsa_different_users CHECK (facilitator_id <> student_id)
);

-- Only one *active* facilitator per student at a time
CREATE UNIQUE INDEX uq_fsa_active_assignment
    ON facilitator_student_assignments (facilitator_id, student_id)
    WHERE is_active = true;

-- ---------------------------------------------------------------------
-- Module C2: assignment_history
-- ---------------------------------------------------------------------
CREATE TABLE assignment_history (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id        UUID NOT NULL REFERENCES facilitator_student_assignments(id),
    student_id           UUID NOT NULL REFERENCES users(id),
    old_facilitator_id   UUID REFERENCES users(id),
    new_facilitator_id   UUID REFERENCES users(id),
    action               VARCHAR(50) NOT NULL CHECK (action IN ('ASSIGNED', 'REASSIGNED', 'UNASSIGNED')),
    performed_by         UUID NOT NULL REFERENCES users(id),
    performed_at         TIMESTAMP NOT NULL DEFAULT now(),
    remarks              TEXT
);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — V2 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_fsa_facilitator ON facilitator_student_assignments (facilitator_id);
CREATE INDEX idx_fsa_student ON facilitator_student_assignments (student_id);
CREATE INDEX idx_fsa_org_unit ON facilitator_student_assignments (org_unit_id);
CREATE INDEX idx_fsa_assigned_by ON facilitator_student_assignments(assigned_by);

-- Not in doc's explicit list, but needed given consent lookups are
-- always by user and translations/config lookups are always by group
CREATE INDEX idx_consent_records_user ON consent_records (user_id);
CREATE INDEX idx_assignment_history_assignment ON assignment_history (assignment_id);
CREATE INDEX idx_assignment_history_student ON assignment_history (student_id);
CREATE INDEX idx_assignment_history_performed_by ON assignment_history(performed_by);
CREATE INDEX idx_configurations_group ON configurations (configuration_group_id);
CREATE INDEX idx_translations_language ON translations (language_id);
CREATE INDEX idx_tenant_configurations_tenant ON tenant_configurations (tenant_id);

CREATE UNIQUE INDEX uq_config_groups_tenant_code_active ON configuration_groups (tenant_id, code) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uq_configurations_group_code_active ON configurations (configuration_group_id, code) WHERE is_deleted = FALSE;

CREATE INDEX idx_user_profiles_grade ON user_profiles(grade_config_id);
CREATE INDEX idx_user_profiles_age ON user_profiles(age_group_config_id);