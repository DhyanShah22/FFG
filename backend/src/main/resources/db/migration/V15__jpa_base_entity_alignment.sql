-- JPA BaseEntity column alignment for NGO schema tables used by the Spring backend.

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE org_units ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE org_units ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE organizational_clusters ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE organizational_clusters ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE organizational_clusters ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE organizational_clusters ADD COLUMN IF NOT EXISTS deleted_by UUID;

ALTER TABLE configuration_groups ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE configuration_groups ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE configuration_groups ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE configuration_groups ADD COLUMN IF NOT EXISTS deleted_by UUID;

ALTER TABLE configurations ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE configurations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE configurations ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE configurations ADD COLUMN IF NOT EXISTS updated_by UUID;
ALTER TABLE configurations ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE configurations ADD COLUMN IF NOT EXISTS deleted_by UUID;

ALTER TABLE roles ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE permissions ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS updated_by UUID;
