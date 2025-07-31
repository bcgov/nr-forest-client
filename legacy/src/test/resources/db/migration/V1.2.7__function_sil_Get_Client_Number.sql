CREATE OR REPLACE FUNCTION THE.sil_Get_Client_Number

/******************************************************************************
Purpose:   Retrieve Client Number when passed an acronym
Return:    Either the retrieved client number or the passed value
           if none found as it is already a client number.

REVISIONS:
Ver        Date        Author           Description
---------  ----------  ---------------  ------------------------------------
1.0        01/28/2001  S.Petersen       Created.
******************************************************************************/
(p_client_acronym        IN VARCHAR2)

RETURN VARCHAR2 IS
	ClientNumber    VARCHAR2(8);

BEGIN

	SELECT CLIENT_NUMBER
	INTO ClientNumber
    FROM CLIENT_ACRONYM
	WHERE CLIENT_ACRONYM= p_client_acronym;

	RETURN(ClientNumber); -- Client Number found, return it

EXCEPTION

	WHEN NO_DATA_FOUND THEN
		RETURN(p_client_acronym); --It's not an acronym, assume it's a client number

END sil_Get_Client_Number;