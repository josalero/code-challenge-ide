CREATE TABLE access_requests (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    message TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    reviewed_by_user_id UUID REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    reviewed_at TIMESTAMPTZ,
    CONSTRAINT access_requests_status_check CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE UNIQUE INDEX access_requests_pending_email_idx
    ON access_requests (LOWER(email))
    WHERE status = 'PENDING';

CREATE INDEX access_requests_status_created_idx
    ON access_requests (status, created_at DESC);
