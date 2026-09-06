-- =====================================================================
-- V9__seed_mvp_roles_permissions.sql
-- Antarang CAP Platform — MVP Seed Data
-- Scope: Seed Roles, Core Configuration Groups, Languages, Permissions
--        and Role-Permission Mappings for MVP
-- Depends on: V1__core_tenant_org_user_rbac.sql
--             V2__profile_consent_assignment_config.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Seed Roles
-- ---------------------------------------------------------------------
INSERT INTO roles (code, name, description, is_system_role) VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'Global system administrator', TRUE),
    ('ADMIN', 'Tenant Administrator', 'Administrator for a specific tenant', TRUE),
    ('SUB_ADMIN', 'Sub-Administrator', 'Limited administrator for specific org units', TRUE),
    ('FACILITATOR', 'Facilitator/Counsellor', 'Conducts sessions and assessments', TRUE),
    ('STUDENT', 'Student/Candidate', 'End user taking assessments', TRUE)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Seed Configuration Groups
-- ---------------------------------------------------------------------
INSERT INTO configuration_groups (tenant_id, code, name, is_system_defined) VALUES
    (NULL, 'GENDER', 'Gender', TRUE),
    (NULL, 'GRADE', 'Grade', TRUE),
    (NULL, 'AGE_GROUP', 'Age Group', TRUE),
    (NULL, 'INSTITUTION_TYPE', 'Institution Type', TRUE),
    (NULL, 'ASSESSMENT_LANGUAGE', 'Assessment Language', TRUE),
    (NULL, 'PLATFORM_LANGUAGE', 'Platform Language', TRUE),
    (NULL, 'ASSESSMENT_TIMER', 'Assessment Timer', TRUE),
    (NULL, 'ASSESSMENT_CYCLE', 'Assessment Cycle', TRUE),
    (NULL, 'ACADEMIC_YEAR', 'Academic Year', TRUE)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- 3. Seed Languages
-- ---------------------------------------------------------------------
INSERT INTO languages (code, name, native_name, is_default) VALUES
    ('en', 'English', 'English', TRUE),
    ('hi', 'Hindi', 'हिन्दी', FALSE)
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------
-- 4. Seed Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, module, description, is_active)
VALUES
    -- User & RBAC permissions
    ('USER_READ',             'USER',          'View user profile details',               TRUE),
    ('USER_WRITE',            'USER',          'Create or update user accounts',          TRUE),
    ('USER_DELETE',           'USER',          'Soft delete or deactivate users',         TRUE),

    -- Assessment permissions
    ('ASSESSMENT_READ',       'ASSESSMENT',    'View assessment master definitions',      TRUE),
    ('ASSESSMENT_EXECUTE',    'ASSESSMENT',    'Take and submit assessments',             TRUE),
    ('ASSESSMENT_CONFIG',     'ASSESSMENT',    'Configure assessment parameters',         TRUE),

    -- Question bank & Questionnaire permissions
    ('QUESTION_READ',         'QUESTION',      'View questions in question bank',         TRUE),
    ('QUESTION_WRITE',        'QUESTION',      'Create/edit question bank items',          TRUE),
    ('QUESTIONNAIRE_READ',    'QUESTIONNAIRE', 'View questionnaires and versioning',      TRUE),
    ('QUESTIONNAIRE_WRITE',   'QUESTIONNAIRE', 'Create/edit questionnaire versions',       TRUE),

    -- Report & Counsellor feedback permissions
    ('REPORT_READ',           'REPORT',        'View generated student reports',          TRUE),
    ('REPORT_GENERATE',       'REPORT',        'Trigger PDF report generation',           TRUE),
    ('FEEDBACK_READ',         'FEEDBACK',      'View counsellor feedback',                TRUE),
    ('FEEDBACK_WRITE',        'FEEDBACK',      'Add/edit counsellor feedback notes',       TRUE),

    -- System & Config permissions
    ('CONFIG_READ',           'CONFIG',        'View system configurations',              TRUE),
    ('CONFIG_WRITE',          'CONFIG',        'Modify system configurations',            TRUE),
    ('AUDIT_READ',            'AUDIT',         'View audit and activity logs',            TRUE)
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------
-- 5. Seed Role-Permissions Mapping
-- ---------------------------------------------------------------------

-- STUDENT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'STUDENT'
  AND p.code IN ('USER_READ', 'ASSESSMENT_EXECUTE', 'REPORT_READ', 'FEEDBACK_READ')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- FACILITATOR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'FACILITATOR'
  AND p.code IN ('USER_READ', 'ASSESSMENT_READ', 'REPORT_READ', 'REPORT_GENERATE', 'FEEDBACK_READ', 'FEEDBACK_WRITE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ADMIN & SUB_ADMIN permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code IN ('ADMIN', 'SUB_ADMIN')
  AND p.code IN (
      'USER_READ', 'USER_WRITE', 'ASSESSMENT_READ', 'ASSESSMENT_CONFIG',
      'QUESTION_READ', 'QUESTION_WRITE', 'QUESTIONNAIRE_READ', 'QUESTIONNAIRE_WRITE',
      'REPORT_READ', 'REPORT_GENERATE', 'FEEDBACK_READ', 'FEEDBACK_WRITE',
      'CONFIG_READ', 'CONFIG_WRITE', 'AUDIT_READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- SUPER_ADMIN permissions (All permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
