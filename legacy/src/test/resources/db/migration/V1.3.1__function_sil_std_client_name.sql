CREATE OR REPLACE FUNCTION THE.sil_std_client_name
/******************************************************************************
Purpose:   Returns standard client name given:
             FOREST_CLIENT.CLIENT_NAME
             FOREST_CLIENT.LEGAL_FIRST_NAME
             FOREST_CLIENT.LEGAL_MIDDLE_NAME

REVISIONS:
Date        Author           Description
----------  ---------------  --------------------------------------------------
2006-08-08 R.A.Rob           Taken from SIL_GET_CLIENT_NAME
******************************************************************************/
(
  p_client_name                    IN       VARCHAR2
, p_legal_first_name               IN       VARCHAR2
, p_legal_middle_name              IN       VARCHAR2)
  RETURN VARCHAR2
IS
  v_client_name                      VARCHAR2(200);
BEGIN
  IF (TRIM(p_legal_first_name) IS NOT NULL)
     OR(TRIM(p_legal_middle_name) IS NOT NULL) THEN
    v_client_name := p_client_name || ', ' || p_legal_first_name || ' ' || p_legal_middle_name;
  ELSE
    v_client_name := p_client_name;
  END IF;

  RETURN(v_client_name);
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RETURN(NULL);   -- no name found
END sil_std_client_name;