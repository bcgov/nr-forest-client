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
