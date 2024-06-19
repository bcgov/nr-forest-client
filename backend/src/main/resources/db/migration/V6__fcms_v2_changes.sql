create table if not exists nrfc.identification_type_code (
    identification_type_code    varchar(4)      not null,
    description                 varchar(100)    not null,
    country_code                varchar(2)      null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint identification_type_code_pk primary key (identification_type_code)
);

comment on column nrfc.identification_type_code.identification_type_code is 'A code indicating the identification type of a client. Examples of possible ID types include Canadian Birth Certificate, Canadian Passport, and others.';
comment on column nrfc.identification_type_code.description is 'The description of the code value.';
comment on column nrfc.identification_type_code.country_code is 'A code representing the country to which the ID type belongs. If the country code is populated, a province or state will be required.';
comment on column nrfc.identification_type_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.identification_type_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.identification_type_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.identification_type_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.identification_type_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.identification_type_code.update_user is 'The user or proxy account that created or last updated the record.';


insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('BRTH', 'Canadian birth certificate', null, current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('CDDL', 'Canadian driver''s licence', 'CA', current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('PASS', 'Canadian passport', null, current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('CITZ', 'Canadian citizenship card', null, current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('FNID', 'First Nation status card', null, current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('USDL', 'US driver''s licence', 'US', current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;


-- Add columns if they don't exist
alter table nrfc.submission_detail
add column if not exists work_safe_bc_number varchar(6) null,
add column if not exists doing_business_as varchar(120) null,
add column if not exists client_acronym varchar(8) null,
add column if not exists first_name varchar(30) null,
add column if not exists middle_name varchar(30) null,
add column if not exists last_name varchar(30) null,
add column if not exists notes varchar(4000) null,
add column if not exists identification_type_code varchar(4) null,
add column if not exists client_identification varchar(40) null,
add column if not exists country_code varchar(2) null,
add column if not exists province_code varchar(4) null;

-- Add constraints if they don't exist
do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'identification_type_code_fk'
    ) then
        alter table nrfc.submission_detail
        add constraint identification_type_code_fk foreign key (identification_type_code)
        references nrfc.identification_type_code(identification_type_code);
    end if;

    if not exists (
        select 1
        from pg_constraint
        where conname = 'province_code_fk'
    ) then
        alter table nrfc.submission_detail
        add constraint province_code_fk foreign key (country_code, province_code)
        references nrfc.province_code(country_code, province_code);
    end if;
end $$;
comment on column nrfc.submission_detail.work_safe_bc_number is 'A Work Safe BC number of a client.';
comment on column nrfc.submission_detail.doing_business_as is 'An alternate name that a client may be conducting business under. For example: John Smith doing business as: John''s Logging.';
comment on column nrfc.submission_detail.client_acronym is 'A familiar alphabetic acronym to be used as an alternative to the ministry''s client number for data entry and display.';
comment on column nrfc.submission_detail.first_name is 'The first name of the client.';
comment on column nrfc.submission_detail.middle_name is 'The middle name of the client.';
comment on column nrfc.submission_detail.last_name is 'The last name of the client.';
comment on column nrfc.submission_detail.notes is 'Any additional information about the client or specific instructions.';
comment on column nrfc.submission_detail.identification_type_code is 'A code indicating the identification type of a client.';
comment on column nrfc.submission_detail.client_identification is 'The reference number that identifies a client.';
comment on column nrfc.submission_detail.country_code is 'A code representing the code of a country.';
comment on column nrfc.submission_detail.province_code is 'A code representing the code of a province, state, department or territory.';


alter table nrfc.submission_contact
add column if not exists secondary_phone_number varchar(14) null,
add column if not exists fax_number varchar(14) null;

comment on column nrfc.submission_contact.secondary_phone_number is 'A secondary phone number of the client''s contact.';
comment on column nrfc.submission_contact.fax_number is 'The fax number of the client''s contact.';


alter table nrfc.submission_location
add column if not exists business_phone_number varchar(14) null,
add column if not exists secondary_phone_number varchar(14) null,
add column if not exists fax_number varchar(14) null,
add column if not exists notes varchar(4000) null,
add column if not exists complementary_address_1 varchar(40) null,
add column if not exists complementary_address_2 varchar(40) null,
add column if not exists email_address varchar(100) null;

comment on column nrfc.submission_location.business_phone_number is 'The phone number of a client''s specific location.';
comment on column nrfc.submission_location.secondary_phone_number is 'A secondary phone number of a client''s specific location.';
comment on column nrfc.submission_location.fax_number is 'The fax number of a client''s specific location.';
comment on column nrfc.submission_location.notes is 'Any additional information about the client''s location or specific instructions for contacting them.';
comment on column nrfc.submission_location.complementary_address_1 is 'A complementary address line of a client';
comment on column nrfc.submission_location.complementary_address_2 is 'A second complementary address line of a client';
comment on column nrfc.submission_location.email_address is 'The email address of the client''s location.';


alter table nrfc.province_code
add column if not exists display_order integer null;

comment on column nrfc.province_code.display_order is 'The the display order of a province/state. If no value is provided, the application will order provinces/states alphabetically by default.';

update nrfc.province_code set display_order = 1 where province_code = 'BC';
