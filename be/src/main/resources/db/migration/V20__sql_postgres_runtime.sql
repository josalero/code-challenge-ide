-- PostgreSQL SQL runner (query result comparison via pytest).

INSERT INTO languages (id, name, display_name)
VALUES ('00000000-0000-0000-0000-000000000012', 'sql', 'SQL')
ON CONFLICT (name) DO NOTHING;

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000012170',
    '00000000-0000-0000-0000-000000000012',
    '17',
    'code-challenge-ide-pro-runner-postgres-17:local',
    TRUE
)
ON CONFLICT (language_id, version) DO UPDATE
SET docker_image = EXCLUDED.docker_image,
    active = EXCLUDED.active;
