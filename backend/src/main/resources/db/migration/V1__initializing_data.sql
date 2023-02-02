-- 
-- DROPPING TABLES IF EXIST AS THIS IS THE INIT FILE TO CREATE THE DB
--
drop table if exists nrfc.submission_detail;
drop table if exists nrfc.submission_location;
drop table if exists nrfc.submission_contact;
drop table if exists nrfc.submission;
drop table if exists nrfc.client_type_code;
drop table if exists nrfc.submission_status_code;
drop table if exists nrfc.province_code;
drop table if exists nrfc.country_code;
drop table if exists nrfc.contact_type_code;
---

create schema if not exists nrfc;

create table if not exists nrfc.client_type_code (
    client_type_code            varchar(1 byte)     not null,
    description                 varchar(100 byte)   not null,
    effective_date              date                not null,
    expiry_date                 date                default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp           default current_timestamp not null,
    update_timestamp            timestamp           default current_timestamp,
    create_user                 varchar(60 byte)    not null,
    update_user                 varchar(60 byte)	null,
    constraint client_type_code_pk primary key (client_type_code)
);

create table if not exists nrfc.submission_status_code (
    submission_status_code  	varchar(5 byte)     not null,
    description                 varchar(100 byte)   not null,
    effective_date              date                not null,
    expiry_date                 date                default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp           default current_timestamp not null,
    update_timestamp            timestamp           default current_timestamp,
    create_user                 varchar(60 byte)    not null,
    update_user                 varchar(60 byte)	null,
    constraint submission_status_code_pk primary key (submission_status_code)
);

create table if not exists nrfc.country_code (
    country_code                varchar(2 byte)     not null,
    description                 varchar(50 byte)    not null,
    effective_date              date                not null,
    expiry_date                 date                default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp           default current_timestamp not null,
    update_timestamp            timestamp           default current_timestamp,
    create_user                 varchar(60 byte)    not null,
    update_user                 varchar(60 byte)	null,
    constraint country_code_pk  primary key (country_code)
);

create table if not exists nrfc.province_code (
    province_code               varchar(2 byte)     not null,
    country_code                varchar(2 byte)     not null,
    description                 varchar(50 byte)    not null,
    effective_date              date                not null,
    expiry_date                 date                default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp           default current_timestamp not null,
    update_timestamp            timestamp           default current_timestamp,
    create_user                 varchar(60 byte)    not null,
    update_user                 varchar(60 byte)    null,
    constraint province_code_pk primary key (province_code),
    constraint province_code_country_code_fk foreign key (country_code) references nrfc.country_code(country_code)
);

create table if not exists nrfc.contact_type_code (
    contact_type_code           varchar(2 byte)     not null,
    description                 varchar(100 byte)   not null,
    effective_date              date                not null,
    expiry_date                 date                default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp           default current_timestamp not null,
    update_timestamp            timestamp           default current_timestamp,
    create_user                 varchar(60 byte)    not null,
    update_user                 varchar(60 byte)	null,
    constraint contact_type_code_pk primary key (contact_type_code)
);

create table if not exists nrfc.submission(
    submission_id             	integer             not null,
    submitter_user_guid        	varchar(50 byte) 	null,
    submission_status_code		varchar(5 byte)     null,
    submission_date             timestamp           null,
    update_timestamp            timestamp           default current_timestamp,
    create_user                 varchar(60 byte)    not null,
    update_user                 varchar(60 byte)	null,
    constraint submission_pk primary key (submission_id),
    constraint submission_submission_status_code_fk foreign key (submission_status_code) references nrfc.submission_status_code(submission_status_code)
);

create table if not exists nrfc.submission_detail (
    submission_detail_id        integer             not null,
	submission_id				integer			    not null,
	incorporation_number		varchar(50 byte)    null,
    organization_name           varchar(100 byte)   null,
    first_name                  varchar(100 byte)   null,
	middle_name                 varchar(100 byte)   null,
	last_name                 	varchar(100 byte)   null,
	client_type_code          	varchar(1 byte)   	not null,
	date_of_birth				date 			    null,
    doing_business_as_ind       varchar(1 byte)     not null default 'N',
    doing_business_as_name      varchar(100 byte)   null,
	constraint submission_detail_id_pk primary key (submission_detail_id),
	constraint ssubmission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_detail_client_type_code_fk foreign key (client_type_code) references nrfc.client_type_code(client_type_code),
    constraint submission_detail_submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id)
);

create table if not exists nrfc.submission_location (
    submission_location_id      integer			    not null,
	submission_id               integer			    not null,
    street_address              varchar(50 byte)    not null,    
    country_code                varchar(2 byte)     not null,
    province_code               varchar(2 byte)     not null,
    postal_code                 varchar(10 byte)    not null,
    is_billing_address_same_ind varchar(1 byte)     not null default 'N',
    constraint submission_location_id_pk primary key (submission_location_id),
    constraint ssubmission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_location_country_code_fk foreign key (country_code) references nrfc.country_code(country_code),
    constraint submission_location_province_code_fk foreign key (province_code) references nrfc.province_code(province_code)
);

create table if not exists nrfc.submission_contact (
    submission_contact_id       integer			not null,
	submission_id               integer			not null
    --TODO: Complete accordingly to the UX design which is currently pending
);

-- 
-- SEQUENCES
--

create sequence if not exists nrfc.submission_id_seq start 1;
create sequence if not exists nrfc.submission_detail_id_seq start 1;

-- 
-- INSERT STATIC DATA
--

insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('P', 'In Progress', current_timestamp, 'mariamar') on conflict (submission_status_code) do nothing;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('A', 'Approved', current_timestamp, 'mariamar') on conflict (submission_status_code) do nothing;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('R', 'Rejected', current_timestamp, 'mariamar') on conflict (submission_status_code) do nothing;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('D', 'Deleted', current_timestamp, 'mariamar') on conflict (submission_status_code) do nothing;
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('S', 'Submitted', current_timestamp, 'mariamar') on conflict (submission_status_code) do nothing;

insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('A', 'Association', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('B', 'First Nation Band', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('C', 'Corporation', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('F', 'Ministry of Forests and Range', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('G', 'Government', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('I', 'Individual', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('L', 'Limited Partnership', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('P', 'General Partnership', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('R', 'First Nation Group', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('S', 'Society', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('T', 'First Nation Tribal Council', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('U', 'Unregistered Company', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;

insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('AP', 'Accounts Payable', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('AR', 'Accounts Receivable', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('BA', 'First Nations Administrator', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('BC', 'First Nations Council Member', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('BL', 'Billing', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('BM', 'First Nations Manager', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('BN', 'First Nations Treaty Negotiator', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('CH', 'Chief', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('CL', 'Collections', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('DI', 'Director', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('EX', 'Export', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('GP', 'General Partner', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('IL', 'Interior Log Cost Reporting', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('LB', 'Log Broker', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('LP', 'Limited Partner', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('RC', 'Recreation Agreement Holder', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('SI', 'Scale Site Contact', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('SP', 'SPAR System Contact', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('SR', 'Stumpage Rates', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('SS', 'Scaling Software Vendor Contact', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('TC', 'BCTS Contractor', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('TN', 'Tenure Administration', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;
insert into nrfc.contact_type_code (contact_type_code, description, effective_date, create_user) values ('TP', 'EDI Trading Partner', current_timestamp, 'mariamar')  on conflict (contact_type_code) do nothing;