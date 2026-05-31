CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX users_email_active_idx ON users (LOWER(email)) WHERE deleted_at IS NULL;

CREATE TABLE languages (
    id UUID PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    display_name VARCHAR(64) NOT NULL
);

CREATE TABLE language_runtimes (
    id UUID PRIMARY KEY,
    language_id UUID NOT NULL REFERENCES languages(id),
    version VARCHAR(16) NOT NULL,
    docker_image VARCHAR(512) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (language_id, version)
);

CREATE TABLE challenges (
    id UUID PRIMARY KEY,
    slug VARCHAR(128) NOT NULL UNIQUE,
    title VARCHAR(256) NOT NULL,
    description_md TEXT NOT NULL,
    starter_code TEXT NOT NULL,
    gating_config JSONB NOT NULL DEFAULT '{}',
    source VARCHAR(32) NOT NULL DEFAULT 'git',
    difficulty VARCHAR(16),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE challenge_public_tests (
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    name VARCHAR(256) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE challenge_hidden_tests (
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    name VARCHAR(256) NOT NULL,
    test_source TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE challenge_runtime (
    challenge_id UUID NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    runtime_id UUID NOT NULL REFERENCES language_runtimes(id),
    PRIMARY KEY (challenge_id, runtime_id)
);

CREATE TABLE submissions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    challenge_id UUID NOT NULL REFERENCES challenges(id),
    runtime_id UUID NOT NULL REFERENCES language_runtimes(id),
    status VARCHAR(32) NOT NULL,
    solution_code TEXT NOT NULL,
    custom_tests_code TEXT,
    idempotency_key VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX submissions_user_created_idx ON submissions (user_id, created_at DESC);
CREATE INDEX submissions_challenge_status_idx ON submissions (challenge_id, status);
CREATE UNIQUE INDEX submissions_idempotency_idx
    ON submissions (user_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE TABLE submission_reports (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL UNIQUE REFERENCES submissions(id) ON DELETE CASCADE,
    schema_version INT NOT NULL DEFAULT 1,
    summary JSONB NOT NULL DEFAULT '{}',
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tool_results (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    tool_name VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE feedback_items (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL REFERENCES submission_reports(id) ON DELETE CASCADE,
    category VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    severity VARCHAR(16),
    message TEXT,
    stable_id VARCHAR(128),
    ai_explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX feedback_items_report_status_idx ON feedback_items (report_id, status);

CREATE TABLE custom_tests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    challenge_id UUID NOT NULL REFERENCES challenges(id),
    code TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, challenge_id)
);

CREATE TABLE user_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    challenge_id UUID NOT NULL REFERENCES challenges(id),
    state VARCHAR(32) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, challenge_id)
);
