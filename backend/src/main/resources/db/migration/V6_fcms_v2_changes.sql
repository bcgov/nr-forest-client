alter table nrfc.submission_detail
add column if not exists work_safe_bc_number varchar(6) null,
add column if not exists doing_business_as varchar(120) null,
add column if not exists client_acronym varchar(8) null,
add column if not exists TBD_ID_TYPE varchar(8) null,
;

comment on column nrfc.submission_detail.work_safe_bc_number is 'TBD';
comment on column nrfc.submission_detail.doing_business_as is 'TBD';
comment on column nrfc.submission_detail.client_acronym is 'TBD';

alter table nrfc.submission_contact
add column if not exists middle_name varchar(30) null,
add column if not exists secondary_phone_number varchar(14) null,
add column if not exists fax_number varchar(14) null,
;

comment on column nrfc.submission_contact.middle_name is 'TBD';
comment on column nrfc.submission_contact.secondary_phone_number is 'TBD';
comment on column nrfc.submission_contact.fax_number is 'TBD';

