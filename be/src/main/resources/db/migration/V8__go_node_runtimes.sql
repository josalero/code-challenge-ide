-- Activate Go and Node.js runners (local images via make runners).

UPDATE language_runtimes
SET docker_image = 'code-challenge-ide-pro-runner-go-123:local',
    active = TRUE
WHERE id = '00000000-0000-0000-0000-000000005123';

UPDATE language_runtimes
SET docker_image = 'code-challenge-ide-pro-runner-node-22:local',
    active = TRUE
WHERE id = '00000000-0000-0000-0000-000000004122';

-- C# runtime (inactive until dotnet runner image is built).
INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000004080',
    '00000000-0000-0000-0000-000000000004',
    '8.0',
    'code-challenge-ide-pro-runner-dotnet-8:local',
    FALSE
)
ON CONFLICT (language_id, version) DO NOTHING;
