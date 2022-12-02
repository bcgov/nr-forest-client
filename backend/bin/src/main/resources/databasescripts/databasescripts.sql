create schema if not exists nrfc;

drop table if exists nrfc.submission_detail;
drop table if exists nrfc.submission;
drop table if exists nrfc.submission_status_code;
drop table if exists nrfc.client_type_code;

drop sequence if exists nrfc.submission_id_seq;
drop sequence if exists nrfc.submission_detail_id_seq;

-- 
-- TABLE: submission
--

create table nrfc.submission(
    submission_id             	integer 		not null,
    submitter_user_guid        	varchar(50) 	null,
    submission_status_code		varchar(5)      null,
    submission_date             timestamp       null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null
);

-- 
-- TABLE: submission_detail
--

create table nrfc.submission_detail (
    submission_detail_id        integer			not null,
	submission_id				integer			not null,
	incorporation_number		varchar(50)    	null,
    organization_name           varchar(100)    null,
    first_name                  varchar(100)    null,
	middle_name                 varchar(100)    null,
	last_name                 	varchar(100)    null,
	client_type_code          	varchar(1)    	not null,
	date_of_birth				date 			null,
	client_comment				varchar(5000)	null
);

-- 
-- TABLE: submission_status_code
--

create table nrfc.submission_status_code (
    submission_status_code  	varchar(5)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null
);

-- 
-- TABLE: client_type_code
--

create table nrfc.client_type_code (
    client_type_code            varchar(1)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null
);

-- 
-- CONSTRAINTS
--

alter table nrfc.submission 
add constraint submission_id_pk 
primary key (submission_id);

alter table nrfc.submission_detail 
add constraint submission_detail_id_pk 
primary key (submission_detail_id);

alter table nrfc.submission_status_code 
add constraint submission_status_code_pk 
primary key (submission_status_code);

alter table nrfc.client_type_code 
add constraint client_type_code_pk 
primary key (client_type_code);

alter table nrfc.submission
add constraint submission_submission_status_code_fk
foreign key (submission_status_code)
references nrfc.submission_status_code(submission_status_code);

alter table nrfc.submission_detail
add constraint submission_detail_submission_id_fk
foreign key (submission_id)
references nrfc.submission(submission_id);

alter table nrfc.submission_detail
add constraint submission_detail_client_type_code_fk
foreign key (client_type_code)
references nrfc.client_type_code(client_type_code);

-- 
-- SEQUENCES
--

create sequence nrfc.submission_id_seq start 1;
create sequence nrfc.submission_detail_id_seq start 1;

-- 
-- INSERT STATIC DATA
--

insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('P', 'In Progress', current_timestamp, 'mariamar');
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('A', 'Approved', current_timestamp, 'mariamar');
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('R', 'Rejected', current_timestamp, 'mariamar');
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('D', 'Deleted', current_timestamp, 'mariamar');
insert into nrfc.submission_status_code (submission_status_code, description, effective_date, create_user) values ('S', 'Submitted', current_timestamp, 'mariamar');

insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('A', 'Association', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('B', 'First Nation Band', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('C', 'Corporation', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('F', 'Ministry of Forests and Range', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('G', 'Government', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('I', 'Individual', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('L', 'Limited Partnership', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('P', 'General Partnership', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('R', 'First Nation Group', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('S', 'Society', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('T', 'First Nation Tribal Council', current_timestamp, 'mariamar');
insert into nrfc.client_type_code (client_type_code, description, effective_date, create_user) values ('U', 'Unregistered Company', current_timestamp, 'mariamar');