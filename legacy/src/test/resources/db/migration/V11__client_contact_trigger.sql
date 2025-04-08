CREATE OR REPLACE TRIGGER THE.CLIENT_CLI_CONTACT_AR_IUD_TRG
AFTER INSERT OR UPDATE OR DELETE
  OF client_contact_id
   , client_number
   , client_locn_code
   , bus_contact_code
   , contact_name
   , business_phone
   , cell_phone
   , fax_number
   , email_address
  ON client_contact
  FOR EACH ROW
DECLARE
  v_client_audit_code                for_cli_audit.client_audit_code%TYPE;
BEGIN
  IF INSERTING THEN
    v_client_audit_code := 'INS';
  ELSIF UPDATING THEN
    v_client_audit_code := 'UPD';
  ELSE
    v_client_audit_code := 'DEL';
  END IF;

  IF INSERTING OR UPDATING THEN
    INSERT INTO cli_con_audit
           (client_contact_audit_id
          , client_audit_code
          , client_contact_id
          , client_number
          , client_locn_code
          , bus_contact_code
          , contact_name
          , business_phone
          , cell_phone
          , fax_number
          , email_address
          , update_timestamp
          , update_userid
          , update_org_unit_no
          , add_timestamp
          , add_userid
          , add_org_unit)
    VALUES (client_contact_audit_seq.NEXTVAL
          , v_client_audit_code
          , :NEW.client_contact_id
          , :NEW.client_number
          , :NEW.client_locn_code
          , :NEW.bus_contact_code
          , :NEW.contact_name
          , :NEW.business_phone
          , :NEW.cell_phone
          , :NEW.fax_number
          , :NEW.email_address
          , :NEW.update_timestamp
          , :NEW.update_userid
          , :NEW.update_org_unit
          , :NEW.add_timestamp
          , :NEW.add_userid
          , :NEW.add_org_unit);
  ELSE
      INSERT INTO cli_con_audit
             (client_contact_audit_id
            , client_audit_code
            , client_contact_id
            , client_number
            , client_locn_code
            , bus_contact_code
            , contact_name
            , business_phone
            , cell_phone
            , fax_number
            , email_address
            , update_timestamp
            , update_userid
            , update_org_unit_no
            , add_timestamp
            , add_userid
            , add_org_unit)
      VALUES (client_contact_audit_seq.NEXTVAL
            , v_client_audit_code
            , :OLD.client_contact_id
            , :OLD.client_number
            , :OLD.client_locn_code
            , :OLD.bus_contact_code
            , :OLD.contact_name
            , :OLD.business_phone
            , :OLD.cell_phone
            , :OLD.fax_number
            , :OLD.email_address
            , SYSDATE
            , 'TRIGGERAUDIT'
            , 70
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit);
  END IF;
END CLIENT_CLI_CONTACT_AR_IUD_TRG;