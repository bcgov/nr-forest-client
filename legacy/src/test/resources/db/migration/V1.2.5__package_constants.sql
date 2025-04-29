CREATE OR REPLACE PACKAGE THE.client_constants AS

  TYPE REF_CUR_T IS REF CURSOR;

  --Audit codes
  C_AUDIT_INSERT                       CONSTANT VARCHAR2(3) := 'INS';
  C_AUDIT_UPDATE                       CONSTANT VARCHAR2(3) := 'UPD';
  C_AUDIT_DELETE                       CONSTANT VARCHAR2(3) := 'DEL';


END client_constants;