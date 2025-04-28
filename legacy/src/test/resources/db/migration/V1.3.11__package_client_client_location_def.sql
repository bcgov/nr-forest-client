CREATE OR REPLACE PACKAGE THE.client_client_location AS

  PROCEDURE get;

  PROCEDURE init
  ( p_client_number             IN VARCHAR2 DEFAULT NULL
  , p_client_locn_code          IN VARCHAR2 DEFAULT NULL);

  --***START GETTERS
  FUNCTION error_raised RETURN BOOLEAN;

  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES;

  FUNCTION get_client_number RETURN VARCHAR2;

  FUNCTION get_client_locn_code RETURN VARCHAR2;

  FUNCTION get_client_locn_name RETURN VARCHAR2;

  FUNCTION get_hdbs_company_code RETURN VARCHAR2;

  FUNCTION get_address_1 RETURN VARCHAR2;

  FUNCTION get_address_2 RETURN VARCHAR2;

  FUNCTION get_address_3 RETURN VARCHAR2;

  FUNCTION get_city RETURN VARCHAR2;

  FUNCTION get_province RETURN VARCHAR2;

  FUNCTION get_postal_code RETURN VARCHAR2;

  FUNCTION get_country RETURN VARCHAR2;

  FUNCTION get_business_phone RETURN VARCHAR2;

  FUNCTION get_home_phone RETURN VARCHAR2;

  FUNCTION get_cell_phone RETURN VARCHAR2;

  FUNCTION get_fax_number RETURN VARCHAR2;

  FUNCTION get_email_address RETURN VARCHAR2;

  FUNCTION get_locn_expired_ind RETURN VARCHAR2;

  FUNCTION get_returned_mail_date RETURN DATE;

  FUNCTION get_trust_location_ind RETURN VARCHAR2;

  FUNCTION get_cli_locn_comment RETURN VARCHAR2;

  FUNCTION get_update_timestamp RETURN DATE;

  FUNCTION get_update_userid RETURN VARCHAR2;

  FUNCTION get_update_org_unit RETURN NUMBER;

  FUNCTION get_add_timestamp RETURN DATE;

  FUNCTION get_add_userid RETURN VARCHAR2;

  FUNCTION get_add_org_unit RETURN NUMBER;

  FUNCTION get_revision_count RETURN NUMBER;

  FUNCTION get_ur_reason_addr RETURN VARCHAR2;
  --***END GETTERS

  --***START SETTERS

  PROCEDURE set_client_number(p_value IN VARCHAR2);

  PROCEDURE set_client_locn_code(p_value IN VARCHAR2);

  PROCEDURE set_client_locn_name(p_value IN VARCHAR2);

  PROCEDURE set_hdbs_company_code(p_value IN VARCHAR2);

  PROCEDURE set_address_1(p_value IN VARCHAR2);

  PROCEDURE set_address_2(p_value IN VARCHAR2);

  PROCEDURE set_address_3(p_value IN VARCHAR2);

  PROCEDURE set_city(p_value IN VARCHAR2);

  PROCEDURE set_province(p_value IN VARCHAR2);

  PROCEDURE set_postal_code(p_value IN VARCHAR2);

  PROCEDURE set_country(p_value IN VARCHAR2);

  PROCEDURE set_business_phone(p_value IN VARCHAR2);

  PROCEDURE set_home_phone(p_value IN VARCHAR2);

  PROCEDURE set_cell_phone(p_value IN VARCHAR2);

  PROCEDURE set_fax_number(p_value IN VARCHAR2);

  PROCEDURE set_email_address(p_value IN VARCHAR2);

  PROCEDURE set_locn_expired_ind(p_value IN VARCHAR2);

  PROCEDURE set_returned_mail_date(p_value IN DATE);

  PROCEDURE set_trust_location_ind(p_value IN VARCHAR2);

  PROCEDURE set_cli_locn_comment(p_value IN VARCHAR2);

  PROCEDURE set_update_timestamp(p_value IN DATE);

  PROCEDURE set_update_userid(p_value IN VARCHAR2);

  PROCEDURE set_update_org_unit(p_value IN NUMBER);

  PROCEDURE set_add_timestamp(p_value IN DATE);

  PROCEDURE set_add_userid(p_value IN VARCHAR2);

  PROCEDURE set_add_org_unit(p_value IN NUMBER);

  PROCEDURE set_revision_count(p_value IN NUMBER);
  --***END SETTERS

  PROCEDURE process_update_reasons
  (p_ur_action_addr         IN OUT VARCHAR2
  ,p_ur_reason_addr         IN OUT VARCHAR2);

  PROCEDURE validate;

  PROCEDURE validate_remove;

  PROCEDURE add;

  PROCEDURE change;

  PROCEDURE remove;

  PROCEDURE expire_nonexpired_locns
  ( p_client_number       IN VARCHAR2
  , p_update_userid       IN VARCHAR2
  , p_update_timestamp    IN DATE
  , p_update_org_unit_no  IN NUMBER);

  PROCEDURE unexpire_locns
  ( p_client_number       IN VARCHAR2
  , p_date_deactivated    IN DATE
  , p_update_userid       IN VARCHAR2
  , p_update_timestamp    IN DATE
  , p_update_org_unit_no  IN NUMBER
  , p_deactivated_date    IN DATE);

END client_client_location;