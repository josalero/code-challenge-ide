-- Activate C# and TypeScript runners.

UPDATE language_runtimes
SET docker_image = 'code-challenge-ide-runner-dotnet-8:local',
    active = TRUE
WHERE id = '00000000-0000-0000-0000-000000004080';

INSERT INTO languages (id, name, display_name)
VALUES ('00000000-0000-0000-0000-000000000006', 'typescript', 'TypeScript')
ON CONFLICT (name) DO NOTHING;

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000006050',
    '00000000-0000-0000-0000-000000000006',
    '5.7',
    'code-challenge-ide-runner-typescript-57:local',
    TRUE
)
ON CONFLICT (language_id, version) DO UPDATE
SET docker_image = EXCLUDED.docker_image,
    active = EXCLUDED.active;
