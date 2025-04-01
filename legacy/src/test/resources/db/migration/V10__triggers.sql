CREATE OR REPLACE TRIGGER CLIENT_FOR_CLIENT_AR_IUD_TRG
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
    v_client_audit_code := 'INS';
  ELSIF UPDATING THEN
    v_client_audit_code := 'UPD';
  ELSE
    v_client_audit_code := 'DEL';
  END IF;

  IF INSERTING OR UPDATING THEN
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
  END IF;


  IF UPDATING THEN

      v_client_update_action_code := NULL;

	  IF NVL(:OLD.client_status_code,CHR(255)) != NVL(:NEW.client_status_code,CHR(255)) THEN
  	    IF :NEW.client_status_code = 'SPN' THEN
	        v_client_update_action_code := 'SPN';
		ELSIF :NEW.client_status_code = 'DAC' THEN
	        v_client_update_action_code := 'DAC';
	    ELSIF :OLD.client_status_code = 'SPN' AND :NEW.client_status_code = 'ACT' THEN
	        v_client_update_action_code := 'USPN';
      	ELSIF :OLD.client_status_code = 'DEC' AND :NEW.client_status_code = 'ACT' THEN
	        v_client_update_action_code := 'RACT';
      	ELSIF :OLD.client_status_code = 'DAC' AND :NEW.client_status_code = 'ACT' THEN
	        v_client_update_action_code := 'RACT';
      	END IF;
	  END IF;


	  IF v_client_update_action_code IS NOT NULL THEN

	  	INSERT INTO THE.CLIENT_UPDATE_REASON
		(CLIENT_UPDATE_REASON_ID,
			CLIENT_UPDATE_ACTION_CODE,
			CLIENT_UPDATE_REASON_CODE,
			FOREST_CLIENT_AUDIT_ID,
			CLIENT_TYPE_CODE,
			UPDATE_TIMESTAMP,
			UPDATE_USERID,
			ADD_TIMESTAMP,
			ADD_USERID
		) VALUES (client_update_reason_seq.NEXTVAL,
			v_client_update_action_code,
			'UND',
			v_forest_client_audit_id,
			:NEW.client_type_code,
			:NEW.update_timestamp,
			:NEW.update_userid,
			:NEW.update_timestamp,
			:NEW.update_userid
		);

	  END IF;
  END IF;

END client_for_client_ar_iud_trg;

create or replace trigger client_cli_contact_ar_iud_trg
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
END client_cli_contact_ar_iud_trg;
