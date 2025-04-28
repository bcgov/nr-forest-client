CREATE OR REPLACE PACKAGE BODY THE.client_utils AS
  PROCEDURE add_error(
    p_error_message                  IN OUT   sil_error_messages
  , p_db_field                       IN       VARCHAR2
  , p_message                        IN       VARCHAR2
  , p_params                         IN       sil_error_params DEFAULT NULL
  , p_warning_flag                   IN       BOOLEAN          DEFAULT FALSE)
  IS
    v_error_msg                        sil_error_message;
    v_warning_flag                     VARCHAR2(1) := 'N';
  BEGIN
    IF p_warning_flag THEN
      v_warning_flag := 'Y';
    END IF;

    v_error_msg := sil_error_message(p_db_field, p_message, p_params, v_warning_flag);

    IF    p_error_message IS NULL
       OR p_error_message.COUNT = 0 THEN
      p_error_message := sil_error_messages(v_error_msg);
    ELSE
      p_error_message.EXTEND;
      p_error_message(p_error_message.COUNT) := v_error_msg;
    END IF;
  END add_error;

  PROCEDURE append_arrays(
    p_orig_array                     IN OUT   sil_error_messages
  , p_add_array                      IN       sil_error_messages)
  IS
  BEGIN
    IF    p_orig_array IS NULL
       OR p_orig_array.COUNT = 0 THEN
      p_orig_array := p_add_array;
    ELSE
      IF     p_add_array IS NOT NULL
         AND p_add_array.COUNT > 0 THEN
        FOR i IN p_add_array.FIRST .. p_add_array.LAST LOOP
          p_orig_array.EXTEND;
          p_orig_array(p_orig_array.COUNT) := p_add_array(i);
        END LOOP;
      END IF;
    END IF;
  END append_arrays;
END client_utils;