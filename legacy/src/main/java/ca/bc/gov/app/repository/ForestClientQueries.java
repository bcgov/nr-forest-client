package ca.bc.gov.app.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForestClientQueries {

  public static final String CLIENT_INFORMATION_HISTORY = """
      WITH BASE_DATA AS (
        SELECT
            AL.FOREST_CLIENT_AUDIT_ID,
            AL.CLIENT_NUMBER,
            AL.CLIENT_NAME,
            AL.CLIENT_ACRONYM,
            AL.LEGAL_FIRST_NAME,
            AL.LEGAL_MIDDLE_NAME,
            AL.CLIENT_TYPE_CODE,
            TO_CHAR(AL.BIRTHDATE, 'YYYY-MM-DD') AS BIRTHDATE,
            AL.CLIENT_ID_TYPE_CODE,
            AL.CLIENT_IDENTIFICATION,
            AL.REGISTRY_COMPANY_TYPE_CODE || AL.CORP_REGN_NMBR AS CORP_REGN_NMBR,
            AL.WCB_FIRM_NUMBER,
            AL.OCG_SUPPLIER_NMBR,
            AL.CLIENT_STATUS_CODE,
            AL.CLIENT_COMMENT,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            UR.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            TC.DESCRIPTION AS CLIENT_TYPE_DESC,
            IDTC.DESCRIPTION AS CLIENT_ID_TYPE_DESC,
            SC.DESCRIPTION AS CLIENT_STATUS_DESC
        FROM THE.FOR_CLI_AUDIT AL
        LEFT JOIN THE.CLIENT_UPDATE_REASON UR
            ON AL.FOREST_CLIENT_AUDIT_ID = UR.FOREST_CLIENT_AUDIT_ID
        LEFT JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON UR.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        LEFT JOIN THE.CLIENT_TYPE_CODE TC
            ON AL.CLIENT_TYPE_CODE = TC.CLIENT_TYPE_CODE
        LEFT JOIN THE.CLIENT_ID_TYPE_CODE IDTC
            ON AL.CLIENT_ID_TYPE_CODE = IDTC.CLIENT_ID_TYPE_CODE
        LEFT JOIN THE.CLIENT_STATUS_CODE SC
            ON AL.CLIENT_STATUS_CODE = SC.CLIENT_STATUS_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
      ),
      AUDIT_DATA AS (
        SELECT
            'ClientInformation' AS TABLE_NAME,
            B.FOREST_CLIENT_AUDIT_ID AS IDX,
            CASE
                WHEN B.CHANGE_TYPE = 'INS' THEN 'Client created'
                WHEN B.CHANGE_TYPE = 'UPD' THEN 'Client summary updated'
            END AS IDENTIFIER_LABEL,
            COL.COLUMN_NAME,
            CASE COL.COLUMN_NAME
                WHEN 'clientName' THEN B.CLIENT_NAME
                WHEN 'clientAcronym' THEN B.CLIENT_ACRONYM
                WHEN 'legalFirstName' THEN B.LEGAL_FIRST_NAME
                WHEN 'legalMiddleName' THEN B.LEGAL_MIDDLE_NAME
                WHEN 'clientTypeDesc' THEN B.CLIENT_TYPE_DESC
                WHEN 'birthdate' THEN B.BIRTHDATE
                WHEN 'clientIdTypeDesc' THEN B.CLIENT_ID_TYPE_DESC
                WHEN 'clientIdentification' THEN B.CLIENT_IDENTIFICATION
                WHEN 'corpRegnNmbr' THEN B.CORP_REGN_NMBR
                WHEN 'wcbFirmNumber' THEN B.WCB_FIRM_NUMBER
                WHEN 'ocgSupplierNmbr' THEN B.OCG_SUPPLIER_NMBR
                WHEN 'clientStatusDesc' THEN B.CLIENT_STATUS_DESC
                WHEN 'notes' THEN B.CLIENT_COMMENT
            END AS NEW_VALUE,
            LAG(
                CASE COL.COLUMN_NAME
                    WHEN 'clientName' THEN B.CLIENT_NAME
                    WHEN 'clientAcronym' THEN B.CLIENT_ACRONYM
                    WHEN 'legalFirstName' THEN B.LEGAL_FIRST_NAME
                    WHEN 'legalMiddleName' THEN B.LEGAL_MIDDLE_NAME
                    WHEN 'clientTypeDesc' THEN B.CLIENT_TYPE_DESC
                    WHEN 'birthdate' THEN B.BIRTHDATE
                    WHEN 'clientIdTypeDesc' THEN B.CLIENT_ID_TYPE_DESC
                    WHEN 'clientIdentification' THEN B.CLIENT_IDENTIFICATION
                    WHEN 'corpRegnNmbr' THEN B.CORP_REGN_NMBR
                    WHEN 'wcbFirmNumber' THEN B.WCB_FIRM_NUMBER
                    WHEN 'ocgSupplierNmbr' THEN B.OCG_SUPPLIER_NMBR
                    WHEN 'clientStatusDesc' THEN B.CLIENT_STATUS_DESC
                    WHEN 'notes' THEN B.CLIENT_COMMENT
                END
            ) OVER (
                PARTITION BY B.CLIENT_NUMBER, COL.COLUMN_NAME
                ORDER BY B.FOREST_CLIENT_AUDIT_ID
            ) AS OLD_VALUE,
            B.UPDATE_TIMESTAMP,
            B.UPDATE_USERID,
            B.CHANGE_TYPE,
            B.ACTION_CODE,
            B.REASON,
            COL.FIELD_ORDER
        FROM BASE_DATA B
        CROSS JOIN (
            SELECT 'clientName' AS COLUMN_NAME, 1 AS FIELD_ORDER FROM DUAL
            UNION ALL
            SELECT 'clientAcronym' AS COLUMN_NAME, 2 FROM DUAL
            UNION ALL
            SELECT 'legalFirstName' AS COLUMN_NAME, 3 FROM DUAL
            UNION ALL
            SELECT 'legalMiddleName' AS COLUMN_NAME, 4 FROM DUAL
            UNION ALL
            SELECT 'clientTypeDesc' AS COLUMN_NAME, 5 FROM DUAL
            UNION ALL
            SELECT 'birthdate' AS COLUMN_NAME, 6 FROM DUAL
            UNION ALL
            SELECT 'clientIdTypeDesc' AS COLUMN_NAME, 7 FROM DUAL
            UNION ALL
            SELECT 'clientIdentification' AS COLUMN_NAME, 8 FROM DUAL
            UNION ALL
            SELECT 'corpRegnNmbr' AS COLUMN_NAME, 9 FROM DUAL
            UNION ALL
            SELECT 'wcbFirmNumber' AS COLUMN_NAME, 10 FROM DUAL
            UNION ALL
            SELECT 'ocgSupplierNmbr' AS COLUMN_NAME, 11 FROM DUAL
            UNION ALL
            SELECT 'clientStatusDesc' AS COLUMN_NAME, 12 FROM DUAL
            UNION ALL
            SELECT 'notes' AS COLUMN_NAME, 13 FROM DUAL
        ) COL
      )
      SELECT
        A.TABLE_NAME,
        A.IDX,
        A.IDENTIFIER_LABEL,
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
        (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
      )
      ORDER BY A.IDX DESC, A.FIELD_ORDER ASC
      """;
  
  public static final String LOCATION_HISTORY = """
      WITH AUDIT_DATA AS (
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'locnExpiredInd' AS COLUMN_NAME,
            CASE
                WHEN AL.LOCN_EXPIRED_IND = 'Y' THEN 'Active'
                ELSE 'Deactivated'
            END AS NEW_VALUE,
            LAG(
                CASE
                    WHEN AL.LOCN_EXPIRED_IND = 'Y' THEN 'Active'
                    ELSE 'Deactivated'
                END
            ) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            1 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'locationName' AS COLUMN_NAME,
            AL.CLIENT_LOCN_NAME AS NEW_VALUE,
            LAG(AL.CLIENT_LOCN_NAME) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            2 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'addressTwo' AS COLUMN_NAME,
            AL.ADDRESS_2 AS NEW_VALUE,
            LAG(AL.ADDRESS_2) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            3 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'addressThree' AS COLUMN_NAME,
            AL.ADDRESS_3 AS NEW_VALUE,
            LAG(AL.ADDRESS_3) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            4 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'addressOne' AS COLUMN_NAME,
            AL.ADDRESS_1 AS NEW_VALUE,
            LAG(AL.ADDRESS_1) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            5 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'city' AS COLUMN_NAME,
            AL.CITY AS NEW_VALUE,
            LAG(AL.CITY) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            6 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'provinceDesc' AS COLUMN_NAME,
            CASE
                WHEN PC.PROVINCE_STATE_NAME IS NULL THEN AL.PROVINCE
                ELSE PC.PROVINCE_STATE_NAME
            END AS NEW_VALUE,
            LAG(
                CASE
                    WHEN PC.PROVINCE_STATE_NAME IS NULL THEN AL.PROVINCE
                    ELSE PC.PROVINCE_STATE_NAME
                END
            ) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            7 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        LEFT OUTER JOIN THE.MAILING_PROVINCE_STATE PC
            ON AL.PROVINCE = PC.PROVINCE_STATE_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'country' AS COLUMN_NAME,
            AL.COUNTRY AS NEW_VALUE,
            LAG(AL.COUNTRY) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            8 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'postalCode' AS COLUMN_NAME,
            AL.POSTAL_CODE AS NEW_VALUE,
            LAG(AL.POSTAL_CODE) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            9 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'emailAddress' AS COLUMN_NAME,
            AL.EMAIL_ADDRESS AS NEW_VALUE,
            LAG(AL.EMAIL_ADDRESS) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            10 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'businessPhone' AS COLUMN_NAME,
            AL.BUSINESS_PHONE AS NEW_VALUE,
            LAG(AL.BUSINESS_PHONE) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            11 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'cellPhone' AS COLUMN_NAME,
            AL.CELL_PHONE AS NEW_VALUE,
            LAG(AL.CELL_PHONE) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            12 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'homePhone' AS COLUMN_NAME,
            AL.HOME_PHONE AS NEW_VALUE,
            LAG(AL.HOME_PHONE) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            13 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'faxNumber' AS COLUMN_NAME,
            AL.FAX_NUMBER AS NEW_VALUE,
            LAG(AL.FAX_NUMBER) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            14 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'cliLocnComment' AS COLUMN_NAME,
            AL.CLI_LOCN_COMMENT AS NEW_VALUE,
            LAG(AL.CLI_LOCN_COMMENT) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            15 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'hdbsCompanyCode' AS COLUMN_NAME,
            AL.HDBS_COMPANY_CODE AS NEW_VALUE,
            LAG(AL.HDBS_COMPANY_CODE) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            16 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'returnedMailDate' AS COLUMN_NAME,
            TO_CHAR(AL.RETURNED_MAIL_DATE, 'YYYY-MM-DD') AS NEW_VALUE,
            LAG(TO_CHAR(AL.RETURNED_MAIL_DATE, 'YYYY-MM-DD')) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            17 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
        
        UNION ALL
        
        SELECT
            'ClientLocation' AS TABLE_NAME,
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            'trustLocationInd' AS COLUMN_NAME,
            AL.TRUST_LOCATION_IND AS NEW_VALUE,
            LAG(AL.TRUST_LOCATION_IND) OVER (
                PARTITION BY AL.CLIENT_LOCN_CODE 
                ORDER BY AL.CLIENT_LOCATION_AUDIT_ID
            ) AS OLD_VALUE,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON,
            18 AS FIELD_ORDER
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
      )
      
      SELECT
          A.TABLE_NAME,
          A.IDX,
          CASE
              WHEN A.CHANGE_TYPE = 'INS' THEN 
                  CASE 
                      WHEN CL.CLIENT_LOCN_NAME IS NOT NULL THEN 'Location "' || 
                           CL.CLIENT_LOCN_CODE || ' - ' || CL.CLIENT_LOCN_NAME || '" added'
                      ELSE 'Location "' || CL.CLIENT_LOCN_CODE || '" added'
                  END
              WHEN A.CHANGE_TYPE = 'UPD' THEN 
                  CASE 
                      WHEN CL.CLIENT_LOCN_NAME IS NOT NULL THEN 'Location "' || 
                           CL.CLIENT_LOCN_CODE || ' - ' || CL.CLIENT_LOCN_NAME || '" updated'
                      ELSE 'Location "' || CL.CLIENT_LOCN_CODE || '" updated'
                  END
          END AS IDENTIFIER_LABEL,
          A.COLUMN_NAME,
          A.OLD_VALUE,
          A.NEW_VALUE,
          A.UPDATE_TIMESTAMP,
          A.UPDATE_USERID,
          A.CHANGE_TYPE,
          A.ACTION_CODE,
          A.REASON
      FROM AUDIT_DATA A
      LEFT OUTER JOIN THE.CLIENT_LOCATION CL
          ON SUBSTR(A.IDX, -2) = CL.CLIENT_LOCN_CODE
      WHERE (
          (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL) OR
          (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL) OR
          (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
      )
      AND CL.CLIENT_NUMBER = :clientNumber
      ORDER BY A.IDX DESC, A.FIELD_ORDER ASC    
      """;
}
