ALTER TABLE users       ADD COLUMN avatar_updated_at TIMESTAMPTZ;

ALTER TABLE auth_tokens ADD COLUMN email TEXT;
UPDATE auth_tokens at SET email = u.email FROM users u WHERE at.user_id = u.id;
ALTER TABLE auth_tokens ALTER COLUMN email SET NOT NULL;

