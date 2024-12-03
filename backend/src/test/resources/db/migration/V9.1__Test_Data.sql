update nrfc.client_type_code set effective_date = '1970-01-01 00:00:00.000';
update nrfc.district_code set email_address = 'mail@mail.ca';

-- Test case data Review new client submission

insert into nrfc.submission
values
	(365, 'R', 'RNC', current_timestamp, current_timestamp, 'BCEIDBUSINESS\\UAT', ' BCEIDBUSINESS\\UAT') on conflict do nothing;
insert into nrfc.submission_contact (submission_contact_id, submission_id, contact_type_code, first_name, last_name, business_phone_number, email_address, idp_user_id)

values
  (365, 365, 'DI', 'Load', 'NRS', '(777) 777-7777', 'uattestingmail@uat.testing.lo', 'BCEIDBUSINESS\\UAT') on conflict do nothing;

insert into nrfc.submission_detail
values
	(365, 365, NULL, 'R', 'BC0000006', 'HYPERION CORP', 'C', 'Y', NULL, 'DCR') on conflict do nothing;

insert into nrfc.submission_location (submission_location_id, submission_id, street_address, country_code, province_code, city_name, postal_code, location_name)
values
  (365, 365, '712 Canyon View Dr', 'US', 'KS', 'Lansing', '66043-6271', 'Mailing address') on conflict do nothing;

insert into nrfc.submission_location_contact_xref
values
	(365, 365) on conflict do nothing;

insert into nrfc.submission_matching_detail
values
	(365, 365, '{"contact": "00000000,00000000,00000001,00000001,00000001", "registrationNumber": "00000002,00000002"}', 'N', ' already has one. The number is: tyututu. Be sure to keep it for your records.', '2024-04-16 16:00:06.678395', 'idir\\ottomated', true) on conflict do nothing;

-- Test case data Staff submitted data - Individual

insert into nrfc.submission
values
	(364, 'A', 'SSD', current_timestamp, current_timestamp, 'BCEIDBUSINESS\\UAT', ' BCEIDBUSINESS\\UAT') on conflict do nothing;

insert into nrfc.submission_contact (submission_contact_id, submission_id, contact_type_code, first_name, last_name, business_phone_number, email_address, idp_user_id)
values
  (364, 364, 'DI', 'Jhon', 'Wick', '(999) 888-7766', 'jhonwick@uat.testing.lo', 'BCEIDBUSINESS\\UAT') on conflict do nothing;

insert into nrfc.submission_detail (submission_detail_id, submission_id, client_number, business_type_code, incorporation_number, organization_name, client_type_code, good_standing_ind, birthdate, district_code, work_safe_bc_number, doing_business_as, client_acronym, first_name, middle_name, last_name, notes, identification_type_code, client_identification, identification_country_code, identification_province_code)
values
  (364, 364, NULL, 'U', NULL, 'ROLAND SOLDIER', 'I', 'Y', '1972-12-04', 'DCR', NULL, NULL, NULL,'ROLAND', NULL,'SOLDIER','GOOD AT SUPPORT','CDDL','9999911','CA','BC') on conflict do nothing;

insert into nrfc.submission_location (submission_location_id, submission_id, street_address, country_code, province_code, city_name, postal_code, location_name)
values
  (364, 364, '999 Canyon View Dr', 'US', 'CO', 'Denver', '66043-6271', 'Mailing address') on conflict do nothing;

insert into nrfc.submission_location_contact_xref
values
	(364, 364) on conflict do nothing;

insert into nrfc.submission_matching_detail
values
	(364, 364, '{}', 'N', NULL, '2024-04-16 16:00:06.678395', 'idir\\ottomated', true) on conflict do nothing;


-- Email log data

insert into nrfc.email_log (create_timestamp, email_address, email_id, email_log_id, email_sent_ind, email_subject, email_variables, exception_message, template_name, update_timestamp)
values
('2024-12-03 06:34:35.753139', 'mail@mail.ca', '94cd9b2e-bb7d-4eb3-be15-37eaedb8d1f7', 1, 'Y', '[UAT] Client number application received', '{"business":{"name":"01-DEV, LOAD","notes":"","district":"UAT","lastName":"","birthdate":"1990-01-01","firstName":"","legalType":"SP","clientType":"USP","middleName":"","businessType":"U","goodStanding":"Y","clientAcronym":"","doingBusinessAs":"","workSafeBcNumber":"","identificationType":"","registrationNumber":"","clientIdentification":"","identificationCountry":"","identificationProvince":""},"frontend":"https://nr-forest-client-00-frontend.uat.gov.bc.ca","userName":"LOAD 01-DEV","address.[0]":{"city":"Victoria","name":"Mailing address","notes":"","address":"2975 Jutland Rd","country":"Canada","province":"British Columbia","faxNumber":"","postalCode":"V8T5J9","emailAddress":"","businessPhoneNumber":"","secondaryPhoneNumber":"","complementaryAddressOne":"","complementaryAddressTwo":""},"contact.[0]":{"name":"John Wick","email":"jwick@thecontinental.ca","phone":"2501234568","lastName":"Wick","faxNumber":"","firstName":"John","secondaryPhoneNumber":""},"districtName":"UAT District","districtEmail":"alliance@mail.ca"}', NULL, 'registration', '2024-12-03 06:34:35.753139'),
('2024-12-03 06:34:35.753139', 'mail@mail.ca', '2b3d2b63-a536-492a-b9d7-da2660d8ba68', 2, 'Y', '[UAT] Client number application received', '{"business":{"name":"HYPERION CORP","notes":"","district":"UAT","lastName":"","birthdate":"1990-01-01","firstName":"","legalType":"SP","clientType":"RSP","middleName":"","businessType":"R","goodStanding":"Y","clientAcronym":"","doingBusinessAs":"HYPERION CORP","workSafeBcNumber":"","identificationType":"","registrationNumber":"FM0403309","clientIdentification":"","identificationCountry":"","identificationProvince":""},"frontend":"https://nr-forest-client-00-frontend.uat.gov.bc.ca","userName":"LOAD 01-DEV","address.[0]":{"city":"Campbell River","name":"Mailing address","notes":"","address":"823 Georgia DR","country":"Canada","province":"British Columbia","faxNumber":"","postalCode":"V9H1S2","emailAddress":"","businessPhoneNumber":"","secondaryPhoneNumber":"","complementaryAddressOne":"","complementaryAddressTwo":""},"contact.[0]":{"name":"LOAD 01-DEV","email":"notanemail@mail.ca","phone":"2255522552","lastName":"01-DEV","faxNumber":"","firstName":"LOAD","secondaryPhoneNumber":""},"contact.[1]":{"name":"JOHN WICK","email":"jwicker@mail.ca","phone":"7787787778","lastName":"WICK","faxNumber":"","firstName":"JOHN","secondaryPhoneNumber":""},"districtName":"UAT District","districtEmail":"alliance@mail.ca"}', NULL, 'registration', '2024-12-03 06:34:35.753139'),
('2024-12-03 06:34:35.753139', 'mail@mail.ca', '914e1e8d-2849-45a9-bba3-6f638398ad4c', 3, 'Y', '[UAT] Client number application approved', '{"business":{"name":"01-DEV, LOAD","clientNumber":"00000172","districtName":"UAT District","districtEmail":"alliance@mail.ca"},"frontend":"https://nr-forest-client-00-frontend.uat.gov.bc.ca","userName":"LOAD 01-DEV"}', NULL, 'approval', '2024-12-03 06:34:35.753139'),
('2024-12-03 06:34:35.753139', 'mail@mail.ca', 'b1602808-49f2-4eb4-b701-2fa6097b13e9', 4, 'Y', '[UAT] HYPERION CORP requires review', '{"business":{"name":"HYPERION CORP","districtName":"UAT District"},"frontend":"https://nr-forest-client-00-frontend.uat.gov.bc.ca","userName":"JOHN WICK","submission":2}', NULL, 'revision', '2024-12-03 06:34:35.753139')  on conflict do nothing;
