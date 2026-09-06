-- Backend auth tokens, career translation FKs, soft-delete audit columns, JPA alignment, TENANT_WRITE permission.

CREATE TABLE IF NOT EXISTS verification_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users (id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    token_type  VARCHAR(50)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT verification_tokens_type_check CHECK (
        token_type IN ('PASSWORD_RESET', 'EMAIL_VERIFICATION')
    )
);

CREATE INDEX IF NOT EXISTS idx_verification_tokens_user_id ON verification_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_token_hash ON verification_tokens (token_hash);

CREATE TABLE IF NOT EXISTS career_cluster_translations (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    career_cluster_id  UUID         NOT NULL REFERENCES career_clusters (id),
    language_id        UUID         NOT NULL REFERENCES languages (id),
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT career_cluster_translations_unique UNIQUE (career_cluster_id, language_id)
);

CREATE INDEX IF NOT EXISTS idx_career_cluster_translations_cluster_id ON career_cluster_translations (career_cluster_id);
CREATE INDEX IF NOT EXISTS idx_career_cluster_translations_language_id ON career_cluster_translations (language_id);

CREATE TABLE IF NOT EXISTS career_translations (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    career_id          UUID         NOT NULL REFERENCES careers (id),
    language_id        UUID         NOT NULL REFERENCES languages (id),
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    education_pathway  TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT career_translations_unique UNIQUE (career_id, language_id)
);

CREATE INDEX IF NOT EXISTS idx_career_translations_career_id ON career_translations (career_id);
CREATE INDEX IF NOT EXISTS idx_career_translations_language_id ON career_translations (language_id);

ALTER TABLE tenants     ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP, ADD COLUMN IF NOT EXISTS deleted_by UUID;
ALTER TABLE org_units   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP, ADD COLUMN IF NOT EXISTS deleted_by UUID;
ALTER TABLE users       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP, ADD COLUMN IF NOT EXISTS deleted_by UUID;
ALTER TABLE roles       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP, ADD COLUMN IF NOT EXISTS deleted_by UUID;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP, ADD COLUMN IF NOT EXISTS deleted_by UUID;
ALTER TABLE user_roles  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP, ADD COLUMN IF NOT EXISTS deleted_by UUID;

ALTER TABLE roles       ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE roles       ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE user_roles  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE user_roles  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE user_roles  ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE languages ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE languages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE languages ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE languages ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE languages ADD COLUMN IF NOT EXISTS deleted_by UUID;
ALTER TABLE languages ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE languages ADD COLUMN IF NOT EXISTS updated_by UUID;

INSERT INTO permissions (code, module, description, is_active)
SELECT 'TENANT_WRITE', 'TENANT', 'Create and manage tenants', TRUE
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'TENANT_WRITE');
