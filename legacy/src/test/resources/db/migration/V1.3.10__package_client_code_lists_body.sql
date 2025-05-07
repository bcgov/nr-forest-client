CREATE OR REPLACE PACKAGE BODY THE.client_code_lists
AS
  /*******************************************************************************
   * Procedure:  GET_REORG_TYPE_CODES                                            *
   *                                                                             *
   * Purpose:    To retrieve the list of client reorganization types.            *
   *******************************************************************************/
  PROCEDURE get_reorg_type_codes(
    p_reorg_type_code                IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_reorg_type_code
     FOR
        SELECT DISTINCT client_reorg_type_code reorg_type
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
        FROM client_reorg_type_code
        ORDER BY reorg_type;
  END get_reorg_type_codes;

  /*******************************************************************************
   * Procedure:  GET_REORG_STATUS_CODES                                          *
   *                                                                             *
   * Purpose:    To retrieve the list of client reorganization status codes.     *
   *******************************************************************************/
  PROCEDURE get_reorg_status_codes(
    p_reorg_status_code              IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_reorg_status_code
     FOR
        SELECT DISTINCT client_reorg_status_code reorg_status
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
        FROM client_reorg_status_code
        ORDER BY reorg_status;
  END get_reorg_status_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_LOCATION_CODES                                       *
   *                                                                             *
   * Purpose:    To retrieve the list of client location codes expired values    *
   *             will contain *s at the front and end of it.                     *
   *******************************************************************************/
  PROCEDURE get_client_location_codes(
     p_client_number             IN OUT   VARCHAR2
   , p_client_location_code              IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_client_location_code
     FOR
        SELECT DISTINCT client_locn_code
             , DECODE(locn_expired_ind, 'Y', '* ', '') ||
               client_locn_code||' - '||client_locn_name ||
               DECODE(locn_expired_ind, 'Y', ' *', '') client_locn_name
          FROM client_location
         WHERE client_number = p_client_number
         ORDER BY client_locn_code;
  END get_client_location_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_STATUS_CODES                                         *
   *                                                                             *
   * Purpose:    To retrieve the list of client status codes.                    *
   *******************************************************************************/
  PROCEDURE get_client_status_codes(
    p_client_status_code             IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_client_status_code
     FOR
       SELECT DISTINCT
              client_status_code
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
         FROM client_status_code
        ORDER BY description;
  END get_client_status_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_TYPE_CODES                                           *
   *                                                                             *
   * Purpose:    To retrieve the list of client type codes.                      *
   *******************************************************************************/
  PROCEDURE get_client_type_codes(
    p_client_type_code               IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_client_type_code
     FOR
       SELECT DISTINCT
              client_type_code
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
         FROM client_type_code
        ORDER BY description;
  END get_client_type_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_RELATIONSHIP_CODES                                   *
   *                                                                             *
   * Purpose:    To retrieve the list of client relationship codes.              *
   *******************************************************************************/
  PROCEDURE get_client_relationship_codes(
    p_client_relationship_code       IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_client_relationship_code
     FOR
       SELECT DISTINCT
              client_relationship_code
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
         FROM client_relationship_code
        ORDER BY description;
  END get_client_relationship_codes;

  /*******************************************************************************
   * Procedure:  GET_BUSINESS_CONTACT_CODES                                      *
   *                                                                             *
   * Purpose:    To retrieve the list of business contact codes.                 *
   *******************************************************************************/
  PROCEDURE get_business_contact_codes(
    p_business_contact_code          IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_business_contact_code
     FOR
       SELECT DISTINCT
              business_contact_code
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
         FROM business_contact_code
        ORDER BY description;
  END get_business_contact_codes;

  /*******************************************************************************
   * Procedure:  GET_REGISTRY_COMPANY_CODES                                      *
   *                                                                             *
   * Purpose:    To retrieve the list of registry company type codes.            *
   *******************************************************************************/
  PROCEDURE get_reg_company_type_codes(
    p_client_type_code               IN       VARCHAR2
   ,p_reg_company_type_code          IN OUT   client_code_lists_cur)
  IS
  BEGIN

    IF p_client_type_code IS NULL THEN
      OPEN p_reg_company_type_code
        FOR
         SELECT DISTINCT
                r.registry_company_type_code code
              , r.description description
              , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
              , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
           FROM registry_company_type_code r
          ORDER BY r.registry_company_type_code;
       ELSE
      OPEN p_reg_company_type_code
        FOR
         SELECT DISTINCT
                r.registry_company_type_code code
              , r.description description
              , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
              , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
           FROM client_type_company_xref x
              , registry_company_type_code r
          WHERE x.client_type_code = p_client_type_code
            AND r.registry_company_type_code = x.registry_company_type_code
          ORDER BY r.registry_company_type_code;
       END IF;
  END get_reg_company_type_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_UPDATE_REASON_CODES                                  *
   *                                                                             *
   * Purpose:    To retrieve the list of update reason codes for a given action  *
   *******************************************************************************/
  PROCEDURE get_client_update_reason_codes(
    p_client_update_action_code      IN       VARCHAR2
   ,p_client_type_code               IN       VARCHAR2
   ,p_client_update_reason_code      IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_client_update_reason_code
     FOR
       SELECT c.client_update_reason_code code
            , c.description description
            , sil_convert_to_char(c.effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(c.expiry_date, 'YYYY-MM-DD') expiry_date
         FROM client_action_reason_xref x
            , client_update_reason_code c
        WHERE x.client_update_action_code = p_client_update_action_code
          AND x.client_type_code = p_client_type_code
          AND c.client_update_reason_code = x.client_update_reason_code
        ORDER BY c.description;
  END get_client_update_reason_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_ID_TYPE_CODES                                        *
   *                                                                             *
   * Purpose:    To retrieve the list of client id type codes.                   *
   *******************************************************************************/
  PROCEDURE get_client_id_type_codes(
    p_client_id_type_code            IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_client_id_type_code
     FOR
       SELECT DISTINCT
              client_id_type_code
            , description
            , sil_convert_to_char(effective_date, 'YYYY-MM-DD') effective_date
            , sil_convert_to_char(expiry_date, 'YYYY-MM-DD') expiry_date
         FROM client_id_type_code
        ORDER BY description;
  END get_client_id_type_codes;

  /*******************************************************************************
   * Procedure:  GET_CLIENT_UPDATE_ACTION_DESC                                   *
   *                                                                             *
   * Purpose:    To retrieve the list of client id type codes.                   *
   *******************************************************************************/
  FUNCTION get_client_update_action_desc(
    p_client_update_action_code            IN   VARCHAR2)
  RETURN VARCHAR2
  IS
    CURSOR c_desc
    IS
      SELECT description
        FROM client_update_action_code
       WHERE client_update_action_code = p_client_update_action_code;
    r_desc        c_desc%ROWTYPE;
  BEGIN
    IF p_client_update_action_code IS NOT NULL THEN
      OPEN c_desc;
      FETCH c_desc INTO r_desc;
      CLOSE c_desc;
    END IF;
    RETURN r_desc.description;
  END get_client_update_action_desc;

  --ADDRESS
  -->Country
  PROCEDURE get_country(
    p_countries                      IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_countries
    FOR
      SELECT country_code code
           , country_name description
           , '0001-01-01' effective_date
           , '9999-12-31' expiry_date
        FROM mailing_country
       ORDER BY country_name;
  END get_country;
  -->Prov/State
  PROCEDURE get_prov(
    p_country                        IN       VARCHAR2
  , p_provinces                      IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_provinces
    FOR
      SELECT NVL(province_state_code,province_state_name) code
           , province_state_name||DECODE(province_state_code,NULL,NULL,'('||province_state_code||')') description
           , '0001-01-01' effective_date
           , '9999-12-31' expiry_date
        FROM mailing_province_state
       WHERE country_name = p_country
       ORDER BY province_state_name;
  END get_prov;
  -->City
  PROCEDURE get_city(
    p_country                        IN       VARCHAR2
  , p_province                       IN       VARCHAR2
  , p_cities                         IN OUT   client_code_lists_cur)
  IS
  BEGIN
    OPEN p_cities
    FOR
        SELECT city_name code
             , city_name description
             , '0001-01-01' effective_date
             , '9999-12-31' expiry_date
          FROM mailing_province_state p
             , mailing_city c
         WHERE c.country_name = p_country
           AND p.country_name = c.country_name
           AND p.province_state_name = c.province_state_name
           --because province could be the code or the name (if there is no code)
           AND (c.province_state_name = p_province
               OR p.province_state_code = p_province)
         ORDER BY city_name;
  END get_city;

 /******************************************************************************
    Procedure:  get_valid_relationships

    Purpose:    Get a list of relationships that are valid for a particular
                primary client type.
  ******************************************************************************/
  PROCEDURE get_valid_relationships (
     p_client_type_code IN VARCHAR2,
     p_relationships    IN OUT client_code_lists_cur)
  IS
  BEGIN
     OPEN p_relationships FOR
        SELECT distinct
               crc.client_relationship_code code,
               crc.description,
               crc.effective_date,
               crc.expiry_date
          FROM client_type_code ctc,
               client_relationship_type_xref crtx,
               client_relationship_code crc
         WHERE ctc.client_type_code = crtx.primary_client_type_code
           AND crc.client_relationship_code = crtx.client_relationship_code
           AND UPPER (ctc.client_type_code) = UPPER (p_client_type_code)
           ORDER BY crc.description ASC;

  END get_valid_relationships;


END client_code_lists;