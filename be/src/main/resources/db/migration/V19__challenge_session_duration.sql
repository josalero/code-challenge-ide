ALTER TABLE challenges
    ADD COLUMN session_duration_minutes INT;

UPDATE challenges
SET session_duration_minutes = 30
WHERE LOWER(difficulty) = 'easy' AND session_duration_minutes IS NULL;

UPDATE challenges
SET session_duration_minutes = 60
WHERE session_duration_minutes IS NULL;
