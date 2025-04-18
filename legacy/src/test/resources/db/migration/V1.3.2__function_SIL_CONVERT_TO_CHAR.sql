CREATE OR REPLACE FUNCTION THE.SIL_CONVERT_TO_CHAR 
(
	p_value IN DATE, 
	p_format IN VARCHAR2
) 
RETURN VARCHAR2 IS
	dateStr VARCHAR2(25);
BEGIN
    dateStr := TO_CHAR(p_value, p_format);
    RETURN (dateStr);
EXCEPTION
    WHEN OTHERS THEN
      RAISE;
END SIL_CONVERT_TO_CHAR;