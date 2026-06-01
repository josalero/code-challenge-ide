-- On-demand analyzer requests against a submission (Coach, Sonar, Complexity, …).
CREATE TABLE submission_feedback_actions (
    id            UUID PRIMARY KEY,
    submission_id UUID         NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    action        VARCHAR(64)  NOT NULL,
    status        VARCHAR(32)  NOT NULL,
    result        TEXT,
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_submission_feedback_actions_submission
    ON submission_feedback_actions (submission_id);
