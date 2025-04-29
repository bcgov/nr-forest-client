CREATE OR REPLACE PACKAGE BODY THE.client_client_contact AS

  --member vars

  g_error_message                              SIL_ERROR_MESSAGES;

  g_client_contact_id                          client_contact.client_contact_id%TYPE;
  gb_client_contact_id                         VARCHAR2(1);

  g_client_number                              client_contact.client_number%TYPE;
  gb_client_number                             VARCHAR2(1);

  g_client_locn_code                           client_contact.client_locn_code%TYPE;
  gb_client_locn_code                          VARCHAR2(1);

  g_bus_contact_code                           client_contact.bus_contact_code%TYPE;
  gb_bus_contact_code                          VARCHAR2(1);

  g_contact_name                               client_contact.contact_name%TYPE;
  gb_contact_name                              VARCHAR2(1);

  g_business_phone                             client_contact.business_phone%TYPE;
  gb_business_phone                            VARCHAR2(1);

  g_cell_phone                                 client_contact.cell_phone%TYPE;
  gb_cell_phone                                VARCHAR2(1);

  g_fax_number                                 client_contact.fax_number%TYPE;
  gb_fax_number                                VARCHAR2(1);

  g_email_address                              client_contact.email_address%TYPE;
  gb_email_address                             VARCHAR2(1);

  g_update_timestamp                           client_contact.update_timestamp%TYPE;
  gb_update_timestamp                          VARCHAR2(1);

  g_update_userid                              client_contact.update_userid%TYPE;
  gb_update_userid                             VARCHAR2(1);

  g_update_org_unit                            client_contact.update_org_unit%TYPE;
  gb_update_org_unit                           VARCHAR2(1);

  g_add_timestamp                              client_contact.add_timestamp%TYPE;
  gb_add_timestamp                             VARCHAR2(1);

  g_add_userid                                 client_contact.add_userid%TYPE;
  gb_add_userid                                VARCHAR2(1);

  g_add_org_unit                               client_contact.add_org_unit%TYPE;
  gb_add_org_unit                              VARCHAR2(1);

  g_revision_count                             client_contact.revision_count%TYPE;
  gb_revision_count                            VARCHAR2(1);

  -- Record to hold previous values
  TYPE t_previous IS RECORD
  ( client_contact_id           client_contact.client_contact_id%TYPE
  , client_number               client_contact.client_number%TYPE
  , client_locn_code            client_contact.client_locn_code%TYPE
  , bus_contact_code            client_contact.bus_contact_code%TYPE
  , contact_name                client_contact.contact_name%TYPE
  , business_phone              client_contact.business_phone%TYPE
  , cell_phone                  client_contact.cell_phone%TYPE
  , fax_number                  client_contact.fax_number%TYPE
  , email_address               client_contact.email_address%TYPE
  , update_timestamp            client_contact.update_timestamp%TYPE
  , update_userid               client_contact.update_userid%TYPE
  , update_org_unit             client_contact.update_org_unit%TYPE
  , add_timestamp               client_contact.add_timestamp%TYPE
  , add_userid                  client_contact.add_userid%TYPE
  , add_org_unit                client_contact.add_org_unit%TYPE);
  r_previous      t_previous;

/******************************************************************************
    Procedure:  get

    Purpose:    SELECT one row from CLIENT_CONTACT

******************************************************************************/
  PROCEDURE get
  (p_client_contact_id   IN VARCHAR2)
  IS
  BEGIN
    SELECT
           client_contact_id
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
         , update_org_unit
         , add_timestamp
         , add_userid
         , add_org_unit
         , revision_count
      INTO
           g_client_contact_id
         , g_client_number
         , g_client_locn_code
         , g_bus_contact_code
         , g_contact_name
         , g_business_phone
         , g_cell_phone
         , g_fax_number
         , g_email_address
         , g_update_timestamp
         , g_update_userid
         , g_update_org_unit
         , g_add_timestamp
         , g_add_userid
         , g_add_org_unit
         , g_revision_count
      FROM client_contact
     WHERE client_contact_id = TO_NUMBER(p_client_contact_id);

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      IF p_client_contact_id IS NOT NULL THEN
       -- record with rowid not found, most likely deleted.
        CLIENT_UTILS.add_error(g_error_message
                             , NULL
                             , 'client.web.usr.database.no.record.found'
                             , NULL);
      END IF;
  END get;


/******************************************************************************
    Procedure:  get_previous

    Purpose:    Get previous (saved) values and populate previous record

******************************************************************************/
  PROCEDURE get_previous
  IS
  BEGIN
    SELECT client_contact_id
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
         , update_org_unit
         , add_timestamp
         , add_userid
         , add_org_unit
      INTO r_previous
      FROM client_contact
     WHERE client_contact_id = g_client_contact_id;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      NULL;
  END get_previous;

/******************************************************************************
    Procedure:  init

    Purpose:    Initialize member variables

******************************************************************************/
  PROCEDURE init
  (p_client_contact_id   IN VARCHAR2)
  IS

  BEGIN

    g_error_message := NULL;

    g_client_contact_id := NULL;
    gb_client_contact_id := 'N';

    g_client_number := NULL;
    gb_client_number := 'N';

    g_client_locn_code := NULL;
    gb_client_locn_code := 'N';

    g_bus_contact_code := NULL;
    gb_bus_contact_code := 'N';

    g_contact_name := NULL;
    gb_contact_name := 'N';

    g_business_phone := NULL;
    gb_business_phone := 'N';

    g_cell_phone := NULL;
    gb_cell_phone := 'N';

    g_fax_number := NULL;
    gb_fax_number := 'N';

    g_email_address := NULL;
    gb_email_address := 'N';

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

    IF p_client_contact_id IS NOT NULL THEN
      get(p_client_contact_id);
      get_previous;
    END IF;

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

  --get client_contact_id
  FUNCTION get_client_contact_id RETURN NUMBER
  IS
  BEGIN
    RETURN g_client_contact_id;
  END get_client_contact_id;

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

  --get bus_contact_code
  FUNCTION get_bus_contact_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_bus_contact_code;
  END get_bus_contact_code;

  --get contact_name
  FUNCTION get_contact_name RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_contact_name;
  END get_contact_name;

  --get business_phone
  FUNCTION get_business_phone RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_business_phone;
  END get_business_phone;

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
  --***END GETTERS

  --***START SETTERS
  --set client_contact_id
  PROCEDURE set_client_contact_id(p_value IN NUMBER)
  IS
  BEGIN
    g_client_contact_id := p_value;
    gb_client_contact_id := 'Y';
  END set_client_contact_id;

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

  --set bus_contact_code
  PROCEDURE set_bus_contact_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_bus_contact_code := p_value;
    gb_bus_contact_code := 'Y';
  END set_bus_contact_code;

  --set contact_name
  PROCEDURE set_contact_name(p_value IN VARCHAR2)
  IS
  BEGIN
    g_contact_name := p_value;
    gb_contact_name := 'Y';
  END set_contact_name;

  --set business_phone
  PROCEDURE set_business_phone(p_value IN VARCHAR2)
  IS
  BEGIN
    g_business_phone := p_value;
    gb_business_phone := 'Y';
  END set_business_phone;

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
    Procedure:  validate_mandatories

    Purpose:    Validate optionality

******************************************************************************/
  PROCEDURE validate_mandatories
  IS
  BEGIN
    IF g_client_number IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'client_number'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Client Number'));
    END IF;
    IF g_client_locn_code IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'client_locn_code'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Location'));
    END IF;
    IF g_bus_contact_code IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'bus_contact_code'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Contact Type'));
    END IF;
    IF g_contact_name IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'contact_name'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Contact Name'));
    END IF;
    IF g_update_userid IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'update_userid'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Update Userid'));
    END IF;
    IF g_update_org_unit IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'update_org_unit'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Update Org Unit'));
    END IF;
  END validate_mandatories;


/******************************************************************************
    Procedure:  validate

    Purpose:    Column validators

******************************************************************************/
  PROCEDURE validate
  IS
    v_expired_contact_type               NUMBER := 0;
    v_client_type                        forest_client.client_type_code%TYPE;
    v_general_partner_type               NUMBER :=0;
  BEGIN
    validate_mandatories;

    -- validate that the contact type code is not expired.
    SELECT COUNT(1)
      INTO v_expired_contact_type
      FROM BUSINESS_CONTACT_CODE
     WHERE BUSINESS_CONTACT_CODE = g_bus_contact_code
       AND SYSDATE BETWEEN EFFECTIVE_DATE AND EXPIRY_DATE;
    IF v_expired_contact_type = 0 THEN
      -- update allowed if the expired contact type code is unchanged for an
      -- existing record.
      IF g_client_contact_id IS NULL OR g_bus_contact_code <> r_previous.bus_contact_code THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'bus_contact_code'
                             , 'sil.web.error.usr.code-expired'
                             , SIL_ERROR_PARAMS('Contact Type'));
      END IF;
    END IF;

    -- validate only Client Type = L can have "General Partner" or "Limited Partner" as contact
    SELECT client_type_code
      INTO v_client_type
      FROM forest_client
     WHERE client_number = g_client_number;

    IF v_client_type <> 'L' THEN
      IF g_bus_contact_code IN ('GP', 'LP') THEN
        CLIENT_UTILS.add_error(g_error_message
                     , 'bus_contact_code'
                     , 'client.web.usr.database.invalid.contact.type'
                     , NULL);
      END IF;
    ELSE
      -- validate only one General Partner for a Client
      IF g_bus_contact_code = 'GP' THEN
        SELECT COUNT(1)
          INTO v_general_partner_type
          FROM client_contact
          WHERE client_number = g_client_number
          AND client_contact_id <> NVL(g_client_contact_id, -999999999)
          AND bus_contact_code = 'GP';

        IF v_general_partner_type > 0 THEN
          CLIENT_UTILS.add_error(g_error_message
                     , 'bus_contact_code'
                     , 'client.web.usr.database.dup.contact.gp.type'
                     , NULL);
        END IF;
      END IF;
    END IF;



  END validate;


/******************************************************************************
    Procedure:  validate_remove

    Purpose:    DELETE validations - check for child records, etc.

******************************************************************************/
  PROCEDURE validate_remove
  IS
    l_flag VARCHAR2(1);
  BEGIN
    --Add assignment to remove PLSQL warning for unreachable code
    l_flag := 'Y';
  END validate_remove;


/******************************************************************************
    Procedure:  add

    Purpose:    INSERT one row into CLIENT_CONTACT

******************************************************************************/
  PROCEDURE add
  IS
  BEGIN
    INSERT INTO client_contact
       ( client_contact_id
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
       , update_org_unit
       , add_timestamp
       , add_userid
       , add_org_unit
       , revision_count
       )
     VALUES
       ( client_contact_seq.NEXTVAL
       , g_client_number
       , g_client_locn_code
       , g_bus_contact_code
       , g_contact_name
       , g_business_phone
       , g_cell_phone
       , g_fax_number
       , g_email_address
       , SYSDATE
       , g_update_userid
       , g_update_org_unit
       , SYSDATE
       , g_update_userid
       , g_update_org_unit
       , 1
       )
      RETURNING client_contact_id
              , update_timestamp
              , add_timestamp
              , revision_count
           INTO g_client_contact_id
              , g_update_timestamp
              , g_add_timestamp
              , g_revision_count;
  END add;


/******************************************************************************
    Procedure:  change

    Purpose:    UPDATE one row in CLIENT_CONTACT

******************************************************************************/
  PROCEDURE change
  IS
  BEGIN
    UPDATE client_contact
       SET client_locn_code = DECODE(gb_client_locn_code,'Y',g_client_locn_code,client_locn_code)
         , bus_contact_code = DECODE(gb_bus_contact_code,'Y',g_bus_contact_code,bus_contact_code)
         , contact_name = DECODE(gb_contact_name,'Y',g_contact_name,contact_name)
         , business_phone = DECODE(gb_business_phone,'Y',g_business_phone,business_phone)
         , cell_phone = DECODE(gb_cell_phone,'Y',g_cell_phone,cell_phone)
         , fax_number = DECODE(gb_fax_number,'Y',g_fax_number,fax_number)
         , email_address = DECODE(gb_email_address,'Y',g_email_address,email_address)
         , update_timestamp = SYSDATE
         , update_userid = g_update_userid
         , update_org_unit = DECODE(gb_update_org_unit,'Y',g_update_org_unit,update_org_unit)
         , revision_count = revision_count + 1
     WHERE client_contact_id = g_client_contact_id
       AND revision_count = g_revision_count;

    IF SQL%ROWCOUNT != 1 THEN
      CLIENT_UTILS.add_error(g_error_message
                           , NULL
                           , 'sil.web.usr.database.record.modified'
                           , SIL_ERROR_PARAMS('CLIENT_CLIENT_CONTACT'
                                            , 'CHANGE'
                                            , 'CLIENT_CONTACT'));
    END IF;
  END change;


/******************************************************************************
    Procedure:  remove

    Purpose:    DELETE one row from CLIENT_CONTACT

******************************************************************************/
  PROCEDURE remove
  IS
  BEGIN
    DELETE FROM client_contact
     WHERE client_contact_id = g_client_contact_id
       AND revision_count = g_revision_count;

    IF SQL%ROWCOUNT != 1 THEN
      CLIENT_UTILS.add_error(g_error_message
                           , NULL
                           , 'sil.web.usr.database.record.modified'
                           , SIL_ERROR_PARAMS('CLIENT_CLIENT_CONTACT'
                                            , 'REMOVE'
                                            , 'CLIENT_CONTACT'));
    END IF;
  END remove;

END client_client_contact;