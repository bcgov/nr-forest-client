create or replace trigger client_cli_contact_ar_iud_trg
/******************************************************************************
   Trigger: CLIENT_CLI_CONTACT_AR_IUD_TRG
   Purpose: This trigger audits changes to the CLIENT_CONTACT table
   Revision History
   Person               Date       Comments
   -----------------    ---------  --------------------------------
   R.A.Robb             2006-12-27 Created
   E.Wong               2007-01-24 Added Client Contact Id auditing
   P.Cruz               2025-04-10 Removed check for contact id
******************************************************************************/
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
    v_client_audit_code := client_constants.c_audit_insert;
  ELSIF UPDATING THEN
    v_client_audit_code := client_constants.c_audit_update;
  ELSE
    v_client_audit_code := client_constants.c_audit_delete;
  END IF;

  IF    INSERTING
     OR UPDATING THEN
    --Put the new row into the audit table
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
    --DELETING: Put the last row into the audit table before deleting
    --          replacing update userid/timestamp/org
    -->check PK to make sure we are deleting the record in progress
    IF /*client_client_contact.get_client_contact_id = :OLD.client_contact_id
    -->check that userid and timestamp are available
    AND*/ client_client_contact.get_update_timestamp IS NOT NULL
    AND client_client_contact.get_update_userid IS NOT NULL
    AND client_client_contact.get_update_org_unit IS NOT NULL THEN
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
            , client_client_contact.get_update_timestamp
            , client_client_contact.get_update_userid
            , client_client_contact.get_update_org_unit
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit);
    ELSE
      RAISE_APPLICATION_ERROR(-20500,'Data consistency error in auditing deletion of CLIENT_CONTACT');
    END IF;
  END IF;
END client_cli_contact_ar_iud_trg;


 