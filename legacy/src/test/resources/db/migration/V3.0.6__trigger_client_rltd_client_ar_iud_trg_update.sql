CREATE OR REPLACE TRIGGER THE.client_rltd_client_ar_iud_trg
/******************************************************************************
   Trigger: CLIENT_RLTD_CLIENT_AR_IUD_TRG
   Purpose: This trigger audits changes to the RELATED_CLIENT table
   Revision History
   Person               Date       Comments
   -----------------    ---------  --------------------------------
   R.A.Robb             2006-12-27 Created
******************************************************************************/
AFTER INSERT OR UPDATE OR DELETE
  OF client_number
   , client_locn_code
   , related_clnt_nmbr
   , related_clnt_locn
   , relationship_code
   , signing_auth_ind
   , percent_ownership
  ON related_client
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
    INSERT INTO rel_cli_audit
           (related_client_audit_id
          , client_audit_code
          , client_number
          , client_locn_code
          , related_clnt_nmbr
          , related_clnt_locn
          , relationship_code
          , signing_auth_ind
          , percent_ownership
          , update_timestamp
          , update_userid
          , update_org_unit
          , add_timestamp
          , add_userid
          , add_org_unit)
    VALUES (related_client_audit_seq.NEXTVAL
          , v_client_audit_code
          , :NEW.client_number
          , :NEW.client_locn_code
          , :NEW.related_clnt_nmbr
          , :NEW.related_clnt_locn
          , :NEW.relationship_code
          , :NEW.signing_auth_ind
          , :NEW.percent_ownership
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
    IF  client_related_client.get_update_timestamp IS NOT NULL
    AND client_related_client.get_update_userid IS NOT NULL
    AND client_related_client.get_update_org_unit IS NOT NULL THEN
      INSERT INTO rel_cli_audit
             (related_client_audit_id
            , client_audit_code
            , client_number
            , client_locn_code
            , related_clnt_nmbr
            , related_clnt_locn
            , relationship_code
            , signing_auth_ind
            , percent_ownership
            , update_timestamp
            , update_userid
            , update_org_unit
            , add_timestamp
            , add_userid
            , add_org_unit)
      VALUES (related_client_audit_seq.NEXTVAL
            , v_client_audit_code
            , :OLD.client_number
            , :OLD.client_locn_code
            , :OLD.related_clnt_nmbr
            , :OLD.related_clnt_locn
            , :OLD.relationship_code
            , :OLD.signing_auth_ind
            , :OLD.percent_ownership
            , client_related_client.get_update_timestamp
            , client_related_client.get_update_userid
            , client_related_client.get_update_org_unit
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit);
    ELSE
      RAISE_APPLICATION_ERROR(-20500,'Data consistency error in auditing deletion of RELATED_CLIENT');
    END IF;
  END IF;
END client_rltd_client_ar_iud_trg;