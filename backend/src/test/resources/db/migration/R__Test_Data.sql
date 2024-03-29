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