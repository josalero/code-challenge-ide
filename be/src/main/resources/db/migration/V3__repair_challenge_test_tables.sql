-- Repair databases that ran an older V1 before challenge_*_tests tables existed.
-- Safe to run on fresh installs (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS challenge_public_tests (
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    name VARCHAR(256) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS challenge_hidden_tests (
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    name VARCHAR(256) NOT NULL,
    test_source TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);
