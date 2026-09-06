-- =====================================================================
-- V1__core_tenant_org_user_rbac.sql
-- Antarang CAP Platform — Core Tenant, Org Unit, User & RBAC schema
-- Scope: tenants, org_units, organizational_clusters,
--        organizational_cluster_members, users, roles, permissions,
--        role_permissions, user_roles, refresh_tokens, login_history
-- =====================================================================

-- Ensure extension is created in the public schema to avoid environments
-- where the search_path is empty or restricted.
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;  -- for gen_random_uuid()

-- ---------------------------------------------------------------------
-- Trigger Function for updated_at
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
-- ---------------------------------------------------------------------
-- Module A1: tenants
-- ---------------------------------------------------------------------
CREATE TABLE tenants (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code             VARCHAR(50) NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    logo_url         TEXT,
    branding_config  JSONB,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_update_tenants BEFORE UPDATE ON tenants FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module A2: org_units (generic hierarchy: STATE/DISTRICT/INSTITUTION/
-- SCHOOL/PROGRAM/COHORT)
-- ---------------------------------------------------------------------
CREATE TABLE org_units (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    parent_org_unit_id  UUID REFERENCES org_units(id),
    org_unit_type       VARCHAR(50) NOT NULL CHECK (org_unit_type IN ('STATE', 'DISTRICT', 'INSTITUTION', 'SCHOOL', 'PROGRAM', 'COHORT')),
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    address             TEXT,
    metadata            JSONB,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_update_org_units BEFORE UPDATE ON org_units FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module A3: organizational_clusters
-- ---------------------------------------------------------------------
CREATE TABLE organizational_clusters (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    code         VARCHAR(100) NOT NULL,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    cluster_type VARCHAR(50) NOT NULL CHECK (cluster_type IN ('REGION', 'PROGRAM', 'CUSTOM')),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_update_organizational_clusters BEFORE UPDATE ON organizational_clusters FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- ---------------------------------------------------------------------
-- Module A4: organizational_cluster_members
-- ---------------------------------------------------------------------
CREATE TABLE organizational_cluster_members (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cluster_id  UUID NOT NULL REFERENCES organizational_clusters(id),
    member_type VARCHAR(50) NOT NULL CHECK (member_type IN ('ORG_UNIT', 'USER')),
    member_id   UUID NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    added_at    TIMESTAMP NOT NULL DEFAULT now(),
    added_by    UUID,  -- FK to users.id added after users table exists
    CONSTRAINT uq_cluster_member UNIQUE (cluster_id, member_type, member_id)
);

-- ---------------------------------------------------------------------
-- Module B3: roles
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(50) NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module B4: permissions
-- ---------------------------------------------------------------------
CREATE TABLE permissions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(100) NOT NULL UNIQUE,
    module       VARCHAR(100) NOT NULL,
    description  TEXT,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- Module B5: role_permissions
-- ---------------------------------------------------------------------
CREATE TABLE role_permissions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id       UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
);

-- ---------------------------------------------------------------------
-- Module B1: users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                         UUID NOT NULL REFERENCES tenants(id),
    primary_org_unit_id               UUID REFERENCES org_units(id),
    username                          VARCHAR(100),
    email                             VARCHAR(255),
    mobile_number                     VARCHAR(20),
    password_hash                     TEXT NOT NULL,
    first_name                        VARCHAR(100) NOT NULL,
    last_name                         VARCHAR(100),
    user_type                         VARCHAR(30) NOT NULL CHECK (user_type IN ('STUDENT', 'FACILITATOR', 'ADMIN', 'SUB_ADMIN', 'SUPER_ADMIN')),
    date_of_birth                     DATE,
    gender_config_id                  UUID,  -- FK to configurations.id, added in V2
    preferred_platform_language_id    UUID,  -- FK to languages.id, added in V2
    preferred_assessment_language_id  UUID,  -- FK to languages.id, added in V2
    status                            VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING')),
    last_login_at                     TIMESTAMP,
    is_active                         BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted                        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                        TIMESTAMP NOT NULL DEFAULT now(),
    created_by                        UUID REFERENCES users(id),
    updated_by                        UUID REFERENCES users(id)
);

CREATE TRIGGER trigger_update_users BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- Now that users exists, attach the deferred FK from cluster members
ALTER TABLE organizational_cluster_members
    ADD CONSTRAINT fk_cluster_member_added_by FOREIGN KEY (added_by) REFERENCES users(id);

-- ---------------------------------------------------------------------
-- Module B6: user_roles (supports scoped access for Sub-Admins)
-- ---------------------------------------------------------------------
CREATE TABLE user_roles (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id),
    role_id      UUID NOT NULL REFERENCES roles(id),
    scope_type   VARCHAR(50) NOT NULL CHECK (scope_type IN ('TENANT', 'ORG_UNIT', 'CLUSTER', 'GLOBAL')),
    scope_id     UUID,  -- nullable for GLOBAL
    assigned_by  UUID REFERENCES users(id),
    assigned_at  TIMESTAMP NOT NULL DEFAULT now(),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_user_role_scope UNIQUE (user_id, role_id, scope_type, scope_id)
);

-- ---------------------------------------------------------------------
-- Module B7: refresh_tokens
-- ---------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  TEXT NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Module B8: login_history
-- ---------------------------------------------------------------------
CREATE TABLE login_history (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID REFERENCES users(id),  -- nullable for failed unknown login
    login_at       TIMESTAMP NOT NULL DEFAULT now(),
    ip_address     VARCHAR(100),
    user_agent     TEXT,
    login_status   VARCHAR(30) NOT NULL CHECK (login_status IN ('SUCCESS', 'FAILED')),
    failure_reason TEXT
);

-- ---------------------------------------------------------------------
-- Indexes (per design doc §8 — High Priority Indexes, V1 scope)
-- ---------------------------------------------------------------------
CREATE INDEX idx_org_units_tenant_type ON org_units (tenant_id, org_unit_type);
CREATE INDEX idx_org_units_parent ON org_units (parent_org_unit_id);
CREATE INDEX idx_cluster_members_member ON organizational_cluster_members (member_type, member_id);

CREATE UNIQUE INDEX uq_tenants_code_active ON tenants (code) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uq_org_units_tenant_type_code_active ON org_units (tenant_id, org_unit_type, code) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uq_org_clusters_tenant_code_active ON organizational_clusters (tenant_id, code) WHERE is_deleted = FALSE;

CREATE INDEX idx_users_tenant ON users (tenant_id);
CREATE INDEX idx_users_org_unit ON users (primary_org_unit_id);
CREATE INDEX idx_users_user_type ON users (user_type);
CREATE INDEX idx_users_status ON users (status);

CREATE UNIQUE INDEX uq_users_username_active ON users (username) WHERE username IS NOT NULL AND is_deleted = FALSE;
CREATE UNIQUE INDEX uq_users_email_active ON users (email) WHERE email IS NOT NULL AND is_deleted = FALSE;
CREATE UNIQUE INDEX uq_users_mobile_number_active ON users (mobile_number) WHERE mobile_number IS NOT NULL AND is_deleted = FALSE;
CREATE UNIQUE INDEX idx_users_email_lower_active ON users (LOWER(email)) WHERE email IS NOT NULL AND is_deleted = FALSE;

CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_scope ON user_roles (scope_type, scope_id);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_login_history_user ON login_history (user_id);

CREATE INDEX idx_users_created_by ON users(created_by);
CREATE INDEX idx_users_updated_by ON users(updated_by);
CREATE INDEX idx_users_gender_config ON users(gender_config_id);
CREATE INDEX idx_users_platform_lang ON users(preferred_platform_language_id);
CREATE INDEX idx_users_assessment_lang ON users(preferred_assessment_language_id);
CREATE INDEX idx_cluster_members_added_by ON organizational_cluster_members(added_by);
CREATE INDEX idx_user_roles_assigned_by ON user_roles(assigned_by);

