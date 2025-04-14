CREATE OR REPLACE FUNCTION THE.client_get_client_name
/******************************************************************************
Purpose:   Retrieve Client Name, return null if none found

REVISIONS:
Ver          Date        Author           Description
---------   ----------   ---------------  ------------------------------------
1.0        July 25/2008  T. Blanchard    Original.- created to replace
                                         sil_get_client_name which has a defect

******************************************************************************/
(
  p_client_number                  IN       VARCHAR2)
  RETURN VARCHAR2
IS
  v_client_name                         VARCHAR2(150);
  v_first_name                          VARCHAR2(60);
  v_middle_name                         VARCHAR2(60);
BEGIN
  SELECT client_name
       , legal_first_name
       , legal_middle_name
    INTO v_client_name
       , v_first_name
       , v_middle_name
    FROM v_client_public
   WHERE client_number = p_client_number;

  IF    (TRIM(v_first_name) IS NOT NULL)
     OR (TRIM(v_middle_name) IS NOT NULL) THEN
    v_client_name := v_client_name || ', ' || v_first_name || ' ' || v_middle_name;
  END IF;

  RETURN(v_client_name);
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RETURN(NULL);                                                                  -- no name found
END client_get_client_name;