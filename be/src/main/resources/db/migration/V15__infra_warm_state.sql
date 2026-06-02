-- Persists runner pool smoke warm and LSP warm state (replaces JSON stamp files).
CREATE TABLE runner_pool_warm_state (
    docker_image VARCHAR(512) PRIMARY KEY,
    image_id VARCHAR(256) NOT NULL,
    warmed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE lsp_warm_state (
    label VARCHAR(64) NOT NULL,
    docker_image VARCHAR(512) NOT NULL,
    image_id VARCHAR(256) NOT NULL,
    warmed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (label, docker_image)
);
