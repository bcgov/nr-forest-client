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