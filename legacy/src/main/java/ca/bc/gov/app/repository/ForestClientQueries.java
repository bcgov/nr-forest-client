package ca.bc.gov.app.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForestClientQueries {

  public static final String CLIENT_INFORMATION_HISTORY = """
      WITH AUDIT_DATA AS (
      SELECT
          'ClientInformation' AS TABLE_NAME,
          AL.FOREST_CLIENT_AUDIT_ID AS IDX,
          'clientName' AS COLUMN_NAME,
          CLIENT_NAME AS NEW_VALUE,
          LAG(CLIENT_NAME) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ) AS OLD_VALUE,
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
          UR.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
          RC.DESCRIPTION AS REASON,
          1 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'clientAcronym',
          CLIENT_ACRONYM,
          LAG(CLIENT_ACRONYM) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          2 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'legalFirstName',
          LEGAL_FIRST_NAME,
          LAG(LEGAL_FIRST_NAME) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          3 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'legalMiddleName',
          LEGAL_MIDDLE_NAME,
          LAG(LEGAL_MIDDLE_NAME) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          4 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'clientTypeDesc',
          TC.DESCRIPTION,
          LAG(TC.DESCRIPTION) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          5 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      LEFT OUTER JOIN THE.CLIENT_TYPE_CODE TC
          ON AL.CLIENT_TYPE_CODE = TC.CLIENT_TYPE_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'birthdate',
          TO_CHAR(BIRTHDATE, 'YYYY-MM-DD'),
          LAG(TO_CHAR(BIRTHDATE, 'YYYY-MM-DD')) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          6 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'clientIdTypeDesc',
          IDTC.DESCRIPTION,
          LAG(IDTC.DESCRIPTION) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          7 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      LEFT OUTER JOIN THE.CLIENT_ID_TYPE_CODE IDTC
          ON AL.CLIENT_ID_TYPE_CODE = IDTC.CLIENT_ID_TYPE_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'clientIdentification',
          CLIENT_IDENTIFICATION,
          LAG(CLIENT_IDENTIFICATION) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          8 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'corpRegnNmbr',
          REGISTRY_COMPANY_TYPE_CODE || CORP_REGN_NMBR,
          LAG(REGISTRY_COMPANY_TYPE_CODE) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ) || LAG(CORP_REGN_NMBR) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          9 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'wcbFirmNumber',
          WCB_FIRM_NUMBER,
          LAG(WCB_FIRM_NUMBER) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          10 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'ocgSupplierNmbr',
          OCG_SUPPLIER_NMBR,
          LAG(OCG_SUPPLIER_NMBR) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          11 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'clientStatusDesc',
          SC.DESCRIPTION,
          LAG(SC.DESCRIPTION) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          12 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      LEFT OUTER JOIN THE.CLIENT_STATUS_CODE SC
          ON AL.CLIENT_STATUS_CODE = SC.CLIENT_STATUS_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber

      UNION ALL

      SELECT
          'ClientInformation',
          AL.FOREST_CLIENT_AUDIT_ID,
          'notes',
          CLIENT_COMMENT,
          LAG(CLIENT_COMMENT) OVER (
              PARTITION BY AL.CLIENT_NUMBER
              ORDER BY AL.FOREST_CLIENT_AUDIT_ID
          ),
          AL.UPDATE_TIMESTAMP,
          AL.UPDATE_USERID,
          AL.CLIENT_AUDIT_CODE,
          UR.CLIENT_UPDATE_ACTION_CODE,
          RC.DESCRIPTION,
          13 AS FIELD_ORDER
      FROM THE.FOR_CLI_AUDIT AL
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON UR
          ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
      LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
          ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
      WHERE AL.CLIENT_NUMBER = :clientNumber
      )

      SELECT
      A.TABLE_NAME,
      A.IDX,
      '' AS IDENTIFIER_LABEL,
      A.COLUMN_NAME,
      A.OLD_VALUE,
      A.NEW_VALUE,
      A.UPDATE_TIMESTAMP,
      A.UPDATE_USERID,
      A.CHANGE_TYPE,
      A.ACTION_CODE,
      A.REASON
      FROM AUDIT_DATA A
      WHERE (
      (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL) OR
      (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL) OR
      (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE)) OR
      (OLD_VALUE IS NULL AND NEW_VALUE IS NULL AND CHANGE_TYPE = 'INS')
      )
      ORDER BY A.IDX DESC, A.FIELD_ORDER ASC
      """;

}
