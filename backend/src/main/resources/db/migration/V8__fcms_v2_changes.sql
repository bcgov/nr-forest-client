do $$
begin
    -- rename country_code to identification_country_code if it doesn't already exist
    if not exists (
        select 1
        from information_schema.columns
        where table_schema = 'nrfc'
          and table_name = 'submission_detail'
          and column_name = 'identification_country_code'
    ) then
        alter table nrfc.submission_detail
        rename column country_code to identification_country_code;
    end if;

    -- rename province_code to identification_province_code if it doesn't already exist
    if not exists (
        select 1
        from information_schema.columns
        where table_schema = 'nrfc'
          and table_name = 'submission_detail'
          and column_name = 'identification_province_code'
    ) then
        alter table nrfc.submission_detail
        rename column province_code to identification_province_code;
    end if;
end $$;

insert into nrfc.identification_type_code (identification_type_code, description, country_code, effective_date, create_user) values ('OTHR', 'Other', null, current_timestamp, 'mariamar') on conflict (identification_type_code) do nothing;
