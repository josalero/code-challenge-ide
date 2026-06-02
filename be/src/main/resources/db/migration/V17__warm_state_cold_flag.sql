-- Track explicit cold (not warmed) inventory, not only successful warms.
ALTER TABLE runner_pool_warm_state
    ADD COLUMN warmed BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE runner_pool_warm_state
    ALTER COLUMN image_id DROP NOT NULL;

UPDATE runner_pool_warm_state
SET warmed = (image_id IS NOT NULL);

ALTER TABLE lsp_warm_state
    ADD COLUMN warmed BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE lsp_warm_state
    ALTER COLUMN image_id DROP NOT NULL;

UPDATE lsp_warm_state
SET warmed = (image_id IS NOT NULL);
