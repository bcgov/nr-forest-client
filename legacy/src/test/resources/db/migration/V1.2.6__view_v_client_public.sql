CREATE OR REPLACE VIEW THE.V_CLIENT_PUBLIC
(CLIENT_NUMBER,CLIENT_NAME,LEGAL_FIRST_NAME,LEGAL_MIDDLE_NAME,CLIENT_STATUS_CODE,CLIENT_TYPE_CODE)
AS
SELECT client_number
       , client_name
       , legal_first_name
       , legal_middle_name
       , client_status_code
       , client_type_code
    FROM forest_client;