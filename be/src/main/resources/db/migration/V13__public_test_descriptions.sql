-- Human-readable description of what each public test verifies.
ALTER TABLE challenge_public_tests
    ADD COLUMN IF NOT EXISTS description TEXT NOT NULL DEFAULT '';
