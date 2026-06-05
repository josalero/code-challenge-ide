ALTER TABLE users
    ADD COLUMN integrity_monitoring_disabled BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN users.integrity_monitoring_disabled IS
    'When true, clipboard/focus integrity monitoring is off for this learner.';

CREATE TABLE challenge_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    challenge_id UUID NOT NULL REFERENCES challenges (id),
    started_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    end_reason VARCHAR(32)
);

CREATE UNIQUE INDEX challenge_sessions_active_user_challenge_idx
    ON challenge_sessions (user_id, challenge_id)
    WHERE ended_at IS NULL;

CREATE INDEX challenge_sessions_user_challenge_started_idx
    ON challenge_sessions (user_id, challenge_id, started_at DESC);

CREATE TABLE challenge_integrity_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    challenge_id UUID NOT NULL REFERENCES challenges (id),
    session_id UUID REFERENCES challenge_sessions (id),
    event_type VARCHAR(32) NOT NULL,
    editor_surface VARCHAR(32),
    char_count INTEGER,
    away_ms BIGINT,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX challenge_integrity_events_user_challenge_idx
    ON challenge_integrity_events (user_id, challenge_id, occurred_at DESC);

INSERT INTO challenge_integrity_events (
    id, user_id, challenge_id, session_id, event_type, editor_surface, char_count, away_ms, occurred_at)
SELECT
    id, user_id, challenge_id, NULL, event_type, editor_surface, char_count, NULL, occurred_at
FROM challenge_clipboard_events;

DROP TABLE challenge_clipboard_events;
