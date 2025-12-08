insert into nrfc.contact_type_code (contact_type_code, description, effective_date, expiry_date, create_user) 
values ('GC', 'General Contact', to_date('99991231','YYYYMMDD'), current_timestamp, 'mariamar')  
on conflict (contact_type_code) do nothing;
