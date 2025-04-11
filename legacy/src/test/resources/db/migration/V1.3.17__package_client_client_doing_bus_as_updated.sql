CREATE OR REPLACE PACKAGE BODY THE.client_client_doing_bus_as AS

  --member vars

  g_error_message                              SIL_ERROR_MESSAGES;

  g_client_number                              client_doing_business_as.client_number%TYPE;
  gb_client_number                             VARCHAR2(1);

  --array of names
  g_doing_business_as_names                    client_generic_string_varray;
  gb_doing_business_as_names                   VARCHAR2(1);

  g_update_timestamp                           client_doing_business_as.update_timestamp%TYPE;
  gb_update_timestamp                          VARCHAR2(1);

  g_update_userid                              client_doing_business_as.update_userid%TYPE;
  gb_update_userid                             VARCHAR2(1);

  g_update_org_unit                            client_doing_business_as.update_org_unit%TYPE;
  gb_update_org_unit                           VARCHAR2(1);

  g_add_timestamp                              client_doing_business_as.add_timestamp%TYPE;
  gb_add_timestamp                             VARCHAR2(1);

  g_add_userid                                 client_doing_business_as.add_userid%TYPE;
  gb_add_userid                                VARCHAR2(1);

  g_add_org_unit                               client_doing_business_as.add_org_unit%TYPE;
  gb_add_org_unit                              VARCHAR2(1);

/******************************************************************************
    Procedure:  get

    Purpose:    SELECT from CLIENT_DOING_BUSINESS_AS for g_client_number

******************************************************************************/
  PROCEDURE get
  IS
  BEGIN

    SELECT doing_business_as_name
    BULK COLLECT INTO g_doing_business_as_names
      FROM client_doing_business_as
     WHERE client_number = g_client_number
    ORDER BY add_timestamp;

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

  --get doing_business_as_name
  FUNCTION get_doing_business_as_names RETURN client_generic_string_varray
  IS
  BEGIN
    RETURN g_doing_business_as_names;
  END get_doing_business_as_names;

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

  --***END GETTERS

  --***START SETTERS

  --set client_number
  PROCEDURE set_client_number(p_value IN VARCHAR2)
  IS
  BEGIN
    g_client_number := p_value;
    gb_client_number := 'Y';
  END set_client_number;

  --set doing_business_as_names
  PROCEDURE set_doing_business_as_names(p_value IN client_generic_string_varray)
  IS
  BEGIN
    g_doing_business_as_names := p_value;
    gb_doing_business_as_names := 'Y';
  END set_doing_business_as_names;

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

  --***END SETTERS

/******************************************************************************
    Procedure:  init

    Purpose:    Initialize member variables

******************************************************************************/
  PROCEDURE init
  (p_client_number      IN VARCHAR2)
  IS
    v_empty_dba_array           client_generic_string_varray;
  BEGIN

    g_error_message := NULL;

    g_client_number := NULL;
    gb_client_number := 'N';

    g_doing_business_as_names := v_empty_dba_array;
    gb_doing_business_as_names := 'N';

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

    IF p_client_number IS NOT NULL THEN
      set_client_number(p_client_number);
      get;
    END IF;

  END init;


/******************************************************************************
    Procedure:  validate_mandatories

    Purpose:    Validate optionality

******************************************************************************/
  PROCEDURE validate_mandatories
  IS
  BEGIN
    IF g_client_number IS NULL THEN
        CLIENT_UTILS.add_error(g_error_message
                         , NULL
                         , 'sil.error.usr.isrequired'
                         , SIL_ERROR_PARAMS('Client Number'));
    END IF;
  END validate_mandatories;


/******************************************************************************
    Procedure:  validate

    Purpose:    Column validators

******************************************************************************/
  PROCEDURE validate
  IS
  BEGIN
    validate_mandatories;
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

    Purpose:    INSERT one row into CLIENT_DOING_BUSINESS_AS

******************************************************************************/
  PROCEDURE add
  (p_doing_business_as_name   IN VARCHAR2)
  IS
  BEGIN
    INSERT INTO client_doing_business_as
       ( client_dba_id
       , client_number
       , doing_business_as_name
       , update_timestamp
       , update_userid
       , update_org_unit
       , add_timestamp
       , add_userid
       , add_org_unit
       , revision_count
       )
     VALUES
       ( client_dba_seq.NEXTVAL
       , g_client_number
       , p_doing_business_as_name
       , g_update_timestamp
       , g_update_userid
       , g_update_org_unit
       , g_add_timestamp
       , g_add_userid
       , g_add_org_unit
       , 1
       );
  END add;

/******************************************************************************
    Procedure:  dba_name_exists

    Purpose:    Returns TRUE if Doing Business As Name passed in exists for
                the current client

******************************************************************************/
  FUNCTION dba_name_exists
  (p_doing_business_as_name   IN VARCHAR2)
  RETURN BOOLEAN
  IS
    CURSOR c_dba
    IS
      SELECT doing_business_as_name
        FROM client_doing_business_as
       WHERE client_number = g_client_number
         AND doing_business_as_name = p_doing_business_as_name;
    r_dba   c_dba%ROWTYPE;
  BEGIN

    OPEN c_dba;
    FETCH c_dba INTO r_dba;
    CLOSE c_dba;

    RETURN (r_dba.doing_business_as_name IS NOT NULL);

  END dba_name_exists;

/******************************************************************************
    Procedure:  dba_name_in_array

    Purpose:    Returns TRUE if Doing Business As Name is in the array
                NOTE: If array is emtpy or uninitialized, return FALSE, not NULL

******************************************************************************/
  FUNCTION dba_name_in_array
  (p_doing_business_as_name   IN VARCHAR2)
  RETURN BOOLEAN
  IS
    b_match       BOOLEAN := FALSE;
  BEGIN
    IF g_doing_business_as_names IS NOT NULL THEN
      FOR i IN g_doing_business_as_names.FIRST..g_doing_business_as_names.LAST LOOP
        IF g_doing_business_as_names(i) = p_doing_business_as_name THEN
          b_match := TRUE;
          EXIT;
        END IF;
      END LOOP;
    END IF;

    RETURN b_match;
  END dba_name_in_array;

/******************************************************************************
    Procedure:  remove

    Purpose:    DELETE one row from CLIENT_DOING_BUSINESS_AS

******************************************************************************/
  PROCEDURE remove
  ( p_doing_business_as_name   IN VARCHAR2
  , p_revision_count           IN NUMBER)
  IS
  BEGIN
    DELETE FROM client_doing_business_as
     WHERE client_number = g_client_number
       AND doing_business_as_name = p_doing_business_as_name
       AND revision_count = p_revision_count ;
  END remove;

/******************************************************************************
    Procedure:  merge_dba

    Purpose:    Merges list of names into CLIENT_DOING_BUSINESS_AS table.
                This is done as opposed to DELETE/INSERT so as not to generate
                superfluous auditing.

******************************************************************************/
  PROCEDURE merge_dba
  IS
    CURSOR c_dba
    IS
      SELECT doing_business_as_name
           , revision_count
        FROM client_doing_business_as
       WHERE client_number = g_client_number;

  BEGIN
    --add those that have been added
    IF g_doing_business_as_names IS NOT NULL THEN
      FOR i IN g_doing_business_as_names.FIRST..g_doing_business_as_names.LAST LOOP
        --if it doesn't exist, add it
        IF TRIM(g_doing_business_as_names(i)) IS NOT NULL
        AND NOT dba_name_exists(g_doing_business_as_names(i)) THEN
          add(g_doing_business_as_names(i));
        END IF;
      END LOOP;
    END IF;

    --delete those that have been removed if names were set
    IF gb_doing_business_as_names = 'Y' THEN
      FOR r_dba IN c_dba LOOP
        IF NOT dba_name_in_array(r_dba.doing_business_as_name) THEN
          remove(r_dba.doing_business_as_name,r_dba.revision_count);
        END IF;
      END LOOP;
    END IF;

  END merge_dba;

END client_client_doing_bus_as;