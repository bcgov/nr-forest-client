-- 
-- DROPPING TABLES AND SEQUENCES IF EXIST AS THIS IS THE INIT FILE TO CREATE THE DB
--
drop table if exists nrfc.submission_detail;
drop table if exists nrfc.submission_location_contact;
drop table if exists nrfc.submission_location;
drop table if exists nrfc.submission_submitter;
drop table if exists nrfc.submission_contact;
drop table if exists nrfc.submission;
drop table if exists nrfc.client_type_code;
drop table if exists nrfc.submission_status_code;
drop table if exists nrfc.province_code;
drop table if exists nrfc.country_code;
drop table if exists nrfc.contact_type_code;

drop sequence if exists nrfc.submission_id_seq;
drop sequence if exists nrfc.submission_detail_id_seq;
drop sequence if exists nrfc.submission_location_seq;
drop sequence if exists nrfc.submission_location_contact_seq;
drop sequence if exists nrfc.submission_submitter_seq;
---

create schema if not exists nrfc;

create table if not exists nrfc.client_type_code (
    client_type_code            varchar(1)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    constraint client_type_code_pk primary key (client_type_code)
);

create table if not exists nrfc.submission_status_code (
    submission_status_code  	varchar(5)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    constraint submission_status_code_pk primary key (submission_status_code)
);

create table if not exists nrfc.country_code (
    country_code                varchar(2)      not null,
    description                 varchar(100)     not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    display_order               integer         null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    constraint country_code_pk  primary key (country_code),
    constraint display_order_uk unique (display_order)
);

create table if not exists nrfc.province_code (
    province_code               varchar(2)      not null,
    country_code                varchar(2)      null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    constraint province_code_pk primary key (province_code),
    constraint province_code_country_code_fk foreign key (country_code) references nrfc.country_code(country_code)
);

create table if not exists nrfc.contact_type_code (
    contact_type_code           varchar(2)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    constraint contact_type_code_pk primary key (contact_type_code)
);

create table if not exists nrfc.submission(
    submission_id             	integer 		not null,
    submitter_user_guid        	varchar(50) 	null,
    submission_status_code		varchar(5)      null,
    submission_date             timestamp       null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    constraint submission_pk primary key (submission_id),
    constraint submission_submission_status_code_fk foreign key (submission_status_code) references nrfc.submission_status_code(submission_status_code)
);

create table if not exists nrfc.submission_detail (
    submission_detail_id        integer			not null,
	submission_id				integer			not null,
	incorporation_number		varchar(50)    	null,
    organization_name           varchar(100)    null,
    first_name                  varchar(100)    null,
	middle_name                 varchar(100)    null,
	last_name                 	varchar(100)    null,
	client_type_code          	varchar(1)    	not null,
	date_of_birth				date 			null,
    doing_business_as_ind       varchar(1)      not null default 'N',
    doing_business_as_name      varchar(100)    null,
    has_additional_location_ind varchar(1)      not null default 'N',
	constraint submission_detail_id_pk primary key (submission_detail_id),
	constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_detail_client_type_code_fk foreign key (client_type_code) references nrfc.client_type_code(client_type_code),
    constraint submission_detail_submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id)
);

create table if not exists nrfc.submission_location (
    submission_location_id      integer			not null,
	submission_id               integer			not null,
    street_address              varchar(50)    	not null,    
    country_code                varchar(2)      not null,
    province_code               varchar(2)      not null,
    city_name                   varchar(100)    not null,
    postal_code                 varchar(10)     not null,
    main_address_ind            varchar(1)      not null default 'N',
    constraint submission_location_id_pk primary key (submission_location_id),
    constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_location_country_code_fk foreign key (country_code) references nrfc.country_code(country_code),
    constraint submission_location_province_code_fk foreign key (province_code) references nrfc.province_code(province_code)
);

create table if not exists nrfc.submission_location_contact (
    submission_location_contact_id      integer			not null,
	submission_location_id              integer			not null,
    contact_type_code                   varchar(2)      not null,
    first_name                          varchar(100)    null,
	middle_name                         varchar(100)    null,
    business_phone_number               varchar(20)     not null,
    email_address                       varchar(100)    not null,
    constraint submission_location_contact_id_pk primary key (submission_location_contact_id),
    constraint submission_location_contact_id_fk foreign key (submission_location_id) references nrfc.submission_location(submission_location_id),
    constraint submission_location_contact_contact_type_code_fk foreign key (contact_type_code) references nrfc.contact_type_code(contact_type_code)
);

create table if not exists nrfc.submission_submitter (
    submission_submitter_id     integer			not null,
	submission_id               integer			not null,
    first_name                  varchar(100)    null,
	last_name                 	varchar(100)    null,
    phone_number       			varchar(20)     not null,
    email_address               varchar(100)    not null,
    constraint submission_submitter_id_pk primary key (submission_submitter_id),
    constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id)
);

-- 
-- SEQUENCES
--

create sequence if not exists nrfc.submission_id_seq start 1;
alter table nrfc.submission alter column submission_id set default nextval('nrfc.submission_id_seq');

create sequence if not exists nrfc.submission_detail_id_seq start 1;
alter table nrfc.submission_detail alter column submission_detail_id set default nextval('nrfc.submission_detail_id_seq');

create sequence if not exists nrfc.submission_location_seq start 1;
alter table nrfc.submission_location alter column submission_location_id set default nextval('nrfc.submission_location_seq');

create sequence if not exists nrfc.submission_location_contact_seq start 1;
alter table nrfc.submission_location_contact alter column submission_location_contact_id set default nextval('nrfc.submission_location_contact_seq');

create sequence if not exists nrfc.submission_submitter_seq start 1;
alter table nrfc.submission_submitter alter column submission_submitter_id set default nextval('nrfc.submission_submitter_seq');

-- 
-- INSERT STATIC DATA
-- Note: We have created draft scripts for the onboarding flow, but these are subject to change pending feedback from the UX team. Once we receive their confirmation, we can finalize the scripts and move forward with the final version.
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
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('Z', 'Sole Proprietorship', current_timestamp, 'mariamar') on conflict (client_type_code) do nothing;

update nrfc.client_type_code set expiry_date =  current_timestamp where client_type_code not in ('C', 'Z');

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

insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AD', 'Andorra', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AE', 'United Arab Emirates', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AF', 'Afghanistan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AG', 'Antigua and Barbuda', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AI', 'Anguilla', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AL', 'Albania', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AM', 'Armenia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AO', 'Angola', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AQ', 'Antarctica', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AR', 'Argentina', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AS', 'American Samoa', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AT', 'Austria', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AU', 'Australia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AW', 'Aruba', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AX', 'Åland Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('AZ', 'Azerbaijan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BA', 'Bosnia and Herzegovina', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BB', 'Barbados', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BD', 'Bangladesh', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BE', 'Belgium', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BF', 'Burkina Faso', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BG', 'Bulgaria', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BH', 'Bahrain', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BI', 'Burundi', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BJ', 'Benin', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BL', 'Saint Barthélemy', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BM', 'Bermuda', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BN', 'Brunei Darussalam', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BO', 'Bolivia (Plurinational State of)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BQ', 'Bonaire, Sint Eustatius and Saba', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BR', 'Brazil', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BS', 'Bahamas', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BT', 'Bhutan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BV', 'Bouvet Island', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BW', 'Botswana', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BY', 'Belarus', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('BZ', 'Belize', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CA', 'Canada', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CC', 'Cocos (Keeling) Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CD', 'Congo, Democratic Republic of the', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CF', 'Central African Republic', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CG', 'Congo', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CH', 'Switzerland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CI', 'Côte d''Ivoire', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CK', 'Cook Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CL', 'Chile', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CM', 'Cameroon', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CN', 'China', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CO', 'Colombia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CR', 'Costa Rica', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CU', 'Cuba', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CV', 'Cabo Verde', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CW', 'Curaçao', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CX', 'Christmas Island', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CY', 'Cyprus', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('CZ', 'Czechia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('DE', 'Germany', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('DJ', 'Djibouti', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('DK', 'Denmark', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('DM', 'Dominica', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('DO', 'Dominican Republic', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('DZ', 'Algeria', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('EC', 'Ecuador', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('EE', 'Estonia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('EG', 'Egypt', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('EH', 'Western Sahara', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ER', 'Eritrea', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ES', 'Spain', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ET', 'Ethiopia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('FI', 'Finland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('FJ', 'Fiji', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('FK', 'Falkland Islands (Malvinas)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('FM', 'Micronesia (Federated States of)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('FO', 'Faroe Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('FR', 'France', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GA', 'Gabon', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GB', 'United Kingdom of Great Britain and Northern Ireland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GD', 'Grenada', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GE', 'Georgia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GF', 'French Guiana', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GG', 'Guernsey', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GH', 'Ghana', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GI', 'Gibraltar', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GL', 'Greenland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GM', 'Gambia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GN', 'Guinea', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GP', 'Guadeloupe', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GQ', 'Equatorial Guinea', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GR', 'Greece', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GS', 'South Georgia and the South Sandwich Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GT', 'Guatemala', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GU', 'Guam', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GW', 'Guinea-Bissau', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('GY', 'Guyana', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('HK', 'Hong Kong', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('HM', 'Heard Island and McDonald Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('HN', 'Honduras', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('HR', 'Croatia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('HT', 'Haiti', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('HU', 'Hungary', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ID', 'Indonesia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IE', 'Ireland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IL', 'Israel', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IM', 'Isle of Man', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IN', 'India', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IO', 'British Indian Ocean Territory', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IQ', 'Iraq', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IR', 'Iran (Islamic Republic of)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IS', 'Iceland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('IT', 'Italy', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('JE', 'Jersey', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('JM', 'Jamaica', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('JO', 'Jordan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('JP', 'Japan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KE', 'Kenya', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KG', 'Kyrgyzstan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KH', 'Cambodia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KI', 'Kiribati', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KM', 'Comoros', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KN', 'Saint Kitts and Nevis', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KP', 'Korea (Democratic People''s Republic of)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KR', 'Korea, Republic of', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KW', 'Kuwait', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KY', 'Cayman Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('KZ', 'Kazakhstan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LA', 'Lao People''s Democratic Republic', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LB', 'Lebanon', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LC', 'Saint Lucia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LI', 'Liechtenstein', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LK', 'Sri Lanka', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LR', 'Liberia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LS', 'Lesotho', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LT', 'Lithuania', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LU', 'Luxembourg', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LV', 'Latvia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('LY', 'Libya', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MA', 'Morocco', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MC', 'Monaco', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MD', 'Moldova, Republic of', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ME', 'Montenegro', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MF', 'Saint Martin (French part)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MG', 'Madagascar', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MH', 'Marshall Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MK', 'North Macedonia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ML', 'Mali', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MM', 'Myanmar', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MN', 'Mongolia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MO', 'Macao', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MP', 'Northern Mariana Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MQ', 'Martinique', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MR', 'Mauritania', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MS', 'Montserrat', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MT', 'Malta', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MU', 'Mauritius', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MV', 'Maldives', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MW', 'Malawi', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MX', 'Mexico', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MY', 'Malaysia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('MZ', 'Mozambique', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NA', 'Namibia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NC', 'New Caledonia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NE', 'Niger', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NF', 'Norfolk Island', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NG', 'Nigeria', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NI', 'Nicaragua', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NL', 'Netherlands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NO', 'Norway', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NP', 'Nepal', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NR', 'Nauru', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NU', 'Niue', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('NZ', 'New Zealand', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('OM', 'Oman', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PA', 'Panama', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PE', 'Peru', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PF', 'French Polynesia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PG', 'Papua New Guinea', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PH', 'Philippines', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PK', 'Pakistan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PL', 'Poland', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PM', 'Saint Pierre and Miquelon', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PN', 'Pitcairn', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PR', 'Puerto Rico', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PS', 'Palestine, State of', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PT', 'Portugal', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PW', 'Palau', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('PY', 'Paraguay', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('QA', 'Qatar', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('RE', 'Réunion', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('RO', 'Romania', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('RS', 'Serbia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('RU', 'Russian Federation', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('RW', 'Rwanda', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SA', 'Saudi Arabia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SB', 'Solomon Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SC', 'Seychelles', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SD', 'Sudan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SE', 'Sweden', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SG', 'Singapore', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SH', 'Saint Helena, Ascension and Tristan da Cunha', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SI', 'Slovenia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SJ', 'Svalbard and Jan Mayen', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SK', 'Slovakia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SL', 'Sierra Leone', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SM', 'San Marino', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SN', 'Senegal', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SO', 'Somalia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SR', 'Suriname', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SS', 'South Sudan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ST', 'Sao Tome and Principe', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SV', 'El Salvador', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SX', 'Sint Maarten (Dutch part)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SY', 'Syrian Arab Republic', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('SZ', 'Eswatini', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TC', 'Turks and Caicos Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TD', 'Chad', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TF', 'French Southern Territories', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TG', 'Togo', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TH', 'Thailand', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TJ', 'Tajikistan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TK', 'Tokelau', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TL', 'Timor-Leste', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TM', 'Turkmenistan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TN', 'Tunisia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TO', 'Tonga', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TR', 'Türkiye', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TT', 'Trinidad and Tobago', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TV', 'Tuvalu', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TW', 'Taiwan, Province of China', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('TZ', 'Tanzania, United Republic of', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('UA', 'Ukraine', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('UG', 'Uganda', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('UM', 'United States Minor Outlying Islands', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('US', 'United States of America', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('UY', 'Uruguay', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('UZ', 'Uzbekistan', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VA', 'Holy See', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VC', 'Saint Vincent and the Grenadines', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VE', 'Venezuela (Bolivarian Republic of)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VG', 'Virgin Islands (British)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VI', 'Virgin Islands (U.S.)', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VN', 'Viet Nam', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('VU', 'Vanuatu', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('WF', 'Wallis and Futuna', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('WS', 'Samoa', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('YE', 'Yemen', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('YT', 'Mayotte', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ZA', 'South Africa', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ZM', 'Zambia', current_timestamp, 'mariamar') on conflict (country_code) do nothing;
insert into nrfc.country_code (country_code, description, effective_date, create_user) values ('ZW', 'Zimbabwe', current_timestamp, 'mariamar') on conflict (country_code) do nothing;

update nrfc.country_code set display_order = 1 where country_code = 'CA';
update nrfc.country_code set display_order = 2 where country_code = 'US';

insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NL', 'Newfoundland and Labrador', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('PE', 'Prince Edward Island', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NS', 'Nova Scotia', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NB', 'New Brunswick', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('QC', 'Quebec', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('ON', 'Ontario', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MB', 'Manitoba', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('SK', 'Saskatchewan', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('AB', 'Alberta', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('BC', 'British Columbia', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('YT', 'Yukon', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NT', 'Northwest Territories', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NU', 'Nunavut', current_timestamp, 'mariamar') on conflict (province_code) do nothing;

update nrfc.province_code set country_code = 'CA';

insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('AL', 'Alabama', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('AK', 'Alaska', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('AZ', 'Arizona', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('AR', 'Arkansas', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('CA', 'California', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('CO', 'Colorado', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('CT', 'Connecticut', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('DE', 'Delaware', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('DC', 'District of Columbia', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('FL', 'Florida', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('GA', 'Georgia', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('HI', 'Hawaii', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('ID', 'Idaho', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('IL', 'Illinois', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('IN', 'Indiana', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('IA', 'Iowa', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('KS', 'Kansas', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('KY', 'Kentucky', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('LA', 'Louisiana', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('ME', 'Maine', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MD', 'Maryland', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MA', 'Massachusetts', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MI', 'Michigan', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MN', 'Minnesota', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MS', 'Mississippi', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MO', 'Missouri', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('MT', 'Montana', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NE', 'Nebraska', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NV', 'Nevada', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NH', 'New Hampshire', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NJ', 'New Jersey', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NM', 'New Mexico', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NY', 'New York', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('NC', 'North Carolina', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('ND', 'North Dakota', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('OH', 'Ohio', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('OK', 'Oklahoma', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('OR', 'Oregon', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('PA', 'Pennsylvania', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('RI', 'Rhode Island', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('SC', 'South Carolina', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('SD', 'South Dakota', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('TN', 'Tennessee', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('TX', 'Texas', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('UT', 'Utah', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('VT', 'Vermont', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('VA', 'Virginia', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('WA', 'Washington', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('WV', 'West Virginia', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('WI', 'Wisconsin', current_timestamp, 'mariamar') on conflict (province_code) do nothing;
insert into nrfc.province_code (province_code, description, effective_date, create_user) values ('WY', 'Wyoming', current_timestamp, 'mariamar') on conflict (province_code) do nothing;

update nrfc.province_code set country_code = 'US' where country_code is null;
