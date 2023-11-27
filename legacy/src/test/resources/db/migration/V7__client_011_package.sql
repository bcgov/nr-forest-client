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
