CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE nrfc.email_log
ADD COLUMN IF NOT EXISTS email_address_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS email_variables_t  BYTEA NULL;

UPDATE nrfc.email_log SET email_address_t = pgp_sym_encrypt(email_address, current_setting('cryptic.key'));
UPDATE nrfc.email_log SET email_variables_t = pgp_sym_encrypt(email_variables::text, current_setting('cryptic.key'));

ALTER TABLE nrfc.email_log DROP COLUMN IF EXISTS email_address;
ALTER TABLE nrfc.email_log DROP COLUMN IF EXISTS email_variables;

ALTER TABLE nrfc.email_log RENAME COLUMN email_address_t TO email_address;
ALTER TABLE nrfc.email_log RENAME COLUMN email_variables_t TO email_variables;

ALTER TABLE nrfc.submission_contact
ADD COLUMN IF NOT EXISTS first_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS last_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS business_phone_number_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS secondary_phone_number_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS fax_number_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS email_address_t BYTEA NULL;

UPDATE nrfc.submission_contact SET first_name_t = pgp_sym_encrypt(first_name,current_setting('cryptic.key'));
UPDATE nrfc.submission_contact SET last_name_t = pgp_sym_encrypt(last_name,current_setting('cryptic.key'));
UPDATE nrfc.submission_contact SET business_phone_number_t = pgp_sym_encrypt(business_phone_number,current_setting('cryptic.key'));
UPDATE nrfc.submission_contact SET secondary_phone_number_t = pgp_sym_encrypt(secondary_phone_number,current_setting('cryptic.key'));
UPDATE nrfc.submission_contact SET fax_number_t = pgp_sym_encrypt(fax_number,current_setting('cryptic.key'));
UPDATE nrfc.submission_contact SET email_address_t = pgp_sym_encrypt(email_address,current_setting('cryptic.key'));

ALTER TABLE nrfc.submission_contact DROP COLUMN IF EXISTS first_name;
ALTER TABLE nrfc.submission_contact DROP COLUMN IF EXISTS last_name;
ALTER TABLE nrfc.submission_contact DROP COLUMN IF EXISTS business_phone_number;
ALTER TABLE nrfc.submission_contact DROP COLUMN IF EXISTS secondary_phone_number;
ALTER TABLE nrfc.submission_contact DROP COLUMN IF EXISTS fax_number;
ALTER TABLE nrfc.submission_contact DROP COLUMN IF EXISTS email_address;

ALTER TABLE nrfc.submission_contact RENAME COLUMN first_name_t to first_name;
ALTER TABLE nrfc.submission_contact RENAME COLUMN last_name_t to last_name;
ALTER TABLE nrfc.submission_contact RENAME COLUMN business_phone_number_t to business_phone_number;
ALTER TABLE nrfc.submission_contact RENAME COLUMN secondary_phone_number_t to secondary_phone_number;
ALTER TABLE nrfc.submission_contact RENAME COLUMN fax_number_t to fax_number;
ALTER TABLE nrfc.submission_contact RENAME COLUMN email_address_t to email_address;

ALTER TABLE nrfc.submission_detail
ADD COLUMN IF NOT EXISTS birthdate_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS organization_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS first_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS middle_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS last_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS client_identification_t BYTEA NULL;

UPDATE nrfc.submission_detail SET birthdate_t = pgp_sym_encrypt(birthdate::text,current_setting('cryptic.key'));
UPDATE nrfc.submission_detail SET organization_name_t = pgp_sym_encrypt(organization_name,current_setting('cryptic.key'));
UPDATE nrfc.submission_detail SET first_name_t = pgp_sym_encrypt(first_name,current_setting('cryptic.key'));
UPDATE nrfc.submission_detail SET middle_name_t = pgp_sym_encrypt(middle_name,current_setting('cryptic.key'));
UPDATE nrfc.submission_detail SET last_name_t = pgp_sym_encrypt(last_name,current_setting('cryptic.key'));
UPDATE nrfc.submission_detail SET client_identification_t = pgp_sym_encrypt(client_identification,current_setting('cryptic.key'));

ALTER TABLE nrfc.submission_detail DROP COLUMN IF EXISTS birthdate;
ALTER TABLE nrfc.submission_detail DROP COLUMN IF EXISTS organization_name;
ALTER TABLE nrfc.submission_detail DROP COLUMN IF EXISTS first_name;
ALTER TABLE nrfc.submission_detail DROP COLUMN IF EXISTS middle_name;
ALTER TABLE nrfc.submission_detail DROP COLUMN IF EXISTS last_name;
ALTER TABLE nrfc.submission_detail DROP COLUMN IF EXISTS client_identification;

ALTER TABLE nrfc.submission_detail RENAME COLUMN birthdate_t TO birthdate;
ALTER TABLE nrfc.submission_detail RENAME COLUMN organization_name_t TO organization_name;
ALTER TABLE nrfc.submission_detail RENAME COLUMN first_name_t TO first_name;
ALTER TABLE nrfc.submission_detail RENAME COLUMN middle_name_t TO middle_name;
ALTER TABLE nrfc.submission_detail RENAME COLUMN last_name_t TO last_name;
ALTER TABLE nrfc.submission_detail RENAME COLUMN client_identification_t TO client_identification;

ALTER TABLE nrfc.submission_location
ADD COLUMN IF NOT EXISTS street_address_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS city_name_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS postal_code_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS business_phone_number_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS secondary_phone_number_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS fax_number_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS email_address_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS complementary_address_1_t BYTEA NULL,
ADD COLUMN IF NOT EXISTS complementary_address_2_t BYTEA NULL;

UPDATE nrfc.submission_location SET street_address_t = pgp_sym_encrypt(street_address, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET city_name_t = pgp_sym_encrypt(city_name, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET postal_code_t = pgp_sym_encrypt(postal_code, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET business_phone_number_t = pgp_sym_encrypt(business_phone_number, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET secondary_phone_number_t = pgp_sym_encrypt(secondary_phone_number, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET fax_number_t = pgp_sym_encrypt(fax_number, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET email_address_t = pgp_sym_encrypt(email_address, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET complementary_address_1_t = pgp_sym_encrypt(complementary_address_1, current_setting('cryptic.key'));
UPDATE nrfc.submission_location SET complementary_address_2_t = pgp_sym_encrypt(complementary_address_2, current_setting('cryptic.key'));

ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS street_address;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS city_name;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS postal_code;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS business_phone_number;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS secondary_phone_number;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS fax_number;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS email_address;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS complementary_address_1;
ALTER TABLE nrfc.submission_location DROP COLUMN IF EXISTS complementary_address_2;

ALTER TABLE nrfc.submission_location RENAME COLUMN street_address_t TO street_address;
ALTER TABLE nrfc.submission_location RENAME COLUMN city_name_t TO city_name;
ALTER TABLE nrfc.submission_location RENAME COLUMN postal_code_t TO postal_code;
ALTER TABLE nrfc.submission_location RENAME COLUMN business_phone_number_t TO business_phone_number;
ALTER TABLE nrfc.submission_location RENAME COLUMN secondary_phone_number_t TO secondary_phone_number;
ALTER TABLE nrfc.submission_location RENAME COLUMN fax_number_t TO fax_number;
ALTER TABLE nrfc.submission_location RENAME COLUMN email_address_t TO email_address;
ALTER TABLE nrfc.submission_location RENAME COLUMN complementary_address_1_t TO complementary_address_1;
ALTER TABLE nrfc.submission_location RENAME COLUMN complementary_address_2_t TO complementary_address_2;
