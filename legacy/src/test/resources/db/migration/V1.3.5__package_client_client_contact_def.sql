CREATE OR REPLACE PACKAGE THE.client_client_contact AS

  PROCEDURE get
  (p_client_contact_id   IN VARCHAR2);

  PROCEDURE init
  (p_client_contact_id   IN VARCHAR2);
  --***START GETTERS
  FUNCTION error_raised RETURN BOOLEAN;

  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES;

  FUNCTION get_client_contact_id RETURN NUMBER;

  FUNCTION get_client_number RETURN VARCHAR2;

  FUNCTION get_client_locn_code RETURN VARCHAR2;

  FUNCTION get_bus_contact_code RETURN VARCHAR2;

  FUNCTION get_contact_name RETURN VARCHAR2;

  FUNCTION get_business_phone RETURN VARCHAR2;

  FUNCTION get_cell_phone RETURN VARCHAR2;

  FUNCTION get_fax_number RETURN VARCHAR2;

  FUNCTION get_email_address RETURN VARCHAR2;

  FUNCTION get_update_timestamp RETURN DATE;

  FUNCTION get_update_userid RETURN VARCHAR2;

  FUNCTION get_update_org_unit RETURN NUMBER;

  FUNCTION get_add_timestamp RETURN DATE;

  FUNCTION get_add_userid RETURN VARCHAR2;

  FUNCTION get_add_org_unit RETURN NUMBER;

  FUNCTION get_revision_count RETURN NUMBER;
  --***END GETTERS

  --***START SETTERS

  PROCEDURE set_client_contact_id(p_value IN NUMBER);

  PROCEDURE set_client_number(p_value IN VARCHAR2);

  PROCEDURE set_client_locn_code(p_value IN VARCHAR2);

  PROCEDURE set_bus_contact_code(p_value IN VARCHAR2);

  PROCEDURE set_contact_name(p_value IN VARCHAR2);

  PROCEDURE set_business_phone(p_value IN VARCHAR2);

  PROCEDURE set_cell_phone(p_value IN VARCHAR2);

  PROCEDURE set_fax_number(p_value IN VARCHAR2);

  PROCEDURE set_email_address(p_value IN VARCHAR2);

  PROCEDURE set_update_timestamp(p_value IN DATE);

  PROCEDURE set_update_userid(p_value IN VARCHAR2);

  PROCEDURE set_update_org_unit(p_value IN NUMBER);

  PROCEDURE set_add_timestamp(p_value IN DATE);

  PROCEDURE set_add_userid(p_value IN VARCHAR2);

  PROCEDURE set_add_org_unit(p_value IN NUMBER);

  PROCEDURE set_revision_count(p_value IN NUMBER);
  --***END SETTERS

  PROCEDURE validate;

  PROCEDURE validate_remove;

  PROCEDURE add;

  PROCEDURE change;

  PROCEDURE remove;

END client_client_contact;