-- 
-- DROPPING TABLES AND SEQUENCES IF EXIST AS THIS IS THE INIT FILE TO CREATE THE DB
--
drop table if exists nrfc.submission_detail;
drop table if exists nrfc.submission_matching_detail;
drop table if exists nrfc.submission_location_contact;--Legacy table
drop table if exists nrfc.submission_location_contact_xref;
drop table if exists nrfc.submission_location;
drop table if exists nrfc.submission_submitter;--Legacy table
drop table if exists nrfc.submission_contact;
drop table if exists nrfc.submission;
drop table if exists nrfc.client_type_code;
drop table if exists nrfc.submission_status_code;
drop table if exists nrfc.submission_type_code;
drop table if exists nrfc.province_code;
drop table if exists nrfc.country_code;
drop table if exists nrfc.contact_type_code;
drop table if exists nrfc.business_type_code;

drop sequence if exists nrfc.submission_id_seq;
drop sequence if exists nrfc.submission_detail_id_seq;
drop sequence if exists nrfc.submission_matching_detail_id_seq;
drop sequence if exists nrfc.submission_location_seq;
drop sequence if exists nrfc.submission_location_contact_seq;--Legacy sequence
drop sequence if exists nrfc.submission_contact_seq;
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
    update_user                 varchar(60)		  null,
    constraint client_type_code_pk primary key (client_type_code)
);

comment on table nrfc.client_type_code is 'A code indicating the type of a client. Examples include, but are not limited to: Corporation, Individual, Association, and others.';
comment on column nrfc.client_type_code.client_type_code is 'A code representing the type of a client.';
comment on column nrfc.client_type_code.description is 'The description of the code value.';
comment on column nrfc.client_type_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.client_type_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.client_type_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.client_type_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.client_type_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.client_type_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.submission_status_code (
    submission_status_code  	  varchar(5)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint submission_status_code_pk primary key (submission_status_code)
);

comment on table nrfc.submission_status_code is 'A code indicating the status of a client creation submission request. Examples of possible statuses include Approved, Rejected, and others.';
comment on column nrfc.submission_status_code.submission_status_code is 'A code representing the status of a submission request.';
comment on column nrfc.submission_status_code.description is 'The description of the code value.';
comment on column nrfc.submission_status_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.submission_status_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.submission_status_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.submission_status_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.submission_status_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.submission_status_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.submission_type_code (
    submission_type_code  	    varchar(5)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint submission_type_code_pk primary key (submission_type_code)
);

comment on table nrfc.submission_type_code is 'A code indicating the type of a submission request. Examples of possible type are TBD.';
comment on column nrfc.submission_type_code.submission_type_code is 'A code representing the type of a submission request.';
comment on column nrfc.submission_type_code.description is 'The description of the code value.';
comment on column nrfc.submission_type_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.submission_type_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.submission_type_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.submission_type_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.submission_type_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.submission_type_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.country_code (
    country_code                varchar(2)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    display_order               integer         null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint country_code_pk  primary key (country_code),
    constraint display_order_uk unique (display_order)
);

comment on table nrfc.country_code is 'A list of countries, serving as a reference for country-specific data.';
comment on column nrfc.country_code.country_code is 'A code representing the code of a country.';
comment on column nrfc.country_code.description is 'The description of the code value.';
comment on column nrfc.country_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.country_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.country_code.display_order is 'The the display order of a country. If no value is provided, the application will order countries alphabetically by default.';
comment on column nrfc.country_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.country_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.country_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.country_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.province_code (
    province_code               varchar(4)      not null,
    country_code                varchar(2)      null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint province_code_pk primary key (country_code, province_code),
    constraint province_code_country_code_fk foreign key (country_code) references nrfc.country_code(country_code)
);

comment on table nrfc.province_code is 'A list of provinces, states, departments or territories, serving as a reference for province-specific data.';
comment on column nrfc.province_code.province_code is 'A code representing the code of a province, state, department or territory.';
comment on column nrfc.province_code.country_code is 'A code representing the country in which the province is located.';
comment on column nrfc.province_code.description is 'The description of the code value.';
comment on column nrfc.province_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.province_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.province_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.province_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.province_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.province_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.contact_type_code (
    contact_type_code           varchar(2)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint contact_type_code_pk primary key (contact_type_code)
);

comment on table nrfc.contact_type_code is 'A code indicating the role of a contact of a client.';
comment on column nrfc.contact_type_code.contact_type_code is 'A code representing the code of a role of a client''s contact.';
comment on column nrfc.contact_type_code.description is 'The description of the code value.';
comment on column nrfc.contact_type_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.contact_type_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.contact_type_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.contact_type_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.contact_type_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.contact_type_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.business_type_code (
    business_type_code        	varchar(1)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint business_type_code_pk primary key (business_type_code)
);

comment on table nrfc.business_type_code is 'A code indicating the business type of a client. It could be either Registered Business and Unregistered Business.';
comment on column nrfc.business_type_code.business_type_code is 'A code indicating the business type of a client.';
comment on column nrfc.business_type_code.description is 'The description of the code value.';
comment on column nrfc.business_type_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.business_type_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.business_type_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.business_type_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.business_type_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.business_type_code.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.submission(
    submission_id             	integer 		    not null,
    submission_status_code		  varchar(5)      null,
    submission_type_code		    varchar(5)      null,
    submission_date             timestamp       null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint submission_pk primary key (submission_id),
    constraint submission_submission_status_code_fk foreign key (submission_status_code) references nrfc.submission_status_code(submission_status_code),
    constraint submission_submission_type_code_fk foreign key (submission_type_code) references nrfc.submission_type_code(submission_type_code)
);

comment on table nrfc.submission is 'A submission request to create a client.';
comment on column nrfc.submission.submission_id is 'Incremental id generated for a submission of a client.';
comment on column nrfc.submission.submission_status_code is 'TBD.';
comment on column nrfc.submission.submission_type_code is 'A code indicating the status of a submission request. Examples include, but are not limited to: New, Approved, Rejected, and others.';
comment on column nrfc.submission.submission_date is 'The date and time the record was created.';
comment on column nrfc.submission.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.submission.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.submission.update_user is 'The user or proxy account that created or last updated the record.';

create table if not exists nrfc.submission_detail (
    submission_detail_id    integer		    	not null,
	  submission_id				    integer   			not null,
    client_number           varchar(8)      null,
	  business_type_code			varchar(1)    	not null,
	  incorporation_number		varchar(50)    	null,
    organization_name       varchar(100)    null,
	  client_type_code        varchar(1)    	not null,
    good_standing_ind       varchar(1)      null,
	  constraint submission_detail_id_pk primary key (submission_detail_id),
	  constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_detail_business_type_code_fk foreign key (business_type_code) references nrfc.business_type_code(business_type_code),
    constraint submission_detail_client_type_code_fk foreign key (client_type_code) references nrfc.client_type_code(client_type_code)
);

comment on table nrfc.submission_detail is 'The details of a submission request to create a client.';
comment on column nrfc.submission_detail.submission_detail_id is 'Incremental id generated for a submission detail of a client.';
comment on column nrfc.submission_detail.submission_id is 'Incremental id generated for a submission of a client.';
comment on column nrfc.submission_detail.business_type_code is 'A code indicating the business type of a client. It could be either Registered Business and Unregistered Business.';
comment on column nrfc.submission_detail.incorporation_number is 'A number provided to B.C. corporations, businesses or societies as part of the registration or incorporation process.';
comment on column nrfc.submission_detail.organization_name is 'The name of the client.';
comment on column nrfc.submission_detail.client_type_code is 'A code representing the type of a client.';
comment on column nrfc.submission_detail.good_standing_ind is 'An indicator that determines whether a client is in good standing with respect to their financial obligations.';

create table if not exists nrfc.submission_matching_detail (
  submission_matching_detail_id   integer			  not null,
	submission_id				            integer			  not null,
	matching_fields		              jsonb 			  null,
	confirmed_match_status_ind	    varchar(1)    null,
	confirmed_match_message         varchar(255)  null,
	confirmed_match_timestamp       timestamp     null,
	confirmed_match_userid          varchar(60)   null,
	submission_matching_processed   bool          not null default false,
	constraint submission_matching_detail_id_pk primary key (submission_matching_detail_id),
	constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id)
);

comment on table nrfc.submission_matching_detail is 'The number of matches, if any, found in the legacy oracle database. These matches are determined through a combination of fuzzy search in specific fields and exact equality in others.';
comment on column nrfc.submission_matching_detail.submission_matching_detail_id is 'Incremental id generated for a submission matching detail of a client.';
comment on column nrfc.submission_matching_detail.submission_id is 'Incremental id generated for a submission of a client.';
comment on column nrfc.submission_matching_detail.matching_fields is 'A JSON object provides information about matches found in the existing database. The matches can result from fuzzy search, like matching a name pattern, or from exact equality, such as matching incorporation number or good standing status.';
comment on column nrfc.submission_matching_detail.confirmed_match_status_ind is 'An indicator that determines whether a specific match has been approved, allowing the processing to continue, or denied, requiring immediate action to be taken during the submission process.';
comment on column nrfc.submission_matching_detail.confirmed_match_message is 'A field with the reason for the denial of the submission.';
comment on column nrfc.submission_matching_detail.confirmed_match_timestamp is 'The timestamp when the changes happened.';
comment on column nrfc.submission_matching_detail.confirmed_match_userid is 'The ID of the user submitting the changes.';
comment on column nrfc.submission_matching_detail.submission_matching_processed is 'A flag to indicate that the submission was finally processed.';

create table if not exists nrfc.submission_location (
    submission_location_id      integer			    not null,
	  submission_id               integer			    not null,
    street_address              varchar(50)    	not null,    
    country_code                varchar(2)      not null,
    province_code               varchar(2)      not null,
    city_name                   varchar(100)    not null,
    postal_code                 varchar(10)     not null,
    location_name               varchar(20)     not null,
    constraint submission_location_id_pk primary key (submission_location_id),
    constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_location_country_code_fk foreign key (country_code) references nrfc.country_code(country_code),
    constraint submission_location_province_code_fk foreign key (country_code, province_code) references nrfc.province_code(country_code, province_code)
);

comment on table nrfc.submission_location is 'The details of a client''s location.';
comment on column nrfc.submission_location.submission_location_id is 'Incremental id generated for a location of a client.';
comment on column nrfc.submission_location.submission_id is 'Incremental id generated for a submission of a client.';
comment on column nrfc.submission_location.street_address is 'The address of a client, including the street number and street name.';
comment on column nrfc.submission_location.country_code is 'A code representing the code of a country.';
comment on column nrfc.submission_location.province_code is 'A code representing the code of a province, state, department or territory.';
comment on column nrfc.submission_location.city_name is 'The name of the city of the location.';
comment on column nrfc.submission_location.location_name is 'The location name of an address. Examples of location names include, but are not limited to, Mailing Address, Billing Address among others.';

create table if not exists nrfc.submission_contact (
    submission_contact_id      integer		      not null,
	  submission_id              integer			    not null,
    contact_type_code          varchar(2)       not null,
    first_name                 varchar(100)     null,
	  last_name                  varchar(100)     null,
    business_phone_number      varchar(20)      not null,
    email_address              varchar(100)     not null,
    idp_user_id                varchar(50)      null,
    constraint submission_contact_id_pk primary key (submission_contact_id),
    constraint submission_id_fk foreign key (submission_id) references nrfc.submission(submission_id),
    constraint submission_contact_contact_type_code_fk foreign key (contact_type_code) references nrfc.contact_type_code(contact_type_code)
);

comment on table nrfc.submission_contact is 'The details of a contacts for each client''s location.';
comment on column nrfc.submission_contact.submission_contact_id is 'Incremental id generated for the contact details of a client.';
comment on column nrfc.submission_contact.submission_id is 'Incremental id generated for a submission of a client.';
comment on column nrfc.submission_contact.contact_type_code is 'A code representing the code of a role of a client''s contact.';
comment on column nrfc.submission_contact.first_name is 'The first name of the client''s contact.';
comment on column nrfc.submission_contact.last_name is 'The last name of the client''s contact.';
comment on column nrfc.submission_contact.business_phone_number is 'The phone number of the client''s contact.';
comment on column nrfc.submission_contact.email_address is 'The email address of the client''s contact.';
comment on column nrfc.submission_contact.idp_user_id is 'The unique identifier assigned to a user by the Identity Provider.';

create table if not exists nrfc.submission_location_contact_xref (
    submission_location_id      integer			not null,
    submission_contact_id       integer		  not null,
    constraint submission_location_contact_xref_id_pk primary key (submission_location_id, submission_contact_id),
    constraint submission_location_id_fk foreign key (submission_location_id) references nrfc.submission_location(submission_location_id),
    constraint submission_contact_id_fk foreign key (submission_contact_id) references nrfc.submission_contact(submission_contact_id)
);

comment on table nrfc.submission_location_contact_xref is 'Identifies the multiple addresses for a given client, as well as the many contacts that may belong to a given address.';
comment on column nrfc.submission_location_contact_xref.submission_location_id is 'Incremental id generated for a location of a client.';
comment on column nrfc.submission_location_contact_xref.submission_contact_id is 'Incremental id generated for the contact details of a client.';

-- 
-- SEQUENCES
--

create sequence if not exists nrfc.submission_id_seq start 1;
alter table nrfc.submission alter column submission_id set default nextval('nrfc.submission_id_seq');

create sequence if not exists nrfc.submission_detail_id_seq start 1;
alter table nrfc.submission_detail alter column submission_detail_id set default nextval('nrfc.submission_detail_id_seq');

create sequence if not exists nrfc.submission_matching_detail_id_seq start 1;
alter table nrfc.submission_matching_detail alter column submission_matching_detail_id set default nextval('nrfc.submission_matching_detail_id_seq');

create sequence if not exists nrfc.submission_location_seq start 1;
alter table nrfc.submission_location alter column submission_location_id set default nextval('nrfc.submission_location_seq');

create sequence if not exists nrfc.submission_contact_seq start 1;
alter table nrfc.submission_contact alter column submission_contact_id set default nextval('nrfc.submission_contact_seq');
