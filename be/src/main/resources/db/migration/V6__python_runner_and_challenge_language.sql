-- Challenge language + Python 3.12 runner (local image tag).
ALTER TABLE challenges
    ADD COLUMN IF NOT EXISTS language VARCHAR(32) NOT NULL DEFAULT 'java';

UPDATE challenges SET language = 'java' WHERE language IS NULL OR language = '';

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000003112',
    '00000000-0000-0000-0000-000000000002',
    '3.12',
    'code-challenge-ide-runner-python-312:local',
    TRUE
)
ON CONFLICT (language_id, version) DO UPDATE
SET docker_image = EXCLUDED.docker_image,
    active = EXCLUDED.active;

-- Future languages (inactive until runner images exist).
INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000004122',
    '00000000-0000-0000-0000-000000000003',
    '22',
    'code-challenge-ide-runner-node-22:local',
    FALSE
)
ON CONFLICT (language_id, version) DO NOTHING;

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES (
    '00000000-0000-0000-0000-000000005123',
    '00000000-0000-0000-0000-000000000005',
    '1.23',
    'code-challenge-ide-runner-go-123:local',
    FALSE
)
ON CONFLICT (language_id, version) DO NOTHING;
