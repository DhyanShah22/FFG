-- IF2AF0203: user_type -> profile_type, remap profile values, rename role codes, scope SUPER_ADMIN permissions.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'user_type'
    ) THEN
        ALTER TABLE users RENAME COLUMN user_type TO profile_type;
    END IF;
END $$;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_type_check;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_profile_type_check;

UPDATE users
SET profile_type = CASE profile_type
    WHEN 'STUDENT' THEN 'CAREER_EXPLORER'
    WHEN 'FACILITATOR' THEN 'CAREER_COUNSELLOR'
    WHEN 'ADMIN' THEN 'ADMINISTRATOR'
    WHEN 'SUB_ADMIN' THEN 'ADMINISTRATOR'
    WHEN 'SUPER_ADMIN' THEN 'ADMINISTRATOR'
    ELSE profile_type
END;

ALTER TABLE users
    ADD CONSTRAINT users_profile_type_check CHECK (
        profile_type IN ('CAREER_EXPLORER', 'CAREER_COUNSELLOR', 'ADMINISTRATOR')
    );

UPDATE roles SET code = 'CAREER_EXPLORER', name = 'Career Explorer', description = 'Career explorer with read-only self access'
WHERE code = 'STUDENT';

UPDATE roles SET code = 'CAREER_COUNSELLOR', name = 'Career Counsellor', description = 'Career counsellor with scoped read access'
WHERE code = 'FACILITATOR';

UPDATE roles SET code = 'ADMINISTRATOR', name = 'Administrator', description = 'Tenant administrator'
WHERE code = 'ADMIN';

DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM roles WHERE code = 'SUPER_ADMIN');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('USER_WRITE', 'TENANT_WRITE')
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
