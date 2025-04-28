CREATE OR REPLACE PACKAGE THE.client_code_lists
IS
  TYPE client_code_lists_cur IS REF CURSOR;

  PROCEDURE get_reorg_type_codes(
    p_reorg_type_code                IN OUT   client_code_lists_cur);

  PROCEDURE get_reorg_status_codes(
    p_reorg_status_code              IN OUT   client_code_lists_cur);

  PROCEDURE get_client_location_codes(
    p_client_number                  IN OUT   VARCHAR2
  , p_client_location_code           IN OUT   client_code_lists_cur);

  PROCEDURE get_client_status_codes(
    p_client_status_code             IN OUT   client_code_lists_cur);

  PROCEDURE get_client_type_codes(
    p_client_type_code               IN OUT   client_code_lists_cur);

  PROCEDURE get_client_relationship_codes(
    p_client_relationship_code       IN OUT   client_code_lists_cur);

  PROCEDURE get_business_contact_codes(
    p_business_contact_code          IN OUT   client_code_lists_cur);

  PROCEDURE get_reg_company_type_codes(
    p_client_type_code               IN       VARCHAR2
   ,p_reg_company_type_code          IN OUT   client_code_lists_cur);

  PROCEDURE get_client_update_reason_codes(
    p_client_update_action_code      IN      VARCHAR2
   ,p_client_type_code               IN   VARCHAR2
   ,p_client_update_reason_code      IN OUT   client_code_lists_cur);

  PROCEDURE get_client_id_type_codes(
    p_client_id_type_code            IN OUT   client_code_lists_cur);

  FUNCTION get_client_update_action_desc(
    p_client_update_action_code      IN   VARCHAR2)
  RETURN VARCHAR2;

  --ADDRESS
  -->Country
  PROCEDURE get_country(
    p_countries                      IN OUT   client_code_lists_cur);
  -->Prov/State
  PROCEDURE get_prov(
    p_country                        IN       VARCHAR2
  , p_provinces                      IN OUT   client_code_lists_cur);
  -->City
  PROCEDURE get_city(
    p_country                        IN       VARCHAR2
  , p_province                       IN       VARCHAR2
  , p_cities                         IN OUT   client_code_lists_cur);

  PROCEDURE get_valid_relationships
  ( p_client_type_code   IN VARCHAR2
   ,p_relationships      IN OUT client_code_lists_cur);

END client_code_lists;