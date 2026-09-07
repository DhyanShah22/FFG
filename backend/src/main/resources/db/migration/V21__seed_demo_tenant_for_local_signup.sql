-- Seed the tenant used by the local frontend signup flow.
-- The insert is idempotent and does not replace an existing tenant.
INSERT INTO tenants (code, name, description)
SELECT 'demo', 'Demo Tenant', 'Tenant for local signup testing'
WHERE NOT EXISTS (
    SELECT 1
    FROM tenants
    WHERE code = 'demo'
      AND is_deleted = FALSE
);
