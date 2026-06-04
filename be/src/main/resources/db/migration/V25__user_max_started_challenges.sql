ALTER TABLE users
    ADD COLUMN max_started_challenges INTEGER NULL;

COMMENT ON COLUMN users.max_started_challenges IS
    'Per-user exercise slot override. NULL = platform default; 0 = unlimited; >0 = explicit cap.';
