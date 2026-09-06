-- IF2AF0203 signup profile fields and DATA_ANALYST profile type.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS middle_name VARCHAR(100);

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS country VARCHAR(100);

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_profile_type_check;

ALTER TABLE users
    ADD CONSTRAINT users_profile_type_check CHECK (
        profile_type IN (
            'CAREER_EXPLORER',
            'CAREER_COUNSELLOR',
            'ADMINISTRATOR',
            'DATA_ANALYST'
        )
    );

INSERT INTO roles (code, name, description, is_system_role)
SELECT 'DATA_ANALYST', 'Data Analyst', 'Data analyst with scoped read access', TRUE
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'DATA_ANALYST');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('USER_READ', 'ASSESSMENT_READ', 'REPORT_READ', 'AUDIT_READ')
WHERE r.code = 'DATA_ANALYST'
ON CONFLICT (role_id, permission_id) DO NOTHING;
