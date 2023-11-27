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