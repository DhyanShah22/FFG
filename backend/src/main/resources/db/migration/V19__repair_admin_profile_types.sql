-- Repair users created with an administrator role but the default career explorer profile type.
UPDATE users u
SET profile_type = 'ADMINISTRATOR'
FROM user_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE ur.user_id = u.id
  AND r.code = 'ADMINISTRATOR'
  AND u.profile_type <> 'ADMINISTRATOR';