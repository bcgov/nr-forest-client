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

END CLIENT_FOR_CLIENT_AR_IUD_TRG;
