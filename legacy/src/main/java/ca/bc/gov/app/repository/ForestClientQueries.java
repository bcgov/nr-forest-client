package ca.bc.gov.app.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForestClientQueries {

  public static final String CLIENT_INFORMATION_HISTORY = """
      WITH BASE_DATA AS (
        SELECT
            AL.FOREST_CLIENT_AUDIT_ID AS IDX,
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
            B.IDX,
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
                WHEN 'clientComment' THEN B.CLIENT_COMMENT
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
                    WHEN 'clientComment' THEN B.CLIENT_COMMENT
                END
            ) OVER (
                PARTITION BY B.CLIENT_NUMBER, COL.COLUMN_NAME
                ORDER BY B.IDX
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
            SELECT 'clientComment' AS COLUMN_NAME, 13 FROM DUAL
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
      WITH BASE_DATA AS (
        SELECT
            AL.CLIENT_LOCATION_AUDIT_ID || '-' || CLIENT_LOCN_CODE AS IDX,
            AL.CLIENT_LOCATION_AUDIT_ID,
            AL.CLIENT_LOCN_CODE,
            CASE
                WHEN AL.LOCN_EXPIRED_IND = 'Y' THEN 'Active'
                ELSE 'Deactivated'
            END AS LOCN_EXPIRED_IND,
            AL.CLIENT_LOCN_NAME,
            AL.ADDRESS_2,
            AL.ADDRESS_3,
            AL.ADDRESS_1,
            AL.CITY,
            CASE
                WHEN PC.PROVINCE_STATE_NAME IS NULL THEN AL.PROVINCE
                ELSE PC.PROVINCE_STATE_NAME
            END AS PROVINCE_DESC,
            AL.COUNTRY AS COUNTRY_DESC,
            AL.POSTAL_CODE,
            AL.EMAIL_ADDRESS,
            AL.BUSINESS_PHONE,
            AL.CELL_PHONE,
            AL.HOME_PHONE,
            AL.FAX_NUMBER,
            AL.CLI_LOCN_COMMENT,
            AL.HDBS_COMPANY_CODE,
            TO_CHAR(AL.RETURNED_MAIL_DATE, 'YYYY-MM-DD') AS RETURNED_MAIL_DATE,
            AL.TRUST_LOCATION_IND,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON
        FROM THE.CLI_LOCN_AUDIT AL
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
            ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
        LEFT OUTER JOIN THE.MAILING_PROVINCE_STATE PC
            ON AL.PROVINCE = PC.PROVINCE_STATE_CODE
        WHERE AL.CLIENT_NUMBER = :clientNumber
      ),
      AUDIT_DATA AS (
          SELECT
              'ClientLocation' AS TABLE_NAME,
              B.IDX,
              CASE
                  WHEN B.CHANGE_TYPE = 'INS' THEN 
                      CASE 
                          WHEN B.CLIENT_LOCN_NAME IS NOT NULL THEN 'Location "' || B.CLIENT_LOCN_CODE || ' - ' || B.CLIENT_LOCN_NAME || '" added'
                          ELSE 'Location "' || B.CLIENT_LOCN_CODE || '" added'
                      END
                  WHEN B.CHANGE_TYPE = 'UPD' THEN 
                      CASE 
                          WHEN B.CLIENT_LOCN_NAME IS NOT NULL THEN 'Location "' || B.CLIENT_LOCN_CODE || ' - ' || B.CLIENT_LOCN_NAME || '" updated'
                          ELSE 'Location "' || B.CLIENT_LOCN_CODE || '" updated'
                      END
              END AS IDENTIFIER_LABEL,
              COL.COLUMN_NAME,
              CASE COL.COLUMN_NAME
                  WHEN 'locnExpiredInd' THEN B.LOCN_EXPIRED_IND
                  WHEN 'locationName' THEN B.CLIENT_LOCN_NAME
                  WHEN 'addressTwo' THEN B.ADDRESS_2
                  WHEN 'addressThree' THEN B.ADDRESS_3
                  WHEN 'addressOne' THEN B.ADDRESS_1
                  WHEN 'city' THEN B.CITY
                  WHEN 'provinceDesc' THEN B.PROVINCE_DESC
                  WHEN 'countryDesc' THEN B.COUNTRY_DESC
                  WHEN 'postalCode' THEN B.POSTAL_CODE
                  WHEN 'emailAddress' THEN B.EMAIL_ADDRESS
                  WHEN 'businessPhone' THEN B.BUSINESS_PHONE
                  WHEN 'cellPhone' THEN B.CELL_PHONE
                  WHEN 'homePhone' THEN B.HOME_PHONE
                  WHEN 'faxNumber' THEN B.FAX_NUMBER
                  WHEN 'cliLocnComment' THEN B.CLI_LOCN_COMMENT
                  WHEN 'hdbsCompanyCode' THEN B.HDBS_COMPANY_CODE
                  WHEN 'returnedMailDate' THEN B.RETURNED_MAIL_DATE
                  WHEN 'trustLocationInd' THEN B.TRUST_LOCATION_IND
              END AS NEW_VALUE,
              LAG(
                  CASE COL.COLUMN_NAME
                      WHEN 'locnExpiredInd' THEN B.LOCN_EXPIRED_IND
                      WHEN 'locationName' THEN B.CLIENT_LOCN_NAME
                      WHEN 'addressTwo' THEN B.ADDRESS_2
                      WHEN 'addressThree' THEN B.ADDRESS_3
                      WHEN 'addressOne' THEN B.ADDRESS_1
                      WHEN 'city' THEN B.CITY
                      WHEN 'provinceDesc' THEN B.PROVINCE_DESC
                      WHEN 'countryDesc' THEN B.COUNTRY_DESC
                      WHEN 'postalCode' THEN B.POSTAL_CODE
                      WHEN 'emailAddress' THEN B.EMAIL_ADDRESS
                      WHEN 'businessPhone' THEN B.BUSINESS_PHONE
                      WHEN 'cellPhone' THEN B.CELL_PHONE
                      WHEN 'homePhone' THEN B.HOME_PHONE
                      WHEN 'faxNumber' THEN B.FAX_NUMBER
                      WHEN 'cliLocnComment' THEN B.CLI_LOCN_COMMENT
                      WHEN 'hdbsCompanyCode' THEN B.HDBS_COMPANY_CODE
                      WHEN 'returnedMailDate' THEN B.RETURNED_MAIL_DATE
                      WHEN 'trustLocationInd' THEN B.TRUST_LOCATION_IND
                  END
              ) OVER (
                  PARTITION BY B.CLIENT_LOCN_CODE, COL.COLUMN_NAME
                  ORDER BY B.CLIENT_LOCATION_AUDIT_ID
              ) AS OLD_VALUE,
              B.UPDATE_TIMESTAMP,
              B.UPDATE_USERID,
              B.CHANGE_TYPE,
              B.ACTION_CODE,
              B.REASON,
              COL.FIELD_ORDER
          FROM BASE_DATA B
          CROSS JOIN (
              SELECT 'locnExpiredInd' AS COLUMN_NAME, 1 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'locationName' AS COLUMN_NAME, 2 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'addressTwo' AS COLUMN_NAME, 3 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'addressThree' AS COLUMN_NAME, 4 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'addressOne' AS COLUMN_NAME, 5 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'city' AS COLUMN_NAME, 6 AS FIELD_ORDER FROM DUAL    
              UNION ALL
              SELECT 'provinceDesc' AS COLUMN_NAME, 7 AS FIELD_ORDER FROM DUAL    
              UNION ALL
              SELECT 'countryDesc' AS COLUMN_NAME, 8 AS FIELD_ORDER FROM DUAL 
              UNION ALL
              SELECT 'postalCode' AS COLUMN_NAME, 9 AS FIELD_ORDER FROM DUAL  
              UNION ALL
              SELECT 'emailAddress' AS COLUMN_NAME, 10 AS FIELD_ORDER FROM DUAL   
              UNION ALL
              SELECT 'businessPhone' AS COLUMN_NAME, 11 AS FIELD_ORDER FROM DUAL  
              UNION ALL
              SELECT 'cellPhone' AS COLUMN_NAME, 12 AS FIELD_ORDER FROM DUAL  
              UNION ALL
              SELECT 'homePhone' AS COLUMN_NAME, 13 AS FIELD_ORDER FROM DUAL  
              UNION ALL
              SELECT 'faxNumber' AS COLUMN_NAME, 14 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'cliLocnComment' AS COLUMN_NAME, 15 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'hdbsCompanyCode' AS COLUMN_NAME, 16 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'returnedMailDate' AS COLUMN_NAME, 17 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'trustLocationInd' AS COLUMN_NAME, 18 AS FIELD_ORDER FROM DUAL
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
}
