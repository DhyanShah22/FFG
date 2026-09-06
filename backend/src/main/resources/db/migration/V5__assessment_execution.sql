-- =====================================================================
-- V5__assessment_execution.sql
-- Antarang CAP Platform — Assessment Execution Schema
-- Scope: assessment_attempts, assessment_responses,
--        assessment_response_options, assessment_question_timings,
--        attempt_status_history
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
--             V3__question_bank_questionnaire.sql
--             V4__assessment_config_groups.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module I1: assessment_attempts
-- Stores each assessment attempt with full configuration version traceability.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_attempts (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   UUID NOT NULL REFERENCES tenants(id),
    student_id                  UUID NOT NULL REFERENCES users(id),
    assessment_id               UUID NOT NULL REFERENCES assessments(id),
    assessment_version_id       UUID NOT NULL REFERENCES assessment_versions(id),
    configuration_group_id      UUID NOT NULL REFERENCES assessment_configuration_groups(id),
    questionnaire_version_id     UUID NOT NULL REFERENCES questionnaire_versions(id),
    iar_logic_version_id         UUID,  -- FK to iar_logic_versions(id) attached in V6
    scoring_rule_version_id      UUID,  -- FK to scoring_rule_versions(id) attached in V6
    report_template_version_id   UUID,  -- FK to report_template_versions(id) attached in V7
    assessment_language_id       UUID NOT NULL REFERENCES languages(id),
    output_language_id          UUID NOT NULL REFERENCES languages(id),
    status                      VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'EVALUATED', 'EXPIRED', 'CANCELLED')),
    timer_mode                  VARCHAR(30) NOT NULL DEFAULT 'COUNT_UP' CHECK (timer_mode IN ('COUNT_UP', 'COUNT_DOWN', 'NONE')),
    started_at                  TIMESTAMP NOT NULL DEFAULT now(),
    submitted_at                TIMESTAMP,
    evaluated_at                TIMESTAMP,
    elapsed_seconds             INT,  -- Nullable until submit
    submission_reason           VARCHAR(50) CHECK (submission_reason IN ('MANUAL_SUBMIT', 'AUTO_SUBMIT', 'ADMIN_FORCE_SUBMIT', 'SESSION_EXPIRED', 'RESTARTED')),
    score_status                VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (score_status IN ('PENDING', 'COMPLETED', 'FAILED')),
    is_admin_test_attempt       BOOLEAN NOT NULL DEFAULT FALSE,
    exclude_from_analytics      BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- Module I2: assessment_responses
-- Stores individual responses to assessment questions.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_responses (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id        UUID NOT NULL REFERENCES assessment_attempts(id),
    question_id       UUID NOT NULL REFERENCES questions(id),
    response_text     TEXT,             -- For text answers
    response_numeric  NUMERIC(10,2),    -- For rating/numeric answers
    response_json     JSONB,            -- Flexible answer payload
    answered_at       TIMESTAMP NOT NULL DEFAULT now(),
    is_skipped        BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_assessment_responses_attempt_question UNIQUE (attempt_id, question_id)
);

-- ---------------------------------------------------------------------
-- Module I3: assessment_response_options
-- Stores selected options for radio/checkbox questions.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_response_options (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    response_id  UUID NOT NULL REFERENCES assessment_responses(id),
    option_id    UUID NOT NULL REFERENCES question_options(id),
    CONSTRAINT uq_assessment_response_options_response_option UNIQUE (response_id, option_id)
);

-- ---------------------------------------------------------------------
-- Module I4: assessment_question_timings
-- Tracks time spent per question.
-- ---------------------------------------------------------------------
CREATE TABLE assessment_question_timings (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id       UUID NOT NULL REFERENCES assessment_attempts(id),
    question_id      UUID NOT NULL REFERENCES questions(id),
    started_at       TIMESTAMP,
    ended_at         TIMESTAMP,
    elapsed_seconds  INT,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_assessment_question_timings_attempt_question UNIQUE (attempt_id, question_id)
);

-- ---------------------------------------------------------------------
-- Module I5: attempt_status_history
-- Audit log for assessment attempt status transitions.
-- ---------------------------------------------------------------------
CREATE TABLE attempt_status_history (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id  UUID NOT NULL REFERENCES assessment_attempts(id),
    old_status  VARCHAR(30),
    new_status  VARCHAR(30) NOT NULL,
    changed_at  TIMESTAMP NOT NULL DEFAULT now(),
    changed_by  UUID REFERENCES users(id),
    remarks     TEXT
);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V5 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_attempts_student ON assessment_attempts (student_id);
CREATE INDEX idx_attempts_status ON assessment_attempts (status);
CREATE INDEX idx_attempts_config_group ON assessment_attempts (configuration_group_id);
CREATE INDEX idx_attempts_traceability ON assessment_attempts (questionnaire_version_id, iar_logic_version_id, scoring_rule_version_id);

CREATE INDEX idx_responses_attempt ON assessment_responses (attempt_id);
CREATE INDEX idx_question_timings_attempt ON assessment_question_timings (attempt_id);
CREATE INDEX idx_attempt_status_history_attempt ON attempt_status_history (attempt_id);

CREATE INDEX idx_attempts_assessment ON assessment_attempts(assessment_id);
CREATE INDEX idx_attempts_assessment_version ON assessment_attempts(assessment_version_id);
CREATE INDEX idx_attempts_assessment_lang ON assessment_attempts(assessment_language_id);
CREATE INDEX idx_attempts_output_lang ON assessment_attempts(output_language_id);
CREATE INDEX idx_attempts_report_template_version ON assessment_attempts(report_template_version_id);
CREATE INDEX idx_attempt_status_changed_by ON attempt_status_history(changed_by);
