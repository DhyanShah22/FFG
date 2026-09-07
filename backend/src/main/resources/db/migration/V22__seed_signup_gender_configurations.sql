-- Seed the system gender options required by the public signup flow.
INSERT INTO configurations (configuration_group_id, code, value, display_order)
SELECT g.id, v.code, v.value, v.display_order
FROM configuration_groups g
JOIN (VALUES
    ('FEMALE', 'Female', 1),
    ('MALE', 'Male', 2),
    ('NON_BINARY', 'Non-binary', 3),
    ('PREFER_NOT_TO_SAY', 'Prefer not to say', 4)
) AS v(code, value, display_order) ON TRUE
WHERE g.code = 'GENDER'
  AND g.tenant_id IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM configurations c
      WHERE c.configuration_group_id = g.id
        AND c.code = v.code
  );
