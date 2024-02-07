create table if not exists nrfc.district_code (
    district_code               varchar(5)      not null,
    description                 varchar(100)    not null,
    email_address               varchar(100)    not null,
    effective_date              date            not null,
    expiry_date                 date            default to_date('99991231','YYYYMMDD') not null,
    create_timestamp            timestamp       default current_timestamp not null,
    update_timestamp            timestamp       default current_timestamp,
    create_user                 varchar(60)     not null,
    update_user                 varchar(60)		  null,
    constraint district_code_pk primary key (district_code)
);

alter table nrfc.submission_detail 
add column district_code varchar(100);

alter table nrfc.submission_detail
add constraint submission_detail_district_code_fk 
  foreign key (district_code) 
  references nrfc.district_code(district_code);

comment on table nrfc.district_code is 'A list of Natural Resource Districts.';
comment on column nrfc.district_code.district_code is 'A code representing the code of a Natural Resource District.';
comment on column nrfc.district_code.description is 'The description of the code value.';
comment on column nrfc.district_code.email_address is 'The email address of the Natural Resource District.';
comment on column nrfc.district_code.effective_date is 'The date that the code value has become or is expected to become effective. Default is the data that the code value is created.';
comment on column nrfc.district_code.expiry_date is 'The date on which the code value has expired or is expected to expire.  Default 9999-12-31.';
comment on column nrfc.district_code.create_timestamp is 'The date and time the record was created.';
comment on column nrfc.district_code.update_timestamp is 'The date and time the record was created or last updated.';
comment on column nrfc.district_code.create_user is 'The user or proxy account that created the record.';
comment on column nrfc.district_code.update_user is 'The user or proxy account that created or last updated the record.';

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DMH', '100 Mile House Natural Resource District', 'FLNR.100MileHouseDistrict@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DCR', 'Campbell River Natural Resource District', 'FTA.DCR@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DCC', 'Cariboo-Chilcotin Natural Resource District', 'DCC.Tenures@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DCS', 'Cascades Natural Resource District', 'Forests.CascadesDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DCK', 'Chilliwack Natural Resource District', 'FTA.DCKDSQ@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DKM', 'Coast Mountains Natural Resource District', 'DCM.Tenures@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DFN', 'Fort Nelson Natural Resource District', 'Forests.FortNelsonDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DQC', 'Haida Gwaii Natural Resource District', 'FrontCounterHaidaGwaii@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DMK', 'Mackenzie Natural Resource District', 'Forests.MackenzieDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DND', 'Nadina Natural Resource District', 'Forests.NadinaDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DNI', 'North Island - Central Coast Natural Resource District', 'FTA.DIC@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DOS', 'Okanagan Shuswap Natural Resource District', 'DOS.CPRP@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DPC', 'Peace Natural Resource District', 'PeaceDistrict.Tenures@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DPG', 'Prince George Natural Resource District', 'Forests.PrinceGeorgeDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DQU', 'Quesnel Natural Resource District', 'Forests.QuesnelDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DRM', 'Rocky Mountain Natural Resource District', 'FORESTS.rockymountaindistrictoffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DSQ', 'Sea to Sky Natural Resource District', 'FTA.DCKDSQ@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DSE', 'Selkirk Natural Resource District', 'Resources.Nelson@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DSS', 'Skeena Stikine Natural Resource District', 'Forests.SkeenaStikineDistrictOffice@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DSI', 'South Island Natural Resource District', 'FTA.DSI@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DVA', 'Stuart Nechako Natural Resource District', 'dsn.submissions@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DSC', 'Sunshine Coast Natural Resource District', 'FTA.DSC@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;

insert into nrfc.district_code (district_code, description, email_address, effective_date, create_user) 
values ('DKA', 'Thompson Rivers Natural Resource District', 'DTR.Tenures@gov.bc.ca', current_timestamp, 'mariamar') on conflict (district_code) do nothing;
