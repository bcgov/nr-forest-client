CREATE OR REPLACE TRIGGER THE.CLIENT_DBA_AR_IUD_TRG
/******************************************************************************
   Trigger: CLIENT_DBA_AR_IUD_TRG
   Purpose: This trigger audits changes to the CLIENT_DOING_BUSINESS_AS table
   Revision History
   Person               Date       Comments
   -----------------    ---------- ---------------------------------
   E.Wong               2007-03-29 Created
******************************************************************************/
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
    v_client_audit_code := client_constants.c_audit_insert;
  ELSIF UPDATING THEN
    v_client_audit_code := client_constants.c_audit_update;
  ELSE
    v_client_audit_code := client_constants.c_audit_delete;
  END IF;

  IF    INSERTING
     OR UPDATING THEN
    --Put the new row into the audit table
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
    --DELETING: Put the last row into the audit table before deleting
    --          replacing update userid/timestamp/org
    -->check PK to make sure we are deleting the record in progress
    IF /*client_client_doing_bus_as.get_client_dba_id = :OLD.client_dba_id
    -->check that userid and timestamp are available
    AND*/ client_client_doing_bus_as.get_update_timestamp IS NOT NULL
    AND client_client_doing_bus_as.get_update_userid IS NOT NULL
    AND client_client_doing_bus_as.get_update_org_unit IS NOT NULL THEN
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
            , client_client_doing_bus_as.get_update_timestamp
            , client_client_doing_bus_as.get_update_userid
            , client_client_doing_bus_as.get_update_org_unit
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit);
    ELSE
      RAISE_APPLICATION_ERROR(-20500,'Data consistency error in auditing deletion of CLIENT_DOING_BUSINESS_AS');
    END IF;
  END IF;
END CLIENT_DBA_AR_IUD_TRG;