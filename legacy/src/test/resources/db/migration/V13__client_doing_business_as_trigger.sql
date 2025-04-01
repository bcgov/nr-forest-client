CREATE OR REPLACE TRIGGER THE.CLIENT_DBA_AR_IUD_TRG
AFTER INSERT OR UPDATE OR DELETE
  OF client_dba_id
   , client_number
   , doing_business_as_name
  ON client_doing_business_as
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
    INSERT INTO client_doing_business_as_audit
           (client_dba_audit_id
          , client_audit_code
          , client_dba_id
          , client_number
          , doing_business_as_name
          , update_timestamp
          , update_userid
          , update_org_unit
          , add_timestamp
          , add_userid
          , add_org_unit)
    VALUES (client_dba_audit_seq.NEXTVAL
          , v_client_audit_code
          , :NEW.client_dba_id
          , :NEW.client_number
          , :NEW.doing_business_as_name
          , :NEW.update_timestamp
          , :NEW.update_userid
          , :NEW.update_org_unit
          , :NEW.add_timestamp
          , :NEW.add_userid
          , :NEW.add_org_unit);
  ELSE
      INSERT INTO client_doing_business_as_audit
             (client_dba_audit_id
            , client_audit_code
            , client_dba_id
            , client_number
            , doing_business_as_name
            , update_timestamp
            , update_userid
            , update_org_unit
            , add_timestamp
            , add_userid
            , add_org_unit)
      VALUES (client_dba_audit_seq.NEXTVAL
            , v_client_audit_code
            , :OLD.client_dba_id
            , :OLD.client_number
            , :OLD.doing_business_as_name
            , SYSDATE
            , 'TRIGGERAUDIT'
            , 70
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit);
  END IF;
END CLIENT_DBA_AR_IUD_TRG;