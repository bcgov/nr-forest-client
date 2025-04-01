ALTER TABLE nrfc.submission_detail
ADD COLUMN IF NOT EXISTS notify_client_ind VARCHAR(1) NULL DEFAULT 'Y';

COMMENT ON COLUMN nrfc.submission_detail.notify_client_ind
IS 'An indicator that determines whether the submission details should be sent to the client.';
