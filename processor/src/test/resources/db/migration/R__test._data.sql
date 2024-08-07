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

UPDATE nrfc.district_code SET email_address = 'alliance@mail.ca';

-- Test case data

-- Test case data Review new client submission
INSERT INTO nrfc.submission VALUES (365, 'N', 'SPP', '2024-04-16 15:57:57.065704', '2024-04-16 16:00:02.690147', 'BCEIDBUSINESS\\UAT', ' Martinez') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_contact VALUES (365, 365, 'DI', 'Load', 'NRS', '(777) 777-7777', 'uattestingmail@uat.testing.lo', 'BCEIDBUSINESS\\UAT') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_detail VALUES (365, 365, NULL, 'R', 'XX0000006', 'HYPERION CORP', 'C', 'Y', NULL, 'DCR') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location VALUES (365, 365, '712 Canyon View Dr', 'US', 'KS', 'Lansing', '66043-6271', 'Mailing address') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location_contact_xref VALUES (365, 365) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_matching_detail VALUES (365, 365, '{}', null, null, '2024-04-16 16:00:06.678395', 'idir\\ottomated', false) ON CONFLICT DO NOTHING;

-- Test case auto approved
INSERT INTO nrfc.submission VALUES (366, 'A', 'AAC', '2024-04-16 15:57:57.065704', '2024-04-16 16:00:02.690147', 'BCEIDBUSINESS\\UAT', ' Martinez') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_contact VALUES (366, 366, 'DI', 'Load', 'NRS', '(777) 777-7777', 'uattestingmail@uat.testing.lo', 'BCEIDBUSINESS\\UAT') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_detail VALUES (366, 366, NULL, 'R', 'XX0000006', 'HYPERION CORP', 'C', 'Y', NULL, 'DCR') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location VALUES (366, 366, '712 Canyon View Dr', 'US', 'KS', 'Lansing', '66043-6271', 'Mailing address') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location_contact_xref VALUES (366, 366) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_matching_detail VALUES (366, 366, '{}', 'N', null, '2024-04-16 16:00:06.678395', 'idir\\ottomated', true) ON CONFLICT DO NOTHING;

-- Test case data Review conflict submission
INSERT INTO nrfc.submission VALUES (367, 'R', 'RNC', '2024-04-16 15:57:57.065704', '2024-04-16 16:00:02.690147', 'BCEIDBUSINESS\\UAT', ' Martinez') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_contact VALUES (367, 367, 'DI', 'Load', 'NRS', '(777) 777-7777', 'uattestingmail@uat.testing.lo', 'BCEIDBUSINESS\\UAT') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_detail VALUES (367, 367, NULL, 'R', 'XX0000006', 'HYPERION CORP', 'C', 'Y', NULL, 'DCR') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location VALUES (367, 367, '712 Canyon View Dr', 'US', 'KS', 'Lansing', '66043-6271', 'Mailing address') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location_contact_xref VALUES (367, 367) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_matching_detail VALUES (367, 367, '{"contact": "00000000,00000000,00000001,00000001,00000001", "registrationNumber": "00000002,00000002"}', 'N', ' already has one. The number is: tyututu. Be sure to keep it for your records.', '2024-04-16 16:00:06.678395', 'idir\\ottomated', true) ON CONFLICT DO NOTHING;

-- Test case data Staff submitted data - Individual
INSERT INTO nrfc.submission VALUES (368, 'A', 'SSD', current_timestamp, current_timestamp, 'BCEIDBUSINESS\\UAT', 'BCEIDBUSINESS\\UAT') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_contact
(submission_contact_id, submission_id, contact_type_code, first_name, last_name, business_phone_number, email_address, idp_user_id, secondary_phone_number, fax_number)
VALUES(368, 368, 'DI', 'Johnathan', 'Wick', '9994441245', 'uattestingmail@uat.testing.lo', 'BCEIDBUSINESS\\UAT', NULL, NULL) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_detail
(submission_detail_id, submission_id, client_number, business_type_code, incorporation_number, organization_name, client_type_code, good_standing_ind, birthdate, district_code, work_safe_bc_number, doing_business_as, client_acronym, first_name, middle_name, last_name, notes, identification_type_code, client_identification, identification_country_code, identification_province_code)
VALUES(368, 368, NULL, 'U', NULL, 'Johnathan Wick', 'I', 'Y', '1962-12-10', NULL, NULL, NULL, NULL, 'Johnathan', NULL, 'Wick', NULL, 'CDDL', '77744458', 'CA', 'AB') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location
(submission_location_id, submission_id, street_address, country_code, province_code, city_name, postal_code, location_name, business_phone_number, secondary_phone_number, fax_number, notes, complementary_address_1, complementary_address_2, email_address)
VALUES(368, 368, '712 Canyon View Dr', 'US', 'AZ', 'Lansing', '66043-6271', 'Mailing address', '7894562486', NULL, NULL, 'This is the main office', 'In care of Julius', NULL, NULL) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location_contact_xref VALUES (368, 368) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_matching_detail VALUES (368, 368, '{"info":{}}', 'N', null, '2024-04-16 16:00:06.678395', 'idir\\ottomated', true) ON CONFLICT DO NOTHING;
