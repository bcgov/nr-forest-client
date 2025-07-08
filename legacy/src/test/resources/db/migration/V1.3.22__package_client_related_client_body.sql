CREATE OR REPLACE PACKAGE BODY THE.client_related_client AS

  --member vars

  g_error_message                              SIL_ERROR_MESSAGES;

  g_rowid                                      ROWID;

  g_client_number                              related_client.client_number%TYPE;
  gb_client_number                             VARCHAR2(1);

  g_client_locn_code                           related_client.client_locn_code%TYPE;
  gb_client_locn_code                          VARCHAR2(1);

  g_related_clnt_nmbr                          related_client.related_clnt_nmbr%TYPE;
  gb_related_clnt_nmbr                         VARCHAR2(1);

  g_related_clnt_locn                          related_client.related_clnt_locn%TYPE;
  gb_related_clnt_locn                         VARCHAR2(1);

  g_relationship_code                          related_client.relationship_code%TYPE;
  gb_relationship_code                         VARCHAR2(1);

  g_signing_auth_ind                           related_client.signing_auth_ind%TYPE;
  gb_signing_auth_ind                          VARCHAR2(1);

  g_percent_ownership                          related_client.percent_ownership%TYPE;
  gb_percent_ownership                         VARCHAR2(1);

  g_update_timestamp                           related_client.update_timestamp%TYPE;
  gb_update_timestamp                          VARCHAR2(1);

  g_update_userid                              related_client.update_userid%TYPE;
  gb_update_userid                             VARCHAR2(1);

  g_update_org_unit                            related_client.update_org_unit%TYPE;
  gb_update_org_unit                           VARCHAR2(1);

  g_add_timestamp                              related_client.add_timestamp%TYPE;
  gb_add_timestamp                             VARCHAR2(1);

  g_add_userid                                 related_client.add_userid%TYPE;
  gb_add_userid                                VARCHAR2(1);

  g_add_org_unit                               related_client.add_org_unit%TYPE;
  gb_add_org_unit                              VARCHAR2(1);

  g_revision_count                             related_client.revision_count%TYPE;
  gb_revision_count                            VARCHAR2(1);

  -- Record to hold previous values
  TYPE t_previous IS RECORD
  ( row_id                      rowid
  , client_number               related_client.client_number%TYPE
  , client_locn_code            related_client.client_locn_code%TYPE
  , related_clnt_nmbr           related_client.related_clnt_nmbr%TYPE
  , related_clnt_locn           related_client.related_clnt_locn%TYPE
  , relationship_code           related_client.relationship_code%TYPE
  , signing_auth_ind            related_client.signing_auth_ind%TYPE
  , percent_ownership           related_client.percent_ownership%TYPE
  , update_timestamp            related_client.update_timestamp%TYPE
  , update_userid               related_client.update_userid%TYPE
  , update_org_unit             related_client.update_org_unit%TYPE
  , add_timestamp               related_client.add_timestamp%TYPE
  , add_userid                  related_client.add_userid%TYPE
  , add_org_unit                related_client.add_org_unit%TYPE
  , revision_count              related_client.revision_count%TYPE);
  r_previous      t_previous;

/******************************************************************************
    Procedure:  get

    Purpose:    SELECT one row from RELATED_CLIENT

******************************************************************************/
  PROCEDURE get
  IS
  BEGIN
    SELECT
           rowid
         , client_number
         , client_locn_code
         , related_clnt_nmbr
         , related_clnt_locn
         , relationship_code
         , signing_auth_ind
         , percent_ownership
         , update_timestamp
         , update_userid
         , update_org_unit
         , add_timestamp
         , add_userid
         , add_org_unit
         , revision_count
      INTO
           g_rowid
         , g_client_number
         , g_client_locn_code
         , g_related_clnt_nmbr
         , g_related_clnt_locn
         , g_relationship_code
         , g_signing_auth_ind
         , g_percent_ownership
         , g_update_timestamp
         , g_update_userid
         , g_update_org_unit
         , g_add_timestamp
         , g_add_userid
         , g_add_org_unit
         , g_revision_count
      FROM related_client
     WHERE rowid = g_rowid
        OR (g_rowid IS NULL
        AND client_number    = g_client_number
        AND client_locn_code = g_client_locn_code
        AND related_clnt_nmbr = g_related_clnt_nmbr
        AND related_clnt_locn = g_related_clnt_locn);

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      IF g_rowid IS NOT NULL THEN
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
    SELECT rowid
         , client_number
         , client_locn_code
         , related_clnt_nmbr
         , related_clnt_locn
         , relationship_code
         , signing_auth_ind
         , percent_ownership
         , update_timestamp
         , update_userid
         , update_org_unit
         , add_timestamp
         , add_userid
         , add_org_unit
         , revision_count
      INTO r_previous
      FROM related_client
     WHERE rowid = g_rowid;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      NULL;
  END get_previous;


/******************************************************************************
    Procedure:  init

    Purpose:    Initialize member variables

******************************************************************************/
  PROCEDURE init
  IS

  BEGIN

    g_error_message := NULL;

    g_client_number := NULL;
    gb_client_number := 'N';

    g_client_locn_code := NULL;
    gb_client_locn_code := 'N';

    g_related_clnt_nmbr := NULL;
    gb_related_clnt_nmbr := 'N';

    g_related_clnt_locn := NULL;
    gb_related_clnt_locn := 'N';

    g_relationship_code := NULL;
    gb_relationship_code := 'N';

    g_signing_auth_ind := NULL;
    gb_signing_auth_ind := 'N';

    g_percent_ownership := NULL;
    gb_percent_ownership := 'N';

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

    IF g_rowid IS NOT NULL THEN
      get;
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

  -- primary keys have changed.
  FUNCTION primary_keys_changed RETURN BOOLEAN
  IS
  BEGIN
    IF g_rowid IS NOT NULL AND
       (g_client_number <> r_previous.client_number OR
        g_client_locn_code <> r_previous.client_locn_code OR
        g_related_clnt_nmbr <> r_previous.related_clnt_nmbr OR
        g_related_clnt_locn <> r_previous.related_clnt_locn) THEN
      RETURN TRUE;
    END IF;
    RETURN FALSE;
  END primary_keys_changed;

  --get error message
  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES
  IS
  BEGIN
    RETURN g_error_message;
  END get_error_message;

  --get rowid
  FUNCTION get_rowid RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_rowid;
  END get_rowid;

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

  --get related_clnt_nmbr
  FUNCTION get_related_clnt_nmbr RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_related_clnt_nmbr;
  END get_related_clnt_nmbr;

  --get related_clnt_locn
  FUNCTION get_related_clnt_locn RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_related_clnt_locn;
  END get_related_clnt_locn;

  --get relationship_code
  FUNCTION get_relationship_code RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_relationship_code;
  END get_relationship_code;

  --get signing_auth_ind
  FUNCTION get_signing_auth_ind RETURN VARCHAR2
  IS
  BEGIN
    RETURN g_signing_auth_ind;
  END get_signing_auth_ind;

  --get percent_ownership
  FUNCTION get_percent_ownership RETURN NUMBER
  IS
  BEGIN
    RETURN g_percent_ownership;
  END get_percent_ownership;

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
  --set rowid
  PROCEDURE set_rowid(p_value IN VARCHAR2)
  IS
  BEGIN
    g_rowid := p_value;
  END set_rowid;

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

  --set related_clnt_nmbr
  PROCEDURE set_related_clnt_nmbr(p_value IN VARCHAR2)
  IS
  BEGIN
    g_related_clnt_nmbr := p_value;
    gb_related_clnt_nmbr := 'Y';
  END set_related_clnt_nmbr;

  --set related_clnt_locn
  PROCEDURE set_related_clnt_locn(p_value IN VARCHAR2)
  IS
  BEGIN
    g_related_clnt_locn := p_value;
    gb_related_clnt_locn := 'Y';
  END set_related_clnt_locn;

  --set relationship_code
  PROCEDURE set_relationship_code(p_value IN VARCHAR2)
  IS
  BEGIN
    g_relationship_code := p_value;
    gb_relationship_code := 'Y';
  END set_relationship_code;

  --set signing_auth_ind
  PROCEDURE set_signing_auth_ind(p_value IN VARCHAR2)
  IS
  BEGIN
    g_signing_auth_ind := p_value;
    gb_signing_auth_ind := 'Y';
  END set_signing_auth_ind;

  --set percent_ownership
  PROCEDURE set_percent_ownership(p_value IN NUMBER)
  IS
  BEGIN
    g_percent_ownership := p_value;
    gb_percent_ownership := 'Y';
  END set_percent_ownership;

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
    IF g_related_clnt_nmbr IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'related_clnt_nmbr'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Related Client Number'));
    END IF;
    IF g_related_clnt_locn IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'related_clnt_locn'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Related Client Locn'));
    END IF;
    IF g_relationship_code IS NULL THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'relationship_code'
                           , 'sil.error.usr.isrequired'
                           , SIL_ERROR_PARAMS('Relationship'));
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
    v_related_client_status              FOREST_CLIENT.CLIENT_STATUS_CODE%TYPE;
    v_expired_related_client_locn        CLIENT_LOCATION.LOCN_EXPIRED_IND%TYPE;
    v_expired_relationship               NUMBER := 0;
    v_expired_client_locn                CLIENT_LOCATION.LOCN_EXPIRED_IND%TYPE;
    v_client_type                        FOREST_CLIENT.CLIENT_TYPE_CODE%TYPE;
    v_related_client_type                FOREST_CLIENT.CLIENT_TYPE_CODE%TYPE;
    v_matching_relationship              NUMBER;
    v_pri_client_type_desc               CLIENT_TYPE_CODE.description%TYPE;
    v_sec_client_type_desc               CLIENT_TYPE_CODE.description%TYPE;
    v_relationship_desc                  CLIENT_RELATIONSHIP_CODE.description%TYPE;
  BEGIN
    validate_mandatories;

    -- PT#35728 - JV relationship cannot have Related Client Locn of 00.
    -- PT#37718 - had it backwards, original client cannot be 00, related client can be 00 for JV
    IF g_relationship_code = 'JV' AND
       g_client_locn_code = '00' THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'client_locn_code'
                           , 'client.web.error.database.relation.invalid'
                           , SIL_ERROR_PARAMS('Joint Venture','the Client Location is 00'));
    END IF;

    -- Get the client type code.
    SELECT client_type_code
      INTO v_client_type
      FROM forest_client
     WHERE client_number = g_client_number;

    -- validate that the client location in not expired.
    SELECT LOCN_EXPIRED_IND
      INTO v_expired_client_locn
      FROM CLIENT_LOCATION
     WHERE CLIENT_NUMBER = g_client_number
       AND CLIENT_LOCN_CODE = g_client_locn_code;

    IF v_expired_client_locn = 'Y' THEN
      -- update allowed if the expired client location code is unchanged for an
      -- existing record.
      IF g_rowid IS NULL OR g_client_locn_code <> r_previous.client_locn_code THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'client_locn_code'
                             , 'sil.web.error.usr.code-expired'
                             , SIL_ERROR_PARAMS('Location'));
      END IF;
    END IF;

    -- Convert related client number to number, in case acronym was entered.
    g_related_clnt_nmbr := sil_get_client_number(g_related_clnt_nmbr);

    BEGIN
      -- make sure the related client number exists.
      SELECT client_number
           , client_status_code
           , client_type_code
        INTO g_related_clnt_nmbr
           , v_related_client_status
           , v_related_client_type
        FROM forest_client
       WHERE client_number = g_related_clnt_nmbr;

      -- check for recursive relationship
      IF g_related_clnt_nmbr = g_client_number THEN
          CLIENT_UTILS.add_error(g_error_message
                               , 'related_clnt_nmbr'
                               , 'client.web.error.recip.relationship'
                               , NULL);
      END IF;

      -- make sure the related client is not deactivated.
      IF v_related_client_status = 'DAC' THEN
        -- update allowed if the deactivated related client is unchanged for an
        -- existing record.
        IF g_rowid IS NULL OR
           g_related_clnt_nmbr <> r_previous.related_clnt_nmbr THEN
          CLIENT_UTILS.add_error(g_error_message
                               , 'related_clnt_nmbr'
                               , 'client.web.error.database.invalid'
                               , SIL_ERROR_PARAMS('Related Client','Deactivated'));
        END IF;
      END IF;

      -- make sure the related client location exists.
      BEGIN
        SELECT client_locn_code
             , locn_expired_ind
          INTO g_related_clnt_locn
             , v_expired_related_client_locn
          FROM client_location
         WHERE client_number = g_related_clnt_nmbr
           AND client_locn_code = g_related_clnt_locn;

        -- validate that the related client location in not expired.
        IF v_expired_related_client_locn = 'Y' THEN
          -- update allowed if the expired related client location code is
          -- unchanged for an existing record.
          IF g_rowid IS NULL OR
            g_related_clnt_locn <> r_previous.related_clnt_locn THEN
            CLIENT_UTILS.add_error(g_error_message
                                 , 'related_clnt_locn'
                                 , 'sil.web.error.usr.code-expired'
                                 , SIL_ERROR_PARAMS('Related Client Locn'));
          END IF;
        END IF;


      EXCEPTION
      WHEN NO_DATA_FOUND THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'related_clnt_locn'
                             , 'client.web.usr.database.record.not.found'
                             , SIL_ERROR_PARAMS('Related Client Locn'));

      END;

      SELECT COUNT(1)
        INTO v_matching_relationship
        FROM client_relationship_type_xref crtx
       WHERE crtx.primary_client_type_code = v_client_type
         AND crtx.secondary_client_type_code = v_related_client_type
         AND crtx.client_relationship_code = g_relationship_code;

      IF v_matching_relationship <= 0 THEN
        SELECT description
          INTO v_pri_client_type_desc
          FROM client_type_code
         WHERE client_type_code = v_client_type;

        SELECT description
          INTO v_sec_client_type_desc
          FROM client_type_code
         WHERE client_type_code = v_related_client_type;

        SELECT description
          INTO v_relationship_desc
          FROM client_relationship_code
         WHERE client_relationship_code = g_relationship_code;

        CLIENT_UTILS.add_error(g_error_message
                             , 'relationship_code'
                             , 'client.web.error.db.relation.type.invalid'
                             , SIL_ERROR_PARAMS(v_relationship_desc, v_pri_client_type_desc, v_sec_client_type_desc));
      END IF;

    EXCEPTION
    WHEN NO_DATA_FOUND THEN
      CLIENT_UTILS.add_error(g_error_message
                           , 'related_clnt_nmbr'
                           , 'client.web.usr.database.record.not.found'
                           , SIL_ERROR_PARAMS('Related Client Number'));
    END;

    -- validate that the relationship code is not expired.
    SELECT COUNT(1)
      INTO v_expired_relationship
      FROM CLIENT_RELATIONSHIP_CODE
     WHERE CLIENT_RELATIONSHIP_CODE = g_relationship_code
       AND SYSDATE BETWEEN EFFECTIVE_DATE AND EXPIRY_DATE;

    IF v_expired_relationship = 0 THEN
      -- update allowed if the expired relationship code is unchanged for an
      -- existing record.
      IF g_rowid IS NULL OR g_relationship_code <> r_previous.relationship_code THEN
        CLIENT_UTILS.add_error(g_error_message
                             , 'relationship_code'
                             , 'sil.web.error.usr.code-expired'
                             , SIL_ERROR_PARAMS('Relationship'));
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

    Purpose:    INSERT one row into RELATED_CLIENT

******************************************************************************/
  PROCEDURE add
  IS
  BEGIN
    INSERT INTO related_client
       ( client_number
       , client_locn_code
       , related_clnt_nmbr
       , related_clnt_locn
       , relationship_code
       , signing_auth_ind
       , percent_ownership
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
       , g_client_locn_code
       , g_related_clnt_nmbr
       , g_related_clnt_locn
       , g_relationship_code
       , g_signing_auth_ind
       , g_percent_ownership
       , SYSDATE
       , g_update_userid
       , g_update_org_unit
       , SYSDATE
       , g_add_userid
       , g_add_org_unit
       , 1
       )
      RETURNING rowid
              , update_timestamp
              , add_timestamp
              , revision_count
           INTO g_rowid
              , g_update_timestamp
              , g_add_timestamp
              , g_revision_count;
  END add;


/******************************************************************************
    Procedure:  change

    Purpose:    UPDATE one row in RELATED_CLIENT

******************************************************************************/
  PROCEDURE change
  IS
  BEGIN
    UPDATE related_client
       SET client_locn_code = DECODE(gb_client_locn_code,'Y',g_client_locn_code,client_locn_code)
         , related_clnt_nmbr = DECODE(gb_related_clnt_nmbr,'Y',g_related_clnt_nmbr,related_clnt_nmbr)
         , related_clnt_locn = DECODE(gb_related_clnt_locn,'Y',g_related_clnt_locn,related_clnt_locn)
         , relationship_code = DECODE(gb_relationship_code,'Y',g_relationship_code,relationship_code)
         , signing_auth_ind = DECODE(gb_signing_auth_ind,'Y',g_signing_auth_ind,signing_auth_ind)
         , percent_ownership = DECODE(gb_percent_ownership,'Y',g_percent_ownership,percent_ownership)
         , update_timestamp = g_update_timestamp
         , update_userid = g_update_userid
         , update_org_unit = DECODE(gb_update_org_unit,'Y',g_update_org_unit,update_org_unit)
         , revision_count = revision_count + 1
     WHERE rowid = g_rowid
       AND revision_count = g_revision_count;

    IF SQL%ROWCOUNT != 1 THEN
      CLIENT_UTILS.add_error(g_error_message
                           , NULL
                           , 'sil.web.usr.database.record.modified'
                           , SIL_ERROR_PARAMS('CLIENT_RELATED_CLIENT'
                                            , 'CHANGE'
                                            , 'RELATED_CLIENT'));
    END IF;
  END change;


/******************************************************************************
    Procedure:  remove

    Purpose:    DELETE one row from RELATED_CLIENT

******************************************************************************/
  PROCEDURE remove
  IS
  BEGIN
    DELETE FROM related_client
     WHERE rowid = g_rowid
       AND revision_count = g_revision_count ;

    IF SQL%ROWCOUNT != 1 THEN
      CLIENT_UTILS.add_error(g_error_message
                           , NULL
                           , 'sil.web.usr.database.record.modified'
                           , SIL_ERROR_PARAMS('CLIENT_RELATED_CLIENT'
                                            , 'REMOVE'
                                            , 'RELATED_CLIENT'));
    END IF;
  END remove;

END client_related_client;