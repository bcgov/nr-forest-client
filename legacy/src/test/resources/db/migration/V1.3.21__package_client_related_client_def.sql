CREATE OR REPLACE PACKAGE THE.client_related_client
AS
   PROCEDURE get;

   PROCEDURE init;

   --***START GETTERS
   FUNCTION error_raised
      RETURN BOOLEAN;

   FUNCTION primary_keys_changed
      RETURN BOOLEAN;

   FUNCTION get_error_message
      RETURN sil_error_messages;

   FUNCTION get_rowid
      RETURN VARCHAR2;

   FUNCTION get_client_number
      RETURN VARCHAR2;

   FUNCTION get_client_locn_code
      RETURN VARCHAR2;

   FUNCTION get_related_clnt_nmbr
      RETURN VARCHAR2;

   FUNCTION get_related_clnt_locn
      RETURN VARCHAR2;

   FUNCTION get_relationship_code
      RETURN VARCHAR2;

   FUNCTION get_signing_auth_ind
      RETURN VARCHAR2;

   FUNCTION get_percent_ownership
      RETURN NUMBER;

   FUNCTION get_update_timestamp
      RETURN DATE;

   FUNCTION get_update_userid
      RETURN VARCHAR2;

   FUNCTION get_update_org_unit
      RETURN NUMBER;

   FUNCTION get_add_timestamp
      RETURN DATE;

   FUNCTION get_add_userid
      RETURN VARCHAR2;

   FUNCTION get_add_org_unit
      RETURN NUMBER;

   FUNCTION get_revision_count
      RETURN NUMBER;

   --***END GETTERS

   --***START SETTERS
   PROCEDURE set_rowid (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_client_number (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_client_locn_code (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_related_clnt_nmbr (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_related_clnt_locn (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_relationship_code (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_signing_auth_ind (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_percent_ownership (
      p_value   IN   NUMBER
   );

   PROCEDURE set_update_timestamp (
      p_value   IN   DATE
   );

   PROCEDURE set_update_userid (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_update_org_unit (
      p_value   IN   NUMBER
   );

   PROCEDURE set_add_timestamp (
      p_value   IN   DATE
   );

   PROCEDURE set_add_userid (
      p_value   IN   VARCHAR2
   );

   PROCEDURE set_add_org_unit (
      p_value   IN   NUMBER
   );

   PROCEDURE set_revision_count (
      p_value   IN   NUMBER
   );

   --***END SETTERS
   PROCEDURE VALIDATE;

   PROCEDURE validate_remove;

   PROCEDURE ADD;

   PROCEDURE CHANGE;

   PROCEDURE remove;
END client_related_client;