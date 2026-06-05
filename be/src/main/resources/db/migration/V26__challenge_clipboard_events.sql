CREATE TABLE challenge_clipboard_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    challenge_id UUID NOT NULL REFERENCES challenges (id),
    event_type VARCHAR(16) NOT NULL,
    editor_surface VARCHAR(32) NOT NULL,
    char_count INTEGER,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX challenge_clipboard_events_user_challenge_idx
    ON challenge_clipboard_events (user_id, challenge_id, occurred_at DESC);
