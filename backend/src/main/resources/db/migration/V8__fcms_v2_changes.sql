alter table nrfc.submission_detail rename column country_code to identification_country_code;
alter table nrfc.submission_detail rename column province_code to identification_province_code;

insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('OTHR', 'Other', null, current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
