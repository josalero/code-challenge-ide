-- Local / compose runner image tags (17, 21, 26). Version 25 stays inactive until an image exists.
UPDATE language_runtimes
SET docker_image = 'code-challenge-ide-pro-runner-java-17:local', active = TRUE
WHERE version = '17';

UPDATE language_runtimes
SET docker_image = 'code-challenge-ide-pro-runner-java-21:local', active = TRUE
WHERE version = '21';

UPDATE language_runtimes
SET docker_image = 'code-challenge-ide-pro-runner-java-26:local', active = TRUE
WHERE version = '26';
