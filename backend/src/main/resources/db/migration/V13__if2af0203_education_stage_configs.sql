-- IF2AF0203 Career Explorer: education stage options for signup.

INSERT INTO configuration_groups (tenant_id, code, name, description, is_system_defined)
SELECT NULL, 'EDUCATION_STAGE', 'Education Stage', 'Stage in education for Career Explorer sign-up', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM configuration_groups WHERE code = 'EDUCATION_STAGE' AND tenant_id IS NULL
);

INSERT INTO configurations (configuration_group_id, code, value, display_order)
SELECT g.id, v.code, v.value, v.ord
FROM configuration_groups g
JOIN (VALUES
    ('EDUCATION_STAGE', 'IN_SCHOOL', 'Currently in school', 1),
    ('EDUCATION_STAGE', 'DROPPED_OUT', 'Dropped out of school', 2),
    ('EDUCATION_STAGE', 'COMPLETED_SCHOOL', 'Completed school', 3),
    ('EDUCATION_STAGE', 'OTHER', 'Other', 4)
) AS v(group_code, code, value, ord) ON g.code = v.group_code AND g.tenant_id IS NULL
WHERE NOT EXISTS (
    SELECT 1 FROM configurations c
    WHERE c.configuration_group_id = g.id AND c.code = v.code
);
