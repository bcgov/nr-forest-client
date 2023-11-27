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