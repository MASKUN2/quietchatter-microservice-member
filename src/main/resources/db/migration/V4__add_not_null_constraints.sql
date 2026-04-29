UPDATE member SET created_at = NOW() WHERE created_at IS NULL;
UPDATE member SET last_modified_at = NOW() WHERE last_modified_at IS NULL;

ALTER TABLE member
    ALTER COLUMN nickname SET NOT NULL,
    ALTER COLUMN role SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN provider SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN last_modified_at SET NOT NULL;
