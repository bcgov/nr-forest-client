ALTER TABLE nrfc.email_log ALTER COLUMN email_subject TYPE varchar(50) USING email_subject::varchar(50);
