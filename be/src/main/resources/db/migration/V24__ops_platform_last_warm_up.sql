CREATE TABLE ops_platform_state (
    id VARCHAR(32) PRIMARY KEY,
    last_warm_up_at TIMESTAMPTZ
);

INSERT INTO ops_platform_state (id, last_warm_up_at)
VALUES (
    'default',
    GREATEST(
        COALESCE((SELECT MAX(warmed_at) FROM runner_pool_warm_state WHERE warmed = TRUE), TIMESTAMPTZ '1970-01-01'),
        COALESCE((SELECT MAX(warmed_at) FROM lsp_warm_state WHERE warmed = TRUE), TIMESTAMPTZ '1970-01-01')
    )
);

UPDATE ops_platform_state
SET last_warm_up_at = NULL
WHERE last_warm_up_at = TIMESTAMPTZ '1970-01-01';
