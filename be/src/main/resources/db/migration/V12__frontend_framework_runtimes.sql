-- React, Vue, and Angular Vitest runners.

INSERT INTO languages (id, name, display_name)
VALUES
    ('00000000-0000-0000-0000-000000000009', 'react', 'React'),
    ('00000000-0000-0000-0000-000000000010', 'vue', 'Vue'),
    ('00000000-0000-0000-0000-000000000011', 'angular', 'Angular')
ON CONFLICT (name) DO NOTHING;

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES
    (
        '00000000-0000-0000-0000-000000009190',
        '00000000-0000-0000-0000-000000000009',
        '19',
        'code-challenge-ide-runner-react-19:local',
        TRUE
    ),
    (
        '00000000-0000-0000-0000-000000010351',
        '00000000-0000-0000-0000-000000000010',
        '3.5',
        'code-challenge-ide-runner-vue-35:local',
        TRUE
    ),
    (
        '00000000-0000-0000-0000-000000011191',
        '00000000-0000-0000-0000-000000000011',
        '19',
        'code-challenge-ide-runner-angular-19:local',
        TRUE
    )
ON CONFLICT (language_id, version) DO UPDATE
SET docker_image = EXCLUDED.docker_image,
    active = EXCLUDED.active;
