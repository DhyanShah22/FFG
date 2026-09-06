-- =====================================================================
-- V8__notifications_integrations_audit.sql
-- Antarang CAP Platform — External Integration Readiness,
-- Notifications Foundation, Audit & Activity Logging
-- Scope: integration_clients, external_id_mappings, integration_events,
--        notification_templates, notification_logs,
--        audit_logs, activity_logs
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Module M1: integration_clients
-- Stores external application/client integration credentials and scopes.
-- ---------------------------------------------------------------------
CREATE TABLE integration_clients (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    client_code     VARCHAR(100) NOT NULL,
    client_name     VARCHAR(200) NOT NULL,
    client_type     VARCHAR(50) NOT NULL CHECK (client_type IN ('API', 'PARTNER', 'SYSTEM')),  -- API / PARTNER / SYSTEM
    api_key_hash    TEXT,
    allowed_scopes  JSONB,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_integration_clients_tenant_code UNIQUE (tenant_id, client_code)
);

-- ---------------------------------------------------------------------
-- Module M2: external_id_mappings
-- Maps external IDs (from partner systems) to CAP internal records.
-- ---------------------------------------------------------------------
CREATE TABLE external_id_mappings (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL REFERENCES tenants(id),
    external_system       VARCHAR(100) NOT NULL,
    external_entity_type  VARCHAR(100) NOT NULL CHECK (external_entity_type IN ('STUDENT', 'ATTEMPT', 'REPORT')),  -- STUDENT / ATTEMPT / REPORT
    external_entity_id    VARCHAR(200) NOT NULL,
    internal_entity_type  VARCHAR(100) NOT NULL CHECK (internal_entity_type IN ('USER', 'ATTEMPT', 'REPORT')),  -- USER / ATTEMPT / REPORT
    internal_entity_id    UUID NOT NULL,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_id_mappings UNIQUE (external_system, external_entity_type, external_entity_id)
);

-- ---------------------------------------------------------------------
-- Module M3: integration_events
-- Logs inbound and outbound integration calls.
-- ---------------------------------------------------------------------
CREATE TABLE integration_events (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              UUID NOT NULL REFERENCES tenants(id),
    integration_client_id  UUID NOT NULL REFERENCES integration_clients(id),
    event_type             VARCHAR(100) NOT NULL CHECK (event_type IN ('STUDENT_CREATE', 'RESULT_FETCH', 'REPORT_FETCH')),  -- STUDENT_CREATE / RESULT_FETCH / REPORT_FETCH
    direction              VARCHAR(20) NOT NULL CHECK (direction IN ('INBOUND', 'OUTBOUND')),   -- INBOUND / OUTBOUND
    request_payload        JSONB,
    response_payload       JSONB,
    status                 VARCHAR(30) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),   -- SUCCESS / FAILED
    error_message          TEXT,
    created_at             TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module N1: notification_templates
-- Notification template definition (email, sms, etc.).
-- tenant_id is nullable for global system templates.
-- ---------------------------------------------------------------------
CREATE TABLE notification_templates (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID REFERENCES tenants(id),  -- nullable for global templates
    code              VARCHAR(100) NOT NULL,         -- ACCOUNT_CREATED / PASSWORD_RESET etc.
    channel           VARCHAR(30) NOT NULL CHECK (channel IN ('EMAIL', 'SMS')),          -- EMAIL / SMS
    subject_template  TEXT,                          -- required for EMAIL
    body_template     TEXT NOT NULL,
    language_id       UUID NOT NULL REFERENCES languages(id),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_notification_templates_tenant_code_channel_lang UNIQUE (tenant_id, code, channel, language_id)
);

-- Partial unique index for global notification templates (where tenant_id IS NULL)
CREATE UNIQUE INDEX uq_notification_templates_global_code_channel_lang
    ON notification_templates (code, channel, language_id) WHERE tenant_id IS NULL;

-- ---------------------------------------------------------------------
-- Module N2: notification_logs
-- Logs outbound notification dispatches and provider responses.
-- ---------------------------------------------------------------------
CREATE TABLE notification_logs (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          UUID NOT NULL REFERENCES tenants(id),
    user_id            UUID NOT NULL REFERENCES users(id),
    template_id        UUID NOT NULL REFERENCES notification_templates(id),
    channel            VARCHAR(30) NOT NULL CHECK (channel IN ('EMAIL', 'SMS')),  -- EMAIL / SMS
    recipient          VARCHAR(255) NOT NULL,
    status             VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),  -- PENDING / SENT / FAILED
    provider_response  JSONB,
    sent_at            TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module O1: audit_logs
-- Audit trail tracking changes to business/configuration entities.
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID REFERENCES tenants(id),  -- nullable
    entity_name   VARCHAR(100) NOT NULL,        -- Table or module name
    entity_id     UUID NOT NULL,                -- Changed entity PK
    action        VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'PUBLISH', 'ASSIGN')),         -- CREATE / UPDATE / DELETE / STATUS_CHANGE / PUBLISH / ASSIGN
    old_value     JSONB,
    new_value     JSONB,
    performed_by  UUID REFERENCES users(id),
    performed_at  TIMESTAMP NOT NULL DEFAULT now(),
    ip_address    VARCHAR(100),
    user_agent    TEXT
);

-- ---------------------------------------------------------------------
-- Module O2: activity_logs
-- Tracks user activities across the platform.
-- ---------------------------------------------------------------------
CREATE TABLE activity_logs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID REFERENCES tenants(id),  -- nullable
    user_id        UUID REFERENCES users(id),
    activity_type  VARCHAR(100) NOT NULL CHECK (activity_type IN ('LOGIN', 'LOGOUT', 'ASSESSMENT_SUBMIT', 'REPORT_DOWNLOAD')),        -- LOGIN / LOGOUT / ASSESSMENT_SUBMIT / REPORT_DOWNLOAD
    description    TEXT,
    metadata       JSONB,
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V8 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_audit_entity ON audit_logs (entity_name, entity_id);
CREATE INDEX idx_audit_performed_by ON audit_logs (performed_by);
CREATE INDEX idx_audit_performed_at ON audit_logs (performed_at);

CREATE INDEX idx_activity_user ON activity_logs (user_id);
CREATE INDEX idx_activity_type ON activity_logs (activity_type);

CREATE INDEX idx_external_mapping_external ON external_id_mappings (external_system, external_entity_type, external_entity_id);
CREATE INDEX idx_integration_events_client ON integration_events (integration_client_id);
CREATE INDEX idx_notification_logs_user ON notification_logs (user_id);
CREATE INDEX idx_notification_logs_template ON notification_logs(template_id);
