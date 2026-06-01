-- C++ 20 (GCC 14) runner.

INSERT INTO languages (id, name, display_name)
VALUES ('00000000-0000-0000-0000-000000000008', 'cpp', 'C++')
ON CONFLICT (name) DO NOTHING;

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000008200',
    '00000000-0000-0000-0000-000000000008',
    '20',
    'code-challenge-ide-runner-cpp-20:local',
    TRUE
)
ON CONFLICT (language_id, version) DO UPDATE
SET docker_image = EXCLUDED.docker_image,
    active = EXCLUDED.active;
