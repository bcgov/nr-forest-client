CREATE OR REPLACE PACKAGE THE.client_client_update_reason AS

  PROCEDURE get;

  PROCEDURE init;
  --***START GETTERS
  FUNCTION error_raised RETURN BOOLEAN;

  FUNCTION get_error_message RETURN SIL_ERROR_MESSAGES;

  FUNCTION get_client_update_reason_id RETURN NUMBER;

  FUNCTION get_client_update_action_code RETURN VARCHAR2;

  FUNCTION get_client_update_reason_code RETURN VARCHAR2;

  FUNCTION get_client_type_code RETURN VARCHAR2;

  FUNCTION get_forest_client_audit_id RETURN NUMBER;

  FUNCTION get_update_timestamp RETURN DATE;

  FUNCTION get_update_userid RETURN VARCHAR2;

  FUNCTION get_add_timestamp RETURN DATE;

  FUNCTION get_add_userid RETURN VARCHAR2;
  --***END GETTERS

  --***START SETTERS

  PROCEDURE set_client_update_reason_id(p_value IN NUMBER);

  PROCEDURE set_client_update_action_code(p_value IN VARCHAR2);

  PROCEDURE set_client_update_reason_code(p_value IN VARCHAR2);

  PROCEDURE set_client_type_code(p_value IN VARCHAR2);

  PROCEDURE set_forest_client_audit_id(p_value IN NUMBER);

  PROCEDURE set_update_timestamp(p_value IN DATE);

  PROCEDURE set_update_userid(p_value IN VARCHAR2);

  PROCEDURE set_add_timestamp(p_value IN DATE);

  PROCEDURE set_add_userid(p_value IN VARCHAR2);
  --***END SETTERS

  PROCEDURE validate;

  PROCEDURE add;

  FUNCTION check_client_name
  (p_old_client_name        IN VARCHAR2
  ,p_old_legal_first_name   IN VARCHAR2
  ,p_old_legal_middle_name  IN VARCHAR2
  ,p_new_client_name        IN VARCHAR2
  ,p_new_legal_first_name   IN VARCHAR2
  ,p_new_legal_middle_name  IN VARCHAR2)
  RETURN VARCHAR2;

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
  RETURN VARCHAR2;

  FUNCTION check_id
  (p_old_client_identification  IN VARCHAR2
  ,p_old_client_id_type_code    IN VARCHAR2
  ,p_new_client_identification  IN VARCHAR2
  ,p_new_client_id_type_code    IN VARCHAR2)
  RETURN VARCHAR2;

  FUNCTION check_status
  (p_old_client_status_code  IN VARCHAR2
  ,p_new_client_status_code  IN VARCHAR2)
  RETURN VARCHAR2;

END client_client_update_reason;