package ca.bc.gov.app;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationConstants {

  public static final String ORACLE_ATTRIBUTE_SCHEMA = "THE";
  public static final String CLIENT_NUMBER = "CLIENT_NUMBER";
  public static final String CLIENT_NUMBER_LITERAL = "clientNumber";
  public static final String MDC_USERID = "X-USER";
  public static final String LOAD_CONTACT_QUERY = """
      SELECT
        CLIENT_CONTACT_ID
      FROM THE.CLIENT_CONTACT
      WHERE
        CLIENT_NUMBER = :client_number
        AND
        CONTACT_NAME = (
          SELECT CONTACT_NAME FROM CLIENT_CONTACT WHERE CLIENT_CONTACT_ID = :contact_id
        )""";
  public static final String UPDATE_CONTACT_AUDIT_QUERY = """
      UPDATE THE.CLI_CON_AUDIT
      SET
        UPDATE_USERID = :userid
      WHERE
        UPDATE_USERID = 'TRIGGERAUDIT'
        AND CLIENT_NUMBER = :client_number
        AND CLIENT_CONTACT_ID = :contact_id
        AND CLIENT_AUDIT_CODE = 'DEL'""";
}
