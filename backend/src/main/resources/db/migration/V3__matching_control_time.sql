ALTER TABLE nrfc.submission_matching_detail
ADD COLUMN IF NOT EXISTS submission_matching_processed_time timestamp NULL;

COMMENT ON COLUMN nrfc.submission_matching_detail.submission_matching_processed_time
IS 'The timestamp of when the processor started to process this specific entry';
