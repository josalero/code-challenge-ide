-- Retire Java 17 and 21 runner tracks; platform supports Java 25 and 26 only.
UPDATE language_runtimes
SET active = FALSE
WHERE version IN ('17', '21');
