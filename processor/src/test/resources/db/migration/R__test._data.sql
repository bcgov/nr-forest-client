INSERT INTO nrfc.submission
(submission_id, submission_status_code, submission_type_code, submission_date, update_timestamp, create_user, update_user)
VALUES(1, 'N', 'SPP', '2023-05-02 00:00:00.000', '2023-05-04 10:52:01.689', '58d50cde-c77c-4f4d-8de3-34c4d9367a68', '20d9aa0c-7c62-4fd2-a725-d950ed728d36')
ON CONFLICT (submission_id) DO NOTHING;

INSERT INTO nrfc.submission_detail
(submission_detail_id, submission_id, business_type_code, incorporation_number, organization_name, client_type_code, good_standing_ind)
VALUES(1, 1, 'R', 'GP00000004', 'GREYJOY SEAPORTS INC.', 'I', NULL)
ON CONFLICT (submission_detail_id) DO NOTHING;

INSERT INTO nrfc.submission
(submission_id, submission_status_code, submission_type_code, submission_date, update_timestamp, create_user, update_user)
VALUES(2, 'A', 'AAC', '2023-09-26 15:51:09.861', '2023-09-26 16:40:21.198', 'Paulo', 'AUTO-PROCESSOR')
ON CONFLICT (submission_id) DO NOTHING;

INSERT INTO nrfc.submission_detail
(submission_detail_id, submission_id, client_number, business_type_code, incorporation_number, organization_name, client_type_code, good_standing_ind)
VALUES(2, 2, '00000054', 'R', 'FM0159297', 'STAR DOT STAR VENTURES', 'P', 'Y')
ON CONFLICT (submission_detail_id) DO NOTHING;

INSERT INTO nrfc.submission_contact
(submission_contact_id, submission_id, contact_type_code, first_name, last_name, business_phone_number, email_address, idp_user_id)
VALUES(2, 2, 'BL', 'Paulo', 'Cruz', '(250) 250-2550', 'paulo.cruz@gov.bc.ca', 'C4F7EAD6855E42858A5AB9EF3529A20D')
ON CONFLICT (submission_contact_id) DO NOTHING;

INSERT INTO nrfc.submission_location
(submission_location_id, submission_id, street_address, country_code, province_code, city_name, postal_code, location_name)
VALUES(2, 2, '2975 Jutland Rd', 'CA', 'BC', 'Victoria', 'V8T5J9', 'Mailing address')
ON CONFLICT (submission_location_id) DO NOTHING;

INSERT INTO nrfc.submission_location_contact_xref
(submission_location_id, submission_contact_id)
VALUES(2, 2)
ON CONFLICT (submission_location_id, submission_contact_id) DO NOTHING;

INSERT INTO nrfc.submission_matching_detail
(submission_matching_detail_id, submission_id, matching_fields, confirmed_match_status_ind, confirmed_match_message, confirmed_match_timestamp, confirmed_match_userid, submission_matching_processed)
VALUES(2, 2, '{}'::jsonb, 'Y', NULL, '2023-10-03 11:13:10.443', 'AUTO-PROCESSOR', true)
ON CONFLICT (submission_matching_detail_id) DO NOTHING;