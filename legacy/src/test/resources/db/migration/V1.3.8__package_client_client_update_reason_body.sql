CREATE OR REPLACE PACKAGE BODY THE.client_client_update_reason AS

  --member vars

  g_error_message                              SIL_ERROR_MESSAGES;

  g_client_update_reason_id                    client_update_reason.client_update_reason_id%TYPE;
  gb_client_update_reason_id                   VARCHAR2(1);

  g_client_update_action_code                  client_update_reason.client_update_action_code%TYPE;
  gb_client_update_action_code                 VARCHAR2(1);

  g_client_update_reason_code                  client_update_reason.client_update_reason_code%TYPE;
  gb_client_update_reason_code                 VARCHAR2(1);

  g_client_type_code                           client_update_reason.client_type_code%TYPE;
  gb_client_type_code                          VARCHAR2(1);

  g_forest_client_audit_id                     client_update_reason.forest_client_audit_id%TYPE;
  gb_forest_client_audit_id                    VARCHAR2(1);

  g_update_timestamp                           client_update_reason.update_timestamp%TYPE;
  gb_update_timestamp                          VARCHAR2(1);

  g_update_userid                              client_update_reason.update_userid%TYPE;
  gb_update_userid                             VARCHAR2(1);

  g_add_timestamp                              client_update_reason.add_timestamp%TYPE;
  gb_add_timestamp                             VARCHAR2(1);

  g_add_userid                                 client_update_reason.add_userid%TYPE;
  gb_add_userid                                VARCHAR2(1);

/******************************************************************************
    Procedure:  get

    Purpose:    SELECT one row from CLIENT_UPDATE_REASON

******************************************************************************/
  PROCEDURE get
  IS
  BEGIN
    SELECT
           client_update_reason_id
         , client_update_action_code
         , client_update_reason_code
         , client_type_code
         , forest_client_audit_id
         , update_timestamp
         , update_userid
         , add_timestamp
         , add_userid
      INTO
           g_client_update_reason_id
         , g_client_update_action_code
         , g_client_update_reason_code
         , g_client_type_code
         , g_forest_client_audit_id
         , g_update_timestamp
         , g_update_userid
         , g_add_timestamp
         , g_add_userid
      FROM client_update_reason
     WHERE client_update_reason_id = g_client_update_reason_id;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        NULL;
  END get;


/******************************************************************************
    Procedure:  init

    Purpose:    Initialize member variables

******************************************************************************/
  PROCEDURE init
  IS

  BEGIN

    g_error_message := NULL;

    g_client_update_reason_id := NULL;
    gb_client_update_reason_id := 'N';

    g_client_update_action_code := NULL;
    gb_client_update_action_code := 'N';

    g_client_update_reason_code := NULL;
    gb_client_update_reason_code := 'N';

    g_client_type_code := NULL;
    gb_client_type_code := 'N';

    g_forest_client_audit_id := NULL;
    gb_forest_client_audit_id := 'N';

    g_update_timestamp := NULL;
    gb_update_timestamp := 'N';

    g_update_userid := NULL;
    gb_update_userid := 'N';

    g_add_timestamp := NULL;
    gb_add_timestamp := 'N';

    g_add_userid := NULL;
    gb_add_userid := 'N';

  END init;

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

  --get client_update_reason_id
  FUNCTION get_client_update_reason_id RETURN NUMBER
  IS
  BEGIN
    RETURN g_client_update_reason_id;
  END get_client_update_reason_id;

  --get client_update_action_code
  FUNCTION get_client_update_action_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_update_action_code;
  END get_client_update_action_code;

  --get client_update_reason_code
  FUNCTION get_client_update_reason_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_update_reason_code;
  END get_client_update_reason_code;

  --get client_type_code
  FUNCTION get_client_type_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_client_type_code;
  END get_client_type_code;

  --get forest_client_audit_id
  FUNCTION get_forest_client_audit_id RETURN NUMBER
  IS
  BEGIN
    RETURN g_forest_client_audit_id;
  END get_forest_client_audit_id;

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
  --***END GETTERS

  --***START SETTERS

  --set client_update_reason_id
  PROCEDURE set_client_update_reason_id(p_value IN NUMBER)
  IS
  BEGIN
    g_client_update_reason_id := p_value;
    gb_client_update_reason_id := 'Y';
  END set_client_update_reason_id;

  --set client_update_action_code
  PROCEDURE set_client_update_action_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_update_action_code := p_value;
    gb_client_update_action_code := 'Y';
  END set_client_update_action_code;

  --set client_update_reason_code
  PROCEDURE set_client_update_reason_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_update_reason_code := p_value;
    gb_client_update_reason_code := 'Y';
  END set_client_update_reason_code;

  --set client_type_code
  PROCEDURE set_client_type_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_type_code := p_value;
    gb_client_type_code := 'Y';
  END set_client_type_code;

  --set forest_client_audit_id
  PROCEDURE set_forest_client_audit_id(p_value IN NUMBER)
  IS
  BEGIN
    g_forest_client_audit_id := p_value;
    gb_forest_client_audit_id := 'Y';
  END set_forest_client_audit_id;

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
  --***END SETTERS

/******************************************************************************
    Procedure:  validate_mandatories

    Purpose:    Validate optionality

******************************************************************************/
  PROCEDURE validate_mandatories
  IS
  BEGIN
    IF g_client_update_action_code IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                         , NULL
                         , 'sil.error.usr.isrequired'
                         , SIL_ERROR_PARAMS('Client Update Action'));
    END IF;
    IF g_client_update_reason_code IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                         , NULL
                         , 'sil.error.usr.isrequired'
                         , SIL_ERROR_PARAMS('Client Update Reason'));
    END IF;
    IF g_client_type_code IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                         , NULL
                         , 'sil.error.usr.isrequired'
                         , SIL_ERROR_PARAMS('Client Type'));
    END IF;
    IF g_forest_client_audit_id IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                         , NULL
                         , 'sil.error.usr.isrequired'
                         , SIL_ERROR_PARAMS('Client Audit ID'));
    END IF;
  END validate_mandatories;

/******************************************************************************
    Procedure:  validate_action_reason_xref

    Purpose:    Validate action/reason combination

******************************************************************************/
  PROCEDURE validate_action_reason_xref
  IS
    CURSOR c_xref
    IS
      SELECT client_update_action_code
        FROM client_action_reason_xref
       WHERE client_update_action_code = g_client_update_action_code
         AND client_update_reason_code = g_client_update_reason_code
         AND client_type_code = g_client_type_code;
    r_xref          c_xref%ROWTYPE;
  BEGIN
    IF g_client_update_action_code IS NOT NULL
    AND g_client_update_reason_code IS NOT NULL
    AND g_client_type_code IS NOT NULL THEN
      OPEN c_xref;
      FETCH c_xref INTO r_xref;
      CLOSE c_xref;

      IF r_xref.client_update_action_code IS NULL THEN
        --reason is not valid for the action specified
        CLIENT_UTILS.add_error(g_error_message
                         , NULL
                         , 'client.web.usr.database.action.reason.xref'
                         , NULL);
      END IF;
    END IF;
  END validate_action_reason_xref;


/******************************************************************************
    Procedure:  validate

    Purpose:    Column validators

******************************************************************************/
  PROCEDURE validate
  IS
  BEGIN
    validate_mandatories;

    validate_action_reason_xref;
  END validate;


/******************************************************************************
    Procedure:  add

    Purpose:    INSERT one row into CLIENT_UPDATE_REASON

******************************************************************************/
  PROCEDURE add
  IS
  BEGIN
    INSERT INTO client_update_reason
       ( client_update_reason_id
       , forest_client_audit_id
       , client_update_action_code
       , client_update_reason_code
       , client_type_code
       , update_timestamp
       , update_userid
       , add_timestamp
       , add_userid
       )
     VALUES
       ( client_update_reason_seq.NEXTVAL
       , g_forest_client_audit_id
       , g_client_update_action_code
       , g_client_update_reason_code
       , g_client_type_code
       , g_update_timestamp
       , g_update_userid
       , g_add_timestamp
       , g_add_userid
       )
      RETURNING client_update_reason_id
           INTO g_client_update_reason_id;
  END add;

/******************************************************************************

  Following procs determine if items have changed.
  If items have changed, an update action code is returned.

******************************************************************************/
  FUNCTION check_client_name
  (p_old_client_name        IN VARCHAR2
  ,p_old_legal_first_name   IN VARCHAR2
  ,p_old_legal_middle_name  IN VARCHAR2
  ,p_new_client_name        IN VARCHAR2
  ,p_new_legal_first_name   IN VARCHAR2
  ,p_new_legal_middle_name  IN VARCHAR2)
  RETURN VARCHAR2
  IS
  BEGIN
    IF NVL(p_old_client_name,CHR(255))||NVL(p_old_legal_first_name,CHR(255))||NVL(p_old_legal_middle_name,CHR(255)) !=
       NVL(p_new_client_name,CHR(255))||NVL(p_new_legal_first_name,CHR(255))||NVL(p_new_legal_middle_name,CHR(255)) THEN
      RETURN 'NAME';
    ELSE
      RETURN NULL;
    END IF;
  END check_client_name;

  FUNCTION check_address
  (p_old_address_1        IN VARCHAR2
  ,p_old_address_2        IN VARCHAR2
  ,p_old_address_3        IN VARCHAR2
  ,p_old_city             IN VARCHAR2
  ,p_old_province         IN VARCHAR2
  ,p_old_postal_code      IN VARCHAR2
  ,p_old_country          IN VARCHAR2
  ,p_new_address_1        IN VARCHAR2
  ,p_new_address_2        IN VARCHAR2
  ,p_new_address_3        IN VARCHAR2
  ,p_new_city             IN VARCHAR2
  ,p_new_province         IN VARCHAR2
  ,p_new_postal_code      IN VARCHAR2
  ,p_new_country          IN VARCHAR2)
  RETURN VARCHAR2
  IS
  BEGIN
    IF NVL(p_old_address_1,CHR(255))
     ||NVL(p_old_address_2,CHR(255))
     ||NVL(p_old_address_3,CHR(255))
     ||NVL(p_old_city,CHR(255))
     ||NVL(p_old_province,CHR(255))
     ||NVL(p_old_postal_code,CHR(255)) !=
     NVL(p_new_address_1,CHR(255))
     ||NVL(p_new_address_2,CHR(255))
     ||NVL(p_new_address_3,CHR(255))
     ||NVL(p_new_city,CHR(255))
     ||NVL(p_new_province,CHR(255))
     ||NVL(p_new_postal_code,CHR(255)) THEN
      RETURN 'ADDR';
    ELSE
      RETURN NULL;
    END IF;
  END check_address;

  FUNCTION check_id
  (p_old_client_identification  IN VARCHAR2
  ,p_old_client_id_type_code    IN VARCHAR2
  ,p_new_client_identification  IN VARCHAR2
  ,p_new_client_id_type_code    IN VARCHAR2)
  RETURN VARCHAR2
  IS
  BEGIN
    IF NVL(p_old_client_identification,CHR(255))||NVL(p_old_client_id_type_code,CHR(255)) !=
       NVL(p_new_client_identification,CHR(255))||NVL(p_new_client_id_type_code,CHR(255)) THEN
      RETURN 'ID';
    ELSE
      RETURN NULL;
    END IF;
  END check_id;

  FUNCTION check_status
  (p_old_client_status_code  IN VARCHAR2
  ,p_new_client_status_code  IN VARCHAR2)
  RETURN VARCHAR2
  IS
  BEGIN
    IF NVL(p_old_client_status_code,CHR(255)) != NVL(p_new_client_status_code,CHR(255)) THEN
      IF p_new_client_status_code = 'SPN' THEN
        RETURN 'SPN';
      ELSIF p_new_client_status_code = 'DAC' THEN
        RETURN 'DAC';
      ELSIF p_old_client_status_code = 'SPN'
      AND p_new_client_status_code = 'ACT' THEN
        RETURN 'USPN';
      ELSIF p_old_client_status_code = 'DEC'
      AND p_new_client_status_code = 'ACT' THEN
        RETURN 'RACT';
      ELSIF p_old_client_status_code = 'DAC'
      AND p_new_client_status_code = 'ACT' THEN
        RETURN 'RACT';
      ELSE
        RETURN NULL;
      END IF;
    ELSE
      RETURN NULL;
    END IF;
  END check_status;

END client_client_update_reason;