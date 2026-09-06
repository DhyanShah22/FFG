-- V11 renamed user_type -> profile_type but only dropped users_type_check; V1 created users_user_type_check.

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_user_type_check;
