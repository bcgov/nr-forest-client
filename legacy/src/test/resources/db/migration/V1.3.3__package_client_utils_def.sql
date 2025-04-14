CREATE OR REPLACE PACKAGE THE.client_utils AS
  PROCEDURE add_error(
    p_error_message                  IN OUT   sil_error_messages
  , p_db_field                       IN       VARCHAR2
  , p_message                        IN       VARCHAR2
  , p_params                         IN       sil_error_params DEFAULT NULL
  , p_warning_flag                   IN       BOOLEAN          DEFAULT FALSE);

  PROCEDURE append_arrays(
    p_orig_array                     IN OUT   sil_error_messages
  , p_add_array                      IN       sil_error_messages);
END client_utils;