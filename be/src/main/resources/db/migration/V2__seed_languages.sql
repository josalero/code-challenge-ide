INSERT INTO languages (id, name, display_name)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'java', 'Java'),
    ('00000000-0000-0000-0000-000000000002', 'python', 'Python'),
    ('00000000-0000-0000-0000-000000000003', 'node', 'Node.js'),
    ('00000000-0000-0000-0000-000000000004', 'csharp', 'C#'),
    ('00000000-0000-0000-0000-000000000005', 'go', 'Go');

INSERT INTO language_runtimes (id, language_id, version, docker_image, active)
VALUES
    ('00000000-0000-0000-0000-000000001026', '00000000-0000-0000-0000-000000000001', '26',
     'ghcr.io/example/code-challenge-ide-runner-java-26:latest', TRUE),
    ('00000000-0000-0000-0000-000000001712', '00000000-0000-0000-0000-000000000001', '17',
     'ghcr.io/example/code-challenge-ide-runner-java-17:latest', FALSE),
    ('00000000-0000-0000-0000-000000002112', '00000000-0000-0000-0000-000000000001', '21',
     'ghcr.io/example/code-challenge-ide-runner-java-21:latest', FALSE),
    ('00000000-0000-0000-0000-000000002512', '00000000-0000-0000-0000-000000000001', '25',
     'ghcr.io/example/code-challenge-ide-runner-java-25:latest', FALSE);
