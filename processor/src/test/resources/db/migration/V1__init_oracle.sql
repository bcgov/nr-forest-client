CREATE OR REPLACE PACKAGE THE.client_002_client_maint AS

  PROCEDURE deactivate(
  --client info
    p_client_number                  IN OUT   VARCHAR2
  , p_client_revision_count          IN OUT   VARCHAR2
  --update reasons
  -->certain status changes
  , p_ur_action_status_code          IN OUT   VARCHAR2
  , p_ur_action_status_desc          IN OUT   VARCHAR2
  , p_ur_reason_status               IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages);

  PROCEDURE get
  ( p_client_number                  IN OUT   VARCHAR2
  , p_client_name                    IN OUT   VARCHAR2
  , p_legal_first_name               IN OUT   VARCHAR2
  , p_client_status_desc             IN OUT   VARCHAR2
  , p_client_type_desc               IN OUT   VARCHAR2
  , p_record_count                   IN OUT   VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages
  , p_client_results                 IN OUT   client_constants.ref_cur_t
  , p_loc_results                    IN OUT   client_constants.ref_cur_t);

  PROCEDURE save
  (--client info
    p_client_number                  IN OUT   VARCHAR2
  , p_client_name                    IN OUT   VARCHAR2
  , p_legal_first_name               IN OUT   VARCHAR2
  , p_legal_middle_name              IN OUT   VARCHAR2
  , p_client_status_code             IN OUT   VARCHAR2
  , p_client_type_code               IN OUT   VARCHAR2
  , p_birthdate                      IN OUT   VARCHAR2
  , p_client_id_type_code            IN OUT   VARCHAR2
  , p_client_identification          IN OUT   VARCHAR2
  , p_registry_company_type_code     IN OUT   VARCHAR2
  , p_corp_regn_nmbr                 IN OUT   VARCHAR2
  , p_client_acronym                 IN OUT   VARCHAR2
  , p_wcb_firm_number                IN OUT   VARCHAR2
  , p_ocg_supplier_nmbr              IN OUT   VARCHAR2
  , p_client_comment                 IN OUT   VARCHAR2
  , p_client_revision_count          IN OUT   VARCHAR2
  --locn 00 info
  , p_client_locn_name               IN OUT   VARCHAR2
  , p_address_1                      IN OUT   VARCHAR2
  , p_address_2                      IN OUT   VARCHAR2
  , p_address_3                      IN OUT   VARCHAR2
  , p_city                           IN OUT   VARCHAR2
  , p_province                       IN OUT   VARCHAR2
  , p_postal_code                    IN OUT   VARCHAR2
  , p_country                        IN OUT   VARCHAR2
  , p_business_phone                 IN OUT   VARCHAR2
  , p_home_phone                     IN OUT   VARCHAR2
  , p_cell_phone                     IN OUT   VARCHAR2
  , p_fax_number                     IN OUT   VARCHAR2
  , p_email_address                  IN OUT   VARCHAR2
  , p_locn_expired_ind               IN OUT   VARCHAR2
  , p_returned_mail_ind              IN OUT   VARCHAR2
  , p_trust_location_ind             IN OUT   VARCHAR2
  , p_cli_locn_comment               IN OUT   VARCHAR2
  , p_client_update_action_code      IN OUT   VARCHAR2
  , p_client_update_reason_code      IN OUT   VARCHAR2
  , p_client_locn_revision_count     IN OUT   VARCHAR2
  --doing business as
  , p_doing_business_as_array        IN OUT   client_generic_string_varray
  --update reasons
  -->certain status changes
  , p_ur_action_status_code          IN OUT   VARCHAR2
  , p_ur_action_status_desc          IN OUT   VARCHAR2
  , p_ur_reason_status               IN OUT   VARCHAR2
  -->name changes
  , p_ur_action_name_code            IN OUT   VARCHAR2
  , p_ur_action_name_desc            IN OUT   VARCHAR2
  , p_ur_reason_name                 IN OUT   VARCHAR2
  -->id changes
  , p_ur_action_id_code              IN OUT   VARCHAR2
  , p_ur_action_id_desc              IN OUT   VARCHAR2
  , p_ur_reason_id                   IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   SIL_ERROR_MESSAGES);

END client_002_client_maint;

CREATE OR REPLACE PACKAGE BODY THE.client_002_client_maint
AS

  PROCEDURE deactivate(
  --client info
    p_client_number                  IN OUT   VARCHAR2
  , p_client_revision_count          IN OUT   VARCHAR2
  --update reasons
  -->certain status changes
  , p_ur_action_status_code          IN OUT   VARCHAR2
  , p_ur_action_status_desc          IN OUT   VARCHAR2
  , p_ur_reason_status               IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages)
  IS
    v_ur_action_name_code       client_action_reason_xref.client_update_action_code%TYPE;
    v_ur_reason_name            client_action_reason_xref.client_update_reason_code%TYPE;
    v_ur_action_id_code         client_action_reason_xref.client_update_action_code%TYPE;
    v_ur_reason_id              client_action_reason_xref.client_update_reason_code%TYPE;
  BEGIN
    --setup client
    client_forest_client.init(p_client_number);
    client_forest_client.set_client_status_code('DAC');
    client_forest_client.set_update_timestamp(SYSDATE);
    client_forest_client.set_update_userid(p_update_userid);
    client_forest_client.set_update_org_unit(p_user_org_unit_no);
    client_forest_client.set_revision_count(p_client_revision_count);
    --VALIDATE
    client_forest_client.validate;

    --Check for any actions that require update reason
    IF NOT client_forest_client.error_raised THEN
      --If there are any reasons to process, error raised will be TRUE
      --and messages are obtained via .get_error_message()
      client_forest_client.process_update_reasons
       (p_ur_action_status_code
      ,p_ur_reason_status
      --remainder are null - just to satisfy call spec
      ,v_ur_action_name_code
      ,v_ur_reason_name
      ,v_ur_action_id_code
      ,v_ur_reason_id);
    END IF;

    --Just in case - make sure no other update actions were raised - only expecting status
    IF COALESCE(v_ur_action_name_code,v_ur_reason_name,v_ur_action_id_code,v_ur_reason_id) IS NOT NULL THEN
      RAISE_APPLICATION_ERROR(-20200,'Unexpected Update Actions returned from Deactivate.');
    END IF;

    IF NOT client_forest_client.error_raised THEN
      client_forest_client.change;
    END IF;

    --get descriptions for any update actions that have been set
    p_ur_action_status_desc := client_code_lists.get_client_update_action_desc(p_ur_action_status_code);

    --accumulate messages from client
    CLIENT_UTILS.append_arrays(p_error_message
                               , client_forest_client.get_error_message);

  END deactivate;

  PROCEDURE get(
    p_client_number                  IN OUT   VARCHAR2
  , p_client_name                    IN OUT   VARCHAR2
  , p_legal_first_name               IN OUT   VARCHAR2
  , p_client_status_desc             IN OUT   VARCHAR2
  , p_client_type_desc               IN OUT   VARCHAR2
  , p_record_count                   IN OUT   VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages
  , p_client_results                 IN OUT   client_constants.ref_cur_t
  , p_loc_results                    IN OUT   client_constants.ref_cur_t)
  IS
    v_record_count                   NUMBER := 0;
    v_client_name                    forest_client.client_name%TYPE;
    v_legal_first_name               forest_client.legal_first_name%TYPE;

    --client name criteria only
    CURSOR c_client
    IS
      SELECT client_number
           , COUNT(1) OVER () record_count
        FROM forest_client
       WHERE client_name LIKE v_client_name||'%' ESCAPE sil_sql.g_escape_char;

    --client name and first name criteria
    CURSOR c_client_fname
    IS
      SELECT client_number
           , COUNT(1) OVER () record_count
        FROM forest_client
       WHERE client_name LIKE v_client_name
         AND legal_first_name LIKE v_legal_first_name||'%' ESCAPE sil_sql.g_escape_char;
    r_client    c_client%ROWTYPE;
  BEGIN
    v_client_name := sil_sql.strip_like_predicate(p_client_name);
    v_legal_first_name := sil_sql.strip_like_predicate(p_legal_first_name);

    --user may enter a client name (organization or surname) and a first name
    --in order to search.  if client name passed-in, search on these names,
    --otherwise perform standard tombstone search
    IF v_client_name IS NOT NULL THEN
      p_client_number := NULL;
      IF v_legal_first_name IS NOT NULL THEN
        OPEN c_client_fname;
        FETCH c_client_fname INTO r_client;
        CLOSE c_client_fname;
      ELSE
        OPEN c_client;
        FETCH c_client INTO r_client;
        CLOSE c_client;
      END IF;
      v_record_count := NVL(r_client.record_count,0);
      --if hit produced exactly one record, set client number for GET
      IF v_record_count = 1 THEN
        p_client_number := r_client.client_number;
      END IF;
    ELSIF v_legal_first_name IS NOT NULL THEN
      client_utils.add_error(p_error_message
                           , 'client_name'
                           , 'client.web.usr.database.cli02.go.parms;');
    END IF;

    IF p_error_message IS NULL
    AND p_client_number IS NOT NULL THEN
      client_tombstone.get(p_client_number
                          ,p_client_name
                          ,p_client_status_desc
                          ,p_client_type_desc
                          ,p_error_message);
      IF p_error_message IS NULL THEN
        v_record_count := 1;
        --Client level data
        OPEN p_client_results FOR
          SELECT fc.client_number
               , fc.client_name
               , fc.legal_first_name
               , fc.legal_middle_name
               , fc.client_status_code
               , fc.client_type_code
               , sil_date_conversion.convert_to_char(fc.birthdate) birthdate
               , fc.client_id_type_code
               , fc.client_identification
               , fc.registry_company_type_code
               , fc.corp_regn_nmbr
               , fc.client_acronym
               , fc.wcb_firm_number
               , fc.ocg_supplier_nmbr
               , fc.client_comment
               , fc.revision_count
               , loc.locations_count
               , rc.related_client_ind
               , reason.last_update_reason
               , CAST(MULTISET(SELECT doing_business_as_name
                                 FROM client_doing_business_as cdba
                                WHERE cdba.client_number = fc.client_number
                               ORDER BY add_timestamp) AS client_generic_string_varray) AS dbas
            FROM forest_client fc
               --one row cartesian
               , (SELECT COUNT(1) locations_count
                    FROM client_location l
                   WHERE l.client_number = p_client_number) loc
               --one row cartesian
               , (SELECT DECODE(COUNT(1), 0, 'No', 'Yes') related_client_ind
                    FROM related_client r
                   WHERE r.client_number = p_client_number
                      OR r.related_clnt_nmbr = p_client_number) rc
               --get update reasons - rowno=1 is latest
               , (SELECT fca.client_number
                       , cuac.description || ': ' || curc.description last_update_reason
                       , ROW_NUMBER() OVER (ORDER BY cur.client_update_reason_id DESC) rowno
                    FROM for_cli_audit fca
                       , client_update_reason cur
                       , client_update_action_code cuac
                       , client_update_reason_code curc
                   WHERE fca.client_number = p_client_number
                     AND cur.forest_client_audit_id = fca.forest_client_audit_id
                     AND cur.client_update_action_code = cuac.client_update_action_code
                     AND cur.client_update_reason_code = curc.client_update_reason_code) reason
           WHERE fc.client_number = p_client_number
             AND reason.client_number(+) = fc.client_number
             AND reason.rowno(+) = 1;

        --Client locn level data
        OPEN p_loc_results FOR
          SELECT cl.client_number
               , cl.client_locn_code
               , cl.client_locn_name
               , cl.address_1
               , cl.address_2
               , cl.address_3
               , cl.city
               , cl.province
               , cl.postal_code
               , cl.country
               , cl.business_phone
               , cl.home_phone
               , cl.cell_phone
               , cl.fax_number
               , cl.email_address
               , cl.locn_expired_ind
               , NVL2(cl.returned_mail_date,'Y','N') returned_mail_ind
               , cl.trust_location_ind
               , cl.cli_locn_comment
               --TO DO
               , '' client_update_action_code
               , '' client_update_reason_code
               , cl.revision_count
            FROM client_location cl
           WHERE cl.client_number = p_client_number
             AND cl.client_locn_code = '00';

      END IF; --error message is null and client number is not null
   END IF;

   p_record_count := TO_CHAR(v_record_count);

  END get;

  PROCEDURE SAVE(
  --client info
    p_client_number                  IN OUT   VARCHAR2
  , p_client_name                    IN OUT   VARCHAR2
  , p_legal_first_name               IN OUT   VARCHAR2
  , p_legal_middle_name              IN OUT   VARCHAR2
  , p_client_status_code             IN OUT   VARCHAR2
  , p_client_type_code               IN OUT   VARCHAR2
  , p_birthdate                      IN OUT   VARCHAR2
  , p_client_id_type_code            IN OUT   VARCHAR2
  , p_client_identification          IN OUT   VARCHAR2
  , p_registry_company_type_code     IN OUT   VARCHAR2
  , p_corp_regn_nmbr                 IN OUT   VARCHAR2
  , p_client_acronym                 IN OUT   VARCHAR2
  , p_wcb_firm_number                IN OUT   VARCHAR2
  , p_ocg_supplier_nmbr              IN OUT   VARCHAR2
  , p_client_comment                 IN OUT   VARCHAR2
  , p_client_revision_count          IN OUT   VARCHAR2
  --locn 00 info
  , p_client_locn_name               IN OUT   VARCHAR2
  , p_address_1                      IN OUT   VARCHAR2
  , p_address_2                      IN OUT   VARCHAR2
  , p_address_3                      IN OUT   VARCHAR2
  , p_city                           IN OUT   VARCHAR2
  , p_province                       IN OUT   VARCHAR2
  , p_postal_code                    IN OUT   VARCHAR2
  , p_country                        IN OUT   VARCHAR2
  , p_business_phone                 IN OUT   VARCHAR2
  , p_home_phone                     IN OUT   VARCHAR2
  , p_cell_phone                     IN OUT   VARCHAR2
  , p_fax_number                     IN OUT   VARCHAR2
  , p_email_address                  IN OUT   VARCHAR2
  , p_locn_expired_ind               IN OUT   VARCHAR2
  , p_returned_mail_ind              IN OUT   VARCHAR2
  , p_trust_location_ind             IN OUT   VARCHAR2
  , p_cli_locn_comment               IN OUT   VARCHAR2
  , p_client_update_action_code      IN OUT   VARCHAR2
  , p_client_update_reason_code      IN OUT   VARCHAR2
  , p_client_locn_revision_count     IN OUT   VARCHAR2
  --doing business as
  , p_doing_business_as_array        IN OUT   client_generic_string_varray
  --update reasons
  -->certain status changes
  , p_ur_action_status_code          IN OUT   VARCHAR2
  , p_ur_action_status_desc          IN OUT   VARCHAR2
  , p_ur_reason_status               IN OUT   VARCHAR2
  -->name changes
  , p_ur_action_name_code            IN OUT   VARCHAR2
  , p_ur_action_name_desc            IN OUT   VARCHAR2
  , p_ur_reason_name                 IN OUT   VARCHAR2
  -->id changes
  , p_ur_action_id_code              IN OUT   VARCHAR2
  , p_ur_action_id_desc              IN OUT   VARCHAR2
  , p_ur_reason_id                   IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   SIL_ERROR_MESSAGES)
  IS
    v_prev_returned_mail_ind                  VARCHAR2(1);
    b_adding                                  BOOLEAN;
    v_current_date                            DATE := SYSDATE;
  BEGIN
    b_adding := (p_client_revision_count IS NULL);

    --setup client
    client_forest_client.init(p_client_number);
    client_forest_client.set_client_name(p_client_name);
    client_forest_client.set_legal_first_name(p_legal_first_name);
    client_forest_client.set_legal_middle_name(p_legal_middle_name);
    client_forest_client.set_client_status_code(p_client_status_code);
    client_forest_client.set_client_type_code(p_client_type_code);
    client_forest_client.set_birthdate(sil_date_conversion.convert_to_date(p_birthdate));
    client_forest_client.set_client_id_type_code(p_client_id_type_code);
    client_forest_client.set_client_identification(p_client_identification);
    client_forest_client.set_registry_company_type_code(p_registry_company_type_code);
    client_forest_client.set_corp_regn_nmbr(p_corp_regn_nmbr);
    client_forest_client.set_client_acronym(p_client_acronym);
    client_forest_client.set_wcb_firm_number(p_wcb_firm_number);
    client_forest_client.set_ocg_supplier_nmbr(p_ocg_supplier_nmbr);
    client_forest_client.set_client_comment(p_client_comment);
    client_forest_client.set_update_timestamp(v_current_date); -- using variable to match dba record.
    client_forest_client.set_update_userid(p_update_userid);
    client_forest_client.set_update_org_unit(p_user_org_unit_no);
    client_forest_client.set_revision_count(p_client_revision_count);
    --VALIDATE
    client_forest_client.validate;

    --if updating, skip location processing
    client_client_location.init; -- clear-out client_client_location object.
    IF b_adding THEN
        --setup location 00
        client_client_location.init(p_client_number, '00');
        --derive returned mail ind from existing record
        IF client_client_location.get_returned_mail_date IS NULL THEN
          v_prev_returned_mail_ind := 'N';
        ELSE
          v_prev_returned_mail_ind := 'Y';
        END IF;
        client_client_location.set_client_locn_name(p_client_locn_name);
        client_client_location.set_address_1(p_address_1);
        client_client_location.set_address_2(p_address_2);
        client_client_location.set_address_3(p_address_3);
        client_client_location.set_city(p_city);
        client_client_location.set_province(p_province);
        client_client_location.set_postal_code(p_postal_code);
        client_client_location.set_country(p_country);
        client_client_location.set_business_phone(p_business_phone);
        client_client_location.set_home_phone(p_home_phone);
        client_client_location.set_cell_phone(p_cell_phone);
        client_client_location.set_fax_number(p_fax_number);
        client_client_location.set_email_address(p_email_address);
        client_client_location.set_locn_expired_ind(p_locn_expired_ind);
        IF p_returned_mail_ind = 'Y' THEN
          --if returned mail ind changed to Y, reset date
          IF v_prev_returned_mail_ind = 'N' THEN
            client_client_location.set_returned_mail_date(SYSDATE);
          END IF;
        ELSIF p_returned_mail_ind = 'N' THEN
          client_client_location.set_returned_mail_date(NULL);
        END IF;
        client_client_location.set_trust_location_ind(p_trust_location_ind);
        client_client_location.set_cli_locn_comment(p_cli_locn_comment);
        client_client_location.set_update_timestamp(SYSDATE);
        client_client_location.set_update_userid(p_update_userid);
        client_client_location.set_update_org_unit(TO_NUMBER(p_user_org_unit_no));
        client_client_location.set_revision_count(TO_NUMBER(p_client_locn_revision_count));
        client_client_location.validate;
    END IF;

    --Check for any actions that require update reason
    IF     NOT client_forest_client.error_raised
       AND NOT client_client_location.error_raised THEN
      --If there are any reasons to process, error raised will be TRUE
      --and messages are obtained via .get_error_message()
      client_forest_client.process_update_reasons
       (p_ur_action_status_code
      ,p_ur_reason_status
      ,p_ur_action_name_code
      ,p_ur_reason_name
      ,p_ur_action_id_code
      ,p_ur_reason_id);
    END IF;

    IF     NOT client_forest_client.error_raised
       AND NOT client_client_location.error_raised THEN
      IF p_client_revision_count IS NULL THEN
        client_forest_client.set_add_userid(p_update_userid);
        client_forest_client.set_add_timestamp(SYSDATE);
        client_forest_client.set_add_org_unit(p_user_org_unit_no);
        client_forest_client.set_revision_count(1);
        client_forest_client.add;
        p_client_number := client_forest_client.get_client_number;
      ELSE
        client_forest_client.change;
      END IF;

      IF b_adding
      AND NOT client_forest_client.error_raised THEN
        client_client_location.set_client_number(p_client_number);
        IF p_client_locn_revision_count IS NULL THEN
          client_client_location.set_add_userid(p_update_userid);
          client_client_location.set_add_timestamp(SYSDATE);
          client_client_location.set_add_org_unit(p_user_org_unit_no);
          client_client_location.set_revision_count(1);
          client_client_location.add;
        ELSE
          client_client_location.change;
        END IF;
      END IF;

      IF NOT client_client_location.error_raised THEN
        client_client_doing_bus_as.init(p_client_number);
        client_client_doing_bus_as.set_doing_business_as_names(p_doing_business_as_array);
        -- update timestamp must match forest_client's for the history screen to work.
        client_client_doing_bus_as.set_update_timestamp(v_current_date);
        client_client_doing_bus_as.set_update_userid(p_update_userid);
        client_client_doing_bus_as.set_update_org_unit(TO_NUMBER(p_user_org_unit_no));
        client_client_doing_bus_as.set_add_timestamp(SYSDATE);
        client_client_doing_bus_as.set_add_userid(p_update_userid);
        client_client_doing_bus_as.set_add_org_unit(TO_NUMBER(p_user_org_unit_no));
        client_client_doing_bus_as.merge_dba;
      END IF;

    END IF;

    --get descriptions for any update actions that have been set
    p_ur_action_status_desc := client_code_lists.get_client_update_action_desc(p_ur_action_status_code);
    p_ur_action_name_desc := client_code_lists.get_client_update_action_desc(p_ur_action_name_code);
    p_ur_action_id_desc := client_code_lists.get_client_update_action_desc(p_ur_action_id_code);

    --accumulate messages from client and location
    CLIENT_UTILS.append_arrays(p_error_message
                             , client_forest_client.get_error_message);
    IF b_adding THEN
      CLIENT_UTILS.append_arrays(p_error_message
                               , client_client_location.get_error_message);
    END IF;
    CLIENT_UTILS.append_arrays(p_error_message
                             , client_client_doing_bus_as.get_error_message);

  END save;

END client_002_client_maint;
CREATE TABLE THE.CLIENT_TYPE_CODE (
	CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CTCD_PK PRIMARY KEY (CLIENT_TYPE_CODE),
	CONSTRAINT SYS_C0012021 CHECK ("CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012022 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012023 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012024 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012025 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);
CREATE UNIQUE INDEX CTCD_IDX ON THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE);

CREATE TABLE THE.RESTORED_BLOB_STORAGE (
	BSTO_SEQ_NBR NUMBER(15,0) NOT NULL,
	BSTO_TABLE_NAME VARCHAR2(30) NOT NULL,
	BSTO_TABLE_PRIMARY_KEY VARCHAR2(1000) NOT NULL,
	BSTO_FILE_NAME VARCHAR2(60) NULL,
	BSTO_FILE_DESCRIPTION VARCHAR2(300) NULL,
	CONSTRAINT SYS_C0011049 CHECK ("BSTO_SEQ_NBR" IS NOT NULL),
	CONSTRAINT SYS_C0011050 CHECK ("BSTO_TABLE_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0011051 CHECK ("BSTO_TABLE_PRIMARY_KEY" IS NOT NULL)
);

CREATE TABLE THE.RESTORED_FOREST_COMMENT (
	COMM_SEQ_NBR NUMBER(15,0) NOT NULL,
	COMM_MODULE VARCHAR2(15) NULL,
	COMM_ITEM_TYPE VARCHAR2(30) NULL,
	COMM_ITEM_SEQ_NBR NUMBER(15,0) NULL,
	COMM_COMMENT_DATE DATE NOT NULL,
	COMM_COMMENT VARCHAR2(4000) NOT NULL,
	COMM_AGENCY VARCHAR2(15) NOT NULL,
	COMM_AGENT VARCHAR2(15) NOT NULL,
	COMM_RESPONSE_DATE DATE NULL,
	COMM_RESPONDANT VARCHAR2(15) NULL,
	COMM_RESPONSE VARCHAR2(4000) NULL,
	COMM_ACTION_REQUIRED VARCHAR2(1) NULL,
	COMM_ACTION_ITEM VARCHAR2(2000) NULL,
	COMM_COMMENT_TYPE VARCHAR2(30) NULL,
	COMM_FDP_ID VARCHAR2(50) NULL,
	COMM_PARENT_SEQ_NBR NUMBER(15,0) NULL,
	CONSTRAINT SYS_C0011056 CHECK ("COMM_SEQ_NBR" IS NOT NULL),
	CONSTRAINT SYS_C0011057 CHECK ("COMM_COMMENT_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0011058 CHECK ("COMM_COMMENT" IS NOT NULL),
	CONSTRAINT SYS_C0011059 CHECK ("COMM_AGENCY" IS NOT NULL),
	CONSTRAINT SYS_C0011060 CHECK ("COMM_AGENT" IS NOT NULL)
);

CREATE TABLE THE.CLIENT_UPDATE_ACTION_CODE (
	CLIENT_UPDATE_ACTION_CODE VARCHAR2(4) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CUAC_PK PRIMARY KEY (CLIENT_UPDATE_ACTION_CODE),
	CONSTRAINT SYS_C0012618 CHECK ("CLIENT_UPDATE_ACTION_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012619 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012620 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012621 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012622 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);
CREATE UNIQUE INDEX CUAC_IDX ON THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE);

CREATE TABLE THE.MAX_CLIENT_NMBR (
	DUMMY_ACCESS_KEY VARCHAR2(1) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CONSTRAINT SYS_C0023534 CHECK ("DUMMY_ACCESS_KEY" IS NOT NULL),
	CONSTRAINT SYS_C0023535 CHECK ("CLIENT_NUMBER" IS NOT NULL)
);
CREATE UNIQUE INDEX "I1$_MAX_CLIENT_NMBR" ON THE.MAX_CLIENT_NMBR (DUMMY_ACCESS_KEY);

CREATE TABLE THE.CLIENT_UPDATE_REASON_CODE (
	CLIENT_UPDATE_REASON_CODE VARCHAR2(4) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CURCD_PK PRIMARY KEY (CLIENT_UPDATE_REASON_CODE),
	CONSTRAINT SYS_C0012098 CHECK ("CLIENT_UPDATE_REASON_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012099 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012100 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012101 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012102 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.CODE_LIST_TABLE (
	COLUMN_NAME VARCHAR2(18) NOT NULL,
	CODE_ARGUMENT VARCHAR2(50) NOT NULL,
	EXPANDED_RESULT VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	CONSTRAINT SYS_C0029139 CHECK ("COLUMN_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0029140 CHECK ("CODE_ARGUMENT" IS NOT NULL),
	CONSTRAINT SYS_C0029141 CHECK ("EXPANDED_RESULT" IS NOT NULL),
	CONSTRAINT SYS_C0029142 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0029143 CHECK ("EXPIRY_DATE" IS NOT NULL)
);
CREATE UNIQUE INDEX "I1$_CODE_LIST_TABLE" ON THE.CODE_LIST_TABLE (COLUMN_NAME,CODE_ARGUMENT);

CREATE TABLE THE.CODE_SUBSET_TBL (
	CODE_SUBSET_NAME VARCHAR2(18) NOT NULL,
	COLUMN_NAME VARCHAR2(18) NOT NULL,
	CODE_ARGUMENT VARCHAR2(50) NOT NULL,
	IN_EFFECT_DATE DATE NULL,
	EXPIRED_DATE DATE NULL,
	CONSTRAINT SYS_C0029088 CHECK ("CODE_SUBSET_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0029089 CHECK ("COLUMN_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0029090 CHECK ("CODE_ARGUMENT" IS NOT NULL)
);
CREATE UNIQUE INDEX "I1$_CODE_SUBSET_TBL" ON THE.CODE_SUBSET_TBL (CODE_SUBSET_NAME,COLUMN_NAME,CODE_ARGUMENT);

CREATE TABLE THE.CODE_TABLE (
	COLUMN_NAME VARCHAR2(18) NOT NULL,
	CODE_CUSTODIAN NUMBER(10,0) NOT NULL,
	UPDATE_AUTHORITY VARCHAR2(40) NOT NULL,
	CODE_DESCRIPTION VARCHAR2(254) NOT NULL,
	DISTRIBUTE_TYPE_CD VARCHAR2(3) NOT NULL,
	CONSTRAINT SYS_C0032456 CHECK ("COLUMN_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0032458 CHECK ("CODE_CUSTODIAN" IS NOT NULL),
	CONSTRAINT SYS_C0032460 CHECK ("UPDATE_AUTHORITY" IS NOT NULL),
	CONSTRAINT SYS_C0032462 CHECK ("CODE_DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0032464 CHECK ("DISTRIBUTE_TYPE_CD" IS NOT NULL)
);
CREATE UNIQUE INDEX "I1$_CODE_TABLE" ON THE.CODE_TABLE (COLUMN_NAME);

CREATE TABLE THE.CLIENT_ID_TYPE_CODE (
	CLIENT_ID_TYPE_CODE VARCHAR2(4) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CITC_PK PRIMARY KEY (CLIENT_ID_TYPE_CODE),
	CONSTRAINT SYS_C0012128 CHECK ("CLIENT_ID_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012129 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012130 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012131 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012132 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.EXTERNAL_CLIENT_REORG_SYSTEM (
	APPLICATION_ACRONYM VARCHAR2(10) NOT NULL,
	APPLICATION_UPDATE_IND VARCHAR2(1) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT ECRS_PK PRIMARY KEY (APPLICATION_ACRONYM),
	CONSTRAINT SYS_C0022987 CHECK ("APPLICATION_ACRONYM" IS NOT NULL),
	CONSTRAINT SYS_C0022988 CHECK ("APPLICATION_UPDATE_IND" IS NOT NULL),
	CONSTRAINT SYS_C0022989 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0022990 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0022991 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0022992 CHECK ("UPDATE_USERID" IS NOT NULL)
);

CREATE TABLE THE.CLIENT_RELATIONSHIP_CODE (
	CLIENT_RELATIONSHIP_CODE VARCHAR2(2) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT RELCD_PK PRIMARY KEY (CLIENT_RELATIONSHIP_CODE),
	CONSTRAINT SYS_C0012256 CHECK ("CLIENT_RELATIONSHIP_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012257 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012258 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012259 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012260 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.ORG_UNIT (
	ORG_UNIT_NO NUMBER(10,0) NOT NULL,
	ORG_UNIT_CODE VARCHAR2(6) NOT NULL,
	ORG_UNIT_NAME VARCHAR2(100) NOT NULL,
	LOCATION_CODE VARCHAR2(3) NOT NULL,
	AREA_CODE VARCHAR2(3) NOT NULL,
	TELEPHONE_NO VARCHAR2(7) NOT NULL,
	ORG_LEVEL_CODE VARCHAR2(1) NOT NULL,
	OFFICE_NAME_CODE VARCHAR2(2) NOT NULL,
	ROLLUP_REGION_NO NUMBER(10,0) NOT NULL,
	ROLLUP_REGION_CODE VARCHAR2(6) NOT NULL,
	ROLLUP_DIST_NO NUMBER(10,0) NOT NULL,
	ROLLUP_DIST_CODE VARCHAR2(6) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NULL,
	CONSTRAINT ORG_UNIT_PK PRIMARY KEY (ORG_UNIT_NO),
	CONSTRAINT SYS_C008931 CHECK ("ORG_UNIT_NO" IS NOT NULL),
	CONSTRAINT SYS_C008932 CHECK ("ORG_UNIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008933 CHECK ("ORG_UNIT_NAME" IS NOT NULL),
	CONSTRAINT SYS_C008934 CHECK ("LOCATION_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008935 CHECK ("AREA_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008936 CHECK ("TELEPHONE_NO" IS NOT NULL),
	CONSTRAINT SYS_C008938 CHECK ("ORG_LEVEL_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008939 CHECK ("OFFICE_NAME_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008941 CHECK ("ROLLUP_REGION_NO" IS NOT NULL),
	CONSTRAINT SYS_C008942 CHECK ("ROLLUP_REGION_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008944 CHECK ("ROLLUP_DIST_NO" IS NOT NULL),
	CONSTRAINT SYS_C008946 CHECK ("ROLLUP_DIST_CODE" IS NOT NULL),
	CONSTRAINT SYS_C008947 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C008949 CHECK ("EXPIRY_DATE" IS NOT NULL)
);
CREATE UNIQUE INDEX "I2$_ORG_UNIT" ON THE.ORG_UNIT (ORG_UNIT_CODE,ORG_UNIT_NO);
CREATE UNIQUE INDEX "I3$_ORG_UNIT" ON THE.ORG_UNIT (ORG_UNIT_CODE);
CREATE UNIQUE INDEX "I4$_ORG_UNIT" ON THE.ORG_UNIT (ORG_UNIT_NO,ORG_UNIT_CODE,ORG_UNIT_NAME,LOCATION_CODE,TELEPHONE_NO,ORG_LEVEL_CODE,OFFICE_NAME_CODE);
CREATE INDEX "I5$_ORG_UNIT" ON THE.ORG_UNIT (ROLLUP_DIST_CODE,ROLLUP_REGION_CODE);
CREATE INDEX "I6$_ORG_UNIT" ON THE.ORG_UNIT (ROLLUP_REGION_NO,ROLLUP_DIST_NO);
CREATE INDEX "I7$_ORG_UNIT" ON THE.ORG_UNIT (ROLLUP_REGION_CODE,ROLLUP_DIST_CODE);
CREATE UNIQUE INDEX "I8$_ORG_UNIT" ON THE.ORG_UNIT (ORG_UNIT_NO,ORG_UNIT_CODE);
CREATE UNIQUE INDEX "I9$_ORG_UNIT" ON THE.ORG_UNIT (ORG_UNIT_CODE,ORG_LEVEL_CODE,ORG_UNIT_NO,ORG_UNIT_NAME);

CREATE TABLE THE.CLIENT_AUDIT_CODE (
	CLIENT_AUDIT_CODE VARCHAR2(3) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CAUDC_PK PRIMARY KEY (CLIENT_AUDIT_CODE),
	CONSTRAINT SYS_C0012678 CHECK ("CLIENT_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012679 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012680 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012681 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012682 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.CLIENT_REORG_STATUS_CODE (
	CLIENT_REORG_STATUS_CODE VARCHAR2(4) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CRSCD_PK PRIMARY KEY (CLIENT_REORG_STATUS_CODE),
	CONSTRAINT SYS_C0024849 CHECK ("CLIENT_REORG_STATUS_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024850 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0024851 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0024852 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0024853 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.CLIENT_REORG_TYPE_CODE (
	CLIENT_REORG_TYPE_CODE VARCHAR2(4) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CRTCD_PK PRIMARY KEY (CLIENT_REORG_TYPE_CODE),
	CONSTRAINT SYS_C0024555 CHECK ("CLIENT_REORG_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024556 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0024557 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0024558 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0024559 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.CLIENT_REORG_AUDIT_CODE (
	CLIENT_REORG_AUDIT_CODE VARCHAR2(3) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CRAC_PK PRIMARY KEY (CLIENT_REORG_AUDIT_CODE),
	CONSTRAINT SYS_C0024973 CHECK ("CLIENT_REORG_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024974 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0024975 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0024976 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0024977 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.CLIENT_STATUS_CODE (
	CLIENT_STATUS_CODE VARCHAR2(3) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CSTC_PK PRIMARY KEY (CLIENT_STATUS_CODE),
	CONSTRAINT SYS_C0012691 CHECK ("CLIENT_STATUS_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012692 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012693 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012694 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012695 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.REGISTRY_COMPANY_TYPE_CODE (
	REGISTRY_COMPANY_TYPE_CODE VARCHAR2(4) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CRCTC_PK PRIMARY KEY (REGISTRY_COMPANY_TYPE_CODE),
	CONSTRAINT SYS_C0012219 CHECK ("REGISTRY_COMPANY_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012220 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012221 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012222 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012223 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.LOCATION (
	LOCATION_CODE VARCHAR2(3) NOT NULL,
	STREET VARCHAR2(40) NOT NULL,
	CITY VARCHAR2(15) NOT NULL,
	PROVINCE VARCHAR2(4) NOT NULL,
	PCODE VARCHAR2(6) NOT NULL,
	CONSTRAINT SYS_C0021115 CHECK ("LOCATION_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0021116 CHECK ("STREET" IS NOT NULL),
	CONSTRAINT SYS_C0021117 CHECK ("CITY" IS NOT NULL),
	CONSTRAINT SYS_C0021118 CHECK ("PROVINCE" IS NOT NULL),
	CONSTRAINT SYS_C0021119 CHECK ("PCODE" IS NOT NULL)
);
CREATE UNIQUE INDEX "I1$_LOCATION" ON THE.LOCATION (LOCATION_CODE);
CREATE INDEX "I2$_LOCATION" ON THE.LOCATION (CITY);

CREATE TABLE THE.CLIENT_REORGANIZATION (
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	EFFECTIVE_DATE DATE NULL,
	EXPIRY_DATE DATE NULL,
	COMMENTS VARCHAR2(4000) NULL,
	PARTIAL_REORG_IND VARCHAR2(1) NOT NULL,
	CLIENT_REORG_STATUS_CODE VARCHAR2(4) NOT NULL,
	CLIENT_REORG_TYPE_CODE VARCHAR2(4) NOT NULL,
	COMPLETED_TIMESTAMP DATE NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CREORG_PK PRIMARY KEY (CLIENT_REORGANIZATION_ID),
	CONSTRAINT SYS_C0025015 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0025016 CHECK ("PARTIAL_REORG_IND" IS NOT NULL),
	CONSTRAINT SYS_C0025017 CHECK ("CLIENT_REORG_STATUS_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0025018 CHECK ("CLIENT_REORG_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0025019 CHECK ("REVISION_COUNT" IS NOT NULL),
	CONSTRAINT SYS_C0025020 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0025021 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0025022 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0025023 CHECK ("UPDATE_USERID" IS NOT NULL)
);
CREATE INDEX CREORG_CRSC_FK_I ON THE.CLIENT_REORGANIZATION (CLIENT_REORG_STATUS_CODE);
CREATE INDEX CREORG_CRTC_FK_I ON THE.CLIENT_REORGANIZATION (CLIENT_REORG_TYPE_CODE);

ALTER TABLE THE.CLIENT_REORGANIZATION ADD CONSTRAINT CREORG_CRSC_FK FOREIGN KEY (CLIENT_REORG_STATUS_CODE) REFERENCES THE.CLIENT_REORG_STATUS_CODE(CLIENT_REORG_STATUS_CODE);
ALTER TABLE THE.CLIENT_REORGANIZATION ADD CONSTRAINT CREORG_CRTC_FK FOREIGN KEY (CLIENT_REORG_TYPE_CODE) REFERENCES THE.CLIENT_REORG_TYPE_CODE(CLIENT_REORG_TYPE_CODE);

CREATE TABLE THE.EXTERNAL_CLIENT_REORG_RESULT (
	EXTERNAL_CLIENT_REORG_RSLT_ID NUMBER(12,0) NOT NULL,
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	ORIGINAL_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	ORIGINAL_CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	RESULTING_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	RESULTING_CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	APPLICATION_ACRONYM VARCHAR2(10) NOT NULL,
	EXECUTED_PROCEDURE VARCHAR2(50) NOT NULL,
	EXECUTED_TIMESTAMP DATE NOT NULL,
	EXECUTED_BY_USERID VARCHAR2(30) NOT NULL,
	UPDATED_TABLE VARCHAR2(50) NULL,
	UPDATE_ROW_COUNT NUMBER(9,0) NULL,
	ERROR_MESSAGE VARCHAR2(2000) NULL,
	CONSTRAINT ECRR_PK PRIMARY KEY (EXTERNAL_CLIENT_REORG_RSLT_ID),
	CONSTRAINT SYS_C0023039 CHECK ("EXTERNAL_CLIENT_REORG_RSLT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0023040 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0023041 CHECK ("ORIGINAL_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0023042 CHECK ("ORIGINAL_CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0023043 CHECK ("RESULTING_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0023044 CHECK ("RESULTING_CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0023045 CHECK ("APPLICATION_ACRONYM" IS NOT NULL),
	CONSTRAINT SYS_C0023046 CHECK ("EXECUTED_PROCEDURE" IS NOT NULL),
	CONSTRAINT SYS_C0023047 CHECK ("EXECUTED_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0023048 CHECK ("EXECUTED_BY_USERID" IS NOT NULL)
);
CREATE INDEX ECRR_CRD_FK_I ON THE.EXTERNAL_CLIENT_REORG_RESULT (CLIENT_REORGANIZATION_ID,ORIGINAL_CLIENT_NUMBER,ORIGINAL_CLIENT_LOCN_CODE,RESULTING_CLIENT_NUMBER,RESULTING_CLIENT_LOCN_CODE);
CREATE INDEX ECRR_ECRS_FK_I ON THE.EXTERNAL_CLIENT_REORG_RESULT (APPLICATION_ACRONYM);

ALTER TABLE THE.EXTERNAL_CLIENT_REORG_RESULT ADD CONSTRAINT ECRR_CRD_FK FOREIGN KEY (CLIENT_REORGANIZATION_ID,ORIGINAL_CLIENT_NUMBER,ORIGINAL_CLIENT_LOCN_CODE,RESULTING_CLIENT_NUMBER,RESULTING_CLIENT_LOCN_CODE) REFERENCES THE.CLIENT_REORG_DETAIL(CLIENT_REORGANIZATION_ID,ORIGINAL_CLIENT_NUMBER,ORIGINAL_CLIENT_LOCN_CODE,RESULTING_CLIENT_NUMBER,RESULTING_CLIENT_LOCN_CODE);
ALTER TABLE THE.EXTERNAL_CLIENT_REORG_RESULT ADD CONSTRAINT ECRR_ECRS_FK FOREIGN KEY (APPLICATION_ACRONYM) REFERENCES THE.EXTERNAL_CLIENT_REORG_SYSTEM(APPLICATION_ACRONYM);

CREATE TABLE THE.CLIENT_RELATIONSHIP_TYPE_XREF (
	CLIENT_RELATIONSHIP_CODE VARCHAR2(2) NOT NULL,
	PRIMARY_CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	SECONDARY_CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CRX_PK PRIMARY KEY (CLIENT_RELATIONSHIP_CODE,SECONDARY_CLIENT_TYPE_CODE,PRIMARY_CLIENT_TYPE_CODE),
	CONSTRAINT SYS_C0019306 CHECK ("CLIENT_RELATIONSHIP_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0019307 CHECK ("PRIMARY_CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0019308 CHECK ("SECONDARY_CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0019309 CHECK ("REVISION_COUNT" IS NOT NULL),
	CONSTRAINT SYS_C0019310 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0019311 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0019312 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0019313 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);
CREATE INDEX CRX_PCTC_FK_I ON THE.CLIENT_RELATIONSHIP_TYPE_XREF (PRIMARY_CLIENT_TYPE_CODE);
CREATE INDEX CRX_RELCD_FK_I ON THE.CLIENT_RELATIONSHIP_TYPE_XREF (CLIENT_RELATIONSHIP_CODE);
CREATE INDEX CRX_SCTC_FK_I ON THE.CLIENT_RELATIONSHIP_TYPE_XREF (SECONDARY_CLIENT_TYPE_CODE);


ALTER TABLE THE.CLIENT_RELATIONSHIP_TYPE_XREF ADD CONSTRAINT CRX_PCTC_FK FOREIGN KEY (PRIMARY_CLIENT_TYPE_CODE) REFERENCES THE.CLIENT_TYPE_CODE(CLIENT_TYPE_CODE);
ALTER TABLE THE.CLIENT_RELATIONSHIP_TYPE_XREF ADD CONSTRAINT CRX_RELCD_FK FOREIGN KEY (CLIENT_RELATIONSHIP_CODE) REFERENCES THE.CLIENT_RELATIONSHIP_CODE(CLIENT_RELATIONSHIP_CODE);
ALTER TABLE THE.CLIENT_RELATIONSHIP_TYPE_XREF ADD CONSTRAINT CRX_SCTC_FK FOREIGN KEY (SECONDARY_CLIENT_TYPE_CODE) REFERENCES THE.CLIENT_TYPE_CODE(CLIENT_TYPE_CODE);

CREATE TABLE THE.BUSINESS_CONTACT_CODE (
	BUSINESS_CONTACT_CODE VARCHAR2(3) NOT NULL,
	DESCRIPTION VARCHAR2(120) NOT NULL,
	EFFECTIVE_DATE DATE NOT NULL,
	EXPIRY_DATE DATE NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT BCC_PK PRIMARY KEY (BUSINESS_CONTACT_CODE),
	CONSTRAINT SYS_C0012316 CHECK ("BUSINESS_CONTACT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0012317 CHECK ("DESCRIPTION" IS NOT NULL),
	CONSTRAINT SYS_C0012318 CHECK ("EFFECTIVE_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012319 CHECK ("EXPIRY_DATE" IS NOT NULL),
	CONSTRAINT SYS_C0012320 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);

CREATE TABLE THE.MAILING_COUNTRY (
	COUNTRY_NAME VARCHAR2(50) NOT NULL,
	COUNTRY_CODE VARCHAR2(3) NULL,
	CONSTRAINT CTRY_PK PRIMARY KEY (COUNTRY_NAME),
	CONSTRAINT SYS_C0012419 CHECK ("COUNTRY_NAME" IS NOT NULL)
);

CREATE TABLE THE.MAILING_PROVINCE_STATE (
	COUNTRY_NAME VARCHAR2(50) NOT NULL,
	PROVINCE_STATE_NAME VARCHAR2(50) NOT NULL,
	PROVINCE_STATE_CODE VARCHAR2(2) NULL,
	CONSTRAINT PROV_PK PRIMARY KEY (PROVINCE_STATE_NAME,COUNTRY_NAME),
	CONSTRAINT SYS_C0012368 CHECK ("COUNTRY_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0012369 CHECK ("PROVINCE_STATE_NAME" IS NOT NULL)
);
CREATE INDEX PROV_CTRY_FK_I ON THE.MAILING_PROVINCE_STATE (COUNTRY_NAME);
ALTER TABLE THE.MAILING_PROVINCE_STATE ADD CONSTRAINT PROV_CTRY_FK FOREIGN KEY (COUNTRY_NAME) REFERENCES THE.MAILING_COUNTRY(COUNTRY_NAME);

CREATE TABLE THE.MAILING_CITY (
	COUNTRY_NAME VARCHAR2(50) NOT NULL,
	PROVINCE_STATE_NAME VARCHAR2(50) NOT NULL,
	CITY_NAME VARCHAR2(30) NOT NULL,
	CONSTRAINT CTY_PK PRIMARY KEY (CITY_NAME,PROVINCE_STATE_NAME,COUNTRY_NAME),
	CONSTRAINT SYS_C0010890 CHECK ("COUNTRY_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0010893 CHECK ("PROVINCE_STATE_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0010896 CHECK ("CITY_NAME" IS NOT NULL)
);
CREATE INDEX CTY_PROV_FK_I ON THE.MAILING_CITY (PROVINCE_STATE_NAME,COUNTRY_NAME);
ALTER TABLE THE.MAILING_CITY ADD CONSTRAINT CTY_PROV_FK FOREIGN KEY (PROVINCE_STATE_NAME,COUNTRY_NAME) REFERENCES THE.MAILING_PROVINCE_STATE(PROVINCE_STATE_NAME,COUNTRY_NAME);

CREATE TABLE THE.CLIENT_REORG_AUDIT (
	CLIENT_REORG_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_REORG_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	EFFECTIVE_DATE DATE NULL,
	EXPIRY_DATE DATE NULL,
	COMMENTS VARCHAR2(4000) NULL,
	PARTIAL_REORG_IND VARCHAR2(1) NOT NULL,
	CLIENT_REORG_STATUS_CODE VARCHAR2(4) NOT NULL,
	CLIENT_REORG_TYPE_CODE VARCHAR2(4) NOT NULL,
	COMPLETED_TIMESTAMP DATE NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CREA_PK PRIMARY KEY (CLIENT_REORG_AUDIT_ID),
	CONSTRAINT SYS_C0024919 CHECK ("CLIENT_REORG_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0024920 CHECK ("CLIENT_REORG_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024921 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0024922 CHECK ("PARTIAL_REORG_IND" IS NOT NULL),
	CONSTRAINT SYS_C0024923 CHECK ("CLIENT_REORG_STATUS_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024924 CHECK ("CLIENT_REORG_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024925 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0024926 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0024927 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0024928 CHECK ("UPDATE_USERID" IS NOT NULL)
);
CREATE INDEX CREA_AAC_FK_I ON THE.CLIENT_REORG_AUDIT (CLIENT_REORG_AUDIT_CODE);
ALTER TABLE THE.CLIENT_REORG_AUDIT ADD CONSTRAINT CREA_CRAC_FK FOREIGN KEY (CLIENT_REORG_AUDIT_CODE) REFERENCES THE.CLIENT_REORG_AUDIT_CODE(CLIENT_REORG_AUDIT_CODE);


CREATE TABLE THE.CLIENT_TYPE_COMPANY_XREF (
	CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	REGISTRY_COMPANY_TYPE_CODE VARCHAR2(4) NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	CONSTRAINT CTCX_PK PRIMARY KEY (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE),
	CONSTRAINT SYS_C0011915 CHECK ("CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011916 CHECK ("REGISTRY_COMPANY_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011917 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011918 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011919 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011920 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);
CREATE INDEX CTCX_CRCTC_FK_I ON THE.CLIENT_TYPE_COMPANY_XREF (REGISTRY_COMPANY_TYPE_CODE);
CREATE INDEX CTCX_CTC_FK_I ON THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE);
ALTER TABLE THE.CLIENT_TYPE_COMPANY_XREF ADD CONSTRAINT CTCX_CRCTC_FK FOREIGN KEY (REGISTRY_COMPANY_TYPE_CODE) REFERENCES THE.REGISTRY_COMPANY_TYPE_CODE(REGISTRY_COMPANY_TYPE_CODE);
ALTER TABLE THE.CLIENT_TYPE_COMPANY_XREF ADD CONSTRAINT CTCX_CTC_FK FOREIGN KEY (CLIENT_TYPE_CODE) REFERENCES THE.CLIENT_TYPE_CODE(CLIENT_TYPE_CODE);


CREATE TABLE THE.FOREST_CLIENT (
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_NAME VARCHAR2(60) NOT NULL,
	LEGAL_FIRST_NAME VARCHAR2(30) NULL,
	LEGAL_MIDDLE_NAME VARCHAR2(30) NULL,
	CLIENT_STATUS_CODE VARCHAR2(3) NOT NULL,
	CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	BIRTHDATE DATE NULL,
	CLIENT_ID_TYPE_CODE VARCHAR2(4) NULL,
	CLIENT_IDENTIFICATION VARCHAR2(40) NULL,
	REGISTRY_COMPANY_TYPE_CODE VARCHAR2(4) NULL,
	CORP_REGN_NMBR VARCHAR2(9) NULL,
	CLIENT_ACRONYM VARCHAR2(8) NULL,
	WCB_FIRM_NUMBER VARCHAR2(6) NULL,
	OCG_SUPPLIER_NMBR VARCHAR2(10) NULL,
	CLIENT_COMMENT VARCHAR2(4000) NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	CONSTRAINT FOREST_CLIENT_PK PRIMARY KEY (CLIENT_NUMBER),
	CONSTRAINT SYS_C0029588 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0029589 CHECK ("CLIENT_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0029590 CHECK ("CLIENT_STATUS_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029591 CHECK ("CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029592 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029593 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029594 CHECK ("ADD_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0029595 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029596 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029597 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0029598 CHECK ("REVISION_COUNT" IS NOT NULL)
);
CREATE UNIQUE INDEX FC_ACRONYM_I ON THE.FOREST_CLIENT (CLIENT_ACRONYM);
CREATE INDEX FC_CITC_FK_I ON THE.FOREST_CLIENT (CLIENT_ID_TYPE_CODE);
CREATE INDEX FC_CSC_FK_I ON THE.FOREST_CLIENT (CLIENT_STATUS_CODE);
CREATE INDEX FC_CTCX_FK_I ON THE.FOREST_CLIENT (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE);
CREATE INDEX FC_OU_FK_I ON THE.FOREST_CLIENT (ADD_ORG_UNIT);
CREATE INDEX FC_OU_IS_UPDATED_BY_FK_I ON THE.FOREST_CLIENT (UPDATE_ORG_UNIT);
CREATE UNIQUE INDEX "I2$_FOREST_CLIENT" ON THE.FOREST_CLIENT (CLIENT_NAME,LEGAL_FIRST_NAME,LEGAL_MIDDLE_NAME,CLIENT_NUMBER);
CREATE INDEX "I4$_FOREST_CLIENT" ON THE.FOREST_CLIENT (OCG_SUPPLIER_NMBR);
CREATE INDEX "I5$_FOREST_CLIENT" ON THE.FOREST_CLIENT (CORP_REGN_NMBR);
ALTER TABLE THE.FOREST_CLIENT ADD CONSTRAINT FC_CITC_FK FOREIGN KEY (CLIENT_ID_TYPE_CODE) REFERENCES THE.CLIENT_ID_TYPE_CODE(CLIENT_ID_TYPE_CODE);
ALTER TABLE THE.FOREST_CLIENT ADD CONSTRAINT FC_CSC_FK FOREIGN KEY (CLIENT_STATUS_CODE) REFERENCES THE.CLIENT_STATUS_CODE(CLIENT_STATUS_CODE);
ALTER TABLE THE.FOREST_CLIENT ADD CONSTRAINT FC_CTCX_FK FOREIGN KEY (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE) REFERENCES THE.CLIENT_TYPE_COMPANY_XREF(CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE);
ALTER TABLE THE.FOREST_CLIENT ADD CONSTRAINT FC_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.FOREST_CLIENT ADD CONSTRAINT FC_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);


CREATE TABLE THE.CLIENT_REORG_LINK (
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	ORIGINAL_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	RESULTING_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CRL_PK PRIMARY KEY (CLIENT_REORGANIZATION_ID,ORIGINAL_CLIENT_NUMBER,RESULTING_CLIENT_NUMBER),
	CONSTRAINT SYS_C0023174 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0023175 CHECK ("ORIGINAL_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0023176 CHECK ("RESULTING_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0023177 CHECK ("REVISION_COUNT" IS NOT NULL),
	CONSTRAINT SYS_C0023178 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0023179 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0023180 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0023181 CHECK ("UPDATE_USERID" IS NOT NULL)
);
CREATE INDEX CRL_CR_FK_I ON THE.CLIENT_REORG_LINK (CLIENT_REORGANIZATION_ID);
CREATE INDEX CRL_FC_FK_I ON THE.CLIENT_REORG_LINK (ORIGINAL_CLIENT_NUMBER);
CREATE INDEX CRL_FC_INCLUDES_RESULTING_FK_I ON THE.CLIENT_REORG_LINK (RESULTING_CLIENT_NUMBER);

ALTER TABLE THE.CLIENT_REORG_LINK ADD CONSTRAINT CRL_CR_FK FOREIGN KEY (CLIENT_REORGANIZATION_ID) REFERENCES THE.CLIENT_REORGANIZATION(CLIENT_REORGANIZATION_ID);
ALTER TABLE THE.CLIENT_REORG_LINK ADD CONSTRAINT CRL_FC_FK FOREIGN KEY (ORIGINAL_CLIENT_NUMBER) REFERENCES THE.FOREST_CLIENT(CLIENT_NUMBER);
ALTER TABLE THE.CLIENT_REORG_LINK ADD CONSTRAINT CRL_FC_INCLUDES_RESULTING_FK FOREIGN KEY (RESULTING_CLIENT_NUMBER) REFERENCES THE.FOREST_CLIENT(CLIENT_NUMBER);

CREATE TABLE THE.CLIENT_LOCATION (
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	CLIENT_LOCN_NAME VARCHAR2(40) NULL,
	HDBS_COMPANY_CODE VARCHAR2(5) NULL,
	ADDRESS_1 VARCHAR2(40) NOT NULL,
	ADDRESS_2 VARCHAR2(40) NULL,
	ADDRESS_3 VARCHAR2(40) NULL,
	CITY VARCHAR2(30) NOT NULL,
	PROVINCE VARCHAR2(50) NULL,
	POSTAL_CODE VARCHAR2(10) NULL,
	COUNTRY VARCHAR2(50) NOT NULL,
	BUSINESS_PHONE VARCHAR2(10) NULL,
	HOME_PHONE VARCHAR2(10) NULL,
	CELL_PHONE VARCHAR2(10) NULL,
	FAX_NUMBER VARCHAR2(10) NULL,
	EMAIL_ADDRESS VARCHAR2(128) NULL,
	LOCN_EXPIRED_IND VARCHAR2(1) NOT NULL,
	RETURNED_MAIL_DATE DATE NULL,
	TRUST_LOCATION_IND VARCHAR2(1) NOT NULL,
	CLI_LOCN_COMMENT VARCHAR2(4000) NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	CONSTRAINT "BIN$A8NnKNSZAszgVAAQ4CNh2g==$0" CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT "BIN$A8NnKNSaAszgVAAQ4CNh2g==$0" CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT "BIN$A8NnKNSbAszgVAAQ4CNh2g==$0" CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT "BIN$A8NnKNScAszgVAAQ4CNh2g==$0" CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT "BIN$A8NnKNSdAszgVAAQ4CNh2g==$0" CHECK ("ADD_ORG_UNIT" IS NOT NULL),
	CONSTRAINT "BIN$A8NnKNSeAszgVAAQ4CNh2g==$0" CHECK ("REVISION_COUNT" IS NOT NULL),
	CONSTRAINT CLIENT_LOCATION_PK PRIMARY KEY (CLIENT_NUMBER,CLIENT_LOCN_CODE),
	CONSTRAINT SYS_C0030524 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0030525 CHECK ("CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0030526 CHECK ("ADDRESS_1" IS NOT NULL),
	CONSTRAINT SYS_C0030527 CHECK ("CITY" IS NOT NULL),
	CONSTRAINT SYS_C0030528 CHECK ("COUNTRY" IS NOT NULL),
	CONSTRAINT SYS_C0030529 CHECK ("LOCN_EXPIRED_IND" IS NOT NULL),
	CONSTRAINT SYS_C0030530 CHECK ("TRUST_LOCATION_IND" IS NOT NULL),
	CONSTRAINT SYS_C0030531 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL)
);
CREATE INDEX CL_FC_FK_I ON THE.CLIENT_LOCATION (CLIENT_NUMBER);
CREATE INDEX CL_OU_FK_I ON THE.CLIENT_LOCATION (UPDATE_ORG_UNIT);
CREATE INDEX CL_OU_IS_CREATED_BY_FK_I ON THE.CLIENT_LOCATION (ADD_ORG_UNIT);
CREATE INDEX "I2$_CLIENT_LOCATION" ON THE.CLIENT_LOCATION (HDBS_COMPANY_CODE);
ALTER TABLE THE.CLIENT_LOCATION ADD CONSTRAINT CL_FC_FK FOREIGN KEY (CLIENT_NUMBER) REFERENCES THE.FOREST_CLIENT(CLIENT_NUMBER);
ALTER TABLE THE.CLIENT_LOCATION ADD CONSTRAINT CL_OU_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.CLIENT_LOCATION ADD CONSTRAINT CL_OU_IS_CREATED_BY_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.CLIENT_CONTACT (
	CLIENT_CONTACT_ID NUMBER(12,0) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	BUS_CONTACT_CODE VARCHAR2(3) NOT NULL,
	CONTACT_NAME VARCHAR2(120) NOT NULL,
	BUSINESS_PHONE VARCHAR2(10) NULL,
	CELL_PHONE VARCHAR2(10) NULL,
	FAX_NUMBER VARCHAR2(10) NULL,
	EMAIL_ADDRESS VARCHAR2(128) NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	CONSTRAINT CLC_PK PRIMARY KEY (CLIENT_CONTACT_ID),
	CONSTRAINT SYS_C0030038 CHECK ("CLIENT_CONTACT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0030040 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0030042 CHECK ("CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0030044 CHECK ("BUS_CONTACT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0030046 CHECK ("CONTACT_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0030048 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0030050 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0030051 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0030052 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0030053 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0030054 CHECK ("ADD_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0030055 CHECK ("REVISION_COUNT" IS NOT NULL)
);
CREATE INDEX CC_BCC_FK_I ON THE.CLIENT_CONTACT (BUS_CONTACT_CODE);
CREATE INDEX CC_CL_FK_I ON THE.CLIENT_CONTACT (CLIENT_NUMBER,CLIENT_LOCN_CODE);
CREATE INDEX CC_OU_FK_I ON THE.CLIENT_CONTACT (ADD_ORG_UNIT);
CREATE INDEX CC_OU_IS_UPDATED_BY_FK_I ON THE.CLIENT_CONTACT (UPDATE_ORG_UNIT);
ALTER TABLE THE.CLIENT_CONTACT ADD CONSTRAINT CC_BCC_FK FOREIGN KEY (BUS_CONTACT_CODE) REFERENCES THE.BUSINESS_CONTACT_CODE(BUSINESS_CONTACT_CODE);
ALTER TABLE THE.CLIENT_CONTACT ADD CONSTRAINT CC_CL_FK FOREIGN KEY (CLIENT_NUMBER,CLIENT_LOCN_CODE) REFERENCES THE.CLIENT_LOCATION(CLIENT_NUMBER,CLIENT_LOCN_CODE);
ALTER TABLE THE.CLIENT_CONTACT ADD CONSTRAINT CC_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.CLIENT_CONTACT ADD CONSTRAINT CC_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.CLIENT_REORG_DETAIL (
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	ORIGINAL_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	ORIGINAL_CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	RESULTING_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	RESULTING_CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CRD_PK PRIMARY KEY (CLIENT_REORGANIZATION_ID,ORIGINAL_CLIENT_NUMBER,ORIGINAL_CLIENT_LOCN_CODE,RESULTING_CLIENT_NUMBER,RESULTING_CLIENT_LOCN_CODE),
	CONSTRAINT SYS_C0023091 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0023093 CHECK ("ORIGINAL_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0023095 CHECK ("ORIGINAL_CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0023097 CHECK ("RESULTING_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0023099 CHECK ("RESULTING_CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0023101 CHECK ("REVISION_COUNT" IS NOT NULL),
	CONSTRAINT SYS_C0023103 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0023105 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0023107 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0023109 CHECK ("UPDATE_USERID" IS NOT NULL)
);
CREATE INDEX CRD_CL_FK_I ON THE.CLIENT_REORG_DETAIL (ORIGINAL_CLIENT_NUMBER,ORIGINAL_CLIENT_LOCN_CODE);
CREATE INDEX CRD_CL_INCLUDES_RESULTING_FK_I ON THE.CLIENT_REORG_DETAIL (RESULTING_CLIENT_NUMBER,RESULTING_CLIENT_LOCN_CODE);
CREATE INDEX CRD_CR_FK_I ON THE.CLIENT_REORG_DETAIL (CLIENT_REORGANIZATION_ID);
ALTER TABLE THE.CLIENT_REORG_DETAIL ADD CONSTRAINT CRD_CL_FK FOREIGN KEY (ORIGINAL_CLIENT_NUMBER,ORIGINAL_CLIENT_LOCN_CODE) REFERENCES THE.CLIENT_LOCATION(CLIENT_NUMBER,CLIENT_LOCN_CODE);
ALTER TABLE THE.CLIENT_REORG_DETAIL ADD CONSTRAINT CRD_CL_INCLUDES_RESULTING_FK FOREIGN KEY (RESULTING_CLIENT_NUMBER,RESULTING_CLIENT_LOCN_CODE) REFERENCES THE.CLIENT_LOCATION(CLIENT_NUMBER,CLIENT_LOCN_CODE);
ALTER TABLE THE.CLIENT_REORG_DETAIL ADD CONSTRAINT CRD_CR_FK FOREIGN KEY (CLIENT_REORGANIZATION_ID) REFERENCES THE.CLIENT_REORGANIZATION(CLIENT_REORGANIZATION_ID);

CREATE TABLE THE.CLIENT_DOING_BUSINESS_AS (
	CLIENT_DBA_ID NUMBER(12,0) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	DOING_BUSINESS_AS_NAME VARCHAR2(120) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	CONSTRAINT CDBA_PK PRIMARY KEY (CLIENT_DBA_ID),
	CONSTRAINT SYS_C0011796 CHECK ("CLIENT_DBA_ID" IS NOT NULL),
	CONSTRAINT SYS_C0011797 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0011798 CHECK ("DOING_BUSINESS_AS_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0011799 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011800 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011801 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0011802 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011803 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011804 CHECK ("ADD_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0011805 CHECK ("REVISION_COUNT" IS NOT NULL)
);
CREATE INDEX CDBA_FC_FK_I ON THE.CLIENT_DOING_BUSINESS_AS (CLIENT_NUMBER);
CREATE INDEX CDBA_OU_FK_I ON THE.CLIENT_DOING_BUSINESS_AS (ADD_ORG_UNIT);
CREATE INDEX CDBA_OU_IS_UPDATED_BY_FK_I ON THE.CLIENT_DOING_BUSINESS_AS (UPDATE_ORG_UNIT);
ALTER TABLE THE.CLIENT_DOING_BUSINESS_AS ADD CONSTRAINT CDBA_FC_FK FOREIGN KEY (CLIENT_NUMBER) REFERENCES THE.FOREST_CLIENT(CLIENT_NUMBER);
ALTER TABLE THE.CLIENT_DOING_BUSINESS_AS ADD CONSTRAINT CDBA_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.CLIENT_DOING_BUSINESS_AS ADD CONSTRAINT CDBA_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.CLIENT_REORG_LINK_AUDIT (
	CLIENT_REORG_LINK_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_REORG_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	ORIGINAL_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	RESULTING_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CRLA_PK PRIMARY KEY (CLIENT_REORG_LINK_AUDIT_ID),
	CONSTRAINT SYS_C0022934 CHECK ("CLIENT_REORG_LINK_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0022935 CHECK ("CLIENT_REORG_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0022936 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0022937 CHECK ("ORIGINAL_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0022938 CHECK ("RESULTING_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0022939 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0022940 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0022941 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0022942 CHECK ("UPDATE_USERID" IS NOT NULL)
);
CREATE INDEX CRLA_AAC_FK_I ON THE.CLIENT_REORG_LINK_AUDIT (CLIENT_REORG_AUDIT_CODE);
ALTER TABLE THE.CLIENT_REORG_LINK_AUDIT ADD CONSTRAINT CRLA_CRAC_FK FOREIGN KEY (CLIENT_REORG_AUDIT_CODE) REFERENCES THE.CLIENT_REORG_AUDIT_CODE(CLIENT_REORG_AUDIT_CODE);

CREATE TABLE THE.CLIENT_REORG_DETAIL_AUDIT (
	CLIENT_REORG_DETAIL_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_REORG_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_REORGANIZATION_ID NUMBER(12,0) NOT NULL,
	ORIGINAL_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	ORIGINAL_CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	RESULTING_CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	RESULTING_CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	ENTRY_TIMESTAMP DATE NOT NULL,
	ENTRY_USERID VARCHAR2(30) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CRDA_PK PRIMARY KEY (CLIENT_REORG_DETAIL_AUDIT_ID),
	CONSTRAINT SYS_C0024617 CHECK ("CLIENT_REORG_DETAIL_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0024618 CHECK ("CLIENT_REORG_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024619 CHECK ("CLIENT_REORGANIZATION_ID" IS NOT NULL),
	CONSTRAINT SYS_C0024620 CHECK ("ORIGINAL_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0024621 CHECK ("ORIGINAL_CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024622 CHECK ("RESULTING_CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0024623 CHECK ("RESULTING_CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0024624 CHECK ("ENTRY_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0024625 CHECK ("ENTRY_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0024626 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0024627 CHECK ("UPDATE_USERID" IS NOT NULL)
);
CREATE INDEX CRDA_AAC_FK_I ON THE.CLIENT_REORG_DETAIL_AUDIT (CLIENT_REORG_AUDIT_CODE);
ALTER TABLE THE.CLIENT_REORG_DETAIL_AUDIT ADD CONSTRAINT CRDA_CRAC_FK FOREIGN KEY (CLIENT_REORG_AUDIT_CODE) REFERENCES THE.CLIENT_REORG_AUDIT_CODE(CLIENT_REORG_AUDIT_CODE);

CREATE TABLE THE.FOR_CLI_AUDIT (
	FOREST_CLIENT_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_NAME VARCHAR2(60) NOT NULL,
	LEGAL_FIRST_NAME VARCHAR2(30) NULL,
	LEGAL_MIDDLE_NAME VARCHAR2(30) NULL,
	CLIENT_STATUS_CODE VARCHAR2(3) NOT NULL,
	CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	BIRTHDATE DATE NULL,
	CLIENT_ID_TYPE_CODE VARCHAR2(4) NULL,
	CLIENT_IDENTIFICATION VARCHAR2(40) NULL,
	REGISTRY_COMPANY_TYPE_CODE VARCHAR2(4) NULL,
	CORP_REGN_NMBR VARCHAR2(9) NULL,
	CLIENT_ACRONYM VARCHAR2(8) NULL,
	WCB_FIRM_NUMBER VARCHAR2(6) NULL,
	OCG_SUPPLIER_NMBR VARCHAR2(10) NULL,
	CLIENT_COMMENT VARCHAR2(4000) NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	CONSTRAINT FCAUD_PK PRIMARY KEY (FOREST_CLIENT_AUDIT_ID),
	CONSTRAINT SYS_C0029950 CHECK ("FOREST_CLIENT_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0029951 CHECK ("CLIENT_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029952 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0029953 CHECK ("CLIENT_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0029954 CHECK ("CLIENT_STATUS_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029955 CHECK ("CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029956 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029957 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029958 CHECK ("ADD_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0029959 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029960 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029961 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL)
);
CREATE INDEX FCA_CAUDC_FK_I ON THE.FOR_CLI_AUDIT (CLIENT_AUDIT_CODE);
CREATE INDEX FCA_FC_FK_I ON THE.FOR_CLI_AUDIT (CLIENT_NUMBER);
CREATE INDEX FCA_OU_FK_I ON THE.FOR_CLI_AUDIT (ADD_ORG_UNIT);
CREATE INDEX FCA_OU_IS_UPDATED_BY_FK_I ON THE.FOR_CLI_AUDIT (UPDATE_ORG_UNIT);
ALTER TABLE THE.FOR_CLI_AUDIT ADD CONSTRAINT FCA_CAUDC_FK FOREIGN KEY (CLIENT_AUDIT_CODE) REFERENCES THE.CLIENT_AUDIT_CODE(CLIENT_AUDIT_CODE);
ALTER TABLE THE.FOR_CLI_AUDIT ADD CONSTRAINT FCA_FC_FK FOREIGN KEY (CLIENT_NUMBER) REFERENCES THE.FOREST_CLIENT(CLIENT_NUMBER);
ALTER TABLE THE.FOR_CLI_AUDIT ADD CONSTRAINT FCA_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.FOR_CLI_AUDIT ADD CONSTRAINT FCA_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.CLIENT_DOING_BUSINESS_AS_AUDIT (
	CLIENT_DBA_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_DBA_ID NUMBER(12,0) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	DOING_BUSINESS_AS_NAME VARCHAR2(120) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	CONSTRAINT CDBAA_PK PRIMARY KEY (CLIENT_DBA_AUDIT_ID),
	CONSTRAINT SYS_C0011716 CHECK ("CLIENT_DBA_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0011717 CHECK ("CLIENT_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011718 CHECK ("CLIENT_DBA_ID" IS NOT NULL),
	CONSTRAINT SYS_C0011719 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0011720 CHECK ("DOING_BUSINESS_AS_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0011721 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011722 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011723 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0011724 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011725 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011726 CHECK ("ADD_ORG_UNIT" IS NOT NULL)
);
CREATE INDEX CDBAA_CAUDC_FK_I ON THE.CLIENT_DOING_BUSINESS_AS_AUDIT (CLIENT_AUDIT_CODE);
CREATE INDEX CDBAA_OU_FK_I ON THE.CLIENT_DOING_BUSINESS_AS_AUDIT (ADD_ORG_UNIT);
CREATE INDEX CDBAA_OU_IS_UPDATED_BY_FK_I ON THE.CLIENT_DOING_BUSINESS_AS_AUDIT (UPDATE_ORG_UNIT);
ALTER TABLE THE.CLIENT_DOING_BUSINESS_AS_AUDIT ADD CONSTRAINT CDBAA_CAUDC_FK FOREIGN KEY (CLIENT_AUDIT_CODE) REFERENCES THE.CLIENT_AUDIT_CODE(CLIENT_AUDIT_CODE);
ALTER TABLE THE.CLIENT_DOING_BUSINESS_AS_AUDIT ADD CONSTRAINT CDBAA_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.CLIENT_DOING_BUSINESS_AS_AUDIT ADD CONSTRAINT CDBAA_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.CLIENT_ACTION_REASON_XREF (
	CLIENT_UPDATE_ACTION_CODE VARCHAR2(4) NOT NULL,
	CLIENT_UPDATE_REASON_CODE VARCHAR2(4) NOT NULL,
	CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CARX_PK PRIMARY KEY (CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE),
	CONSTRAINT SYS_C0038307 CHECK ("CLIENT_UPDATE_ACTION_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0038309 CHECK ("CLIENT_UPDATE_REASON_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0038311 CHECK ("CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0038313 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0038315 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0038317 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0038319 CHECK ("ADD_USERID" IS NOT NULL)
);
CREATE INDEX CARX_CTC_FK_I ON THE.CLIENT_ACTION_REASON_XREF (CLIENT_TYPE_CODE);
CREATE INDEX CARX_CUAC_FK_I ON THE.CLIENT_ACTION_REASON_XREF (CLIENT_UPDATE_ACTION_CODE);
CREATE INDEX CARX_CURC_FK_I ON THE.CLIENT_ACTION_REASON_XREF (CLIENT_UPDATE_REASON_CODE);
ALTER TABLE THE.CLIENT_ACTION_REASON_XREF ADD CONSTRAINT CARX_CTC_FK FOREIGN KEY (CLIENT_TYPE_CODE) REFERENCES THE.CLIENT_TYPE_CODE(CLIENT_TYPE_CODE);
ALTER TABLE THE.CLIENT_ACTION_REASON_XREF ADD CONSTRAINT CARX_CUAC_FK FOREIGN KEY (CLIENT_UPDATE_ACTION_CODE) REFERENCES THE.CLIENT_UPDATE_ACTION_CODE(CLIENT_UPDATE_ACTION_CODE);
ALTER TABLE THE.CLIENT_ACTION_REASON_XREF ADD CONSTRAINT CARX_CURC_FK FOREIGN KEY (CLIENT_UPDATE_REASON_CODE) REFERENCES THE.CLIENT_UPDATE_REASON_CODE(CLIENT_UPDATE_REASON_CODE);

CREATE TABLE THE.CLIENT_UPDATE_REASON (
	CLIENT_UPDATE_REASON_ID NUMBER(12,0) NOT NULL,
	CLIENT_UPDATE_ACTION_CODE VARCHAR2(4) NOT NULL,
	CLIENT_UPDATE_REASON_CODE VARCHAR2(4) NOT NULL,
	CLIENT_TYPE_CODE VARCHAR2(1) NOT NULL,
	FOREST_CLIENT_AUDIT_ID NUMBER(12,0) NOT NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	CONSTRAINT CU_PK PRIMARY KEY (CLIENT_UPDATE_REASON_ID),
	CONSTRAINT SYS_C0038356 CHECK ("CLIENT_UPDATE_REASON_ID" IS NOT NULL),
	CONSTRAINT SYS_C0038357 CHECK ("CLIENT_UPDATE_ACTION_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0038358 CHECK ("CLIENT_UPDATE_REASON_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0038359 CHECK ("CLIENT_TYPE_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0038360 CHECK ("FOREST_CLIENT_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0038361 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0038362 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0038363 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0038364 CHECK ("ADD_USERID" IS NOT NULL)
);
CREATE INDEX CU_CARX_FK_I ON THE.CLIENT_UPDATE_REASON (CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE);
CREATE INDEX CU_FCA_FK_I ON THE.CLIENT_UPDATE_REASON (FOREST_CLIENT_AUDIT_ID);
ALTER TABLE THE.CLIENT_UPDATE_REASON ADD CONSTRAINT CU_CARX_FK FOREIGN KEY (CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE) REFERENCES THE.CLIENT_ACTION_REASON_XREF(CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE);
ALTER TABLE THE.CLIENT_UPDATE_REASON ADD CONSTRAINT CU_FCA_FK FOREIGN KEY (FOREST_CLIENT_AUDIT_ID) REFERENCES THE.FOR_CLI_AUDIT(FOREST_CLIENT_AUDIT_ID);

CREATE TABLE THE.CLI_CON_AUDIT (
	CLIENT_CONTACT_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_CONTACT_ID NUMBER(12,0) NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	BUS_CONTACT_CODE VARCHAR2(3) NOT NULL,
	CONTACT_NAME VARCHAR2(120) NOT NULL,
	BUSINESS_PHONE VARCHAR2(10) NULL,
	CELL_PHONE VARCHAR2(10) NULL,
	FAX_NUMBER VARCHAR2(10) NULL,
	EMAIL_ADDRESS VARCHAR2(128) NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT_NO NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	CONSTRAINT CCA_PK PRIMARY KEY (CLIENT_CONTACT_AUDIT_ID),
	CONSTRAINT SYS_C0029803 CHECK ("CLIENT_CONTACT_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0029804 CHECK ("CLIENT_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029805 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0029806 CHECK ("CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029807 CHECK ("BUS_CONTACT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029808 CHECK ("CONTACT_NAME" IS NOT NULL),
	CONSTRAINT SYS_C0029809 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029810 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029811 CHECK ("UPDATE_ORG_UNIT_NO" IS NOT NULL),
	CONSTRAINT SYS_C0029812 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029813 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029814 CHECK ("ADD_ORG_UNIT" IS NOT NULL)
);
CREATE INDEX CCA_OU_FK_I ON THE.CLI_CON_AUDIT (ADD_ORG_UNIT);
CREATE INDEX CCA_OU_IS_UPDATED_BY_FK_I ON THE.CLI_CON_AUDIT (UPDATE_ORG_UNIT_NO);
CREATE UNIQUE INDEX "I1$_CLI_CON_AUDIT" ON THE.CLI_CON_AUDIT (CLIENT_NUMBER,CLIENT_LOCN_CODE,BUS_CONTACT_CODE,UPDATE_TIMESTAMP);
ALTER TABLE THE.CLI_CON_AUDIT ADD CONSTRAINT CCA_CAUDC_FK FOREIGN KEY (CLIENT_AUDIT_CODE) REFERENCES THE.CLIENT_AUDIT_CODE(CLIENT_AUDIT_CODE);
ALTER TABLE THE.CLI_CON_AUDIT ADD CONSTRAINT CCA_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.CLI_CON_AUDIT ADD CONSTRAINT CCA_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT_NO) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.CLI_LOCN_AUDIT (
	CLIENT_LOCATION_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	CLIENT_LOCN_NAME VARCHAR2(40) NULL,
	HDBS_COMPANY_CODE VARCHAR2(5) NULL,
	ADDRESS_1 VARCHAR2(40) NOT NULL,
	ADDRESS_2 VARCHAR2(40) NULL,
	ADDRESS_3 VARCHAR2(40) NULL,
	CITY VARCHAR2(30) NOT NULL,
	PROVINCE VARCHAR2(50) NULL,
	POSTAL_CODE VARCHAR2(10) NULL,
	COUNTRY VARCHAR2(50) NOT NULL,
	BUSINESS_PHONE VARCHAR2(10) NULL,
	HOME_PHONE VARCHAR2(10) NULL,
	CELL_PHONE VARCHAR2(10) NULL,
	FAX_NUMBER VARCHAR2(10) NULL,
	EMAIL_ADDRESS VARCHAR2(128) NULL,
	LOCN_EXPIRED_IND VARCHAR2(1) NOT NULL,
	RETURNED_MAIL_DATE DATE NULL,
	TRUST_LOCATION_IND VARCHAR2(1) NULL,
	CLI_LOCN_COMMENT VARCHAR2(4000) NULL,
	CLIENT_UPDATE_ACTION_CODE VARCHAR2(4) NULL,
	CLIENT_UPDATE_REASON_CODE VARCHAR2(4) NULL,
	CLIENT_TYPE_CODE VARCHAR2(1) NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	CONSTRAINT CLA_PK PRIMARY KEY (CLIENT_LOCATION_AUDIT_ID),
	CONSTRAINT SYS_C0029886 CHECK ("CLIENT_LOCATION_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0029887 CHECK ("CLIENT_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029888 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0029889 CHECK ("CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0029890 CHECK ("ADDRESS_1" IS NOT NULL),
	CONSTRAINT SYS_C0029891 CHECK ("CITY" IS NOT NULL),
	CONSTRAINT SYS_C0029892 CHECK ("COUNTRY" IS NOT NULL),
	CONSTRAINT SYS_C0029893 CHECK ("LOCN_EXPIRED_IND" IS NOT NULL),
	CONSTRAINT SYS_C0029894 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029895 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029896 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0029897 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0029898 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0029899 CHECK ("ADD_ORG_UNIT" IS NOT NULL)
);
CREATE INDEX CLA_CARX_FK_I ON THE.CLI_LOCN_AUDIT (CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE);
CREATE INDEX CLA_CAUDC_FK_I ON THE.CLI_LOCN_AUDIT (CLIENT_AUDIT_CODE);
CREATE INDEX CLA_CL_FK_I ON THE.CLI_LOCN_AUDIT (CLIENT_NUMBER,CLIENT_LOCN_CODE);
CREATE INDEX CLA_OU_FK_I ON THE.CLI_LOCN_AUDIT (UPDATE_ORG_UNIT);
CREATE INDEX CLA_OU_IS_CREATED_BY_FK_I ON THE.CLI_LOCN_AUDIT (ADD_ORG_UNIT);
ALTER TABLE THE.CLI_LOCN_AUDIT ADD CONSTRAINT CLA_CARX_FK FOREIGN KEY (CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE) REFERENCES THE.CLIENT_ACTION_REASON_XREF(CLIENT_UPDATE_ACTION_CODE,CLIENT_UPDATE_REASON_CODE,CLIENT_TYPE_CODE);
ALTER TABLE THE.CLI_LOCN_AUDIT ADD CONSTRAINT CLA_CAUDC_FK FOREIGN KEY (CLIENT_AUDIT_CODE) REFERENCES THE.CLIENT_AUDIT_CODE(CLIENT_AUDIT_CODE);
ALTER TABLE THE.CLI_LOCN_AUDIT ADD CONSTRAINT CLA_CL_FK FOREIGN KEY (CLIENT_NUMBER,CLIENT_LOCN_CODE) REFERENCES THE.CLIENT_LOCATION(CLIENT_NUMBER,CLIENT_LOCN_CODE);
ALTER TABLE THE.CLI_LOCN_AUDIT ADD CONSTRAINT CLA_OU_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.CLI_LOCN_AUDIT ADD CONSTRAINT CLA_OU_IS_CREATED_BY_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);

CREATE TABLE THE.RELATED_CLIENT (
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	RELATED_CLNT_NMBR VARCHAR2(8) NOT NULL,
	RELATED_CLNT_LOCN VARCHAR2(2) NOT NULL,
	RELATIONSHIP_CODE VARCHAR2(2) NOT NULL,
	SIGNING_AUTH_IND VARCHAR2(1) NULL,
	PERCENT_OWNERSHIP NUMBER(4,1) NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	REVISION_COUNT NUMBER(5,0) NOT NULL,
	CONSTRAINT RCLI_PK PRIMARY KEY (CLIENT_NUMBER,CLIENT_LOCN_CODE,RELATED_CLNT_NMBR,RELATED_CLNT_LOCN),
	CONSTRAINT SYS_C0011626 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0011627 CHECK ("CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011628 CHECK ("RELATED_CLNT_NMBR" IS NOT NULL),
	CONSTRAINT SYS_C0011629 CHECK ("RELATED_CLNT_LOCN" IS NOT NULL),
	CONSTRAINT SYS_C0011630 CHECK ("RELATIONSHIP_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011631 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011632 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011633 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0011634 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011635 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011636 CHECK ("ADD_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0011637 CHECK ("REVISION_COUNT" IS NOT NULL)
);
CREATE INDEX RC_CL_FK_I ON THE.RELATED_CLIENT (CLIENT_NUMBER,CLIENT_LOCN_CODE);
CREATE INDEX RC_CL_HAS_RELATIONSHIP_FOR_F_I ON THE.RELATED_CLIENT (RELATED_CLNT_NMBR,RELATED_CLNT_LOCN);
CREATE INDEX RC_OU_FK_I ON THE.RELATED_CLIENT (ADD_ORG_UNIT);
CREATE INDEX RC_OU_IS_UPDATED_BY_FK_I ON THE.RELATED_CLIENT (UPDATE_ORG_UNIT);
CREATE INDEX RC_RELCD_FK_I ON THE.RELATED_CLIENT (RELATIONSHIP_CODE);
ALTER TABLE THE.RELATED_CLIENT ADD CONSTRAINT RC_CL_FK FOREIGN KEY (CLIENT_NUMBER,CLIENT_LOCN_CODE) REFERENCES THE.CLIENT_LOCATION(CLIENT_NUMBER,CLIENT_LOCN_CODE);
ALTER TABLE THE.RELATED_CLIENT ADD CONSTRAINT RC_CL_HAS_RELATIONSHIP_FOR_FK FOREIGN KEY (RELATED_CLNT_NMBR,RELATED_CLNT_LOCN) REFERENCES THE.CLIENT_LOCATION(CLIENT_NUMBER,CLIENT_LOCN_CODE);
ALTER TABLE THE.RELATED_CLIENT ADD CONSTRAINT RC_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.RELATED_CLIENT ADD CONSTRAINT RC_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.RELATED_CLIENT ADD CONSTRAINT RC_RELCD_FK FOREIGN KEY (RELATIONSHIP_CODE) REFERENCES THE.CLIENT_RELATIONSHIP_CODE(CLIENT_RELATIONSHIP_CODE);

CREATE TABLE THE.REL_CLI_AUDIT (
	RELATED_CLIENT_AUDIT_ID NUMBER(12,0) NOT NULL,
	CLIENT_AUDIT_CODE VARCHAR2(3) NOT NULL,
	CLIENT_NUMBER VARCHAR2(8) NOT NULL,
	CLIENT_LOCN_CODE VARCHAR2(2) NOT NULL,
	RELATED_CLNT_NMBR VARCHAR2(8) NOT NULL,
	RELATED_CLNT_LOCN VARCHAR2(2) NULL,
	RELATIONSHIP_CODE VARCHAR2(2) NULL,
	SIGNING_AUTH_IND VARCHAR2(1) NULL,
	PERCENT_OWNERSHIP NUMBER(4,1) NULL,
	UPDATE_TIMESTAMP DATE NOT NULL,
	UPDATE_USERID VARCHAR2(30) NOT NULL,
	UPDATE_ORG_UNIT NUMBER(10,0) NOT NULL,
	ADD_TIMESTAMP DATE NOT NULL,
	ADD_USERID VARCHAR2(30) NOT NULL,
	ADD_ORG_UNIT NUMBER(10,0) NOT NULL,
	CONSTRAINT RCA_PK PRIMARY KEY (RELATED_CLIENT_AUDIT_ID),
	CONSTRAINT SYS_C0011541 CHECK ("RELATED_CLIENT_AUDIT_ID" IS NOT NULL),
	CONSTRAINT SYS_C0011542 CHECK ("CLIENT_AUDIT_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011543 CHECK ("CLIENT_NUMBER" IS NOT NULL),
	CONSTRAINT SYS_C0011544 CHECK ("CLIENT_LOCN_CODE" IS NOT NULL),
	CONSTRAINT SYS_C0011545 CHECK ("RELATED_CLNT_NMBR" IS NOT NULL),
	CONSTRAINT SYS_C0011546 CHECK ("UPDATE_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011547 CHECK ("UPDATE_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011548 CHECK ("UPDATE_ORG_UNIT" IS NOT NULL),
	CONSTRAINT SYS_C0011549 CHECK ("ADD_TIMESTAMP" IS NOT NULL),
	CONSTRAINT SYS_C0011550 CHECK ("ADD_USERID" IS NOT NULL),
	CONSTRAINT SYS_C0011551 CHECK ("ADD_ORG_UNIT" IS NOT NULL)
);
CREATE UNIQUE INDEX "I1$_REL_CLI_AUDIT" ON THE.REL_CLI_AUDIT (CLIENT_NUMBER,CLIENT_LOCN_CODE,RELATED_CLNT_NMBR,UPDATE_TIMESTAMP);
CREATE INDEX RCA_CAUDC_FK_I ON THE.REL_CLI_AUDIT (CLIENT_AUDIT_CODE);
CREATE INDEX RCA_CLI_LOCN_I ON THE.REL_CLI_AUDIT (CLIENT_NUMBER,CLIENT_LOCN_CODE);
CREATE INDEX RCA_OU_FK_I ON THE.REL_CLI_AUDIT (ADD_ORG_UNIT);
CREATE INDEX RCA_OU_IS_UPDATED_BY_FK_I ON THE.REL_CLI_AUDIT (UPDATE_ORG_UNIT);
CREATE INDEX RCA_REL_CLI_LOCN_I ON THE.REL_CLI_AUDIT (RELATED_CLNT_NMBR,RELATED_CLNT_LOCN);
ALTER TABLE THE.REL_CLI_AUDIT ADD CONSTRAINT RCA_CAUDC_FK FOREIGN KEY (CLIENT_AUDIT_CODE) REFERENCES THE.CLIENT_AUDIT_CODE(CLIENT_AUDIT_CODE);
ALTER TABLE THE.REL_CLI_AUDIT ADD CONSTRAINT RCA_OU_FK FOREIGN KEY (ADD_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
ALTER TABLE THE.REL_CLI_AUDIT ADD CONSTRAINT RCA_OU_IS_UPDATED_BY_FK FOREIGN KEY (UPDATE_ORG_UNIT) REFERENCES THE.ORG_UNIT(ORG_UNIT_NO);
INSERT INTO THE.FOREST_CLIENT (CLIENT_NUMBER,CLIENT_NAME,LEGAL_FIRST_NAME,LEGAL_MIDDLE_NAME,CLIENT_STATUS_CODE,CLIENT_TYPE_CODE,BIRTHDATE,CLIENT_ID_TYPE_CODE,CLIENT_IDENTIFICATION,REGISTRY_COMPANY_TYPE_CODE,CORP_REGN_NMBR,CLIENT_ACRONYM,WCB_FIRM_NUMBER,OCG_SUPPLIER_NMBR,CLIENT_COMMENT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,REVISION_COUNT) VALUES
('00000001','BAXTER','JAMES',NULL,'ACT','I',TIMESTAMP'1959-05-18 00:00:00.0','BCDL',NULL,NULL,'00000001',NULL,NULL,NULL,'Il.',TIMESTAMP'1989-11-26 08:52:38.0','CONV',70,TIMESTAMP'1999-02-16 10:40:05.0','JDOH',70,1) ;

INSERT INTO THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID,CLIENT_NUMBER,DOING_BUSINESS_AS_NAME,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
(client_dba_seq.NEXTVAL,'00000001','BAXTER''S FAMILY',TIMESTAMP'2007-04-24 12:21:47.0','IDIR\ITISWATTLES',70,TIMESTAMP'2007-04-24 12:21:47.0','IDIR\ITISWATTLES',70,1);

INSERT INTO THE.CLIENT_LOCATION (CLIENT_NUMBER,CLIENT_LOCN_CODE,CLIENT_LOCN_NAME,HDBS_COMPANY_CODE,ADDRESS_1,ADDRESS_2,ADDRESS_3,CITY,PROVINCE,POSTAL_CODE,COUNTRY,BUSINESS_PHONE,HOME_PHONE,CELL_PHONE,FAX_NUMBER,EMAIL_ADDRESS,LOCN_EXPIRED_IND,RETURNED_MAIL_DATE,TRUST_LOCATION_IND,CLI_LOCN_COMMENT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
('00000001','00',NULL,'01382','2080 Labieux Rd',NULL,NULL,'NANAIMO','BC','V9T6J9','CANADA',NULL,'8006618773',NULL,NULL,NULL,'N',NULL,'N',NULL,TIMESTAMP'2002-03-22 15:52:03.0','JBAXTER',70,TIMESTAMP'1989-11-26 08:52:09.0','CONV',70,1);

-- 00000002 Doug Funny, Doing Buseness and Location

INSERT INTO THE.FOREST_CLIENT (CLIENT_NUMBER,CLIENT_NAME,LEGAL_FIRST_NAME,LEGAL_MIDDLE_NAME,CLIENT_STATUS_CODE,CLIENT_TYPE_CODE,BIRTHDATE,CLIENT_ID_TYPE_CODE,CLIENT_IDENTIFICATION,REGISTRY_COMPANY_TYPE_CODE,CORP_REGN_NMBR,CLIENT_ACRONYM,WCB_FIRM_NUMBER,OCG_SUPPLIER_NMBR,CLIENT_COMMENT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,REVISION_COUNT) VALUES
('00000002','FUNNY','THOMAS','Yansi','ACT','I',TIMESTAMP'1939-07-04 00:00:00.0','BCDL','Wull.',NULL,'00000002',NULL,NULL,NULL,'C v.',TIMESTAMP'1989-11-26 08:52:38.0','CONV',70,TIMESTAMP'2000-08-24 15:59:37.0','PLOUSY',70,1);

INSERT INTO THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID,CLIENT_NUMBER,DOING_BUSINESS_AS_NAME,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
(client_dba_seq.NEXTVAL,'00000002','DOUG FUNNY',TIMESTAMP'2007-04-24 12:21:47.0','IDIR\ITISWATTLES',70,TIMESTAMP'2007-04-24 12:21:47.0','IDIR\ITISWATTLES',70,1);

INSERT INTO THE.CLIENT_LOCATION (CLIENT_NUMBER,CLIENT_LOCN_CODE,CLIENT_LOCN_NAME,HDBS_COMPANY_CODE,ADDRESS_1,ADDRESS_2,ADDRESS_3,CITY,PROVINCE,POSTAL_CODE,COUNTRY,BUSINESS_PHONE,HOME_PHONE,CELL_PHONE,FAX_NUMBER,EMAIL_ADDRESS,LOCN_EXPIRED_IND,RETURNED_MAIL_DATE,TRUST_LOCATION_IND,CLI_LOCN_COMMENT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
('00000002','00',NULL,'01384','Government St','Floor 2',NULL,'VICTORIA','BC','V8V2L8','CANADA',NULL,NULL,NULL,NULL,NULL,'N',NULL,'N',NULL,TIMESTAMP'1991-11-20 13:15:01.0','JBAXTER',70,TIMESTAMP'1989-11-26 08:52:09.0','CONV',70,1);

-- 00000003 Boris and Boris, Doing Business and Location

INSERT INTO THE.FOREST_CLIENT (CLIENT_NUMBER,CLIENT_NAME,LEGAL_FIRST_NAME,LEGAL_MIDDLE_NAME,CLIENT_STATUS_CODE,CLIENT_TYPE_CODE,BIRTHDATE,CLIENT_ID_TYPE_CODE,CLIENT_IDENTIFICATION,REGISTRY_COMPANY_TYPE_CODE,CORP_REGN_NMBR,CLIENT_ACRONYM,WCB_FIRM_NUMBER,OCG_SUPPLIER_NMBR,CLIENT_COMMENT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,REVISION_COUNT) VALUES
('00000003','BORIS AND BORIS INC.',NULL,NULL,'ACT','C',NULL,'BCDL',NULL,NULL,'00000003',NULL,NULL,NULL,NULL,TIMESTAMP'1989-11-26 08:52:38.0','CONV',70,TIMESTAMP'1989-11-26 08:52:38.0','CLIENT_REWRITE_CONV',70,1);

INSERT INTO THE.CLIENT_DOING_BUSINESS_AS (CLIENT_DBA_ID,CLIENT_NUMBER,DOING_BUSINESS_AS_NAME,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
(client_dba_seq.NEXTVAL,'00000003','BORIS AND BORIS INC.',TIMESTAMP'2007-04-24 12:21:47.0','IDIR\ITISWATTLES',70,TIMESTAMP'2007-04-24 12:21:47.0','IDIR\ITISWATTLES',70,1);

INSERT INTO THE.CLIENT_LOCATION (CLIENT_NUMBER,CLIENT_LOCN_CODE,CLIENT_LOCN_NAME,HDBS_COMPANY_CODE,ADDRESS_1,ADDRESS_2,ADDRESS_3,CITY,PROVINCE,POSTAL_CODE,COUNTRY,BUSINESS_PHONE,HOME_PHONE,CELL_PHONE,FAX_NUMBER,EMAIL_ADDRESS,LOCN_EXPIRED_IND,RETURNED_MAIL_DATE,TRUST_LOCATION_IND,CLI_LOCN_COMMENT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
('00000003','00',NULL,'01385','1950 Douglas St',NULL,NULL,'VICTORIA','BC','V8W1Z2','CANADA',NULL,NULL,NULL,NULL,NULL,'N',NULL,'N',NULL,TIMESTAMP'1999-05-04 09:30:11.0','JBAXTER',70,TIMESTAMP'1989-11-26 08:52:09.0','CONV',70,1);

-- 00000004 Indian Band, Multiple Locations

INSERT INTO THE.FOREST_CLIENT (CLIENT_NUMBER,CLIENT_NAME,LEGAL_FIRST_NAME,LEGAL_MIDDLE_NAME,CLIENT_STATUS_CODE,CLIENT_TYPE_CODE,BIRTHDATE,CLIENT_ID_TYPE_CODE,CLIENT_IDENTIFICATION,REGISTRY_COMPANY_TYPE_CODE,CORP_REGN_NMBR,CLIENT_ACRONYM,WCB_FIRM_NUMBER,OCG_SUPPLIER_NMBR,CLIENT_COMMENT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,REVISION_COUNT) VALUES
('00000004','SAMPLE INDIAN BAND COUNCIL',NULL,NULL,'ACT','B',NULL,NULL,NULL,'DINA','684','SAMPLIBC',236967,NULL,NULL,TIMESTAMP'1989-11-26 08:52:38.0','CONV',70,TIMESTAMP'1989-11-26 08:52:38.0','IDIR\ITISWATTLES',70,1);

INSERT INTO THE.CLIENT_LOCATION (CLIENT_NUMBER,CLIENT_LOCN_CODE,CLIENT_LOCN_NAME,HDBS_COMPANY_CODE,ADDRESS_1,ADDRESS_2,ADDRESS_3,CITY,PROVINCE,POSTAL_CODE,COUNTRY,BUSINESS_PHONE,HOME_PHONE,CELL_PHONE,FAX_NUMBER,EMAIL_ADDRESS,LOCN_EXPIRED_IND,RETURNED_MAIL_DATE,TRUST_LOCATION_IND,CLI_LOCN_COMMENT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
('00000004','00',NULL,'26573','PO BOX 999',NULL,NULL,'VICTORIA','BC','V8W1M0','CANADA',NULL,'2502502550',NULL,'2502502550',NULL,'N',NULL,'N',NULL,TIMESTAMP'2002-03-05 10:01:05.0','JBAXTER',70,TIMESTAMP'1989-11-26 12:20:50.0','CONV',70,1);
INSERT INTO THE.CLIENT_LOCATION (CLIENT_NUMBER,CLIENT_LOCN_CODE,CLIENT_LOCN_NAME,HDBS_COMPANY_CODE,ADDRESS_1,ADDRESS_2,ADDRESS_3,CITY,PROVINCE,POSTAL_CODE,COUNTRY,BUSINESS_PHONE,HOME_PHONE,CELL_PHONE,FAX_NUMBER,EMAIL_ADDRESS,LOCN_EXPIRED_IND,RETURNED_MAIL_DATE,TRUST_LOCATION_IND,CLI_LOCN_COMMENT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
('00000004','01','BAND OFFICE','D3475','INDIAN BAND','916-1150 MAINLAND ST',NULL,'VANCOUVER','BC','V6B2T4','CANADA','2505205200',NULL,NULL,'2505205200',NULL,'N',NULL,'N','THIS LOCATION IS ONLY FOR THE AGREEMENT SIGNAGE',TIMESTAMP'2006-04-03 15:01:44.0','itiswattles',70,TIMESTAMP'2001-01-25 14:08:42.0','ITISWATTLES',70,1);
INSERT INTO THE.CLIENT_LOCATION (CLIENT_NUMBER,CLIENT_LOCN_CODE,CLIENT_LOCN_NAME,HDBS_COMPANY_CODE,ADDRESS_1,ADDRESS_2,ADDRESS_3,CITY,PROVINCE,POSTAL_CODE,COUNTRY,BUSINESS_PHONE,HOME_PHONE,CELL_PHONE,FAX_NUMBER,EMAIL_ADDRESS,LOCN_EXPIRED_IND,RETURNED_MAIL_DATE,TRUST_LOCATION_IND,CLI_LOCN_COMMENT,UPDATE_TIMESTAMP,UPDATE_USERID,UPDATE_ORG_UNIT,ADD_TIMESTAMP,ADD_USERID,ADD_ORG_UNIT,REVISION_COUNT) VALUES
('00000004','02','TRUST ACCOUNT  -RSI','T1350','PO BOX 588',NULL,NULL,'CHASE','BC','V0E1M0','CANADA',NULL,'2502502550',NULL,'2502502550',NULL,'Y',NULL,'Y','TRUST ACCOUNT  - RSI',TIMESTAMP'2005-10-06 15:02:02.0','NEGELY',70,TIMESTAMP'9999-12-31 00:00:00.0','CONVSNT',70,1);

-- 00000005 Erna Krakowsky

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000005', 'KRAKOWSKY', 'ERNA', NULL, 'ACT', 'I', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

-- 00000006 Indian Canada, Doing Business, Multiple Locations, Org Unit, Contact

INSERT INTO "THE"."FOREST_CLIENT"
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000006', 'INDIAN CANADA', NULL, NULL, 'ACT', 'G', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '1989-11-26 08:54:15.000000', 'CONV', 70, TIMESTAMP '2001-09-28 11:04:30.000000', 'IDIR\ITISWATTLES', 70, 1);

INSERT INTO "THE"."CLIENT_LOCATION"
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000006', '00', 'MAILING ADDRESS', '04106', '300 - 1550 ALBERNI STREET', NULL, NULL, 'VANCOUVER', 'BC', 'V6G3C5', 'CANADA', NULL, NULL, NULL, NULL, NULL, 'N', NULL, 'N', NULL, TIMESTAMP '1990-06-20 10:51:17.000000', 'IDIR\ITISWATTLES', 70, TIMESTAMP '1989-11-26 08:54:15.000000', 'IDIR\ITISWATTLES', 70, 1);
INSERT INTO "THE"."CLIENT_LOCATION"
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000006', '01', 'TERRACE/LAKALZAP BAND', '05050', '600 1138 MELVILLE ST', NULL, NULL, 'VANCOUVER', 'BC', 'V6E4S3', 'CANADA', '6046666755', NULL, NULL, NULL, NULL, 'N', NULL, 'N', NULL, TIMESTAMP '2001-04-25 13:47:53.000000', 'IDIR\ITISWATTLES', 70, TIMESTAMP '1990-08-27 13:25:25.000000', 'IDIR\ITISWATTLES', 70, 1);
INSERT INTO "THE"."CLIENT_LOCATION"
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000006', '02', NULL, '05893', '209 280 VICTORIA STREET', NULL, NULL, 'PRINCE GEORGE', 'BC', 'V2L4X3', 'CANADA', '2505615121', NULL, NULL, NULL, NULL, 'N', NULL, 'N', NULL, TIMESTAMP '1999-03-24 15:00:18.000000', 'IDIR\ITISWATTLES', 70, TIMESTAMP '1990-08-27 13:26:46.000000', 'IDIR\ITISWATTLES', 70, 1);
INSERT INTO "THE"."CLIENT_LOCATION"
(CLIENT_NUMBER, CLIENT_LOCN_CODE, CLIENT_LOCN_NAME, HDBS_COMPANY_CODE, ADDRESS_1, ADDRESS_2, ADDRESS_3, CITY, PROVINCE, POSTAL_CODE, COUNTRY, BUSINESS_PHONE, HOME_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, LOCN_EXPIRED_IND, RETURNED_MAIL_DATE, TRUST_LOCATION_IND, CLI_LOCN_COMMENT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES('00000006', '03', 'SHUSWAP BAND OFFICE', '06901', 'SHUSWAP BAND OFFICE', 'PO BOX 790', NULL, 'INVERMERE', 'BC', 'V0A1K0', 'CANADA', NULL, NULL, NULL, NULL, NULL, 'N', NULL, 'N', NULL, TIMESTAMP '1999-03-24 15:00:34.000000', 'IDIR\ITISWATTLES', 70, TIMESTAMP '1990-08-27 13:27:57.000000', 'IDIR\ITISWATTLES', 70, 1);

INSERT INTO "THE"."CLIENT_CONTACT"
(CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES(client_dba_seq.NEXTVAL, '00000006', '00', 'BL', 'JASON MOMOA', '6046666755', NULL, NULL, NULL, TIMESTAMP '1993-03-26 17:40:15.000000', 'IDIR\ITISWATTLES', 1, TIMESTAMP '1993-03-26 17:40:15.000000', 'IDIR\ITISWATTLES', 1, 1);
INSERT INTO "THE"."CLIENT_CONTACT"
(CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE, CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER, EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT)
VALUES(client_dba_seq.NEXTVAL, '00000006', '00', 'TN', 'NEDAD KONTIC', '6046666755', NULL, NULL, NULL, TIMESTAMP '1993-03-26 17:37:29.000000', 'IDIR\ITISWATTLES', 1, TIMESTAMP '1993-03-26 17:37:29.000000', 'IDIR\ITISWATTLES', 1, 1);

-- 00000007 James Bond

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000007', 'bond', 'james', 'bond', 'ACT', 'I', TIMESTAMP '1939-07-04 00:00:00.000000', 'BCDL', 'Wull.', NULL, '00000002', NULL, NULL, NULL, 'C v.', TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '2000-08-24 15:59:37.000000', 'PLOUSY', 70, 1);

-- 00000008 James Hunt HUnt

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000008', 'hunt', 'james', 'hunt', 'ACT', 'I', NULL, NULL, NULL, NULL, NULL, 'jhunt', NULL, NULL, NULL,  TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

-- 00000009 James Hunt

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000009', 'james', NULL, 'hunt', 'ACT', 'I', NULL, NULL, NULL, NULL, NULL, 'hunterj', NULL, NULL, NULL, TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

-- 00000010 Jjamess Bbondd

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000010', 'bbondd', 'jjamess', 'bbondd', 'ACT', 'A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

-- 00000011 CORP. OF THE CITY OF VICTORIA

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000011', 'CORP. OF THE CITY OF VICTORIA', NULL, NULL, 'ACT', 'G', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

-- 00000012 Sampler FOREST PRODUCTS INC

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000012', 'SAMPLER FOREST PRODUCTS INC.', NULL, NULL, 'ACT', 'U',NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

-- 00000013 DOREEN FOREST PRODUCTS LTD

INSERT INTO THE.FOREST_CLIENT
(CLIENT_NUMBER, CLIENT_NAME, LEGAL_FIRST_NAME, LEGAL_MIDDLE_NAME, CLIENT_STATUS_CODE, CLIENT_TYPE_CODE, BIRTHDATE, CLIENT_ID_TYPE_CODE, CLIENT_IDENTIFICATION, REGISTRY_COMPANY_TYPE_CODE, CORP_REGN_NMBR, CLIENT_ACRONYM, WCB_FIRM_NUMBER, OCG_SUPPLIER_NMBR, CLIENT_COMMENT, ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT, REVISION_COUNT)
VALUES('00000013', 'DOREEN FOREST PRODUCTS LTD.', NULL, NULL, 'ACT', 'C', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,TIMESTAMP '1989-11-26 08:52:38.000000', 'CONV', 70, TIMESTAMP '1989-11-26 08:52:38.000000', 'IDIR\ITISWATTLES', 70, 1);

UPDATE max_client_nmbr SET client_number = (SELECT LPAD(TO_NUMBER(NVL(max(CLIENT_NUMBER),'0'))+1,8,'0') FROM FOREST_CLIENT);
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('AP','Accounts Payable',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('AR','Accounts Receivable',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BA','First Nations Administrator',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BC','First Nations Council Member',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BL','Billing',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BM','First Nations Manager',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BN','First Nations Treaty Negotiator',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CH','Chief',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CL','Collections',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DI','Director',TIMESTAMP'2005-02-17 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('EX','Export',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('GP','General Partner',TIMESTAMP'2006-11-21 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('IL','Interior Log Cost Reporting',TIMESTAMP'2015-04-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('LB','Log Broker',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('LP','Limited Partner',TIMESTAMP'2006-11-21 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('RC','Recreation Agreement Holder',TIMESTAMP'2005-03-21 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SI','Scale Site Contact',TIMESTAMP'2007-08-02 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SP','SPAR System Contact',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SR','Stumpage Rates',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SS','Scaling Software Vendor Contact',TIMESTAMP'2001-09-12 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('TC','BCTS Contractor',TIMESTAMP'2003-04-16 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('TN','Tenure Administration',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');
INSERT INTO THE.BUSINESS_CONTACT_CODE (BUSINESS_CONTACT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('TP','EDI Trading Partner',TIMESTAMP'2000-07-25 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2016-03-11 09:35:17.0');

INSERT INTO THE.CLIENT_AUDIT_CODE (CLIENT_AUDIT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DEL','Delete',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:43:00.0');
INSERT INTO THE.CLIENT_AUDIT_CODE (CLIENT_AUDIT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('INS','Insert',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:43:00.0');
INSERT INTO THE.CLIENT_AUDIT_CODE (CLIENT_AUDIT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('UPD','Update',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:43:00.0');

INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BCDL','British Columbia Drivers Licence',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BCID','British Columbia Identification',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BRTH','Birth Certificate',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CITZ','Citizenship Card',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('EXDL','Extraprovincial Drivers Licence',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FNID','First Nation Status ID',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('MILI','Canadian Military ID',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('OTHR','Other Identification',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('PASS','Canadian Passport',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');
INSERT INTO THE.CLIENT_ID_TYPE_CODE (CLIENT_ID_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('UNKN','Unknown',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:40.0');

INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('AF','Obsolete - Amalgamated from',TIMESTAMP'2005-09-05 00:00:00.0',TIMESTAMP'2007-03-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('AT','Obsolete - Amalgamated To',TIMESTAMP'2005-09-05 00:00:00.0',TIMESTAMP'2007-03-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('PA','General Partner',TIMESTAMP'2005-09-05 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('AG','Agent',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BR','Broker',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CL','Corporate Linkage',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DB','Does Business With',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FA','First Nations Company Affiliation',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FI','First Nations Member Owned',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FM','Family Member',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FO','First Nations Owned',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FP','First Nations Organizational Association',TIMESTAMP'2007-12-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('JV','Joint Venture Participant',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('OF','Officer',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');
INSERT INTO THE.CLIENT_RELATIONSHIP_CODE (CLIENT_RELATIONSHIP_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SH','Shareholder',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-10 14:10:14.0');

INSERT INTO THE.CLIENT_REORG_AUDIT_CODE (CLIENT_REORG_AUDIT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DEL','Delete',TIMESTAMP'2001-11-19 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:25.0');
INSERT INTO THE.CLIENT_REORG_AUDIT_CODE (CLIENT_REORG_AUDIT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('INS','Insert',TIMESTAMP'2001-11-19 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:25.0');
INSERT INTO THE.CLIENT_REORG_AUDIT_CODE (CLIENT_REORG_AUDIT_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('UPD','Update',TIMESTAMP'2001-11-19 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:25.0');

INSERT INTO THE.CLIENT_REORG_STATUS_CODE (CLIENT_REORG_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('APP','Approved',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:54.0');
INSERT INTO THE.CLIENT_REORG_STATUS_CODE (CLIENT_REORG_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CAN','Cancelled',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:54.0');
INSERT INTO THE.CLIENT_REORG_STATUS_CODE (CLIENT_REORG_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('COM','Complete',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:54.0');
INSERT INTO THE.CLIENT_REORG_STATUS_CODE (CLIENT_REORG_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('EXP','Expired',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:54.0');
INSERT INTO THE.CLIENT_REORG_STATUS_CODE (CLIENT_REORG_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('PND','Pending',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:54.0');
INSERT INTO THE.CLIENT_REORG_STATUS_CODE (CLIENT_REORG_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('PRC','Processing',TIMESTAMP'2001-10-22 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:32:54.0');

INSERT INTO THE.CLIENT_REORG_TYPE_CODE (CLIENT_REORG_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('ACQU','Acquisition',TIMESTAMP'2006-08-23 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:33:23.0');
INSERT INTO THE.CLIENT_REORG_TYPE_CODE (CLIENT_REORG_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('AMAL','Amalgamation',TIMESTAMP'2006-08-23 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2006-11-01 08:33:23.0');

INSERT INTO THE.CLIENT_STATUS_CODE (CLIENT_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('ACT','Active',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:42:21.0');
INSERT INTO THE.CLIENT_STATUS_CODE (CLIENT_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DAC','Deactivated',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:42:21.0');
INSERT INTO THE.CLIENT_STATUS_CODE (CLIENT_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DEC','Deceased',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:42:21.0');
INSERT INTO THE.CLIENT_STATUS_CODE (CLIENT_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('REC','Receivership',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:42:21.0');
INSERT INTO THE.CLIENT_STATUS_CODE (CLIENT_STATUS_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SPN','Suspended',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-03-14 14:42:21.0');

INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('A','Association',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('B','First Nation Band',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('C','Corporation',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('F','Ministry of Forests and Range',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('G','Government',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('I','Individual',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('L','Limited Partnership',TIMESTAMP'2006-11-21 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('P','General Partnership',TIMESTAMP'2009-03-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('R','First Nation Group',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('S','Society',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('T','First Nation Tribal Council',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');
INSERT INTO THE.CLIENT_TYPE_CODE (CLIENT_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('U','Unregistered Company',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2009-03-17 16:17:35.0');

INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('A','Extraprovincial Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('B','Extraprovincial Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BC','British Columbia Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('C','Continuation In',TIMESTAMP'2007-08-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CP','Cooperative Association',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DINA','Federal First Nations ID',TIMESTAMP'2007-10-11 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('EPR','Extraprovincial Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FOR','Extraprovincial Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('LIC','Extraprovincial Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('NON','Non Registered Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('REG','Extraprovincial Company',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('S','British Columbia Society',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('XCP','Extraprovincial Cooperative Association',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('XS','Extraprovincial Society',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2008-02-28 09:50:45.0');
INSERT INTO THE.REGISTRY_COMPANY_TYPE_CODE (REGISTRY_COMPANY_TYPE_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('FM','Sole Proprietorship',TIMESTAMP'2023-10-10 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2023-10-10 00:00:00.0');

INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('A','CP','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('A','XCP','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','A','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','B','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','BC','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','EPR','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','FOR','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','LIC','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','REG','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('S','S','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('S','XS','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('U','NON','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0','CLIADMIN',TIMESTAMP'2007-01-01 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('C','C','CLIADMIN',TIMESTAMP'2007-08-14 10:29:27.0','CLIADMIN',TIMESTAMP'2007-08-14 10:29:27.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('B','DINA','CLIADMIN',TIMESTAMP'2007-10-16 12:50:08.0','CLIADMIN',TIMESTAMP'2007-10-16 12:50:08.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('T','DINA','CLIADMIN',TIMESTAMP'2007-10-16 12:50:12.0','CLIADMIN',TIMESTAMP'2007-10-16 12:50:12.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('I','FM','CLIADMIN',TIMESTAMP'2023-10-10 00:00:00.0','CLIADMIN',TIMESTAMP'2023-10-10 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('U','FM','CLIADMIN',TIMESTAMP'2023-10-10 00:00:00.0','CLIADMIN',TIMESTAMP'2023-10-10 00:00:00.0');
INSERT INTO THE.CLIENT_TYPE_COMPANY_XREF (CLIENT_TYPE_CODE,REGISTRY_COMPANY_TYPE_CODE,ADD_USERID,ADD_TIMESTAMP,UPDATE_USERID,UPDATE_TIMESTAMP) VALUES ('P','FM','CLIADMIN',TIMESTAMP'2023-10-10 00:00:00.0','CLIADMIN',TIMESTAMP'2023-10-10 00:00:00.0');

INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('ACDC','Deceased Client Activated',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('ADDR','Address Change',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DAC','Deactivated',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('ID','ID Change',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('NAME','Name Change',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('RACT','Reactivated',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('SPN','Suspended',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');
INSERT INTO THE.CLIENT_UPDATE_ACTION_CODE (CLIENT_UPDATE_ACTION_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('USPN','Unsuspended',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-07-12 13:00:22.0');

INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('BKR','Bankruptcy',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CNTI','Continuation In',TIMESTAMP'2007-08-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CNTO','Continuation Out',TIMESTAMP'2007-08-02 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('CORR','Correction',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DEC','Deceased',TIMESTAMP'2007-08-02 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DIS','Dissolved',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('DUPL','Duplicate Client',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('EXEC','Executor Acting on Behalf of Deceased Client',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('IBKR','Bankruptcy in Progress',TIMESTAMP'2007-07-27 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('IDIS','Dissolution in Progress',TIMESTAMP'2007-07-27 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('IEST','Estate in Progress',TIMESTAMP'2007-07-27 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('IREC','Receivership In Progress',TIMESTAMP'2007-07-27 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('LNAM','Legal Name Change',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('MOVE','Moved',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('NLDB','No Longer Doing Business with MoFR',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('OBLG','Obligation Satisfied',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('REC','Receivership',TIMESTAMP'2007-07-27 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('REID','Replacement ID',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('RORG','Company Reorganization',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('RSPN','Related to a Suspended Client',TIMESTAMP'2007-10-22 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('TEMP','Temporary Process',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('VFIN','Violation - Financial',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');
INSERT INTO THE.CLIENT_UPDATE_REASON_CODE (CLIENT_UPDATE_REASON_CODE,DESCRIPTION,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES ('VTEN','Violation - Tenure',TIMESTAMP'2007-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2007-12-19 09:13:27.0');

INSERT INTO THE.MAILING_COUNTRY (COUNTRY_NAME,COUNTRY_CODE) VALUES ('BRAZIL',' ');
INSERT INTO THE.MAILING_COUNTRY (COUNTRY_NAME,COUNTRY_CODE) VALUES ('CANADA',' ');
INSERT INTO THE.MAILING_COUNTRY (COUNTRY_NAME,COUNTRY_CODE) VALUES ('COLOMBIA',' ');
INSERT INTO THE.MAILING_COUNTRY (COUNTRY_NAME,COUNTRY_CODE) VALUES ('USA',' ');

INSERT INTO THE.MAILING_PROVINCE_STATE (COUNTRY_NAME, PROVINCE_STATE_NAME, PROVINCE_STATE_CODE) VALUES('CANADA', 'British Columbia', 'BC');
INSERT INTO THE.MAILING_PROVINCE_STATE (COUNTRY_NAME, PROVINCE_STATE_NAME, PROVINCE_STATE_CODE) VALUES('USA', 'Washington', 'WA');
INSERT INTO THE.MAILING_PROVINCE_STATE (COUNTRY_NAME, PROVINCE_STATE_NAME, PROVINCE_STATE_CODE) VALUES('BRAZIL', 'Sao Paulo', 'SP');

INSERT INTO THE.MAILING_CITY (COUNTRY_NAME,PROVINCE_STATE_NAME,CITY_NAME) VALUES ('CANADA','British Columbia','VICTORIA');
INSERT INTO THE.MAILING_CITY (COUNTRY_NAME,PROVINCE_STATE_NAME,CITY_NAME) VALUES ('USA','Washington','SEATTLE');
INSERT INTO THE.MAILING_CITY (COUNTRY_NAME,PROVINCE_STATE_NAME,CITY_NAME) VALUES ('BRAZIL','Sao Paulo','CAMPINAS');

INSERT INTO THE.ORG_UNIT (ORG_UNIT_NO,ORG_UNIT_CODE,ORG_UNIT_NAME,LOCATION_CODE,AREA_CODE,TELEPHONE_NO,ORG_LEVEL_CODE,OFFICE_NAME_CODE,ROLLUP_REGION_NO,ROLLUP_REGION_CODE,ROLLUP_DIST_NO,ROLLUP_DIST_CODE,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES (1,'DEV','Development Team','010','250','3871701','H','VA',0,' ',0,' ',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2011-08-29 15:49:08.0');
INSERT INTO THE.ORG_UNIT (ORG_UNIT_NO,ORG_UNIT_CODE,ORG_UNIT_NAME,LOCATION_CODE,AREA_CODE,TELEPHONE_NO,ORG_LEVEL_CODE,OFFICE_NAME_CODE,ROLLUP_REGION_NO,ROLLUP_REGION_CODE,ROLLUP_DIST_NO,ROLLUP_DIST_CODE,EFFECTIVE_DATE,EXPIRY_DATE,UPDATE_TIMESTAMP) VALUES (70,'HVA','Timber Pricing Branch','010','250','3871701','H','VA',0,' ',0,' ',TIMESTAMP'1905-01-01 00:00:00.0',TIMESTAMP'9999-12-31 00:00:00.0',TIMESTAMP'2011-08-29 15:49:08.0');
CREATE OR REPLACE PACKAGE THE.CLIENT_011_CONTACT AS

  PROCEDURE get
  ( p_client_contact_id            IN OUT VARCHAR2
  , p_client_number                IN OUT VARCHAR2
  , p_client_acronym               IN OUT VARCHAR2
  , p_client_name                  IN OUT VARCHAR2
  , p_client_status                IN OUT VARCHAR2
  , p_client_status_desc           IN OUT VARCHAR2
  , p_client_type                  IN OUT VARCHAR2
  , p_client_type_desc             IN OUT VARCHAR2
  , p_client_locn_code             IN OUT VARCHAR2
  , p_error_message                IN OUT SIL_ERROR_MESSAGES
  , p_results                      IN OUT client_constants.ref_cur_t);

  PROCEDURE save
  ( p_client_contact_id            IN OUT VARCHAR2
  , p_client_number                IN OUT VARCHAR2
  , p_client_locn_code             IN OUT VARCHAR2
  , p_bus_contact_code             IN OUT VARCHAR2
  , p_contact_name                 IN OUT VARCHAR2
  , p_business_phone               IN OUT VARCHAR2
  , p_cell_phone                   IN OUT VARCHAR2
  , p_fax_number                   IN OUT VARCHAR2
  , p_email_address                IN OUT VARCHAR2
  , p_update_userid                IN OUT VARCHAR2
  , p_update_org_unit              IN OUT VARCHAR2
  , p_revision_count               IN OUT VARCHAR2
  , p_error_message                IN OUT SIL_ERROR_MESSAGES);

  PROCEDURE remove
  ( p_client_contact_id            IN OUT VARCHAR2
  , p_update_userid                IN OUT VARCHAR2
  , p_update_org_unit              IN OUT VARCHAR2
  , p_revision_count               IN OUT VARCHAR2
  , p_error_message                IN OUT SIL_ERROR_MESSAGES);

END CLIENT_011_CONTACT;

CREATE OR REPLACE PACKAGE BODY THE.CLIENT_011_CONTACT
AS

  PROCEDURE get
  ( p_client_contact_id            IN OUT VARCHAR2
  , p_client_number                IN OUT VARCHAR2
  , p_client_acronym               IN OUT VARCHAR2
  , p_client_name                  IN OUT VARCHAR2
  , p_client_status                IN OUT VARCHAR2
  , p_client_status_desc           IN OUT VARCHAR2
  , p_client_type                  IN OUT VARCHAR2
  , p_client_type_desc             IN OUT VARCHAR2
  , p_client_locn_code             IN OUT VARCHAR2
  , p_error_message                IN OUT SIL_ERROR_MESSAGES
  , p_results                      IN OUT client_constants.ref_cur_t)
  IS
  BEGIN
    IF p_client_contact_id IS NOT NULL THEN
      -- for the rare case that another user had changed the locn code for the record,
      -- or the record had been deleted, requery data of the record.
      CLIENT_CLIENT_CONTACT.init(p_client_contact_id);

      CLIENT_UTILS.append_arrays(p_error_message
                               , CLIENT_CLIENT_CONTACT.GET_ERROR_MESSAGE);

      IF p_error_message IS NULL THEN
        p_client_number := CLIENT_CLIENT_CONTACT.get_client_number;
        p_client_locn_code := CLIENT_CLIENT_CONTACT.get_client_locn_code;
      END IF;
    END IF;

    client_006_contact_list.get(p_client_number
                              , p_client_acronym
                              , p_client_name
                              , p_client_status
                              , p_client_status_desc
                              , p_client_type
                              , p_client_type_desc
                              , p_client_locn_code
                              , p_error_message
                              , p_results);
  END get;

  PROCEDURE save(
    p_client_contact_id              IN OUT VARCHAR2
  , p_client_number                  IN OUT VARCHAR2
  , p_client_locn_code               IN OUT VARCHAR2
  , p_bus_contact_code               IN OUT VARCHAR2
  , p_contact_name                   IN OUT VARCHAR2
  , p_business_phone                 IN OUT VARCHAR2
  , p_cell_phone                     IN OUT VARCHAR2
  , p_fax_number                     IN OUT VARCHAR2
  , p_email_address                  IN OUT VARCHAR2
  , p_update_userid                  IN OUT VARCHAR2
  , p_update_org_unit                IN OUT VARCHAR2
  , p_revision_count                 IN OUT VARCHAR2
  , p_error_message                  IN OUT SIL_ERROR_MESSAGES)
  IS
  BEGIN

    CLIENT_CLIENT_CONTACT.init(p_client_contact_id);
    CLIENT_CLIENT_CONTACT.SET_CLIENT_NUMBER(p_client_number);
    CLIENT_CLIENT_CONTACT.SET_CLIENT_LOCN_CODE(p_client_locn_code);
    CLIENT_CLIENT_CONTACT.SET_BUS_CONTACT_CODE(p_bus_contact_code);
    CLIENT_CLIENT_CONTACT.SET_CONTACT_NAME(p_contact_name);
    CLIENT_CLIENT_CONTACT.SET_BUSINESS_PHONE(p_business_phone);
    CLIENT_CLIENT_CONTACT.SET_CELL_PHONE(p_cell_phone);
    CLIENT_CLIENT_CONTACT.SET_FAX_NUMBER(p_fax_number);
    CLIENT_CLIENT_CONTACT.SET_EMAIL_ADDRESS(p_email_address);
    CLIENT_CLIENT_CONTACT.SET_UPDATE_USERID(p_update_userid);
    CLIENT_CLIENT_CONTACT.SET_UPDATE_ORG_UNIT(p_update_org_unit);
    CLIENT_CLIENT_CONTACT.SET_UPDATE_TIMESTAMP(SYSDATE);
    CLIENT_CLIENT_CONTACT.SET_REVISION_COUNT(p_revision_count);

    CLIENT_CLIENT_CONTACT.validate;

    IF NOT CLIENT_CLIENT_CONTACT.ERROR_RAISED THEN

      IF p_client_contact_id IS NULL THEN
        CLIENT_CLIENT_CONTACT.SET_ADD_USERID(p_update_userid);
        CLIENT_CLIENT_CONTACT.SET_ADD_ORG_UNIT(p_update_org_unit);
        CLIENT_CLIENT_CONTACT.SET_ADD_TIMESTAMP(SYSDATE);
        CLIENT_CLIENT_CONTACT.add;

        p_client_contact_id := CLIENT_CLIENT_CONTACT.GET_CLIENT_CONTACT_ID;
      ELSE
        CLIENT_CLIENT_CONTACT.change;
      END IF;
    END IF;

    CLIENT_UTILS.append_arrays(p_error_message
                             , CLIENT_CLIENT_CONTACT.GET_ERROR_MESSAGE);

  END save;

  PROCEDURE remove(
    p_client_contact_id            IN OUT VARCHAR2
  , p_update_userid                IN OUT VARCHAR2
  , p_update_org_unit              IN OUT VARCHAR2
  , p_revision_count               IN OUT VARCHAR2
  , p_error_message                IN OUT SIL_ERROR_MESSAGES)
  IS
  BEGIN
    CLIENT_CLIENT_CONTACT.init(p_client_contact_id);
    CLIENT_CLIENT_CONTACT.SET_UPDATE_TIMESTAMP(SYSDATE);
    CLIENT_CLIENT_CONTACT.SET_UPDATE_USERID(p_update_userid);
    CLIENT_CLIENT_CONTACT.SET_UPDATE_ORG_UNIT(p_update_org_unit);
    CLIENT_CLIENT_CONTACT.SET_REVISION_COUNT(p_revision_count);
    CLIENT_CLIENT_CONTACT.validate_remove;

    IF NOT CLIENT_CLIENT_CONTACT.ERROR_RAISED THEN
      CLIENT_CLIENT_CONTACT.remove;
    END IF;

    CLIENT_UTILS.append_arrays(p_error_message
                             , CLIENT_CLIENT_CONTACT.GET_ERROR_MESSAGE);

  END remove;

END CLIENT_011_CONTACT;
-- Package definition:
CREATE OR REPLACE PACKAGE THE.client_forest_client AS

  --Client type constants
  C_CLIENT_TYPE_INDIVIDUAL      CONSTANT forest_client.client_type_code%TYPE := 'I';
  C_CLIENT_TYPE_ASSOCIATION     CONSTANT forest_client.client_type_code%TYPE := 'A';
  C_CLIENT_TYPE_CORPORATION     CONSTANT forest_client.client_type_code%TYPE := 'C';
  C_CLIENT_TYPE_SOCIETY         CONSTANT forest_client.client_type_code%TYPE := 'S';

  --Client id type constants
  C_CLIENT_ID_TYPE_BC_DRIVERS   CONSTANT forest_client.client_id_type_code%TYPE := 'BCDL';

  --Can be used to declare standard client name
  std_client_name_type              VARCHAR2(125);

  PROCEDURE get;

  PROCEDURE init
  (p_client_number          IN VARCHAR2 DEFAULT NULL);

  --***START GETTERS
  FUNCTION error_raised RETURN BOOLEAN;

  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES;

  FUNCTION get_client_number RETURN VARCHAR2;

  FUNCTION get_client_name RETURN VARCHAR2;

  FUNCTION get_legal_first_name RETURN VARCHAR2;

  FUNCTION get_legal_middle_name RETURN VARCHAR2;

  FUNCTION get_client_status_code RETURN VARCHAR2;

  FUNCTION get_client_type_code RETURN VARCHAR2;

  FUNCTION get_birthdate RETURN DATE;

  FUNCTION get_client_id_type_code RETURN VARCHAR2;

  FUNCTION get_client_identification RETURN VARCHAR2;

  FUNCTION get_registry_company_type_code RETURN VARCHAR2;

  FUNCTION get_corp_regn_nmbr RETURN VARCHAR2;

  FUNCTION get_client_acronym RETURN VARCHAR2;

  FUNCTION get_wcb_firm_number RETURN VARCHAR2;

  FUNCTION get_ocg_supplier_nmbr RETURN VARCHAR2;

  FUNCTION get_client_comment RETURN VARCHAR2;

  FUNCTION get_add_timestamp RETURN DATE;

  FUNCTION get_add_userid RETURN VARCHAR2;

  FUNCTION get_add_org_unit RETURN NUMBER;

  FUNCTION get_update_timestamp RETURN DATE;

  FUNCTION get_update_userid RETURN VARCHAR2;

  FUNCTION get_update_org_unit RETURN NUMBER;

  FUNCTION get_revision_count RETURN NUMBER;

  FUNCTION get_ur_reason_status RETURN VARCHAR2;
  FUNCTION get_ur_reason_name RETURN VARCHAR2;
  FUNCTION get_ur_reason_id RETURN VARCHAR2;

  --***END GETTERS

  --***START SETTERS
  FUNCTION get_client_display_name
  (p_client_number              IN VARCHAR2 DEFAULT NULL)
  RETURN std_client_name_type%TYPE;

  PROCEDURE set_client_number(p_value IN VARCHAR2);

  PROCEDURE set_client_name(p_value IN VARCHAR2);

  PROCEDURE set_legal_first_name(p_value IN VARCHAR2);

  PROCEDURE set_legal_middle_name(p_value IN VARCHAR2);

  PROCEDURE set_client_status_code(p_value IN VARCHAR2);

  PROCEDURE set_client_type_code(p_value IN VARCHAR2);

  PROCEDURE set_birthdate(p_value IN DATE);

  PROCEDURE set_client_id_type_code(p_value IN VARCHAR2);

  PROCEDURE set_client_identification(p_value IN VARCHAR2);

  PROCEDURE set_registry_company_type_code(p_value IN VARCHAR2);

  PROCEDURE set_corp_regn_nmbr(p_value IN VARCHAR2);

  PROCEDURE set_client_acronym(p_value IN VARCHAR2);

  PROCEDURE set_wcb_firm_number(p_value IN VARCHAR2);

  PROCEDURE set_ocg_supplier_nmbr(p_value IN VARCHAR2);

  PROCEDURE set_client_comment(p_value IN VARCHAR2);

  PROCEDURE set_add_timestamp(p_value IN DATE);

  PROCEDURE set_add_userid(p_value IN VARCHAR2);

  PROCEDURE set_add_org_unit(p_value IN NUMBER);

  PROCEDURE set_update_timestamp(p_value IN DATE);

  PROCEDURE set_update_userid(p_value IN VARCHAR2);

  PROCEDURE set_update_org_unit(p_value IN NUMBER);

  PROCEDURE set_revision_count(p_value IN NUMBER);

  --***END SETTERS

  FUNCTION formatted_client_number
  (p_client_number IN VARCHAR2)
  RETURN VARCHAR2;

  FUNCTION formatted_client_number
  (p_client_number IN NUMBER)
  RETURN VARCHAR2;

  PROCEDURE process_update_reasons
  (p_ur_action_status       IN OUT VARCHAR2
  ,p_ur_reason_status       IN OUT VARCHAR2
  ,p_ur_action_name         IN OUT VARCHAR2
  ,p_ur_reason_name         IN OUT VARCHAR2
  ,p_ur_action_id           IN OUT VARCHAR2
  ,p_ur_reason_id           IN OUT VARCHAR2);

  PROCEDURE validate;

  PROCEDURE validate_remove;

  PROCEDURE add;

  PROCEDURE change;

  PROCEDURE remove;

END client_forest_client;

-- Package body definition:
CREATE OR REPLACE
PACKAGE BODY client_forest_client AS

  --member vars

  g_error_message                              SIL_ERROR_MESSAGES;

  g_client_number                              forest_client.client_number%TYPE;
  gb_client_number                             VARCHAR2(1);

  g_client_name                                forest_client.client_name%TYPE;
  gb_client_name                               VARCHAR2(1);

  g_legal_first_name                           forest_client.legal_first_name%TYPE;
  gb_legal_first_name                          VARCHAR2(1);

  g_legal_middle_name                          forest_client.legal_middle_name%TYPE;
  gb_legal_middle_name                         VARCHAR2(1);

  g_client_status_code                         forest_client.client_status_code%TYPE;
  gb_client_status_code                        VARCHAR2(1);

  g_client_type_code                           forest_client.client_type_code%TYPE;
  gb_client_type_code                          VARCHAR2(1);

  g_birthdate                                  forest_client.birthdate%TYPE;
  gb_birthdate                                 VARCHAR2(1);

  g_client_id_type_code                        forest_client.client_id_type_code%TYPE;
  gb_client_id_type_code                       VARCHAR2(1);

  g_client_identification                      forest_client.client_identification%TYPE;
  gb_client_identification                     VARCHAR2(1);

  g_registry_company_type_code                 forest_client.registry_company_type_code%TYPE;
  gb_registry_company_type_code                VARCHAR2(1);

  g_corp_regn_nmbr                             forest_client.corp_regn_nmbr%TYPE;
  gb_corp_regn_nmbr                            VARCHAR2(1);

  g_client_acronym                             forest_client.client_acronym%TYPE;
  gb_client_acronym                            VARCHAR2(1);

  g_wcb_firm_number                            forest_client.wcb_firm_number%TYPE;
  gb_wcb_firm_number                           VARCHAR2(1);

  g_ocg_supplier_nmbr                          forest_client.ocg_supplier_nmbr%TYPE;
  gb_ocg_supplier_nmbr                         VARCHAR2(1);

  g_client_comment                             forest_client.client_comment%TYPE;
  gb_client_comment                            VARCHAR2(1);

  g_add_timestamp                              forest_client.add_timestamp%TYPE;
  gb_add_timestamp                             VARCHAR2(1);

  g_add_userid                                 forest_client.add_userid%TYPE;
  gb_add_userid                                VARCHAR2(1);

  g_add_org_unit                               forest_client.add_org_unit%TYPE;
  gb_add_org_unit                              VARCHAR2(1);

  g_update_timestamp                           forest_client.update_timestamp%TYPE;
  gb_update_timestamp                          VARCHAR2(1);

  g_update_userid                              forest_client.update_userid%TYPE;
  gb_update_userid                             VARCHAR2(1);

  g_update_org_unit                            forest_client.update_org_unit%TYPE;
  gb_update_org_unit                           VARCHAR2(1);

  g_revision_count                             forest_client.revision_count%TYPE;
  gb_revision_count                            VARCHAR2(1);

  --update reasons
  --> status change reason
  g_ur_action_status                           client_action_reason_xref.client_update_action_code%TYPE;
  g_ur_reason_status                           client_action_reason_xref.client_update_reason_code%TYPE;
  --> name change reason
  g_ur_action_name                             client_action_reason_xref.client_update_action_code%TYPE;
  g_ur_reason_name                             client_action_reason_xref.client_update_reason_code%TYPE;
  --> id change reason
  g_ur_action_id                               client_action_reason_xref.client_update_action_code%TYPE;
  g_ur_reason_id                               client_action_reason_xref.client_update_reason_code%TYPE;

  r_previous                                   forest_client%ROWTYPE;


  --***START GETTERS

  --error raised?
  FUNCTION error_raised RETURN BOOLEAN
  IS
  BEGIN
    RETURN (g_error_message IS NOT NULL);
  END error_raised;

  --get error message
  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES
  IS
  BEGIN
    RETURN g_error_message;
  END get_error_message;

  --get client_number
  FUNCTION get_client_number RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_number;
  END get_client_number;

  --get client_name
  FUNCTION get_client_name RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_name;
  END get_client_name;

  --get legal_first_name
  FUNCTION get_legal_first_name RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_legal_first_name;
  END get_legal_first_name;

  --get legal_middle_name
  FUNCTION get_legal_middle_name RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_legal_middle_name;
  END get_legal_middle_name;

  --get client_status_code
  FUNCTION get_client_status_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_status_code;
  END get_client_status_code;

  --get client_type_code
  FUNCTION get_client_type_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_type_code;
  END get_client_type_code;

  --get birthdate
  FUNCTION get_birthdate RETURN DATE
  IS
  BEGIN
    RETURN g_birthdate;
  END get_birthdate;

  --get client_id_type_code
  FUNCTION get_client_id_type_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_id_type_code;
  END get_client_id_type_code;

  --get client_identification
  FUNCTION get_client_identification RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_identification;
  END get_client_identification;

  --get registry_company_type_code
  FUNCTION get_registry_company_type_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_registry_company_type_code;
  END get_registry_company_type_code;

  --get corp_regn_nmbr
  FUNCTION get_corp_regn_nmbr RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_corp_regn_nmbr;
  END get_corp_regn_nmbr;

  --get client_acronym
  FUNCTION get_client_acronym RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_acronym;
  END get_client_acronym;

  --get wcb_firm_number
  FUNCTION get_wcb_firm_number RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_wcb_firm_number;
  END get_wcb_firm_number;

  --get ocg_supplier_nmbr
  FUNCTION get_ocg_supplier_nmbr RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_ocg_supplier_nmbr;
  END get_ocg_supplier_nmbr;

  --get client_comment
  FUNCTION get_client_comment RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_comment;
  END get_client_comment;

  --get add_timestamp
  FUNCTION get_add_timestamp RETURN DATE
  IS
  BEGIN
    RETURN g_add_timestamp;
  END get_add_timestamp;

  --get add_userid
  FUNCTION get_add_userid RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_add_userid;
  END get_add_userid;

  --get add_org_unit
  FUNCTION get_add_org_unit RETURN NUMBER
  IS
  BEGIN
    RETURN g_add_org_unit;
  END get_add_org_unit;

  --get update_timestamp
  FUNCTION get_update_timestamp RETURN DATE
  IS
  BEGIN
    RETURN g_update_timestamp;
  END get_update_timestamp;

  --get update_userid
  FUNCTION get_update_userid RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_update_userid;
  END get_update_userid;

  --get update_org_unit
  FUNCTION get_update_org_unit RETURN NUMBER
  IS
  BEGIN
    RETURN g_update_org_unit;
  END get_update_org_unit;

  --get revision_count
  FUNCTION get_revision_count RETURN NUMBER
  IS
  BEGIN
    RETURN g_revision_count;
  END get_revision_count;

  --get update reason code for status change
  FUNCTION get_ur_reason_status RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_ur_reason_status;
  END get_ur_reason_status;
  --get update reason code for name change
  FUNCTION get_ur_reason_name RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_ur_reason_name;
  END get_ur_reason_name;
  --get update reason code for id change
  FUNCTION get_ur_reason_id RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_ur_reason_id;
  END get_ur_reason_id;

  --***END GETTERS

  --***START SETTERS

  --set client_number
  PROCEDURE set_client_number(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_number := p_value;
    gb_client_number := 'Y';
  END set_client_number;

  --set client_name
  PROCEDURE set_client_name(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_name := p_value;
    gb_client_name := 'Y';
  END set_client_name;

  --set legal_first_name
  PROCEDURE set_legal_first_name(p_value IN VARCHAR2)
  IS
  BEGIN
    g_legal_first_name := p_value;
    gb_legal_first_name := 'Y';
  END set_legal_first_name;

  --set legal_middle_name
  PROCEDURE set_legal_middle_name(p_value IN VARCHAR2)
  IS
  BEGIN
    g_legal_middle_name := p_value;
    gb_legal_middle_name := 'Y';
  END set_legal_middle_name;

  --set client_status_code
  PROCEDURE set_client_status_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_status_code := p_value;
    gb_client_status_code := 'Y';
  END set_client_status_code;

  --set client_type_code
  PROCEDURE set_client_type_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_type_code := p_value;
    gb_client_type_code := 'Y';
  END set_client_type_code;

  --set birthdate
  PROCEDURE set_birthdate(p_value IN DATE)
  IS
  BEGIN
    g_birthdate := p_value;
    gb_birthdate := 'Y';
  END set_birthdate;

  --set client_id_type_code
  PROCEDURE set_client_id_type_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_id_type_code := p_value;
    gb_client_id_type_code := 'Y';
  END set_client_id_type_code;

  --set client_identification
  PROCEDURE set_client_identification(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_identification := p_value;
    gb_client_identification := 'Y';
  END set_client_identification;

  --set registry_company_type_code
  PROCEDURE set_registry_company_type_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_registry_company_type_code := p_value;
    gb_registry_company_type_code := 'Y';
  END set_registry_company_type_code;

  --set corp_regn_nmbr
  PROCEDURE set_corp_regn_nmbr(p_value IN VARCHAR2)
  IS
  BEGIN
    g_corp_regn_nmbr := p_value;
    gb_corp_regn_nmbr := 'Y';
  END set_corp_regn_nmbr;

  --set client_acronym
  PROCEDURE set_client_acronym(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_acronym := p_value;
    gb_client_acronym := 'Y';
  END set_client_acronym;

  --set wcb_firm_number
  PROCEDURE set_wcb_firm_number(p_value IN VARCHAR2)
  IS
  BEGIN
    g_wcb_firm_number := p_value;
    gb_wcb_firm_number := 'Y';
  END set_wcb_firm_number;

  --set ocg_supplier_nmbr
  PROCEDURE set_ocg_supplier_nmbr(p_value IN VARCHAR2)
  IS
  BEGIN
    g_ocg_supplier_nmbr := p_value;
    gb_ocg_supplier_nmbr := 'Y';
  END set_ocg_supplier_nmbr;

  --set client_comment
  PROCEDURE set_client_comment(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_comment := p_value;
    gb_client_comment := 'Y';
  END set_client_comment;

  --set add_timestamp
  PROCEDURE set_add_timestamp(p_value IN DATE)
  IS
  BEGIN
    g_add_timestamp := p_value;
    gb_add_timestamp := 'Y';
  END set_add_timestamp;

  --set add_userid
  PROCEDURE set_add_userid(p_value IN VARCHAR2)
  IS
  BEGIN
    g_add_userid := p_value;
    gb_add_userid := 'Y';
  END set_add_userid;

  --set add_org_unit
  PROCEDURE set_add_org_unit(p_value IN NUMBER)
  IS
  BEGIN
    g_add_org_unit := p_value;
    gb_add_org_unit := 'Y';
  END set_add_org_unit;

  --set update_timestamp
  PROCEDURE set_update_timestamp(p_value IN DATE)
  IS
  BEGIN
    g_update_timestamp := p_value;
    gb_update_timestamp := 'Y';
  END set_update_timestamp;

  --set update_userid
  PROCEDURE set_update_userid(p_value IN VARCHAR2)
  IS
  BEGIN
    g_update_userid := p_value;
    gb_update_userid := 'Y';
  END set_update_userid;

  --set update_org_unit
  PROCEDURE set_update_org_unit(p_value IN NUMBER)
  IS
  BEGIN
    g_update_org_unit := p_value;
    gb_update_org_unit := 'Y';
  END set_update_org_unit;

  --set revision_count
  PROCEDURE set_revision_count(p_value IN NUMBER)
  IS
  BEGIN
    g_revision_count := p_value;
    gb_revision_count := 'Y';
  END set_revision_count;

  --***END SETTERS

  PROCEDURE get_previous
  IS
  BEGIN
    --If record already populated, don't requery
    IF r_previous.client_number IS NULL THEN
      SELECT *
        INTO r_previous
        FROM forest_client
       WHERE client_number = g_client_number;
    END IF;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        NULL;

  END get_previous;

  FUNCTION get_client_display_name
  (p_client_number              IN VARCHAR2 DEFAULT NULL)
  RETURN std_client_name_type%TYPE
  IS
  BEGIN
    IF p_client_number IS NOT NULL THEN
      RETURN client_get_client_name(p_client_number);
    ELSE
      RETURN sil_std_client_name(g_client_name,g_legal_first_name,g_legal_middle_name);
    END IF;

  END get_client_display_name;

  FUNCTION client_must_be_registered
  (p_client_number              IN VARCHAR2 DEFAULT NULL)
  RETURN BOOLEAN
  IS
    CURSOR c_cli
    IS
      SELECT client_type_code
        FROM forest_client
       WHERE client_number = p_client_number;
    r_cli                          c_cli%ROWTYPE;
  BEGIN

    IF p_client_number IS NOT NULL THEN
      OPEN c_cli;
      FETCH c_cli INTO r_cli;
      CLOSE c_cli;
    ELSE
      r_cli.client_type_code := g_client_type_code;
    END IF;

    RETURN (r_cli.client_type_code IN (C_CLIENT_TYPE_CORPORATION
                                      ,C_CLIENT_TYPE_ASSOCIATION
                                      ,C_CLIENT_TYPE_SOCIETY));

  END client_must_be_registered;

  FUNCTION client_is_individual
  (p_client_number              IN VARCHAR2 DEFAULT NULL)
  RETURN BOOLEAN
  IS
    CURSOR c_cli
    IS
      SELECT client_type_code
        FROM forest_client
       WHERE client_number = p_client_number;
    r_cli                          c_cli%ROWTYPE;
  BEGIN

    IF p_client_number IS NOT NULL THEN
      OPEN c_cli;
      FETCH c_cli INTO r_cli;
      CLOSE c_cli;
    ELSE
      r_cli.client_type_code := g_client_type_code;
    END IF;

    RETURN (r_cli.client_type_code = C_CLIENT_TYPE_INDIVIDUAL);

  END client_is_individual;

  PROCEDURE get
  IS
  BEGIN
    SELECT
           client_number
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
         , update_org_unit
         , revision_count
      INTO
           g_client_number
         , g_client_name
         , g_legal_first_name
         , g_legal_middle_name
         , g_client_status_code
         , g_client_type_code
         , g_birthdate
         , g_client_id_type_code
         , g_client_identification
         , g_registry_company_type_code
         , g_corp_regn_nmbr
         , g_client_acronym
         , g_wcb_firm_number
         , g_ocg_supplier_nmbr
         , g_client_comment
         , g_add_timestamp
         , g_add_userid
         , g_add_org_unit
         , g_update_timestamp
         , g_update_userid
         , g_update_org_unit
         , g_revision_count
      FROM forest_client
     WHERE client_number = g_client_number;

     EXCEPTION
       WHEN NO_DATA_FOUND THEN
         NULL;
  END get;


  PROCEDURE init
  (p_client_number          IN VARCHAR2 DEFAULT NULL)
  IS
    r_empty                         forest_client%ROWTYPE;
  BEGIN

    g_error_message := NULL;

    g_client_number := NULL;
    gb_client_number := 'N';

    g_client_name := NULL;
    gb_client_name := 'N';

    g_legal_first_name := NULL;
    gb_legal_first_name := 'N';

    g_legal_middle_name := NULL;
    gb_legal_middle_name := 'N';

    g_client_status_code := NULL;
    gb_client_status_code := 'N';

    g_client_type_code := NULL;
    gb_client_type_code := 'N';

    g_birthdate := NULL;
    gb_birthdate := 'N';

    g_client_id_type_code := NULL;
    gb_client_id_type_code := 'N';

    g_client_identification := NULL;
    gb_client_identification := 'N';

    g_registry_company_type_code := NULL;
    gb_registry_company_type_code := 'N';

    g_corp_regn_nmbr := NULL;
    gb_corp_regn_nmbr := 'N';

    g_client_acronym := NULL;
    gb_client_acronym := 'N';

    g_wcb_firm_number := NULL;
    gb_wcb_firm_number := 'N';

    g_ocg_supplier_nmbr := NULL;
    gb_ocg_supplier_nmbr := 'N';

    g_client_comment := NULL;
    gb_client_comment := 'N';

    g_add_timestamp := NULL;
    gb_add_timestamp := 'N';

    g_add_userid := NULL;
    gb_add_userid := 'N';

    g_add_org_unit := NULL;
    gb_add_org_unit := 'N';

    g_update_timestamp := NULL;
    gb_update_timestamp := 'N';

    g_update_userid := NULL;
    gb_update_userid := 'N';

    g_update_org_unit := NULL;
    gb_update_org_unit := 'N';

    g_ur_action_status := NULL;
    g_ur_reason_status := NULL;
    g_ur_action_name := NULL;
    g_ur_reason_name := NULL;
    g_ur_action_id := NULL;
    g_ur_reason_id := NULL;

    r_previous := r_empty;

    IF p_client_number IS NOT NULL THEN
      set_client_number(p_client_number);
      get;
    END IF;

  END init;


  FUNCTION formatted_client_number
  (p_client_number IN VARCHAR2)
  RETURN VARCHAR2
  IS
    v_client_number          forest_client.client_number%TYPE;
  BEGIN
    --strip spaces
    v_client_number := TRIM(p_client_number);

    --strip leading 0's
    v_client_number := TRIM(LEADING '0' FROM v_client_number);

    BEGIN
      v_client_number := TO_CHAR(TO_NUMBER(v_client_number,'99999999'),'FM00000000');
    EXCEPTION
      WHEN value_error THEN
        RAISE_APPLICATION_ERROR(-20200,'client_forest_client.formatted_client_number: client number is not numeric or not of correct structure');
    END;

    RETURN v_client_number;

  END formatted_client_number;
  /* OVERLOADED TO ACCEPT NUMERIC INPUT */
  FUNCTION formatted_client_number
  (p_client_number IN NUMBER)
  RETURN VARCHAR2
  IS
  BEGIN
    RETURN formatted_client_number(TO_CHAR(p_client_number));
  END formatted_client_number;

  FUNCTION is_individual
  (p_client_number IN VARCHAR2 DEFAULT NULL)
  RETURN BOOLEAN
  IS
    CURSOR c_client
    IS
      SELECT client_type_code
        FROM forest_client
       WHERE client_number = p_client_number;

    e_client_type               EXCEPTION;
    b_is_individual             BOOLEAN;
    v_client_type_code          forest_client.client_type_code%TYPE;
  BEGIN
    b_is_individual := FALSE;

    --use client number passed-in if present
    IF p_client_number IS NOT NULL THEN
      OPEN c_client;
      FETCH c_client INTO v_client_type_code;
      CLOSE c_client;
    ELSE
      v_client_type_code := g_client_type_code;
    END IF;

    IF v_client_type_code = C_CLIENT_TYPE_INDIVIDUAL THEN
      b_is_individual := TRUE;
    ELSIF v_client_type_code IS NULL THEN
      RAISE e_client_type;
    END IF;

    RETURN b_is_individual;

    EXCEPTION
      WHEN e_client_type THEN
        RAISE_APPLICATION_ERROR(-20200,'forest_client.is_individual: client type could not be derived');
        --Add RETURN to remove PLSQL warnings
        RETURN b_is_individual;

  END is_individual;


  PROCEDURE validate_reg_type
  IS
    CURSOR c_xref
    IS
      SELECT client_type_code
        FROM client_type_company_xref
       WHERE client_type_code = g_client_type_code
         AND registry_company_type_code = g_registry_company_type_code;
    r_xref                         c_xref%ROWTYPE;
  BEGIN

    IF g_client_type_code IS NOT NULL
    AND g_registry_company_type_code IS NOT NULL THEN
      OPEN c_xref;
      FETCH c_xref INTO r_xref;
      CLOSE c_xref;

      IF r_xref.client_type_code IS NULL THEN
        --Registration Type is not valid for the Client Type specified.
        CLIENT_UTILS.add_error(g_error_message
                             , NULL
                             , 'client.web.usr.database.reg.type.xref'
                             , NULL);
      END IF;
    END IF;

  END validate_reg_type;

  PROCEDURE validate_reg_id
  IS
    CURSOR c_dup
    IS
      SELECT client_number
        FROM forest_client
       WHERE corp_regn_nmbr = g_corp_regn_nmbr
       AND registry_company_type_code = g_registry_company_type_code;
    r_dup               c_dup%ROWTYPE;
  BEGIN

    IF g_corp_regn_nmbr IS NOT NULL THEN

      --dup check
      OPEN c_dup;
      FETCH c_dup INTO r_dup;
      CLOSE c_dup;
      IF r_dup.client_number != NVL(g_client_number,'~') THEN
        --reg id already exists
        CLIENT_UTILS.add_error(g_error_message
                             , 'corp_regn_nmbr'
                             , 'client.web.usr.database.dup.for.client'
                             , SIL_ERROR_PARAMS('Registration Type/Id', r_dup.client_number));
      END IF;
    END IF;

  END validate_reg_id;

  PROCEDURE validate_birthdate
  IS

  BEGIN

    IF g_birthdate IS NOT NULL THEN
      --cannot be in the future
      IF TRUNC(g_birthdate) > TRUNC(SYSDATE) THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'birthdate'
                             , 'client.web.usr.database.date.future'
                             , SIL_ERROR_PARAMS('Birth Date'));

      END IF;
    END IF;

    IF NOT client_is_individual THEN
      --birthdate not applicable
      IF g_birthdate IS NOT NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'birthdate'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('Birth Date', 'Organizations'));
      END IF;
    END IF;

  END validate_birthdate;


  PROCEDURE validate_name
  IS

  BEGIN

    IF NOT client_is_individual THEN
      --first name not applicable
      IF g_legal_first_name IS NOT NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'legal_first_name'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('First Name', 'Organizations'));
      END IF;
      --middle name not applicable
      IF g_legal_middle_name IS NOT NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'legal_middle_name'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('Middle Name', 'Organizations'));
      END IF;
    END IF;

  END validate_name;

  PROCEDURE validate_reg_info
  IS
  BEGIN
    validate_reg_id;

    validate_reg_type;

    --cross-validations
    IF client_is_individual THEN
      --reg info not allowed for individual
      IF g_registry_company_type_code IS NOT NULL THEN
        --reg type not applicable to individuals
        CLIENT_UTILS.add_error(g_error_message
                             , 'registry_company_type_code'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('Registration Type', 'Individuals'));
      END IF;
      IF g_corp_regn_nmbr IS NOT NULL THEN
        --reg id not applicable to individuals
        CLIENT_UTILS.add_error(g_error_message
                             , 'corp_regn_nmbr'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('Registration Id', 'Individuals'));
      END IF;
    ELSIF NOT client_is_individual THEN
      --if type specified, id must be specified
      IF g_registry_company_type_code IS NOT NULL
      AND g_corp_regn_nmbr IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'corp_regn_nmbr'
                             , 'sil.error.usr.date.compare.required'
                             , SIL_ERROR_PARAMS('Id', 'Registration Type'));
      END IF;
    END IF;

  END validate_reg_info;


  PROCEDURE validate_client_id
  IS
  BEGIN

    IF g_client_identification IS NOT NULL THEN
      IF g_client_identification =  C_CLIENT_ID_TYPE_BC_DRIVERS
      AND (LENGTH(g_client_identification) != 9
           OR TRIM(TRANSLATE(g_client_identification,'1234567890','          X')) IS NOT NULL) THEN
        --BCDL must be 9 numeric characters
        CLIENT_UTILS.add_error(g_error_message
                             , 'client_identification'
                             , 'client.web.usr.database.client.id.typelen'
                             , NULL);
      END IF;
    END IF;

  END validate_client_id;


  PROCEDURE validate_identification
  IS
    CURSOR c_dup
    IS
      SELECT client_number
        FROM forest_client
       WHERE client_id_type_code = g_client_id_type_code
         AND client_identification = g_client_identification;
    r_dup               c_dup%ROWTYPE;
    v_field             VARCHAR2(30);
  BEGIN

    validate_client_id;

    --dup check
    OPEN c_dup;
    FETCH c_dup INTO r_dup;
    CLOSE c_dup;
    IF r_dup.client_number != NVL(g_client_number,'~') THEN
      --client type/id already exists
        CLIENT_UTILS.add_error(g_error_message
                             , 'client_identification'
                             , 'client.web.usr.database.dup.for.client'
                             , SIL_ERROR_PARAMS('Client Identification Type/Id', r_dup.client_number));
    END IF;

    --cross-validations
    IF NOT client_is_individual THEN
      --client id type not allowed for organizations
      IF g_client_id_type_code IS NOT NULL THEN
        --client id type not applicable to organizations
        CLIENT_UTILS.add_error(g_error_message
                             , 'client_id_type_code'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('Client Id Type', 'Organizations'));
      END IF;
      IF g_client_identification IS NOT NULL THEN
        --client id not applicable to organizations
        CLIENT_UTILS.add_error(g_error_message
                             , 'client_identification'
                             , 'client.web.usr.database.not.applicable'
                             , SIL_ERROR_PARAMS('Client Id', 'Organizations'));
      END IF;
    ELSIF client_is_individual THEN
      --if type or id specified, both must be specified
      IF COALESCE(g_client_id_type_code,g_client_identification) IS NOT NULL
      AND (g_client_id_type_code IS NULL
          OR g_client_identification IS NULL) THEN
        IF g_client_id_type_code IS NULL THEN
          v_field := 'client_id_type_code';
        ELSE
          v_field := 'client_identification';
        END IF;

        CLIENT_UTILS.add_error(g_error_message
                              , v_field
                              , 'sil.error.usr.field.and'
                              , SIL_ERROR_PARAMS('Client Type', 'Id'));
      END IF;
    END IF;

  END validate_identification;

  PROCEDURE validate_status
  IS
    l_flag VARCHAR2(1);
  BEGIN

    --At this time all status validations are handled in the front-end as
    --they revolve around which roles can update certain statuses.
    --Add assignment to remove PLSQL warning for unreachable code
    l_flag := 'Y';

  END validate_status;


  PROCEDURE validate_acronym
  IS
    CURSOR c_acronym
    IS
      SELECT client_number
        FROM forest_client
       WHERE client_acronym = g_client_acronym;
    r_acronym                 c_acronym%ROWTYPE;

  BEGIN

    IF g_client_acronym IS NOT NULL THEN
      --acronym must be unique
      OPEN c_acronym;
      FETCH c_acronym INTO r_acronym;
      CLOSE c_acronym;
      IF r_acronym.client_number != NVL(g_client_number,'~') THEN
          CLIENT_UTILS.add_error(g_error_message
                         , 'client_acronym'
                         , 'client.web.usr.database.dup.for.client'
                         , SIL_ERROR_PARAMS('Acronym', r_acronym.client_number));
      END IF;
    END IF;

  END validate_acronym;


  PROCEDURE validate_mandatories
  IS
  BEGIN


    IF g_client_type_code IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                     , 'client_type_code'
                     , 'sil.error.usr.isrequired'
                     , SIL_ERROR_PARAMS('Client Type'));

    --Type=Individual
    ELSIF client_is_individual THEN
      IF g_client_name IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                     , 'client_name'
                     , 'sil.error.usr.isrequired'
                     , SIL_ERROR_PARAMS('Surname'));
      END IF;
      IF g_legal_first_name IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                     , 'legal_first_name'
                     , 'sil.error.usr.isrequired'
                     , SIL_ERROR_PARAMS('First Name'));
      END IF;

    --Type=other than Individual
    ELSE
      IF g_client_name IS NULL THEN
          CLIENT_UTILS.add_error(g_error_message
                     , 'client_name'
                     , 'sil.error.usr.isrequired'
                     , SIL_ERROR_PARAMS('Oganization Name'));
      END IF;
    END IF;

    IF g_client_status_code IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                     , 'client_status_code'
                     , 'sil.error.usr.isrequired'
                     , SIL_ERROR_PARAMS('Client Status'));
    END IF;

  END validate_mandatories;

  PROCEDURE process_update_reasons
  (p_ur_action_status       IN OUT VARCHAR2
  ,p_ur_reason_status       IN OUT VARCHAR2
  ,p_ur_action_name         IN OUT VARCHAR2
  ,p_ur_reason_name         IN OUT VARCHAR2
  ,p_ur_action_id           IN OUT VARCHAR2
  ,p_ur_reason_id           IN OUT VARCHAR2)
  IS
    v_client_update_action_code  client_update_reason.client_update_action_code%TYPE;
    e_reason_not_required        EXCEPTION;
  BEGIN
    --Only for updates
    IF g_client_number IS NOT NULL
    AND g_revision_count IS NOT NULL THEN

      get_previous;

      --set globals
      g_ur_action_status := p_ur_action_status;
      g_ur_reason_status := p_ur_reason_status;
      g_ur_action_name := p_ur_action_name;
      g_ur_reason_name := p_ur_reason_name;
      g_ur_action_id := p_ur_action_id;
      g_ur_reason_id := p_ur_reason_id;

      --Status changes
      v_client_update_action_code := NULL;
      IF gb_client_status_code = 'Y' THEN
        v_client_update_action_code := client_client_update_reason.check_status
                                       (--old
                                        r_previous.client_status_code
                                        --new
                                       ,g_client_status_code);
        IF v_client_update_action_code IS NOT NULL THEN
          g_ur_action_status := v_client_update_action_code;
          IF g_ur_reason_status IS NULL THEN
            --"Please provide an update reason for the following change: {0}"
            client_utils.add_error(g_error_message
                            , 'client_status_code'
                            , 'client.web.error.update.reason'
                            , sil_error_params(client_code_lists.get_client_update_action_desc(v_client_update_action_code)));
          END IF;
        ELSIF g_ur_reason_status IS NOT NULL THEN
          RAISE e_reason_not_required;
        END IF;
      END IF;

      --Name changes
      --Change to Select statement to remove PLSQL warning for unreachable code
      SELECT NULL INTO v_client_update_action_code FROM DUAL;

      IF gb_client_name = 'Y'
      OR gb_legal_first_name = 'Y'
      OR gb_legal_middle_name = 'Y' THEN
        v_client_update_action_code := client_client_update_reason.check_client_name
                                       (--old
                                        r_previous.client_name
                                       ,r_previous.legal_first_name
                                       ,r_previous.legal_middle_name
                                        --new
                                       ,g_client_name
                                       ,g_legal_first_name
                                       ,g_legal_middle_name);
        IF v_client_update_action_code IS NOT NULL THEN
          g_ur_action_name := v_client_update_action_code;
          IF g_ur_reason_name IS NULL THEN
            --"Please provide an update reason for the following change: {0}"
            client_utils.add_error(g_error_message
                            , 'client_name'
                            , 'client.web.error.update.reason'
                            , sil_error_params(client_code_lists.get_client_update_action_desc(v_client_update_action_code)));
          END IF;
        ELSIF g_ur_reason_name IS NOT NULL THEN
          RAISE e_reason_not_required;
        END IF;
      END IF;

      --ID changes
      --Change to Select statement to remove PLSQL warning for unreachable code
      SELECT NULL INTO v_client_update_action_code FROM DUAL;

      IF gb_client_identification = 'Y'
      OR gb_client_id_type_code = 'Y' THEN
        v_client_update_action_code := client_client_update_reason.check_id
                                       (--old
                                        r_previous.client_identification
                                       ,r_previous.client_id_type_code
                                        --new
                                       ,g_client_identification
                                       ,g_client_id_type_code);
        IF v_client_update_action_code IS NOT NULL THEN
          g_ur_action_id := v_client_update_action_code;
          IF g_ur_reason_id IS NULL THEN
            --"Please provide an update reason for the following change: {0}"
            client_utils.add_error(g_error_message
                            , 'client_identification'
                            , 'client.web.error.update.reason'
                            , sil_error_params(client_code_lists.get_client_update_action_desc(v_client_update_action_code)));
          END IF;
        ELSIF g_ur_reason_id IS NOT NULL THEN
          RAISE e_reason_not_required;
        END IF;
      END IF;

      --return globals
      p_ur_action_status := g_ur_action_status;
      p_ur_reason_status := g_ur_reason_status;
      p_ur_action_name := g_ur_action_name;
      p_ur_reason_name := g_ur_reason_name;
      p_ur_action_id := g_ur_action_id;
      p_ur_reason_id := g_ur_reason_id;
    END IF; --if updating

  EXCEPTION
    WHEN e_reason_not_required THEN
      RAISE_APPLICATION_ERROR(-20200,'Reason provided but no corresponding change noted.');

  END process_update_reasons;
 
  PROCEDURE validate
  IS
  BEGIN
    get_previous;

    validate_mandatories;

    validate_status;

    validate_name;

    validate_acronym;

    validate_birthdate;

    --reg id/type
    validate_reg_info;

    --client id/type
    validate_identification;

  END validate;


  PROCEDURE validate_remove
  IS
  BEGIN
    --Cannot delete clients - set status to DAC instead
    CLIENT_UTILS.add_error(g_error_message
                 , NULL
                 , 'client.web.usr.database.client.del'
                 , NULL);
  END validate_remove;

  FUNCTION reserve_client_number
  RETURN VARCHAR2
  IS
    v_client_number     forest_client.client_number%TYPE;
  BEGIN

    --update table to get next number and return it
    UPDATE max_client_nmbr
       SET client_number = formatted_client_number(TO_NUMBER(client_number) + 1)
     RETURNING client_number
          INTO v_client_number;

    RETURN v_client_number;

  END reserve_client_number;

  PROCEDURE add
  IS
  BEGIN
    IF g_client_number IS NULL THEN
      set_client_number(reserve_client_number);
    END IF;

    INSERT INTO forest_client
       ( client_number
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
       , update_org_unit
       , revision_count
       )
     VALUES
       ( g_client_number
       , g_client_name
       , g_legal_first_name
       , g_legal_middle_name
       , g_client_status_code
       , g_client_type_code
       , g_birthdate
       , g_client_id_type_code
       , g_client_identification
       , g_registry_company_type_code
       , g_corp_regn_nmbr
       , g_client_acronym
       , g_wcb_firm_number
       , g_ocg_supplier_nmbr
       , g_client_comment
       , g_add_timestamp
       , g_add_userid
       , g_add_org_unit
       , g_update_timestamp
       , g_update_userid
       , g_update_org_unit
       , g_revision_count
       );
  END add;

  PROCEDURE before_change_processing
  IS
  BEGIN
    get_previous;

    --If status changed to Deactivated, Expire all locations
    IF r_previous.client_status_code != 'DAC'
    AND g_client_status_code = 'DAC' THEN
      --expire locns
      client_client_location.expire_nonexpired_locns
      ( g_client_number
      , g_update_userid
      , g_update_timestamp
      , g_update_org_unit );

    END IF;

  END before_change_processing;

  PROCEDURE after_change_processing
  (p_deactivated_date       IN OUT DATE)
  IS
  BEGIN
    get_previous;

    --If status changed from Deactivated to anything else, unexpired those
    --locations that were expired when the status was set to DAC
    --(the assumption here is that the client cannot be updated when status is DAC
    -- so timestamps on the client record and the expired locns should line-up.)
    IF r_previous.client_status_code = 'DAC'
    AND g_client_status_code != 'DAC' THEN
      --unexpire locns
      client_client_location.unexpire_locns
      ( g_client_number
      , r_previous.update_timestamp -- date DAC took place
      , g_update_userid
      , g_update_timestamp
      , g_update_org_unit
      , p_deactivated_date );

    END IF;

  END after_change_processing;

  PROCEDURE change
  IS
       v_ts             date;
  BEGIN

       /* Get the most recent date in which this client was deactivated.
        * This will be passed to after_change_processing in case we're re-activating
        * the client.  This is used to determine which locations were expired at the
        * time of deactivation
        */
       SELECT MIN(update_timestamp) INTO v_ts
       FROM  for_cli_audit
       WHERE client_number = g_client_number
       AND   client_status_code = 'DAC'
       AND   update_timestamp >
          (SELECT MAX(fca.update_timestamp)
             FROM for_cli_audit fca
            WHERE fca.client_number = g_client_number
              AND fca.client_status_code != 'DAC');

    before_change_processing;

    UPDATE forest_client
       SET client_name = DECODE(gb_client_name,'Y',g_client_name,client_name)
         , legal_first_name = DECODE(gb_legal_first_name,'Y',g_legal_first_name,legal_first_name)
         , legal_middle_name = DECODE(gb_legal_middle_name,'Y',g_legal_middle_name,legal_middle_name)
         , client_status_code = DECODE(gb_client_status_code,'Y',g_client_status_code,client_status_code)
         , client_type_code = DECODE(gb_client_type_code,'Y',g_client_type_code,client_type_code)
         , birthdate = DECODE(gb_birthdate,'Y',g_birthdate,birthdate)
         , client_id_type_code = DECODE(gb_client_id_type_code,'Y',g_client_id_type_code,client_id_type_code)
         , client_identification = DECODE(gb_client_identification,'Y',g_client_identification,client_identification)
         , registry_company_type_code = DECODE(gb_registry_company_type_code,'Y',g_registry_company_type_code,registry_company_type_code)
         , corp_regn_nmbr = DECODE(gb_corp_regn_nmbr,'Y',g_corp_regn_nmbr,corp_regn_nmbr)
         , client_acronym = DECODE(gb_client_acronym,'Y',g_client_acronym,client_acronym)
         , wcb_firm_number = DECODE(gb_wcb_firm_number,'Y',g_wcb_firm_number,wcb_firm_number)
         , ocg_supplier_nmbr = DECODE(gb_ocg_supplier_nmbr,'Y',g_ocg_supplier_nmbr,ocg_supplier_nmbr)
         , client_comment = DECODE(gb_client_comment,'Y',g_client_comment,client_comment)
         , update_timestamp = g_update_timestamp
         , update_userid = g_update_userid
         , update_org_unit = DECODE(gb_update_org_unit,'Y',g_update_org_unit,update_org_unit)
         , revision_count = revision_count + 1
     WHERE client_number = g_client_number
       AND revision_count = g_revision_count
     RETURNING revision_count
          INTO g_revision_count;

    after_change_processing(v_ts);

  END change;


  PROCEDURE remove
  IS
  BEGIN
    --Cannot delete clients - set status to DAC instead
    RAISE_APPLICATION_ERROR(-20200,'client_forest_client.remove: Clients may not be deleted');
  END remove;

END client_forest_client;
CREATE OR REPLACE PACKAGE THE.client_003_client_locn AS

  PROCEDURE get
  ( p_client_number                IN OUT VARCHAR2
  , p_client_acronym               IN OUT VARCHAR2
  , p_client_name                  IN OUT VARCHAR2
  , p_client_status                IN OUT VARCHAR2
  , p_client_status_desc           IN OUT VARCHAR2
  , p_client_type                  IN OUT VARCHAR2
  , p_client_type_desc             IN OUT VARCHAR2
  , p_client_locn_code             IN OUT VARCHAR2
  , p_error_message                IN OUT SIL_ERROR_MESSAGES
  , p_loc_results                  IN OUT client_constants.ref_cur_t
  , p_loc_codes                    IN OUT client_constants.ref_cur_t);

  PROCEDURE expire(
    p_client_number                  IN OUT   VARCHAR2
  , p_client_locn_code               IN       VARCHAR2
  , p_client_locn_revision_count     IN OUT   VARCHAR2
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages);

  PROCEDURE unexpire(
    p_client_number                  IN OUT   VARCHAR2
  , p_client_locn_code               IN       VARCHAR2
  , p_client_locn_revision_count     IN OUT   VARCHAR2
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages);

  PROCEDURE save
  ( p_client_number                  IN       VARCHAR2
  , p_client_locn_code               IN OUT   VARCHAR2
  , p_client_locn_name               IN       VARCHAR2
  , p_address_1                      IN       VARCHAR2
  , p_address_2                      IN       VARCHAR2
  , p_address_3                      IN       VARCHAR2
  , p_city                           IN       VARCHAR2
  , p_province                       IN       VARCHAR2
  , p_postal_code                    IN       VARCHAR2
  , p_country                        IN       VARCHAR2
  , p_business_phone                 IN       VARCHAR2
  , p_home_phone                     IN       VARCHAR2
  , p_cell_phone                     IN       VARCHAR2
  , p_fax_number                     IN       VARCHAR2
  , p_email_address                  IN       VARCHAR2
  , p_locn_expired_ind               IN       VARCHAR2
  , p_returned_mail_ind              IN       VARCHAR2
  , p_trust_location_ind             IN       VARCHAR2
  , p_cli_locn_comment               IN       VARCHAR2
  , p_client_locn_revision_count     IN       VARCHAR2
  --update reasons
  -->address changes
  , p_ur_action_addr_code            IN OUT   VARCHAR2
  , p_ur_action_addr_desc            IN OUT   VARCHAR2
  , p_ur_reason_addr                 IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   SIL_ERROR_MESSAGES);

END client_003_client_locn;

CREATE OR REPLACE PACKAGE BODY THE.client_003_client_locn
AS

  PROCEDURE get(
    p_client_number                  IN OUT   VARCHAR2
  , p_client_acronym                 IN OUT   VARCHAR2
  , p_client_name                    IN OUT   VARCHAR2
  , p_client_status                  IN OUT   VARCHAR2
  , p_client_status_desc             IN OUT   VARCHAR2
  , p_client_type                    IN OUT   VARCHAR2
  , p_client_type_desc               IN OUT   VARCHAR2
  , p_client_locn_code               IN OUT   VARCHAR2
  , p_error_message                  IN OUT   SIL_ERROR_MESSAGES
  , p_loc_results                    IN OUT   client_constants.ref_cur_t
  , p_loc_codes                      IN OUT   client_constants.ref_cur_t)
  IS
  BEGIN
    client_tombstone.get(p_client_number
                        ,p_client_acronym
                        ,p_client_name
                        ,p_client_status
                        ,p_client_status_desc
                        ,p_client_type
                        ,p_client_type_desc
                        ,p_error_message);

    IF p_error_message IS NULL THEN

        --Client locn level data
        OPEN p_loc_results FOR
          SELECT cl.client_number
               , cl.client_locn_code
               , cl.client_locn_name
               , cl.address_1
               , cl.address_2
               , cl.address_3
               , cl.city
               , cl.province
               , cl.postal_code
               , cl.country
               , cl.business_phone
               , cl.home_phone
               , cl.cell_phone
               , cl.fax_number
               , cl.email_address
               , cl.locn_expired_ind
               , NVL2(cl.returned_mail_date,'Y','N') returned_mail_ind
               , cl.trust_location_ind
               , cl.cli_locn_comment
               , rc.related_client_ind
               --TO DO
               , '' client_update_action_code
               , '' client_update_reason_code
               , cl.revision_count
            FROM client_location cl
               --one row cartesian
               , (SELECT DECODE(COUNT(1), 0, 'No', 'Yes') related_client_ind
                    FROM related_client r
                   WHERE r.client_number = p_client_number
                      OR r.related_clnt_nmbr = p_client_number) rc
           WHERE cl.client_number = p_client_number
             AND cl.client_locn_code = p_client_locn_code;

        --Client locn code level data
        OPEN p_loc_codes FOR
          SELECT cl.client_locn_code
            FROM client_location cl
           WHERE cl.client_number = p_client_number
           ORDER BY cl.client_locn_code;
    END IF; --error message is null

  END get;


  PROCEDURE expire(
  --client info
    p_client_number                  IN OUT   VARCHAR2
  , p_client_locn_code               IN       VARCHAR2
  , p_client_locn_revision_count     IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages)
  IS
  BEGIN
    --setup location
    client_client_location.init(p_client_number, p_client_locn_code);
    client_client_location.set_locn_expired_ind('Y');
    client_client_location.set_update_timestamp(SYSDATE);
    client_client_location.set_update_userid(p_update_userid);
    client_client_location.set_update_org_unit(TO_NUMBER(p_user_org_unit_no));
    client_client_location.set_revision_count(TO_NUMBER(p_client_locn_revision_count));
    client_client_location.validate;

    IF NOT client_client_location.error_raised THEN
      client_client_location.change;
    END IF;

    --accumulate messages from client location
    CLIENT_UTILS.append_arrays(p_error_message
                               , client_client_location.get_error_message);
  END expire;

  PROCEDURE unexpire(
  --client info
    p_client_number                  IN OUT   VARCHAR2
  , p_client_locn_code               IN       VARCHAR2
  , p_client_locn_revision_count     IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   sil_error_messages)
  IS
  BEGIN
    --setup location
    client_client_location.init(p_client_number, p_client_locn_code);
    client_client_location.set_locn_expired_ind('N');
    client_client_location.set_update_timestamp(SYSDATE);
    client_client_location.set_update_userid(p_update_userid);
    client_client_location.set_update_org_unit(TO_NUMBER(p_user_org_unit_no));
    client_client_location.set_revision_count(TO_NUMBER(p_client_locn_revision_count));
    client_client_location.validate;

    IF NOT client_client_location.error_raised THEN
      client_client_location.change;
    END IF;

    --accumulate messages from client location
    CLIENT_UTILS.append_arrays(p_error_message
                               , client_client_location.get_error_message);
  END unexpire;

  PROCEDURE SAVE(
  --client info
    p_client_number                  IN       VARCHAR2
  , p_client_locn_code               IN OUT   VARCHAR2
  , p_client_locn_name               IN       VARCHAR2
  , p_address_1                      IN       VARCHAR2
  , p_address_2                      IN       VARCHAR2
  , p_address_3                      IN       VARCHAR2
  , p_city                           IN       VARCHAR2
  , p_province                       IN       VARCHAR2
  , p_postal_code                    IN       VARCHAR2
  , p_country                        IN       VARCHAR2
  , p_business_phone                 IN       VARCHAR2
  , p_home_phone                     IN       VARCHAR2
  , p_cell_phone                     IN       VARCHAR2
  , p_fax_number                     IN       VARCHAR2
  , p_email_address                  IN       VARCHAR2
  , p_locn_expired_ind               IN       VARCHAR2
  , p_returned_mail_ind              IN       VARCHAR2
  , p_trust_location_ind             IN       VARCHAR2
  , p_cli_locn_comment               IN       VARCHAR2
  , p_client_locn_revision_count     IN       VARCHAR2
  --update reasons
  -->address changes
  , p_ur_action_addr_code            IN OUT   VARCHAR2
  , p_ur_action_addr_desc            IN OUT   VARCHAR2
  , p_ur_reason_addr                 IN OUT   VARCHAR2
  --user info
  , p_update_userid                  IN       VARCHAR2
  , p_user_org_unit_no               IN       VARCHAR2
  , p_error_message                  IN OUT   SIL_ERROR_MESSAGES)
  IS
  BEGIN
    --setup location
    client_client_location.init(p_client_number, p_client_locn_code);
    client_client_location.set_client_locn_name(p_client_locn_name);
    client_client_location.set_address_1(p_address_1);
    client_client_location.set_address_2(p_address_2);
    client_client_location.set_address_3(p_address_3);
    client_client_location.set_city(p_city);
    client_client_location.set_province(p_province);
    client_client_location.set_postal_code(p_postal_code);
    client_client_location.set_country(p_country);
    client_client_location.set_business_phone(p_business_phone);
    client_client_location.set_home_phone(p_home_phone);
    client_client_location.set_cell_phone(p_cell_phone);
    client_client_location.set_fax_number(p_fax_number);
    client_client_location.set_email_address(p_email_address);
    IF p_trust_location_ind = 'Y' THEN
        --RULE: set expired_ind to 'Y' if trust_ind is set to 'Y'
        client_client_location.set_locn_expired_ind('Y');
       ELSE
           client_client_location.set_locn_expired_ind(p_locn_expired_ind);
       END IF;
    IF p_returned_mail_ind = 'Y' THEN
      --RULE: set returned mail date to today's date if indicator set and not currently set
      IF client_client_location.get_returned_mail_date IS NULL THEN --not set
        client_client_location.set_returned_mail_date(SYSDATE);
      END IF;
    ELSIF p_returned_mail_ind = 'N' THEN
      client_client_location.set_returned_mail_date(NULL);
    END IF;
    client_client_location.set_trust_location_ind(p_trust_location_ind);
    client_client_location.set_cli_locn_comment(p_cli_locn_comment);
    client_client_location.set_update_timestamp(SYSDATE);
    client_client_location.set_update_userid(p_update_userid);
    client_client_location.set_update_org_unit(TO_NUMBER(p_user_org_unit_no));
    client_client_location.set_revision_count(TO_NUMBER(p_client_locn_revision_count));
    client_client_location.validate;

    --Check for any actions that require update reason
    IF NOT client_client_location.error_raised THEN
      --If there are any reasons to process, error raised will be TRUE
      --and messages are obtained via .get_error_message()
      client_client_location.process_update_reasons
       (p_ur_action_addr_code
       ,p_ur_reason_addr);
    END IF;

    IF NOT client_client_location.error_raised THEN
      IF p_client_locn_revision_count IS NULL THEN
        client_client_location.set_add_userid(p_update_userid);
        client_client_location.set_add_timestamp(SYSDATE);
        client_client_location.set_add_org_unit(p_user_org_unit_no);
        client_client_location.set_revision_count(1);
        client_client_location.add;
      ELSE
        client_client_location.change;
      END IF;
    END IF;

    IF NOT client_client_location.error_raised THEN
      --get locn code
      p_client_locn_code := client_client_location.get_client_locn_code;
    END IF;

    --get descriptions for any update actions that have been set
    p_ur_action_addr_desc := client_code_lists.get_client_update_action_desc(p_ur_action_addr_code);

    --accumulate messages from client location
    client_utils.append_arrays(p_error_message
                               , client_client_location.get_error_message);

  END save;

END CLIENT_003_CLIENT_LOCN;
CREATE OR REPLACE TYPE THE."SIL_ERROR_PARAMS" AS VARRAY(50) OF VARCHAR2(500);

CREATE OR REPLACE TYPE THE."SIL_ERROR_MESSAGE" AS OBJECT 
(
  DATABASE_FIELD                 VARCHAR2(50),
  MESSAGE                        VARCHAR2(250),
  PARAMS                         SIL_ERROR_PARAMS,
  WARNING_FLAG                   VARCHAR2(1)
);

CREATE OR REPLACE TYPE THE."SIL_ERROR_MESSAGES" AS VARRAY(100) OF SIL_ERROR_MESSAGE;

CREATE OR REPLACE PACKAGE THE.client_constants AS

  TYPE REF_CUR_T IS REF CURSOR;

  C_AUDIT_INSERT                       CONSTANT VARCHAR2(3) := 'INS';
  C_AUDIT_UPDATE                       CONSTANT VARCHAR2(3) := 'UPD';
  C_AUDIT_DELETE                       CONSTANT VARCHAR2(3) := 'DEL';

END client_constants;

CREATE OR REPLACE TYPE THE."CLIENT_GENERIC_STRING_VARRAY" AS VARRAY(4000) OF VARCHAR2(255);
CREATE OR REPLACE TYPE THE."CLIENT_REORG_RESULTS" AS VARRAY(1000) OF client_reorg_result;

CREATE OR REPLACE TYPE THE."CLIENT_REORG_RESULT" AS OBJECT
(
  TABLE_NAME                     VARCHAR2(30)
, ROWS_UPDATED                   NUMBER(20)
);

CREATE SEQUENCE client_dba_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;


CREATE SEQUENCE client_contact_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
