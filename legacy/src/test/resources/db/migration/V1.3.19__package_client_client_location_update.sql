CREATE OR REPLACE PACKAGE BODY THE.client_client_location AS

  --member vars

  g_error_message                              SIL_ERROR_MESSAGES;

  g_client_number                              client_location.client_number%TYPE;
  gb_client_number                             VARCHAR2(1);

  g_client_locn_code                           client_location.client_locn_code%TYPE;
  gb_client_locn_code                          VARCHAR2(1);

  g_client_locn_name                           client_location.client_locn_name%TYPE;
  gb_client_locn_name                          VARCHAR2(1);

  g_hdbs_company_code                          client_location.hdbs_company_code%TYPE;
  gb_hdbs_company_code                         VARCHAR2(1);

  g_address_1                                  client_location.address_1%TYPE;
  gb_address_1                                 VARCHAR2(1);

  g_address_2                                  client_location.address_2%TYPE;
  gb_address_2                                 VARCHAR2(1);

  g_address_3                                  client_location.address_3%TYPE;
  gb_address_3                                 VARCHAR2(1);

  g_city                                       client_location.city%TYPE;
  gb_city                                      VARCHAR2(1);

  g_province                                   client_location.province%TYPE;
  gb_province                                  VARCHAR2(1);

  g_postal_code                                client_location.postal_code%TYPE;
  gb_postal_code                               VARCHAR2(1);

  g_country                                    client_location.country%TYPE;
  gb_country                                   VARCHAR2(1);

  g_business_phone                             client_location.business_phone%TYPE;
  gb_business_phone                            VARCHAR2(1);

  g_home_phone                                 client_location.home_phone%TYPE;
  gb_home_phone                                VARCHAR2(1);

  g_cell_phone                                 client_location.cell_phone%TYPE;
  gb_cell_phone                                VARCHAR2(1);

  g_fax_number                                 client_location.fax_number%TYPE;
  gb_fax_number                                VARCHAR2(1);

  g_email_address                              client_location.email_address%TYPE;
  gb_email_address                             VARCHAR2(1);

  g_locn_expired_ind                           client_location.locn_expired_ind%TYPE;
  gb_locn_expired_ind                          VARCHAR2(1);

  g_returned_mail_date                         client_location.returned_mail_date%TYPE;
  gb_returned_mail_date                        VARCHAR2(1);

  g_trust_location_ind                         client_location.trust_location_ind%TYPE;
  gb_trust_location_ind                        VARCHAR2(1);

  g_cli_locn_comment                           client_location.cli_locn_comment%TYPE;
  gb_cli_locn_comment                          VARCHAR2(1);

  g_update_timestamp                           client_location.update_timestamp%TYPE;
  gb_update_timestamp                          VARCHAR2(1);

  g_update_userid                              client_location.update_userid%TYPE;
  gb_update_userid                             VARCHAR2(1);

  g_update_org_unit                            client_location.update_org_unit%TYPE;
  gb_update_org_unit                           VARCHAR2(1);

  g_add_timestamp                              client_location.add_timestamp%TYPE;
  gb_add_timestamp                             VARCHAR2(1);

  g_add_userid                                 client_location.add_userid%TYPE;
  gb_add_userid                                VARCHAR2(1);

  g_add_org_unit                               client_location.add_org_unit%TYPE;
  gb_add_org_unit                              VARCHAR2(1);

  g_revision_count                             client_location.revision_count%TYPE;
  gb_revision_count                            VARCHAR2(1);

  --update reasons
  --> address change reason
  g_ur_action_addr                             client_action_reason_xref.client_update_action_code%TYPE;
  g_ur_reason_addr                             client_action_reason_xref.client_update_reason_code%TYPE;

  r_previous                                   client_location%ROWTYPE;

  C_MAX_CLIENT_LOCN_CODE              CONSTANT NUMBER := 99;

/******************************************************************************
    Procedure:  formatted_locn_code

    Purpose:    Format a numeric location code

******************************************************************************/
  FUNCTION formatted_locn_code
  (p_client_locn_code     IN NUMBER)
  RETURN VARCHAR2
  IS
  BEGIN

    RETURN TO_CHAR(p_client_locn_code,'FM00');

  END formatted_locn_code;

/******************************************************************************
    Procedure:  get_previous

    Purpose:    Get current client location info if not already retrieved

******************************************************************************/
  PROCEDURE get_previous
  IS
  BEGIN
    IF r_previous.client_number IS NULL THEN
      SELECT *
        INTO r_previous
        FROM client_location
       WHERE client_number = g_client_number
         AND client_locn_code = g_client_locn_code;
    END IF;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      NULL;
  END get_previous;


/******************************************************************************
    Procedure:  get

    Purpose:    SELECT one row from CLIENT_LOCATION

******************************************************************************/
  PROCEDURE get
  IS
  BEGIN
    SELECT
           client_number
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
         , add_org_unit
         , revision_count
      INTO
           g_client_number
         , g_client_locn_code
         , g_client_locn_name
         , g_hdbs_company_code
         , g_address_1
         , g_address_2
         , g_address_3
         , g_city
         , g_province
         , g_postal_code
         , g_country
         , g_business_phone
         , g_home_phone
         , g_cell_phone
         , g_fax_number
         , g_email_address
         , g_locn_expired_ind
         , g_returned_mail_date
         , g_trust_location_ind
         , g_cli_locn_comment
         , g_update_timestamp
         , g_update_userid
         , g_update_org_unit
         , g_add_timestamp
         , g_add_userid
         , g_add_org_unit
         , g_revision_count
      FROM client_location
     WHERE client_number = g_client_number
       AND client_locn_code = g_client_locn_code;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        NULL;

  END get;

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

  --get client_locn_code
  FUNCTION get_client_locn_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_locn_code;
  END get_client_locn_code;

  --get client_locn_name
  FUNCTION get_client_locn_name RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_locn_name;
  END get_client_locn_name;

  --get hdbs_company_code
  FUNCTION get_hdbs_company_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_hdbs_company_code;
  END get_hdbs_company_code;

  --get address_1
  FUNCTION get_address_1 RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_address_1;
  END get_address_1;

  --get address_2
  FUNCTION get_address_2 RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_address_2;
  END get_address_2;

  --get address_3
  FUNCTION get_address_3 RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_address_3;
  END get_address_3;

  --get city
  FUNCTION get_city RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_city;
  END get_city;

  --get province
  FUNCTION get_province RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_province;
  END get_province;

  --get postal_code
  FUNCTION get_postal_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_postal_code;
  END get_postal_code;

  --get country
  FUNCTION get_country RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_country;
  END get_country;

  --get business_phone
  FUNCTION get_business_phone RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_business_phone;
  END get_business_phone;

  --get home_phone
  FUNCTION get_home_phone RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_home_phone;
  END get_home_phone;

  --get cell_phone
  FUNCTION get_cell_phone RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_cell_phone;
  END get_cell_phone;

  --get fax_number
  FUNCTION get_fax_number RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_fax_number;
  END get_fax_number;

  --get email_address
  FUNCTION get_email_address RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_email_address;
  END get_email_address;

  --get locn_expired_ind
  FUNCTION get_locn_expired_ind RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_locn_expired_ind;
  END get_locn_expired_ind;

  --get returned_mail_date
  FUNCTION get_returned_mail_date RETURN DATE
  IS
  BEGIN
    RETURN g_returned_mail_date;
  END get_returned_mail_date;

  --get trust_location_ind
  FUNCTION get_trust_location_ind RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_trust_location_ind;
  END get_trust_location_ind;

  --get cli_locn_comment
  FUNCTION get_cli_locn_comment RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_cli_locn_comment;
  END get_cli_locn_comment;

  --get update_timestamp
  FUNCTION get_update_timestamp RETURN DATE
  IS
  BEGIN
    RETURN NVL(g_update_timestamp,SYSDATE);
  END get_update_timestamp;

  --get update_userid
  FUNCTION get_update_userid RETURN VARCHAR2
  IS
  BEGIN
    RETURN NVL(g_update_userid,'TEMPUSER');
  END get_update_userid;

  --get update_org_unit
  FUNCTION get_update_org_unit RETURN NUMBER
  IS
  BEGIN
    RETURN NVL(g_update_org_unit,70);
  END get_update_org_unit;

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

  --get revision_count
  FUNCTION get_revision_count RETURN NUMBER
  IS
  BEGIN
    RETURN g_revision_count;
  END get_revision_count;

  --get update reason code for address change
  FUNCTION get_ur_reason_addr RETURN VARCHAR2
  IS
  BEGIN
    RETURN NVL(g_ur_reason_addr,'UND');
  END get_ur_reason_addr;
  --***END GETTERS

  --***START SETTERS

  --set client_number
  PROCEDURE set_client_number(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_number := p_value;
    gb_client_number := 'Y';
  END set_client_number;

  --set client_locn_code
  PROCEDURE set_client_locn_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_locn_code := p_value;
    gb_client_locn_code := 'Y';
  END set_client_locn_code;

  --set client_locn_name
  PROCEDURE set_client_locn_name(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_locn_name := p_value;
    gb_client_locn_name := 'Y';
  END set_client_locn_name;

  --set hdbs_company_code
  PROCEDURE set_hdbs_company_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_hdbs_company_code := p_value;
    gb_hdbs_company_code := 'Y';
  END set_hdbs_company_code;

  --set address_1
  PROCEDURE set_address_1(p_value IN VARCHAR2)
  IS
  BEGIN
    g_address_1 := p_value;
    gb_address_1 := 'Y';
  END set_address_1;

  --set address_2
  PROCEDURE set_address_2(p_value IN VARCHAR2)
  IS
  BEGIN
    g_address_2 := p_value;
    gb_address_2 := 'Y';
  END set_address_2;

  --set address_3
  PROCEDURE set_address_3(p_value IN VARCHAR2)
  IS
  BEGIN
    g_address_3 := p_value;
    gb_address_3 := 'Y';
  END set_address_3;

  --set city
  PROCEDURE set_city(p_value IN VARCHAR2)
  IS
  BEGIN
    g_city := p_value;
    gb_city := 'Y';
  END set_city;

  --set province
  PROCEDURE set_province(p_value IN VARCHAR2)
  IS
  BEGIN
    g_province := p_value;
    gb_province := 'Y';
  END set_province;

  --set postal_code
  PROCEDURE set_postal_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_postal_code := p_value;
    gb_postal_code := 'Y';
  END set_postal_code;

  --set country
  PROCEDURE set_country(p_value IN VARCHAR2)
  IS
  BEGIN
    g_country := p_value;
    gb_country := 'Y';
  END set_country;

  --set business_phone
  PROCEDURE set_business_phone(p_value IN VARCHAR2)
  IS
  BEGIN
    g_business_phone := p_value;
    gb_business_phone := 'Y';
  END set_business_phone;

  --set home_phone
  PROCEDURE set_home_phone(p_value IN VARCHAR2)
  IS
  BEGIN
    g_home_phone := p_value;
    gb_home_phone := 'Y';
  END set_home_phone;

  --set cell_phone
  PROCEDURE set_cell_phone(p_value IN VARCHAR2)
  IS
  BEGIN
    g_cell_phone := p_value;
    gb_cell_phone := 'Y';
  END set_cell_phone;

  --set fax_number
  PROCEDURE set_fax_number(p_value IN VARCHAR2)
  IS
  BEGIN
    g_fax_number := p_value;
    gb_fax_number := 'Y';
  END set_fax_number;

  --set email_address
  PROCEDURE set_email_address(p_value IN VARCHAR2)
  IS
  BEGIN
    g_email_address := p_value;
    gb_email_address := 'Y';
  END set_email_address;

  --set locn_expired_ind
  PROCEDURE set_locn_expired_ind(p_value IN VARCHAR2)
  IS
  BEGIN
    g_locn_expired_ind := p_value;
    gb_locn_expired_ind := 'Y';
  END set_locn_expired_ind;

  --set returned_mail_date
  PROCEDURE set_returned_mail_date(p_value IN DATE)
  IS
  BEGIN
    g_returned_mail_date := p_value;
    gb_returned_mail_date := 'Y';
  END set_returned_mail_date;

  --set trust_location_ind
  PROCEDURE set_trust_location_ind(p_value IN VARCHAR2)
  IS
  BEGIN
    g_trust_location_ind := p_value;
    gb_trust_location_ind := 'Y';
  END set_trust_location_ind;

  --set cli_locn_comment
  PROCEDURE set_cli_locn_comment(p_value IN VARCHAR2)
  IS
  BEGIN
    g_cli_locn_comment := p_value;
    gb_cli_locn_comment := 'Y';
  END set_cli_locn_comment;

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

  --set revision_count
  PROCEDURE set_revision_count(p_value IN NUMBER)
  IS
  BEGIN
    g_revision_count := p_value;
    gb_revision_count := 'Y';
  END set_revision_count;
  --***END SETTERS

/******************************************************************************
    Procedure:  init

    Purpose:    Initialize member variables

******************************************************************************/
  PROCEDURE init
  ( p_client_number             IN VARCHAR2 DEFAULT NULL
  , p_client_locn_code          IN VARCHAR2 DEFAULT NULL)
  IS
    r_previous_empty      client_location%ROWTYPE;
  BEGIN

    g_error_message := NULL;

    g_client_number := NULL;
    gb_client_number := 'N';

    g_client_locn_code := NULL;
    gb_client_locn_code := 'N';

    g_client_locn_name := NULL;
    gb_client_locn_name := 'N';

    g_hdbs_company_code := NULL;
    gb_hdbs_company_code := 'N';

    g_address_1 := NULL;
    gb_address_1 := 'N';

    g_address_2 := NULL;
    gb_address_2 := 'N';

    g_address_3 := NULL;
    gb_address_3 := 'N';

    g_city := NULL;
    gb_city := 'N';

    g_province := NULL;
    gb_province := 'N';

    g_postal_code := NULL;
    gb_postal_code := 'N';

    g_country := NULL;
    gb_country := 'N';

    g_business_phone := NULL;
    gb_business_phone := 'N';

    g_home_phone := NULL;
    gb_home_phone := 'N';

    g_cell_phone := NULL;
    gb_cell_phone := 'N';

    g_fax_number := NULL;
    gb_fax_number := 'N';

    g_email_address := NULL;
    gb_email_address := 'N';

    g_locn_expired_ind := NULL;
    gb_locn_expired_ind := 'N';

    g_returned_mail_date := NULL;
    gb_returned_mail_date := 'N';

    g_trust_location_ind := NULL;
    gb_trust_location_ind := 'N';

    g_cli_locn_comment := NULL;
    gb_cli_locn_comment := 'N';

    g_update_timestamp := NULL;
    gb_update_timestamp := 'N';

    g_update_userid := NULL;
    gb_update_userid := 'N';

    g_update_org_unit := NULL;
    gb_update_org_unit := 'N';

    g_add_timestamp := NULL;
    gb_add_timestamp := 'N';

    g_add_userid := NULL;
    gb_add_userid := 'N';

    g_add_org_unit := NULL;
    gb_add_org_unit := 'N';

    g_revision_count := NULL;
    gb_revision_count := 'N';

    g_ur_action_addr := NULL;
    g_ur_reason_addr := NULL;

    r_previous := r_previous_empty;

    IF p_client_locn_code IS NOT NULL
    OR p_client_number IS NOT NULL THEN
      set_client_number(p_client_number);
      set_client_locn_code(p_client_locn_code);
      get;
    END IF;
  END init;

/******************************************************************************
    Procedure:  validate_trust

    Purpose:    Validate trust location indicator

******************************************************************************/
  PROCEDURE validate_trust
  IS
  BEGIN

    IF g_trust_location_ind = 'Y' THEN

      IF g_locn_expired_ind != 'Y' THEN
        --Trust Location must be Expired
        client_utils.add_error(g_error_message
                             , 'trust_location_ind'
                             , 'client.web.usr.database.trust.exp');
      END IF;

      IF g_client_locn_code = '00' THEN
        --cannot set 00 location as trust location
        client_utils.add_error(g_error_message
                             , 'trust_location_ind'
                             , 'client.web.usr.database.trust.00');
      END IF;
    END IF;

  END validate_trust;

/******************************************************************************
    Procedure:  validate_locn_expired_ind

    Purpose:    Validate location expired ind

******************************************************************************/
  PROCEDURE validate_locn_expired_ind
  IS
    CURSOR c_00
    IS
      SELECT l.locn_expired_ind
           , c.client_status_code
        FROM client_location l
           , forest_client c
       WHERE l.client_number = g_client_number
         AND l.client_locn_code = '00'
         AND c.client_number = l.client_number;
    r_00 c_00%ROWTYPE;
  BEGIN

    IF g_locn_expired_ind = 'Y' THEN
      IF r_previous.locn_expired_ind = 'N' AND g_client_locn_code = '00' THEN
        --cannot expire 00 location
        client_utils.add_error(g_error_message
                             , 'locn_expired_ind'
                             , 'client.web.usr.database.exp.00');
      END IF;
    ELSE --exp=N
      --unexpiring
      IF r_previous.locn_expired_ind = 'Y' THEN
        --cannot unexpire if 00 locn is expired
        OPEN c_00;
        FETCH c_00 INTO r_00;
        CLOSE c_00;
        IF r_00.locn_expired_ind = 'Y' THEN
          --cannot unexpire when 00 location is expired
          client_utils.add_error(g_error_message
                               , 'locn_expired_ind'
                               , 'client.web.usr.database.unexp.00');
        END IF;
        IF r_00.client_status_code = 'DAC' THEN
          --cannot expire 00 location
          client_utils.add_error(g_error_message
                               , 'locn_expired_ind'
                               , 'client.web.usr.database.unexp.dac');
        END IF;
      END IF;
    END IF;

  END validate_locn_expired_ind;


/******************************************************************************
    Procedure:  phone_number_is_valid

    Purpose:    Apply phone number mask and return TRUE if passed telephone
                number is valid, otherwise return FALSE.

******************************************************************************/
  FUNCTION phone_number_is_valid
  (p_phone  IN VARCHAR2)
  RETURN BOOLEAN
  IS
  BEGIN

    IF p_phone IS NULL THEN
      RETURN NULL;
    ELSIF TRANSLATE(p_phone,'1234567890','NNNNNNNNNN') != 'NNNNNNNNNN' THEN
      RETURN FALSE;
    ELSE
      RETURN TRUE;
    END IF;

  END phone_number_is_valid;


/******************************************************************************
    Procedure:  validate_telephone

    Purpose:    Validate telephone numbers

******************************************************************************/
  PROCEDURE validate_telephone
  IS
  BEGIN
    IF NOT phone_number_is_valid(g_business_phone) THEN
        client_utils.add_error(g_error_message
                         , 'business_phone'
                         , 'client.web.usr.database.phone'
                         , sil_error_params('Business Phone'));
    END IF;
    IF NOT phone_number_is_valid(g_home_phone) THEN
        client_utils.add_error(g_error_message
                         , 'home_phone'
                         , 'client.web.usr.database.phone'
                         , sil_error_params('Residence Phone'));
    END IF;
    IF NOT phone_number_is_valid(g_cell_phone) THEN
        client_utils.add_error(g_error_message
                         , 'cell_phone'
                         , 'client.web.usr.database.phone'
                         , sil_error_params('Cell Phone'));
    END IF;
    IF NOT phone_number_is_valid(g_fax_number) THEN
        client_utils.add_error(g_error_message
                         , 'fax_number'
                         , 'client.web.usr.database.phone'
                         , sil_error_params('Fax Number'));
    END IF;

  END validate_telephone;


/******************************************************************************
    Procedure:  validate_postal_code

    Purpose:    Apply postal code masks for known countries

******************************************************************************/
  PROCEDURE validate_postal_code
  IS
  BEGIN

    IF g_country = 'CANADA'
    AND TRANSLATE(g_postal_code,'1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ','NNNNNNNNNNAAAAAAAAAAAAAAAAAAAAAAAAAA') != 'ANANAN' THEN
        client_utils.add_error(g_error_message
                         , 'country'
                         , 'client.web.usr.database.postal.code.mask'
                         , sil_error_params(g_country));
    ELSIF g_country = 'U.S.A.'
    AND TRANSLATE(g_postal_code,'1234567890','NNNNNNNNNN') NOT IN ('NNNNN','NNNNN-NNNN') THEN
        client_utils.add_error(g_error_message
                         , 'country'
                         , 'client.web.usr.database.postal.code.mask'
                         , sil_error_params(g_country));
    END IF;

  END validate_postal_code;


/******************************************************************************
    Procedure:  validate_address

    Purpose:    Validate city, prov/state, country combinations

******************************************************************************/
  PROCEDURE validate_address
  IS
    CURSOR c_country
    IS
      SELECT c.country_name
           , COUNT(mps.province_state_code) prov_count
           , COUNT(DECODE(mps.province_state_code,g_province,1,NULL)) matches_found
        FROM mailing_country c
           , mailing_province_state mps
       WHERE c.country_name = g_country
         AND mps.country_name(+) = c.country_name
       GROUP BY c.country_name;
    r_country                 c_country%ROWTYPE;

    CURSOR c_city
    IS
      SELECT COUNT(1) city_count
           , COUNT(DECODE(city_name,g_city,1,NULL)) matches_found
        FROM mailing_city
       WHERE country_name = g_country
         AND province_state_name = g_province
       GROUP BY country_name
              , province_state_name;
    r_city                    c_city%ROWTYPE;

  BEGIN
    IF g_country IS NOT NULL THEN
      OPEN c_country;
      FETCH c_country INTO r_country;
      CLOSE c_country;

      --if country not found
      IF r_country.country_name IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'country'
                         , 'client.web.usr.database.country.nrf');
      ELSE
        --COUNTRY IS VALID
        -->if any provinces/states exist for the country then the province specified must exist (else freeform)
        IF r_country.prov_count > 0 THEN
          IF r_country.matches_found = 0 THEN
            -->province/state could not be found for the country
            client_utils.add_error(g_error_message
                         , 'province'
                         , 'client.web.usr.database.state.nrf');
          ELSE
            -->if any cities exist for the province/state and country then city specified must exist (else freeform)
            OPEN c_city;
            FETCH c_city INTO r_city;
            CLOSE c_city;

            IF r_city.city_count > 0
            AND r_city.matches_found = 0 THEN
              --city could not be found for the province/state
                client_utils.add_error(g_error_message
                         , 'city'
                         , 'client.web.usr.database.city.nrf');
            END IF;
          END IF;
        END IF;

        validate_postal_code;
      END IF;


    END IF;


  END validate_address;


/******************************************************************************
    Procedure:  validate_mandatories

    Purpose:    Validate optionality

******************************************************************************/
  PROCEDURE validate_mandatories
  IS
  BEGIN
/* Because Client and Location validations are combined on CLIENT02,
   this message was confusing users when client validations failed and a
   number was not generated and passed-on.

    IF g_client_number IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'client_number'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('Client Number'));
    END IF;
*/

/* Location Code generated so don't validate here */

/* Will default to a space if not provided so don't validate here
    IF g_hdbs_company_code IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'hdbs_company_code'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('HDBS Company Code'));
    END IF;
*/
    IF g_address_1 IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'address_1'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('Address'));
    END IF;
    IF g_city IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'city'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('City'));
    END IF;
    IF g_country IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'country'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('Country'));
    END IF;

/* Indicators will default
    IF g_locn_expired_ind IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'locn_expired_ind'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('Location Expired Ind'));
    END IF;
    IF g_trust_location_ind IS NULL THEN
        client_utils.add_error(g_error_message
                         , 'trust_location_ind'
                         , 'sil.error.usr.isrequired'
                         , sil_error_params('Trust Location Ind'));
    END IF;
*/
  END validate_mandatories;


/******************************************************************************
    Procedure:  process_update_reasons

    Purpose:    Certain changes require a reason to be specified

******************************************************************************/
  PROCEDURE process_update_reasons
  (p_ur_action_addr         IN OUT VARCHAR2
  ,p_ur_reason_addr        IN OUT VARCHAR2)
  IS
    v_client_update_action_code  client_update_reason.client_update_action_code%TYPE;
    e_reason_not_required        EXCEPTION;
  BEGIN
    --Only for updates
    IF g_client_number IS NOT NULL
    AND g_client_locn_code IS NOT NULL
    AND g_revision_count IS NOT NULL THEN
      get_previous;

      --set globals
      g_ur_action_addr := p_ur_action_addr;
      g_ur_reason_addr := p_ur_reason_addr;

      --Address changes
      v_client_update_action_code := NULL;
      IF gb_address_1 = 'Y'
      OR gb_address_2 = 'Y'
      OR gb_address_3 = 'Y'
      OR gb_city = 'Y'
      OR gb_province = 'Y'
      OR gb_country = 'Y' THEN
        v_client_update_action_code := client_client_update_reason.check_address
                                      (--old
                                       r_previous.address_1
                                      ,r_previous.address_2
                                      ,r_previous.address_3
                                      ,r_previous.city
                                      ,r_previous.province
                                      ,r_previous.postal_code
                                      ,r_previous.country
                                       --new
                                      ,g_address_1
                                      ,g_address_2
                                      ,g_address_3
                                      ,g_city
                                      ,g_province
                                      ,g_postal_code
                                      ,g_country);
        IF v_client_update_action_code IS NOT NULL THEN
          g_ur_action_addr := v_client_update_action_code;
          IF g_ur_reason_addr IS NULL THEN
            --"Please provide an update reason for the following change: {0}"
            client_utils.add_error(g_error_message
                            , 'address_1'
                            , 'client.web.error.update.reason'
                            , sil_error_params(client_code_lists.get_client_update_action_desc(v_client_update_action_code)));
          END IF;
        ELSIF g_ur_reason_addr IS NOT NULL THEN
          RAISE e_reason_not_required;
        END IF;
      END IF;

      --return globals
      p_ur_action_addr := g_ur_action_addr;
      p_ur_reason_addr := g_ur_reason_addr;

    END IF; --if updating

  EXCEPTION
    WHEN e_reason_not_required THEN
      RAISE_APPLICATION_ERROR(-20200,'Reason provided but no corresponding change noted.');

  END process_update_reasons;

/******************************************************************************
    Procedure:  validate

    Purpose:    Column validators

******************************************************************************/
  PROCEDURE validate
  IS
  BEGIN
    get_previous;

    validate_mandatories;

    validate_address;

    validate_telephone;

    validate_locn_expired_ind;

    validate_trust;

  END validate;


/******************************************************************************
    Procedure:  validate_remove

    Purpose:    DELETE validations - check for child records, etc.

******************************************************************************/
  PROCEDURE validate_remove
  IS
  BEGIN
    --Cannot delete client locations - expire it instead
    RAISE_APPLICATION_ERROR(-20200,'client_client_location.validate_remove: Client Locations may not be deleted');
  END validate_remove;


/******************************************************************************
    Procedure:  get_next_location

    Purpose:    Derive next client location code for insert

******************************************************************************/
  FUNCTION get_next_location
  RETURN VARCHAR2
  IS
    CURSOR c_next
    IS
      SELECT TO_NUMBER(MAX(client_locn_code)) + 1 client_locn_code
        FROM client_location
       WHERE client_number = g_client_number;
    r_next                c_next%ROWTYPE;

  BEGIN

    OPEN c_next;
    FETCH c_next INTO r_next;
    CLOSE c_next;

    IF r_next.client_locn_code IS NULL THEN
      --00 location
      r_next.client_locn_code := 0;
    ELSIF r_next.client_locn_code > C_MAX_CLIENT_LOCN_CODE THEN
      --will not fit in 2 chars - cannot generate
      RAISE_APPLICATION_ERROR(-20200,'Cannot generate next Client Location Code - max has been reached.');
    END IF;

    RETURN formatted_locn_code(r_next.client_locn_code);

  END get_next_location;


/******************************************************************************
    Procedure:  add

    Purpose:    INSERT one row into CLIENT_LOCATION

******************************************************************************/
  PROCEDURE add
  IS
    v_client_locn_code              client_location.client_locn_code%TYPE;
  BEGIN
    v_client_locn_code := get_next_location;

    --So as not to impact HBS, set HDBS Company Code to a space if not provided
    --as it would have been in CLI
    g_hdbs_company_code := NVL(g_hdbs_company_code,' ');

    --Default indicators if not provided
    g_locn_expired_ind := NVL(g_locn_expired_ind,'N');
    g_trust_location_ind := NVL(g_trust_location_ind,'N');


    INSERT INTO client_location
       ( client_number
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
       , add_org_unit
       , revision_count
       )
     VALUES
       ( g_client_number
       , v_client_locn_code
       , g_client_locn_name
       , g_hdbs_company_code
       , g_address_1
       , g_address_2
       , g_address_3
       , g_city
       , g_province
       , g_postal_code
       , g_country
       , g_business_phone
       , g_home_phone
       , g_cell_phone
       , g_fax_number
       , g_email_address
       , g_locn_expired_ind
       , g_returned_mail_date
       , g_trust_location_ind
       , g_cli_locn_comment
       , g_update_timestamp
       , g_update_userid
       , g_update_org_unit
       , g_add_timestamp
       , g_add_userid
       , g_add_org_unit
       , g_revision_count
      )
      RETURNING client_number
              , client_locn_code
           INTO g_client_number
              , g_client_locn_code;
  END add;


/******************************************************************************
    Procedure:  change

    Purpose:    UPDATE one row in CLIENT_LOCATION

******************************************************************************/
  PROCEDURE change
  IS
  BEGIN
    UPDATE client_location
       SET client_locn_name = DECODE(gb_client_locn_name,'Y',g_client_locn_name,client_locn_name)
--      IGNORING SO AS NOT TO IMPACT HBS
--      , hdbs_company_code = DECODE(gb_hdbs_company_code,'Y',g_hdbs_company_code,hdbs_company_code)
         , address_1 = DECODE(gb_address_1,'Y',g_address_1,address_1)
         , address_2 = DECODE(gb_address_2,'Y',g_address_2,address_2)
         , address_3 = DECODE(gb_address_3,'Y',g_address_3,address_3)
         , city = DECODE(gb_city,'Y',g_city,city)
         , province = DECODE(gb_province,'Y',g_province,province)
         , postal_code = DECODE(gb_postal_code,'Y',g_postal_code,postal_code)
         , country = DECODE(gb_country,'Y',g_country,country)
         , business_phone = DECODE(gb_business_phone,'Y',g_business_phone,business_phone)
         , home_phone = DECODE(gb_home_phone,'Y',g_home_phone,home_phone)
         , cell_phone = DECODE(gb_cell_phone,'Y',g_cell_phone,cell_phone)
         , fax_number = DECODE(gb_fax_number,'Y',g_fax_number,fax_number)
         , email_address = DECODE(gb_email_address,'Y',g_email_address,email_address)
         , locn_expired_ind = DECODE(gb_locn_expired_ind,'Y',g_locn_expired_ind,locn_expired_ind)
         , returned_mail_date = DECODE(gb_returned_mail_date,'Y',g_returned_mail_date,returned_mail_date)
         , trust_location_ind = DECODE(gb_trust_location_ind,'Y',g_trust_location_ind,trust_location_ind)
         , cli_locn_comment = DECODE(gb_cli_locn_comment,'Y',g_cli_locn_comment,cli_locn_comment)
         , update_timestamp = g_update_timestamp
         , update_userid = g_update_userid
         , update_org_unit = DECODE(gb_update_org_unit,'Y',g_update_org_unit,update_org_unit)
         , revision_count = revision_count + 1
     WHERE client_number = g_client_number
       AND client_locn_code = g_client_locn_code
       AND revision_count = g_revision_count
     RETURNING revision_count
          INTO g_revision_count;
  END change;


/******************************************************************************
    Procedure:  remove

    Purpose:    DELETE one row from CLIENT_LOCATION

******************************************************************************/
  PROCEDURE remove
  IS
  BEGIN
    --Cannot delete client locations - expire it instead
    RAISE_APPLICATION_ERROR(-20200,'client_client_location.remove: Client Locations may not be deleted');
  END remove;

--*START STATIC METHODS

/******************************************************************************
    Procedure:  expire_nonexpired_locns

    Purpose:    Set locn_expired_ind for all nonexpired locations of p_client_number

******************************************************************************/
  PROCEDURE expire_nonexpired_locns
  ( p_client_number       IN VARCHAR2
  , p_update_userid       IN VARCHAR2
  , p_update_timestamp    IN DATE
  , p_update_org_unit_no  IN NUMBER)
  IS
  BEGIN
    UPDATE client_location
       SET locn_expired_ind = 'Y'
         , update_timestamp = p_update_timestamp
         , update_userid = p_update_userid
         , update_org_unit = p_update_org_unit_no
         , revision_count = revision_count + 1
     WHERE client_number = p_client_number
       AND locn_expired_ind = 'N';

  END expire_nonexpired_locns;

/******************************************************************************
    Procedure:  unexpire_locns

    Purpose:    Set locn_expired_ind for all locations expired on a certain
                date.
                *The assumption is that the client cannot be updated
                once it is set to DAC so the update_timestamp on the client
                record should match the update_timestamp on the expired
                locations.
                * This assumption is no longer valid since deactivated clients can
                now be updated.  We have to look to the audit table to get the
                appropriate date in which the client was deactivated, and compare
                it with the location expiry date.

******************************************************************************/
  PROCEDURE unexpire_locns
  ( p_client_number       IN VARCHAR2
  , p_date_deactivated    IN DATE
  , p_update_userid       IN VARCHAR2
  , p_update_timestamp    IN DATE
  , p_update_org_unit_no  IN NUMBER
  , p_deactivated_date    IN DATE)
  IS
  BEGIN

    UPDATE client_location
       SET locn_expired_ind = 'N'
         , update_timestamp = p_update_timestamp
         , update_userid = p_update_userid
         , update_org_unit = p_update_org_unit_no
         , revision_count = revision_count + 1
     WHERE client_number = p_client_number
       AND locn_expired_ind = 'Y'
       AND client_locn_code IN
       (SELECT client_locn_code
          FROM cli_locn_audit
         WHERE client_number = p_client_number
           AND locn_expired_ind = 'Y'
           AND update_timestamp = p_deactivated_date);

  END unexpire_locns;

--*END STATIC METHODS

END client_client_location;