-- Rust 1.84 runner.

INSERT INTO languages (id, name, display_name)
VALUES ('00000000-0000-0000-0000-000000000007', 'rust', 'Rust')
ON CONFLICT (name) DO NOTHING;

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000007184',
    '00000000-0000-0000-0000-000000000007',
    '1.84',
    'code-challenge-ide-pro-runner-rust-184:local',
    TRUE
)
ON CONFLICT (language_id, version) DO UPDATE
SET docker_image = EXCLUDED.docker_image,
    active = EXCLUDED.active;
