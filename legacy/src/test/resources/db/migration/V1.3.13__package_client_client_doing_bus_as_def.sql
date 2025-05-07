CREATE OR REPLACE PACKAGE THE.client_client_doing_bus_as AS

  PROCEDURE get;

  --***START GETTERS
  FUNCTION error_raised RETURN BOOLEAN;

  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES;

  FUNCTION get_client_number RETURN VARCHAR2;

  FUNCTION get_doing_business_as_names RETURN client_generic_string_varray;

  FUNCTION get_update_timestamp RETURN DATE;

  FUNCTION get_update_userid RETURN VARCHAR2;

  FUNCTION get_update_org_unit RETURN NUMBER;

  FUNCTION get_add_timestamp RETURN DATE;

  FUNCTION get_add_userid RETURN VARCHAR2;

  FUNCTION get_add_org_unit RETURN NUMBER;

  --***END GETTERS

  --***START SETTERS

  PROCEDURE set_client_number(p_value IN VARCHAR2);

  PROCEDURE set_doing_business_as_names(p_value IN client_generic_string_varray);

  PROCEDURE set_update_timestamp(p_value IN DATE);

  PROCEDURE set_update_userid(p_value IN VARCHAR2);

  PROCEDURE set_update_org_unit(p_value IN NUMBER);

  PROCEDURE set_add_timestamp(p_value IN DATE);

  PROCEDURE set_add_userid(p_value IN VARCHAR2);

  PROCEDURE set_add_org_unit(p_value IN NUMBER);

  --***END SETTERS

  PROCEDURE init
  (p_client_number      IN VARCHAR2);

  PROCEDURE validate;

  PROCEDURE merge_dba;

END client_client_doing_bus_as;