CREATE OR REPLACE TRIGGER THE.client_for_client_ar_iud_trg
/******************************************************************************
   Trigger: CLIENT_FOR_CLIENT_AR_IUD_TRG
   Purpose: This trigger audits changes to the FOREST_CLIENT table
   Revision History
   Person               Date       Comments
   -----------------    ---------  --------------------------------
   R.A.Robb             2006-12-27 Created
   TMcClelland          2007-08-31 Added client_type_code to trigger insert
******************************************************************************/
AFTER INSERT OR UPDATE OR DELETE
  OF client_number
   , client_name
   , legal_first_name
   , legal_middle_name
   , client_status_code
   , client_type_code
   , birthdate
   , client_id_type_code
   , client_identification
   , registry_company_type_code
   , corp_regn_nmbr
   , client_acronym
   , wcb_firm_number
   , ocg_supplier_nmbr
   , client_comment
  ON forest_client
  FOR EACH ROW
DECLARE
  v_client_audit_code                for_cli_audit.client_audit_code%TYPE;
  v_client_update_action_code        client_update_action_code.client_update_action_code%TYPE;
  v_forest_client_audit_id           for_cli_audit.forest_client_audit_id%TYPE;
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
    INSERT INTO for_cli_audit
           (forest_client_audit_id
          , client_audit_code
          , client_number
          , client_name
          , legal_first_name
          , legal_middle_name
          , client_status_code
          , client_type_code
          , birthdate
          , client_id_type_code
          , client_identification
          , registry_company_type_code
          , corp_regn_nmbr
          , client_acronym
          , wcb_firm_number
          , ocg_supplier_nmbr
          , client_comment
          , add_timestamp
          , add_userid
          , add_org_unit
          , update_timestamp
          , update_userid
          , update_org_unit)
    VALUES (forest_client_audit_seq.NEXTVAL
          , v_client_audit_code
          , :NEW.client_number
          , :NEW.client_name
          , :NEW.legal_first_name
          , :NEW.legal_middle_name
          , :NEW.client_status_code
          , :NEW.client_type_code
          , :NEW.birthdate
          , :NEW.client_id_type_code
          , :NEW.client_identification
          , :NEW.registry_company_type_code
          , :NEW.corp_regn_nmbr
          , :NEW.client_acronym
          , :NEW.wcb_firm_number
          , :NEW.ocg_supplier_nmbr
          , :NEW.client_comment
          , :NEW.add_timestamp
          , :NEW.add_userid
          , :NEW.add_org_unit
          , :NEW.update_timestamp
          , :NEW.update_userid
          , :NEW.update_org_unit)
       RETURNING forest_client_audit_id INTO v_forest_client_audit_id;
    --Process update reasons
    IF UPDATING THEN
      --Status Change
      v_client_update_action_code := NULL;
      v_client_update_action_code := client_client_update_reason.check_status
                                    (:OLD.client_status_code
                                     ,:NEW.client_status_code);
      IF v_client_update_action_code IS NOT NULL THEN
        client_client_update_reason.init;
        client_client_update_reason.set_forest_client_audit_id(v_forest_client_audit_id);
        client_client_update_reason.set_client_update_action_code(v_client_update_action_code);
        --get reason from client pkg
        client_client_update_reason.set_client_update_reason_code(client_forest_client.get_ur_reason_status);
        client_client_update_reason.set_client_type_code(:NEW.client_type_code);
        client_client_update_reason.set_add_timestamp(:NEW.update_timestamp);
        client_client_update_reason.set_add_userid(:NEW.update_userid);
        client_client_update_reason.set_update_timestamp(:NEW.update_timestamp);
        client_client_update_reason.set_update_userid(:NEW.update_userid);
        client_client_update_reason.validate;
        IF NOT client_client_update_reason.error_raised THEN
          client_client_update_reason.add;
        END IF;
        IF client_client_update_reason.error_raised THEN
          RAISE_APPLICATION_ERROR(-20400,'Error writing update reason (Status) in audit trigger.');
        END IF;
      END IF;

      --Name Change
      v_client_update_action_code := NULL;
      v_client_update_action_code := client_client_update_reason.check_client_name
                                    (:OLD.client_name
                                    ,:OLD.legal_first_name
                                    ,:OLD.legal_middle_name
                                    ,:NEW.client_name
                                    ,:NEW.legal_first_name
                                    ,:NEW.legal_middle_name);
      IF v_client_update_action_code IS NOT NULL THEN
        client_client_update_reason.init;
        client_client_update_reason.set_forest_client_audit_id(v_forest_client_audit_id);
        client_client_update_reason.set_client_update_action_code(v_client_update_action_code);
        --get reason from client pkg
        client_client_update_reason.set_client_update_reason_code(client_forest_client.get_ur_reason_name);
        client_client_update_reason.set_client_type_code(:NEW.client_type_code);
        client_client_update_reason.set_add_timestamp(:NEW.update_timestamp);
        client_client_update_reason.set_add_userid(:NEW.update_userid);
        client_client_update_reason.set_update_timestamp(:NEW.update_timestamp);
        client_client_update_reason.set_update_userid(:NEW.update_userid);
        client_client_update_reason.validate;
        IF NOT client_client_update_reason.error_raised THEN
          client_client_update_reason.add;
        END IF;
        IF client_client_update_reason.error_raised THEN
          RAISE_APPLICATION_ERROR(-20400,'Error writing update reason (Name) in audit trigger.');
        END IF;
      END IF;

        --ID Change
      v_client_update_action_code := NULL;
      v_client_update_action_code := client_client_update_reason.check_id
                                    (:OLD.client_identification
                                    ,:OLD.client_id_type_code
                                    ,:NEW.client_identification
                                    ,:NEW.client_id_type_code);
      IF v_client_update_action_code IS NOT NULL THEN
        client_client_update_reason.init;
        client_client_update_reason.set_forest_client_audit_id(v_forest_client_audit_id);
        client_client_update_reason.set_client_update_action_code(v_client_update_action_code);
        --get reason from client pkg
        client_client_update_reason.set_client_update_reason_code(client_forest_client.get_ur_reason_id);
        client_client_update_reason.set_client_type_code(:NEW.client_type_code);
        client_client_update_reason.set_add_timestamp(:NEW.update_timestamp);
        client_client_update_reason.set_add_userid(:NEW.update_userid);
        client_client_update_reason.set_update_timestamp(:NEW.update_timestamp);
        client_client_update_reason.set_update_userid(:NEW.update_userid);
        client_client_update_reason.validate;
        IF NOT client_client_update_reason.error_raised THEN
          client_client_update_reason.add;
        END IF;
        IF client_client_update_reason.error_raised THEN
          RAISE_APPLICATION_ERROR(-20400,'Error writing update reason (Id) in audit trigger.');
        END IF;
      END IF;

    END IF;
  ELSE
    --DELETING: Put the last row into the audit table before deleting
    --          replacing update userid/timestamp/org
    -->check PK to make sure we are deleting the record in progress
    IF  client_forest_client.get_client_number = :OLD.client_number
    -->check that userid and timestamp are available
    AND client_forest_client.get_update_timestamp IS NOT NULL
    AND client_forest_client.get_update_userid IS NOT NULL
    AND client_forest_client.get_update_org_unit IS NOT NULL THEN
      INSERT INTO for_cli_audit
             (forest_client_audit_id
            , client_audit_code
            , client_number
            , client_name
            , legal_first_name
            , legal_middle_name
            , client_status_code
            , client_type_code
            , birthdate
            , client_id_type_code
            , client_identification
            , registry_company_type_code
            , corp_regn_nmbr
            , client_acronym
            , wcb_firm_number
            , ocg_supplier_nmbr
            , client_comment
            , add_timestamp
            , add_userid
            , add_org_unit
            , update_timestamp
            , update_userid
            , update_org_unit)
      VALUES (forest_client_audit_seq.NEXTVAL
            , v_client_audit_code
            , :OLD.client_number
            , :OLD.client_name
            , :OLD.legal_first_name
            , :OLD.legal_middle_name
            , :OLD.client_status_code
            , :OLD.client_type_code
            , :OLD.birthdate
            , :OLD.client_id_type_code
            , :OLD.client_identification
            , :OLD.registry_company_type_code
            , :OLD.corp_regn_nmbr
            , :OLD.client_acronym
            , :OLD.wcb_firm_number
            , :OLD.ocg_supplier_nmbr
            , :OLD.client_comment
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit
              , client_forest_client.get_update_timestamp
              , client_forest_client.get_update_userid
              , client_forest_client.get_update_org_unit);
    ELSE
      RAISE_APPLICATION_ERROR(-20500,'Data consistency error in auditing deletion of FOREST_CLIENT');
    END IF;
  END IF;
END client_for_client_ar_iud_trg;