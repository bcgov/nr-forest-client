CREATE OR REPLACE TRIGGER THE.client_client_locn_ar_iud_trg
/******************************************************************************
   Trigger: CLIENT_CLIENT_LOCN_AR_IUD_TRG
   Purpose: This trigger audits changes to the CLIENT_LOCATION table
   Revision History
   Person               Date       Comments
   -----------------    ---------  --------------------------------
   R.A.Robb             2006-12-27 Created
   TMcClelland          2007-08-31 Added client_type_code to trigger insert
******************************************************************************/
AFTER INSERT OR UPDATE OR DELETE
  OF client_number
   , client_locn_code
   , client_locn_name
   , hdbs_company_code
   , address_1
   , address_2
   , address_3
   , city
   , province
   , postal_code
   , country
   , business_phone
   , home_phone
   , cell_phone
   , fax_number
   , email_address
   , locn_expired_ind
   , returned_mail_date
   , trust_location_ind
   , cli_locn_comment
  ON client_location
  FOR EACH ROW
DECLARE
  v_client_audit_code                cli_locn_audit.client_audit_code%TYPE;
  v_client_update_action_code        client_update_action_code.client_update_action_code%TYPE;
  v_client_update_reason_code        client_update_reason_code.client_update_reason_code%TYPE;
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

    --Process update reasons
    IF UPDATING THEN
      --Address Change
      v_client_update_action_code := NULL;
      v_client_update_action_code := client_client_update_reason.check_address
                                    (:OLD.address_1
                                    ,:OLD.address_2
                                    ,:OLD.address_3
                                    ,:OLD.city
                                    ,:OLD.province
                                    ,:OLD.postal_code
                                    ,:OLD.country
                                    ,:NEW.address_1
                                    ,:NEW.address_2
                                    ,:NEW.address_3
                                    ,:NEW.city
                                    ,:NEW.province
                                    ,:NEW.postal_code
                                    ,:NEW.country);
      IF v_client_update_action_code IS NOT NULL THEN
        --get reason from client locn pkg
        v_client_update_reason_code := client_client_location.get_ur_reason_addr;
      END IF;
    END IF;

    --Put the new row into the audit table
    INSERT INTO cli_locn_audit
           (client_location_audit_id
          , client_audit_code
          , client_number
          , client_locn_code
          , client_locn_name
          , hdbs_company_code
          , address_1
          , address_2
          , address_3
          , city
          , province
          , postal_code
          , country
          , business_phone
          , home_phone
          , cell_phone
          , fax_number
          , email_address
          , locn_expired_ind
          , returned_mail_date
          , trust_location_ind
          , cli_locn_comment
          , client_update_action_code
          , client_update_reason_code
          , client_type_code
          , update_timestamp
          , update_userid
          , update_org_unit
          , add_timestamp
          , add_userid
          , add_org_unit)
    SELECT client_location_audit_seq.nextval
          , v_client_audit_code
          , :NEW.client_number
          , :NEW.client_locn_code
          , :NEW.client_locn_name
          , :NEW.hdbs_company_code
          , :NEW.address_1
          , :NEW.address_2
          , :NEW.address_3
          , :NEW.city
          , :NEW.province
          , :NEW.postal_code
          , :NEW.country
          , :NEW.business_phone
          , :NEW.home_phone
          , :NEW.cell_phone
          , :NEW.fax_number
          , :NEW.email_address
          , :NEW.locn_expired_ind
          , :NEW.returned_mail_date
          , :NEW.trust_location_ind
          , :NEW.cli_locn_comment
          , v_client_update_action_code
          , v_client_update_reason_code
          , client_type_code
          , :NEW.update_timestamp
          , :NEW.update_userid
          , :NEW.update_org_unit
          , :NEW.add_timestamp
          , :NEW.add_userid
          , :NEW.add_org_unit
    FROM forest_client
    WHERE client_number = :NEW.client_number;

  ELSE
    --DELETING: Put the last row into the audit table before deleting
    --          replacing update userid/timestamp/org
    -->check PK to make sure we are deleting the record in progress
    IF  client_client_location.get_client_number = :OLD.client_number
    AND client_client_location.get_client_locn_code = :OLD.client_locn_code
    -->check that userid and timestamp are available
    AND client_client_location.get_update_timestamp IS NOT NULL
    AND client_client_location.get_update_userid IS NOT NULL
    AND client_client_location.get_update_org_unit IS NOT NULL THEN
       INSERT INTO cli_locn_audit
             (client_location_audit_id
            , client_audit_code
            , client_number
            , client_locn_code
            , client_locn_name
            , hdbs_company_code
            , address_1
            , address_2
            , address_3
            , city
            , province
            , postal_code
            , country
            , business_phone
            , home_phone
            , cell_phone
            , fax_number
            , email_address
            , locn_expired_ind
            , returned_mail_date
            , trust_location_ind
            , cli_locn_comment
            , update_timestamp
            , update_userid
            , update_org_unit
            , add_timestamp
            , add_userid
            , add_org_unit)
      VALUES (client_location_audit_seq.nextval
            , v_client_audit_code
            , :OLD.client_number
            , :OLD.client_locn_code
            , :OLD.client_locn_name
            , :OLD.hdbs_company_code
            , :OLD.address_1
            , :OLD.address_2
            , :OLD.address_3
            , :OLD.city
            , :OLD.province
            , :OLD.postal_code
            , :OLD.country
            , :OLD.business_phone
            , :OLD.home_phone
            , :OLD.cell_phone
            , :OLD.fax_number
            , :OLD.email_address
            , :OLD.locn_expired_ind
            , :OLD.returned_mail_date
            , :OLD.trust_location_ind
            , :OLD.cli_locn_comment
            , client_client_location.get_update_timestamp
            , client_client_location.get_update_userid
            , client_client_location.get_update_org_unit
            , :OLD.add_timestamp
            , :OLD.add_userid
            , :OLD.add_org_unit);
    ELSE
      RAISE_APPLICATION_ERROR(-20500,'Data consistency error in auditing deletion of CLIENT_LOCATION');
    END IF;
  END IF;
END client_client_locn_ar_iud_trg;