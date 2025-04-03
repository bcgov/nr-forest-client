CREATE OR REPLACE TRIGGER THE.client_client_locn_ar_iud_trg
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
    v_client_audit_code := 'INS';
  ELSIF UPDATING THEN
    v_client_audit_code := 'UPD';
  ELSE
    v_client_audit_code := 'DEL';
  END IF;

  IF INSERTING OR UPDATING THEN
    IF UPDATING THEN
      --Address Change
      v_client_update_action_code := NULL;

      IF NVL(:OLD.address_1,CHR(255))
           ||NVL(:OLD.address_2,CHR(255))
           ||NVL(:OLD.address_3,CHR(255))
           ||NVL(:OLD.city,CHR(255))
           ||NVL(:OLD.province,CHR(255))
           ||NVL(:OLD.postal_code,CHR(255)) !=
           NVL(:NEW.address_1,CHR(255))
           ||NVL(:NEW.address_2,CHR(255))
           ||NVL(:NEW.address_3,CHR(255))
           ||NVL(:NEW.city,CHR(255))
           ||NVL(:NEW.province,CHR(255))
           ||NVL(:NEW.postal_code,CHR(255)) THEN
        v_client_update_action_code := 'ADDR';

      END IF;

      IF v_client_update_action_code IS NOT NULL THEN
        v_client_update_reason_code := 'UND';
      END IF;
    END IF;

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
          , SYSDATE
          , 'TRIGGERAUDIT'
          , 70
          , :OLD.add_timestamp
          , :OLD.add_userid
          , :OLD.add_org_unit);
  END IF;
END client_client_locn_ar_iud_trg;