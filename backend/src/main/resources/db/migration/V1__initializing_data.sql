CREATE SCHEMA IF NOT EXISTS nrfc;

CREATE TABLE IF NOT EXISTS nrfc.client_type_code (
    client_type_code            varchar(1)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    CONSTRAINT client_type_code_pk PRIMARY KEY (client_type_code)
);

create table nrfc.submission_status_code (
    submission_status_code  	varchar(5)      not null,
    description                 varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    CONSTRAINT submission_status_code_pk PRIMARY KEY (submission_status_code)
);

CREATE TABLE IF NOT EXISTS nrfc.submission(
    submission_id             	serial4 		not null,
    submitter_user_guid        	varchar(50) 	null,
    submission_status_code		varchar(5)      null,
    submission_date             timestamp       null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		null,
    CONSTRAINT submission_pk PRIMARY KEY (submission_id),
    CONSTRAINT submission_submission_status_code_fk FOREIGN KEY (submission_status_code) REFERENCES nrfc.submission_status_code(submission_status_code)
);

CREATE TABLE IF NOT EXISTS nrfc.submission_detail (
    submission_detail_id        serial4			not null,
	submission_id				integer			not null,
	incorporation_number		varchar(50)    	null,
    organization_name           varchar(100)    null,
    first_name                  varchar(100)    null,
	middle_name                 varchar(100)    null,
	last_name                 	varchar(100)    null,
	client_type_code          	varchar(1)    	not null,
	date_of_birth				date 			null,
	client_comment				varchar(5000)	null,
	CONSTRAINT submission_detail_id_pk PRIMARY KEY (submission_detail_id),
	CONSTRAINT submission_detail_client_type_code_fk FOREIGN KEY (client_type_code) REFERENCES nrfc.client_type_code(client_type_code),
    CONSTRAINT submission_detail_submission_id_fk FOREIGN KEY (submission_id) REFERENCES nrfc.submission(submission_id)
);
