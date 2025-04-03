ALTER TABLE nrfc.submission
ADD COLUMN IF NOT EXISTS notify_client_ind VARCHAR(1) NULL DEFAULT 'Y';

COMMENT ON COLUMN nrfc.submission.notify_client_ind
IS 'An indicator that determines whether the submission should be sent to the client.';
