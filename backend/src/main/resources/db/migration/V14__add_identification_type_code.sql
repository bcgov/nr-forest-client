insert into nrfc.identification_type_code (identification_type_code, description, effective_date, expiry_date, create_user) 
values ('PRCD', 'Permanent resident card', current_timestamp, to_date('99991231','YYYYMMDD'), 'mariamar')  
on conflict (identification_type_code) do nothing;