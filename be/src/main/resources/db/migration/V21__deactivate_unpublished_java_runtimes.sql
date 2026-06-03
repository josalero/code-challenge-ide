-- Java 17/21 runner images are not published to GHCR; only 25 and 26 are built in CI.
UPDATE language_runtimes
SET active = FALSE
WHERE version IN ('17', '21');
