insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('P', 'In Progress', current_timestamp, 'mariamar') ON CONFLICT (submission_status_code) DO NOTHING;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('A', 'Approved', current_timestamp, 'mariamar') ON CONFLICT (submission_status_code) DO NOTHING;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('R', 'Rejected', current_timestamp, 'mariamar') ON CONFLICT (submission_status_code) DO NOTHING;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('D', 'Deleted', current_timestamp, 'mariamar') ON CONFLICT (submission_status_code) DO NOTHING;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('S', 'Submitted', current_timestamp, 'mariamar') ON CONFLICT (submission_status_code) DO NOTHING;

insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('A', 'Association', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('B', 'First Nation Band', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('C', 'Corporation', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('F', 'Ministry of Forests and Range', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('G', 'Government', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('I', 'Individual', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('L', 'Limited Partnership', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('P', 'General Partnership', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('R', 'First Nation Group', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('S', 'Society', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('T', 'First Nation Tribal Council', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('U', 'Unregistered Company', current_timestamp, 'mariamar') ON CONFLICT (client_type_code) DO NOTHING;

update nrfc.client_type_code set effective_date = '1970-01-01 00:00:00.000';

-- Test case data

INSERT INTO nrfc.submission VALUES (365, 'R', 'RNC', '2024-04-16 15:57:57.065704', '2024-04-16 16:00:02.690147', 'BCEIDBUSINESS\\UAT', ' Martinez') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_contact VALUES (365, 365, 'DI', 'Load', 'NRS', '(777) 777-7777', 'uattestingmail@uat.testing.lo', 'BCEIDBUSINESS\\UAT') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_detail VALUES (365, 365, NULL, 'R', 'XX0000006', 'HYPERION CORP', 'C', 'Y', NULL, 'DCR') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location VALUES (365, 365, '712 Canyon View Dr', 'US', 'KS', 'Lansing', '66043-6271', 'Mailing address') ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_location_contact_xref VALUES (365, 365) ON CONFLICT DO NOTHING;
INSERT INTO nrfc.submission_matching_detail VALUES (365, 365, '{"contact": "00000000,00000000,00000001,00000001,00000001", "registrationNumber": "00000002,00000002"}', 'N', ' already has one. The number is: tyututu. Be sure to keep it for your records.', '2024-04-16 16:00:06.678395', 'idir\\ottomated', true) ON CONFLICT DO NOTHING;