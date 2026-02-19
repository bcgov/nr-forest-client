package ca.bc.gov.app.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForestClientQueries {

  public static final String ORACLE_PAGINATION =
      " OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";

  public static final String ORDER_BY =
      " ORDER BY SCORE DESC, C.CLIENT_NAME ASC, C.LEGAL_FIRST_NAME ASC, "
          + "C.CLIENT_NUMBER ASC ";

  public static final String SELECT_COUNT_C_CLIENT_NUMBER =
      "SELECT COUNT(C.CLIENT_NUMBER) ";

  public static final String CLIENT_INFORMATION_HISTORY = """
      WITH BASE_DATA AS (
        SELECT
            AL.FOREST_CLIENT_AUDIT_ID AS IDX,
            AL.CLIENT_NUMBER,
            CASE
                WHEN AL.LEGAL_FIRST_NAME IS NOT NULL AND AL.LEGAL_MIDDLE_NAME IS NOT NULL THEN
                    AL.LEGAL_FIRST_NAME || ' ' || AL.LEGAL_MIDDLE_NAME || ' ' || AL.CLIENT_NAME
                WHEN AL.LEGAL_FIRST_NAME IS NOT NULL THEN
                    AL.LEGAL_FIRST_NAME || ' ' || AL.CLIENT_NAME
                ELSE
                    NVL(AL.LEGAL_FIRST_NAME, '') || NVL(AL.LEGAL_MIDDLE_NAME, '') || AL.CLIENT_NAME
            END AS FULL_NAME,
            AL.CLIENT_ACRONYM,
            AL.LEGAL_MIDDLE_NAME,
            AL.CLIENT_TYPE_CODE,
            TO_CHAR(AL.BIRTHDATE, 'YYYY-MM-DD') AS BIRTHDATE,
            AL.CLIENT_ID_TYPE_CODE,
            AL.CLIENT_IDENTIFICATION,
            AL.REGISTRY_COMPANY_TYPE_CODE || AL.CORP_REGN_NMBR AS CORP_REGN_NMBR,
            TRIM(AL.WCB_FIRM_NUMBER) AS WCB_FIRM_NUMBER,
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
                WHEN 'clientAcronym' THEN B.CLIENT_ACRONYM
                WHEN 'fullName' THEN B.FULL_NAME
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
                    WHEN 'clientAcronym' THEN B.CLIENT_ACRONYM
                    WHEN 'fullName' THEN B.FULL_NAME
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
            SELECT 'fullName' AS COLUMN_NAME, 1 AS FIELD_ORDER FROM DUAL
            UNION ALL
            SELECT 'clientAcronym' AS COLUMN_NAME, 2 FROM DUAL
            UNION ALL
            SELECT 'clientTypeDesc' AS COLUMN_NAME, 3 FROM DUAL
            UNION ALL
            SELECT 'birthdate' AS COLUMN_NAME, 4 FROM DUAL
            UNION ALL
            SELECT 'clientIdTypeDesc' AS COLUMN_NAME, 5 FROM DUAL
            UNION ALL
            SELECT 'clientIdentification' AS COLUMN_NAME, 6 FROM DUAL
            UNION ALL
            SELECT 'corpRegnNmbr' AS COLUMN_NAME, 7 FROM DUAL
            UNION ALL
            SELECT 'wcbFirmNumber' AS COLUMN_NAME, 8 FROM DUAL
            UNION ALL
            SELECT 'ocgSupplierNmbr' AS COLUMN_NAME, 9 FROM DUAL
            UNION ALL
            SELECT 'clientStatusDesc' AS COLUMN_NAME, 10 FROM DUAL
            UNION ALL
            SELECT 'clientComment' AS COLUMN_NAME, 11 FROM DUAL
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
                WHEN AL.LOCN_EXPIRED_IND = 'Y' THEN 'Deactivated'
                WHEN AL.LOCN_EXPIRED_IND = 'N' THEN 'Active'
            END AS LOCN_EXPIRED_IND,
            AL.CLIENT_LOCN_NAME,
            TRIM(
              COALESCE(AL.ADDRESS_1, '') ||
              CASE
                WHEN AL.ADDRESS_2 IS NOT NULL THEN '<br>' || AL.ADDRESS_2
                ELSE ''
              END ||
              CASE
                WHEN AL.ADDRESS_3 IS NOT NULL THEN
                  '<br>' || AL.ADDRESS_3
                ELSE ''
              END
            ) AS ADDRESS,
            AL.CITY,
            CASE
                WHEN UPPER(AL.COUNTRY) IN ('USA', 'US', 'UNITED STATES OF AMERICA')
                    THEN PC.PROVINCE_STATE_NAME
                ELSE NULL
            END AS STATE_DESC,
            CASE
                WHEN UPPER(AL.COUNTRY) IN ('USA', 'US', 'UNITED STATES OF AMERICA')
                    THEN NULL
                WHEN PC.PROVINCE_STATE_NAME IS NULL THEN AL.PROVINCE
                ELSE PC.PROVINCE_STATE_NAME
            END AS PROVINCE_DESC,
            AL.COUNTRY AS COUNTRY_DESC,
            CASE
              WHEN UPPER(AL.COUNTRY) IN ('USA', 'US', 'UNITED STATES OF AMERICA')
                THEN AL.POSTAL_CODE
              ELSE NULL
            END AS ZIP_CODE,

            CASE
              WHEN UPPER(AL.COUNTRY) IN ('USA', 'US', 'UNITED STATES OF AMERICA')
                THEN NULL
              ELSE AL.POSTAL_CODE
            END AS POSTAL_CODE,
            AL.EMAIL_ADDRESS,
            CASE
              WHEN LENGTH(AL.BUSINESS_PHONE) = 10 AND REGEXP_LIKE(AL.BUSINESS_PHONE, '^\\d{10}$') THEN
                '(' || SUBSTR(AL.BUSINESS_PHONE, 1, 3) || ') ' ||
                SUBSTR(AL.BUSINESS_PHONE, 4, 3) || '-' ||
                SUBSTR(AL.BUSINESS_PHONE, 7, 4)
              ELSE AL.BUSINESS_PHONE
            END AS BUSINESS_PHONE,
            CASE
                WHEN LENGTH(AL.CELL_PHONE) = 10 AND REGEXP_LIKE(AL.CELL_PHONE, '^\\d{10}$') THEN
                  '(' || SUBSTR(AL.CELL_PHONE, 1, 3) || ') ' ||
                  SUBSTR(AL.CELL_PHONE, 4, 3) || '-' ||
                  SUBSTR(AL.CELL_PHONE, 7, 4)
                ELSE AL.CELL_PHONE
            END AS CELL_PHONE,
            CASE
                WHEN LENGTH(AL.HOME_PHONE) = 10 AND REGEXP_LIKE(AL.HOME_PHONE, '^\\d{10}$') THEN
                  '(' || SUBSTR(AL.HOME_PHONE, 1, 3) || ') ' ||
                  SUBSTR(AL.HOME_PHONE, 4, 3) || '-' ||
                  SUBSTR(AL.HOME_PHONE, 7, 4)
                ELSE AL.HOME_PHONE
            END AS HOME_PHONE,
            CASE
                WHEN LENGTH(AL.FAX_NUMBER) = 10 AND REGEXP_LIKE(AL.FAX_NUMBER, '^\\d{10}$') THEN
                  '(' || SUBSTR(AL.FAX_NUMBER, 1, 3) || ') ' ||
                  SUBSTR(AL.FAX_NUMBER, 4, 3) || '-' ||
                  SUBSTR(AL.FAX_NUMBER, 7, 4)
                ELSE AL.FAX_NUMBER
            END AS FAX_NUMBER,
            AL.CLI_LOCN_COMMENT,
            AL.HDBS_COMPANY_CODE,
            TO_CHAR(AL.RETURNED_MAIL_DATE, 'YYYY-MM-DD') AS RETURNED_MAIL_DATE,
            CASE
                WHEN AL.TRUST_LOCATION_IND = 'Y' THEN 'Yes'
                WHEN AL.TRUST_LOCATION_IND = 'N' THEN 'No'
            END AS TRUST_LOCATION_IND,
            AL.UPDATE_TIMESTAMP,
            AL.UPDATE_USERID,
            AL.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
            AL.CLIENT_UPDATE_ACTION_CODE AS ACTION_CODE,
            RC.DESCRIPTION AS REASON
        FROM THE.CLI_LOCN_AUDIT AL
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
                  WHEN 'address' THEN B.ADDRESS
                  WHEN 'city' THEN B.CITY
                  WHEN 'provinceDesc' THEN B.PROVINCE_DESC
                  WHEN 'stateDesc' THEN B.STATE_DESC
                  WHEN 'countryDesc' THEN B.COUNTRY_DESC
                  WHEN 'postalCode' THEN B.POSTAL_CODE
                  WHEN 'zipCode' THEN B.ZIP_CODE
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
                      WHEN 'address' THEN B.ADDRESS
                      WHEN 'city' THEN B.CITY
                      WHEN 'provinceDesc' THEN B.PROVINCE_DESC
                      WHEN 'stateDesc' THEN B.STATE_DESC
                      WHEN 'countryDesc' THEN B.COUNTRY_DESC
                      WHEN 'postalCode' THEN B.POSTAL_CODE
                      WHEN 'zipCode' THEN B.ZIP_CODE
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
              SELECT 'address' AS COLUMN_NAME, 3 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'city' AS COLUMN_NAME, 4 AS FIELD_ORDER FROM DUAL    
              UNION ALL
              SELECT 'provinceDesc' AS COLUMN_NAME, 5 AS FIELD_ORDER FROM DUAL 
              UNION ALL
              SELECT 'stateDesc' AS COLUMN_NAME, 6 AS FIELD_ORDER FROM DUAL 
              UNION ALL
              SELECT 'countryDesc' AS COLUMN_NAME, 7 AS FIELD_ORDER FROM DUAL 
              UNION ALL
              SELECT 'postalCode' AS COLUMN_NAME, 8 AS FIELD_ORDER FROM DUAL  
              UNION ALL
              SELECT 'zipCode' AS COLUMN_NAME, 9 AS FIELD_ORDER FROM DUAL  
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
  
  public static final String CONTACT_HISTORY = """
      WITH BASE_DATA AS (
          SELECT
              AL.CLIENT_CONTACT_AUDIT_ID,
              AL.CLIENT_CONTACT_ID,
              AL.CLIENT_AUDIT_CODE,
              AL.CONTACT_NAME,
              CASE
                WHEN LENGTH(AL.BUSINESS_PHONE) = 10 AND REGEXP_LIKE(AL.BUSINESS_PHONE, '^\\d{10}$') THEN
                  '(' || SUBSTR(AL.BUSINESS_PHONE, 1, 3) || ') ' ||
                  SUBSTR(AL.BUSINESS_PHONE, 4, 3) || '-' ||
                  SUBSTR(AL.BUSINESS_PHONE, 7, 4)
                ELSE AL.BUSINESS_PHONE
              END AS BUSINESS_PHONE,
              CASE
                WHEN LENGTH(AL.CELL_PHONE) = 10 AND REGEXP_LIKE(AL.CELL_PHONE, '^\\d{10}$') THEN
                  '(' || SUBSTR(AL.CELL_PHONE, 1, 3) || ') ' ||
                  SUBSTR(AL.CELL_PHONE, 4, 3) || '-' ||
                  SUBSTR(AL.CELL_PHONE, 7, 4)
                ELSE AL.CELL_PHONE
              END AS CELL_PHONE,
              CASE
                WHEN LENGTH(AL.FAX_NUMBER) = 10 AND REGEXP_LIKE(AL.FAX_NUMBER, '^\\d{10}$') THEN
                  '(' || SUBSTR(AL.FAX_NUMBER, 1, 3) || ') ' ||
                  SUBSTR(AL.FAX_NUMBER, 4, 3) || '-' ||
                  SUBSTR(AL.FAX_NUMBER, 7, 4)
                ELSE AL.FAX_NUMBER
              END AS FAX_NUMBER,
              AL.EMAIL_ADDRESS,
              CASE
                WHEN CL.CLIENT_LOCN_NAME IS NOT NULL THEN AL.CLIENT_LOCN_CODE || ' - ' || CL.CLIENT_LOCN_NAME 
                    ELSE AL.CLIENT_LOCN_CODE
              END AS ASSOCIATED_LOCATION,
              AL.UPDATE_TIMESTAMP,
              AL.UPDATE_USERID,
              BC.DESCRIPTION AS CONTACT_TYPE_DESC
          FROM THE.CLI_CON_AUDIT AL
          LEFT OUTER JOIN THE.BUSINESS_CONTACT_CODE BC
              ON AL.BUS_CONTACT_CODE = BC.BUSINESS_CONTACT_CODE
          LEFT OUTER JOIN THE.CLIENT_LOCATION CL
              ON AL.CLIENT_NUMBER = CL.CLIENT_NUMBER 
              AND AL.CLIENT_LOCN_CODE = CL.CLIENT_LOCN_CODE
          WHERE AL.CLIENT_NUMBER = :clientNumber
      ),
      AUDIT_DATA AS (
          SELECT
              'ClientContact' AS TABLE_NAME,
              B.CLIENT_CONTACT_AUDIT_ID || '-' || B.CLIENT_CONTACT_ID AS IDX,
              CASE
                  WHEN B.CLIENT_AUDIT_CODE = 'INS' THEN 'Contact "' || B.CONTACT_NAME || '" added'
                  WHEN B.CLIENT_AUDIT_CODE = 'UPD' THEN 'Contact "' || B.CONTACT_NAME || '" updated'
                  WHEN B.CLIENT_AUDIT_CODE = 'DEL' THEN 'Contact "' || B.CONTACT_NAME || '" deleted'
              END AS IDENTIFIER_LABEL,
              COL.COLUMN_NAME,
              CASE COL.COLUMN_NAME
                  WHEN 'contactTypeDesc' THEN B.CONTACT_TYPE_DESC
                  WHEN 'contactName' THEN B.CONTACT_NAME
                  WHEN 'businessPhone' THEN B.BUSINESS_PHONE
                  WHEN 'secondaryPhone' THEN B.CELL_PHONE
                  WHEN 'faxNumber' THEN B.FAX_NUMBER
                  WHEN 'emailAddress' THEN B.EMAIL_ADDRESS
                  WHEN 'associatedLocation' THEN B.ASSOCIATED_LOCATION
              END AS NEW_VALUE,
              LAG(
                  CASE COL.COLUMN_NAME
                      WHEN 'contactTypeDesc' THEN B.CONTACT_TYPE_DESC
                      WHEN 'contactName' THEN B.CONTACT_NAME
                      WHEN 'businessPhone' THEN B.BUSINESS_PHONE
                      WHEN 'secondaryPhone' THEN B.CELL_PHONE
                      WHEN 'faxNumber' THEN B.FAX_NUMBER
                      WHEN 'emailAddress' THEN B.EMAIL_ADDRESS
                      WHEN 'associatedLocation' THEN B.ASSOCIATED_LOCATION
                  END
              ) OVER (
                  PARTITION BY B.CLIENT_CONTACT_ID, COL.COLUMN_NAME
                  ORDER BY B.CLIENT_CONTACT_AUDIT_ID
              ) AS OLD_VALUE,
              B.UPDATE_TIMESTAMP,
              B.UPDATE_USERID,
              B.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
              COL.FIELD_ORDER
          FROM BASE_DATA B
          CROSS JOIN (
              SELECT 'contactName' AS COLUMN_NAME, 1 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'contactTypeDesc' AS COLUMN_NAME, 2 FROM DUAL
              UNION ALL
              SELECT 'associatedLocation' AS COLUMN_NAME, 3 FROM DUAL
              UNION ALL
              SELECT 'businessPhone' AS COLUMN_NAME, 4 FROM DUAL
              UNION ALL
              SELECT 'secondaryPhone' AS COLUMN_NAME, 5 FROM DUAL
              UNION ALL
              SELECT 'faxNumber' AS COLUMN_NAME, 6 FROM DUAL
              UNION ALL
              SELECT 'emailAddress' AS COLUMN_NAME, 7 FROM DUAL
          ) COL
      )
      SELECT
          A.TABLE_NAME,
          A.IDX,
          A.IDENTIFIER_LABEL,
          A.COLUMN_NAME,
          CASE 
              WHEN A.CHANGE_TYPE <> 'DEL' THEN A.OLD_VALUE 
              ELSE null
          END AS OLD_VALUE,
          A.NEW_VALUE,
          A.UPDATE_TIMESTAMP,
          A.UPDATE_USERID,
          A.CHANGE_TYPE,
          '' AS ACTION_CODE,
          '' AS REASON
      FROM AUDIT_DATA A
      WHERE (
          (A.CHANGE_TYPE = 'DEL' AND OLD_VALUE IS NOT NULL AND NEW_VALUE IS NOT NULL) OR
          (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL) OR
          (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL) OR
          (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
      )
      ORDER BY A.IDX DESC, A.FIELD_ORDER ASC
      """;
  
  public static final String DOING_BUSINESS_AS_HISTORY = """
      WITH BASE_DATA AS (
          SELECT
              AL.CLIENT_DBA_AUDIT_ID AS IDX,
              AL.CLIENT_NUMBER,
              AL.DOING_BUSINESS_AS_NAME,
              AL.UPDATE_TIMESTAMP,
              AL.UPDATE_USERID,
              AL.CLIENT_AUDIT_CODE
          FROM THE.CLIENT_DOING_BUSINESS_AS_AUDIT AL
          WHERE AL.CLIENT_NUMBER = :clientNumber
      ),
      AUDIT_DATA AS (
          SELECT
              'ClientDoingBusinessAs' AS TABLE_NAME,
              B.IDX,
              CASE
                  WHEN B.CLIENT_AUDIT_CODE = 'INS' THEN 'Doing business as "' || B.DOING_BUSINESS_AS_NAME || '" added'
                  WHEN B.CLIENT_AUDIT_CODE = 'UPD' THEN 'Doing business as "' || B.DOING_BUSINESS_AS_NAME || '" updated'
                  WHEN B.CLIENT_AUDIT_CODE = 'DEL' THEN 'Doing business as "' || B.DOING_BUSINESS_AS_NAME || '" deleted'
              END AS IDENTIFIER_LABEL,
              COL.COLUMN_NAME,
              CASE COL.COLUMN_NAME
                  WHEN 'doingBusinessAs' THEN B.DOING_BUSINESS_AS_NAME
              END AS NEW_VALUE,
              LAG(
                  CASE COL.COLUMN_NAME
                      WHEN 'doingBusinessAs' THEN B.DOING_BUSINESS_AS_NAME
                  END
              ) OVER (
                  PARTITION BY B.CLIENT_NUMBER, COL.COLUMN_NAME
                  ORDER BY B.IDX
              ) AS OLD_VALUE,
              B.UPDATE_TIMESTAMP,
              B.UPDATE_USERID,
              B.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
              COL.FIELD_ORDER
          FROM BASE_DATA B
          CROSS JOIN (
              SELECT 'doingBusinessAs' AS COLUMN_NAME, 1 AS FIELD_ORDER FROM DUAL
          ) COL
      )
      SELECT
          A.TABLE_NAME,
          A.IDX,
          A.IDENTIFIER_LABEL,
          A.COLUMN_NAME,
          CASE
              WHEN A.CHANGE_TYPE <> 'DEL' THEN A.OLD_VALUE
              ELSE null
          END AS OLD_VALUE,
          A.NEW_VALUE,
          A.UPDATE_TIMESTAMP,
          A.UPDATE_USERID,
          A.CHANGE_TYPE,
          '' AS ACTION_CODE,
          '' AS REASON
      FROM AUDIT_DATA A
      WHERE (
          (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL) OR
          (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL) OR
          (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
      )
      ORDER BY A.IDX DESC, A.FIELD_ORDER ASC
      """;
  
  public static final String RELATED_CLIENT_HISTORY = """
      WITH BASE_DATA AS (
          SELECT
              AL.RELATED_CLIENT_AUDIT_ID AS IDX,
              AL.CLIENT_NUMBER,
              AL.RELATED_CLNT_NMBR,
              AL.CLIENT_NUMBER || ', ' || PFC.CLIENT_NAME AS PRIMARY_CLIENT,
              AL.RELATED_CLNT_NMBR || ', ' || RFC.CLIENT_NAME AS RELATED_CLIENT,
              CASE 
                  WHEN PCL.CLIENT_LOCN_NAME IS NOT NULL THEN AL.CLIENT_LOCN_CODE || ' - ' || PCL.CLIENT_LOCN_NAME 
                  ELSE AL.CLIENT_LOCN_CODE
              END AS PRIMARY_CLIENT_LOCATION,
              CASE 
                  WHEN RCL.CLIENT_LOCN_NAME IS NOT NULL THEN AL.RELATED_CLNT_LOCN || ' - ' || RCL.CLIENT_LOCN_NAME 
                  ELSE AL.RELATED_CLNT_LOCN
              END AS RELATED_CLIENT_LOCATION,
              AL.RELATIONSHIP_CODE || ' - ' || RC.DESCRIPTION AS RELATIONSHIP_TYPE,
              CASE
                  WHEN AL.SIGNING_AUTH_IND = 'Y' THEN 'Yes'
                  WHEN AL.SIGNING_AUTH_IND = 'N' THEN 'No'
              END AS SIGNING_AUTH_IND,
              CASE 
                  WHEN AL.PERCENT_OWNERSHIP = 0 THEN TO_CHAR(AL.PERCENT_OWNERSHIP, 'FM999') || '%' 
                  WHEN AL.PERCENT_OWNERSHIP IS NOT NULL THEN TO_CHAR(AL.PERCENT_OWNERSHIP, 'FM999.00') || '%' 
                  ELSE NULL
              END AS PERCENT_OWNERSHIP,
              AL.UPDATE_TIMESTAMP,
              AL.UPDATE_USERID,
              AL.CLIENT_AUDIT_CODE
          FROM THE.REL_CLI_AUDIT AL
          LEFT JOIN THE.FOREST_CLIENT PFC
              ON AL.CLIENT_NUMBER = PFC.CLIENT_NUMBER 
          LEFT JOIN THE.FOREST_CLIENT RFC
              ON AL.RELATED_CLNT_NMBR = RFC.CLIENT_NUMBER 
          LEFT JOIN THE.CLIENT_LOCATION PCL
              ON AL.CLIENT_NUMBER = PCL.CLIENT_NUMBER 
              AND AL.CLIENT_LOCN_CODE = PCL.CLIENT_LOCN_CODE
          LEFT JOIN THE.CLIENT_LOCATION RCL
              ON AL.RELATED_CLNT_NMBR = RCL.CLIENT_NUMBER 
              AND AL.RELATED_CLNT_LOCN = RCL.CLIENT_LOCN_CODE
          LEFT JOIN THE.CLIENT_RELATIONSHIP_CODE RC
              ON AL.RELATIONSHIP_CODE = RC.CLIENT_RELATIONSHIP_CODE
          WHERE AL.CLIENT_NUMBER = :clientNumber
      ),
      AUDIT_DATA AS (
          SELECT
              'RelatedClient' AS TABLE_NAME,
              B.IDX,
              CASE
                  WHEN B.CLIENT_AUDIT_CODE = 'INS' THEN 'Relationship with "' || B.RELATED_CLIENT || '" added'
                  WHEN B.CLIENT_AUDIT_CODE = 'UPD' THEN 'Relationship with "' || B.RELATED_CLIENT || '" updated'
                  WHEN B.CLIENT_AUDIT_CODE = 'DEL' THEN 'Relationship deleted'
              END AS IDENTIFIER_LABEL,
              COL.COLUMN_NAME,
              CASE COL.COLUMN_NAME
                  WHEN 'primaryClient' THEN B.PRIMARY_CLIENT
                  WHEN 'primaryClientLocation' THEN B.PRIMARY_CLIENT_LOCATION
                  WHEN 'relationshipType' THEN B.RELATIONSHIP_TYPE
                  WHEN 'relatedClient' THEN B.RELATED_CLIENT
                  WHEN 'relatedClientLocation' THEN B.RELATED_CLIENT_LOCATION
                  WHEN 'signingAuthInd' THEN B.SIGNING_AUTH_IND
                  WHEN 'percentOwnership' THEN B.PERCENT_OWNERSHIP
              END AS NEW_VALUE,
              LAG(
                  CASE COL.COLUMN_NAME
                      WHEN 'primaryClient' THEN B.PRIMARY_CLIENT
                      WHEN 'primaryClientLocation' THEN B.PRIMARY_CLIENT_LOCATION
                      WHEN 'relationshipType' THEN B.RELATIONSHIP_TYPE
                      WHEN 'relatedClient' THEN B.RELATED_CLIENT
                      WHEN 'relatedClientLocation' THEN B.RELATED_CLIENT_LOCATION
                      WHEN 'signingAuthInd' THEN B.SIGNING_AUTH_IND
                      WHEN 'percentOwnership' THEN B.PERCENT_OWNERSHIP
                  END
              ) OVER (
                  PARTITION BY B.CLIENT_NUMBER, B.PRIMARY_CLIENT_LOCATION, COL.COLUMN_NAME
                  ORDER BY B.IDX
              ) AS OLD_VALUE,
              B.UPDATE_TIMESTAMP,
              B.UPDATE_USERID,
              B.CLIENT_AUDIT_CODE AS CHANGE_TYPE,
              COL.FIELD_ORDER
          FROM BASE_DATA B
          CROSS JOIN (
              SELECT 'primaryClient' AS COLUMN_NAME, 1 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'primaryClientLocation' AS COLUMN_NAME, 2 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'relationshipType' AS COLUMN_NAME, 3 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'relatedClient' AS COLUMN_NAME, 4 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'relatedClientLocation' AS COLUMN_NAME, 5 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'signingAuthInd' AS COLUMN_NAME, 6 AS FIELD_ORDER FROM DUAL
              UNION ALL
              SELECT 'percentOwnership' AS COLUMN_NAME, 7 AS FIELD_ORDER FROM DUAL
          ) COL
      )
      SELECT
          A.TABLE_NAME,
          A.IDX,
          A.IDENTIFIER_LABEL,
          A.COLUMN_NAME,
          CASE 
              WHEN A.CHANGE_TYPE <> 'DEL' THEN A.OLD_VALUE 
              ELSE null
          END AS OLD_VALUE,
          A.NEW_VALUE,
          A.UPDATE_TIMESTAMP,
          A.UPDATE_USERID,
          A.CHANGE_TYPE,
          '' AS ACTION_CODE,
          '' AS REASON
      FROM AUDIT_DATA A
      WHERE (
          (A.CHANGE_TYPE = 'DEL' AND OLD_VALUE IS NOT NULL AND NEW_VALUE IS NOT NULL) OR
          (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL) OR
          (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL) OR
          (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
      )
      ORDER BY A.IDX DESC, A.FIELD_ORDER ASC
      """;

  public static final String FIND_CLIENT_BY_REGISTRATION_OR_NAME = """
      SELECT * FROM FOREST_CLIENT x
      WHERE (UPPER(x.REGISTRY_COMPANY_TYPE_CODE) || x.CORP_REGN_NMBR) = UPPER(:registrationNumber)
          OR UPPER(x.CLIENT_NAME) = UPPER(:companyName)
          OR x.CLIENT_IDENTIFICATION = UPPER(:registrationNumber)""";

  public static final String FIND_FUZZY_INDIVIDUAL_BY_NAME_AND_DOB = """
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
          (:dob IS NULL OR BIRTHDATE = :dob)
          AND CLIENT_TYPE_CODE = 'I'
        AND (
          UTL_MATCH.JARO_WINKLER_SIMILARITY(
            UPPER(LEGAL_FIRST_NAME) || ' ' || UPPER(LEGAL_MIDDLE_NAME) || ' ' || UPPER(CLIENT_NAME),
            UPPER(:name)
          ) >= 95
          OR
          UTL_MATCH.JARO_WINKLER_SIMILARITY(
            UPPER(LEGAL_FIRST_NAME) || ' ' || UPPER(CLIENT_NAME),
            UPPER(:name)
          ) >= 95
        )
      ORDER BY CLIENT_NUMBER
      """;

  public static final String FIND_FUZZY_CLIENT_BY_NAME = """
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
          UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(CLIENT_NAME), UPPER(:companyName)) >= 95
      ORDER BY CLIENT_NUMBER
      """;

  public static final String FIND_CLIENT_DETAILS_BY_CLIENT_NUMBER = """
      SELECT
          C.CLIENT_NUMBER,
          C.CLIENT_NAME,
          C.LEGAL_FIRST_NAME,
          C.LEGAL_MIDDLE_NAME,
          C.CLIENT_STATUS_CODE,
          S.DESCRIPTION AS CLIENT_STATUS_DESC,
          C.CLIENT_TYPE_CODE,
          T.DESCRIPTION AS CLIENT_TYPE_DESC,
          C.CLIENT_ID_TYPE_CODE,
          IT.DESCRIPTION AS CLIENT_ID_TYPE_DESC,
          C.CLIENT_IDENTIFICATION,
          C.REGISTRY_COMPANY_TYPE_CODE,
          C.CORP_REGN_NMBR,
          C.CLIENT_ACRONYM,
          C.WCB_FIRM_NUMBER,
          C.CLIENT_COMMENT,
          FCA.UPDATE_USERID AS CLIENT_COMMENT_UPDATE_USER,
          FCA.UPDATE_TIMESTAMP AS CLIENT_COMMENT_UPDATE_DATE,
          '' AS GOOD_STANDING_IND,
          C.BIRTHDATE
      FROM THE.FOREST_CLIENT C
          INNER JOIN THE.CLIENT_STATUS_CODE S ON C.CLIENT_STATUS_CODE = S.CLIENT_STATUS_CODE
          INNER JOIN THE.CLIENT_TYPE_CODE T ON C.CLIENT_TYPE_CODE = T.CLIENT_TYPE_CODE
          LEFT JOIN THE.CLIENT_ID_TYPE_CODE IT ON C.CLIENT_ID_TYPE_CODE = IT.CLIENT_ID_TYPE_CODE
          LEFT JOIN (
              SELECT
                  CLIENT_NUMBER,
                  UPDATE_USERID,
                  UPDATE_TIMESTAMP
              FROM (
                  SELECT
                      AL.FOREST_CLIENT_AUDIT_ID AS IDX,
                      AL.CLIENT_COMMENT AS NEW_VALUE,
                      LAG(AL.CLIENT_COMMENT) OVER (
                          PARTITION BY AL.CLIENT_NUMBER
                          ORDER BY AL.FOREST_CLIENT_AUDIT_ID
                      ) AS OLD_VALUE,
                      AL.UPDATE_TIMESTAMP,
                      AL.UPDATE_USERID,
                      AL.CLIENT_NUMBER
                  FROM THE.FOR_CLI_AUDIT AL
                  WHERE AL.CLIENT_NUMBER = :clientNumber
              )
              WHERE (
                  (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL) OR
                  (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL) OR
                  (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
              )
              ORDER BY IDX DESC
              FETCH FIRST 1 ROWS ONLY
        ) FCA ON C.CLIENT_NUMBER = FCA.CLIENT_NUMBER
      WHERE C.CLIENT_NUMBER = :clientNumber
      """;

  public static final String FIND_BY_PREDICTIVE_SEARCH_SELECT = """
      SELECT
          C.CLIENT_NUMBER,
          C.CLIENT_ACRONYM AS CLIENT_ACRONYM,
          C.CLIENT_NAME,
          C.LEGAL_FIRST_NAME AS CLIENT_FIRST_NAME,
          DBA.DOING_BUSINESS_AS_NAME AS DOING_BUSINESS_AS,
          C.CLIENT_IDENTIFICATION,
          C.LEGAL_MIDDLE_NAME AS CLIENT_MIDDLE_NAME,
          CL.CITY AS CITY,
          CTC.DESCRIPTION AS CLIENT_TYPE,
          CSC.DESCRIPTION AS CLIENT_STATUS,""";

  public static final String FIND_BY_PREDICTIVE_SEARCH_SCORE_LIKE = """
      (
          CASE
              WHEN UPPER(C.CLIENT_NUMBER) LIKE UPPER('%' || :value || '%') THEN 100
              WHEN UPPER(C.CLIENT_ACRONYM) LIKE UPPER('%' || :value || '%') THEN 100
              WHEN UPPER(C.CLIENT_NAME) LIKE UPPER('%' || :value || '%') THEN 100
              WHEN UPPER(C.LEGAL_FIRST_NAME) LIKE UPPER('%' || :value || '%') THEN 90
              WHEN UPPER(C.LEGAL_MIDDLE_NAME) LIKE UPPER('%' || :value || '%') THEN 50
              WHEN UPPER(DBA.DOING_BUSINESS_AS_NAME) LIKE UPPER('%' || :value || '%') THEN 75
              WHEN UPPER(C.CLIENT_IDENTIFICATION) LIKE UPPER('%' || :value || '%') THEN 70
              WHEN UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE UPPER('%' || :value || '%') THEN 90
              WHEN UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE UPPER('%' || :value || '%') THEN 90
              WHEN UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE UPPER('%' || :value || '%') THEN 50
              WHEN UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE UPPER('%' || :value || '%') THEN 50
              WHEN UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') ||
                  COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE UPPER('%' || :value || '%') THEN 70
              WHEN UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') || ' ' ||
                  COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE UPPER('%' || :value || '%') THEN 40
              WHEN UPPER(CL.ADDRESS_1) LIKE UPPER('%' || :value || '%') THEN 50
              WHEN UPPER(CL.POSTAL_CODE) LIKE UPPER('%' || :value || '%') THEN 45
              WHEN UPPER(CL.EMAIL_ADDRESS) LIKE UPPER('%' || :value || '%') THEN 40
              ELSE 0
          END
      ) AS SCORE
      """;

  public static final String FIND_BY_PREDICTIVE_SEARCH_SCORE_SIMILARITY = """
      (
          CASE WHEN C.CLIENT_NUMBER = :value THEN 112 ELSE 0 END +
          CASE WHEN C.CLIENT_ACRONYM = :value THEN 111 ELSE 0 END +
          (UTL_MATCH.JARO_WINKLER_SIMILARITY(
              C.CLIENT_NAME || ' ' || C.LEGAL_FIRST_NAME, :value
          ) + 10) +
          (UTL_MATCH.JARO_WINKLER_SIMILARITY(DBA.DOING_BUSINESS_AS_NAME, :value) + 7) +
          CASE WHEN C.CLIENT_IDENTIFICATION = :value THEN 106 ELSE 0 END +
          UTL_MATCH.JARO_WINKLER_SIMILARITY(C.LEGAL_MIDDLE_NAME, :value)
      ) AS SCORE
      """;

  public static final String FIND_BY_PREDICTIVE_SEARCH_FROM = """
      FROM THE.FOREST_CLIENT C
      LEFT JOIN THE.CLIENT_DOING_BUSINESS_AS DBA ON C.CLIENT_NUMBER = DBA.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_TYPE_CODE CTC ON C.CLIENT_TYPE_CODE = CTC.CLIENT_TYPE_CODE
      LEFT JOIN THE.CLIENT_LOCATION CL ON C.CLIENT_NUMBER = CL.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_STATUS_CODE CSC ON C.CLIENT_STATUS_CODE = CSC.CLIENT_STATUS_CODE
      """;

  public static final String FIND_BY_PREDICTIVE_SEARCH_LIKE_WHERE = """
      WHERE (
          UPPER(C.CLIENT_NUMBER) LIKE '%' || :value || '%'
          OR UPPER(C.CLIENT_ACRONYM) LIKE '%' || :value || '%'
          OR UPPER(C.CLIENT_NAME) LIKE '%' || :value || '%'
          OR UPPER(C.LEGAL_FIRST_NAME) LIKE '%' || :value || '%'
          OR UPPER(C.LEGAL_MIDDLE_NAME) LIKE '%' || :value || '%'
          OR UPPER(DBA.DOING_BUSINESS_AS_NAME) LIKE '%' || :value || '%'
          OR UPPER(C.CLIENT_IDENTIFICATION) LIKE '%' || :value || '%'
          OR UPPER(TRIM(
              COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
              COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
              COALESCE(C.CLIENT_NAME, '')
          )) LIKE '%' || :value || '%'
          OR UPPER(TRIM(
              COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
              COALESCE(C.CLIENT_NAME, '')
          )) LIKE '%' || :value || '%'
          OR UPPER(TRIM(
              COALESCE(C.CLIENT_NAME, '') || ' ' ||
              COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
              COALESCE(C.LEGAL_FIRST_NAME, '')
          )) LIKE '%' || :value || '%'
          OR UPPER(TRIM(
              COALESCE(C.CLIENT_NAME, '') || ' ' ||
              COALESCE(C.LEGAL_FIRST_NAME, '')
          )) LIKE '%' || :value || '%'
          OR UPPER(TRIM(
              COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') ||
              COALESCE(C.CORP_REGN_NMBR, '')
          )) LIKE '%' || :value || '%'
          OR UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') || ' ' ||
                  COALESCE(C.CORP_REGN_NMBR, '')
          )) LIKE '%' || :value || '%'
          OR UPPER(CL.ADDRESS_1) LIKE '%' || :value || '%'
          OR UPPER(CL.POSTAL_CODE) LIKE '%' || :value || '%'
          OR UPPER(CL.EMAIL_ADDRESS) LIKE '%' || :value || '%'
      )
      AND CL.CLIENT_LOCN_CODE = '00'
      """;

  public static final String FIND_BY_PREDICTIVE_SEARCH_SIMILARITY_WHERE = """
      WHERE
        (
            C.CLIENT_NUMBER = :value
            OR C.CLIENT_ACRONYM = :value
            OR UTL_MATCH.JARO_WINKLER_SIMILARITY(C.CLIENT_NAME, :value) >= 90
            OR C.CLIENT_NAME LIKE '%' || :value || '%'
            OR UTL_MATCH.JARO_WINKLER_SIMILARITY(C.LEGAL_FIRST_NAME, :value) >= 90
            OR UTL_MATCH.JARO_WINKLER_SIMILARITY(DBA.DOING_BUSINESS_AS_NAME, :value) >= 90
            OR DBA.DOING_BUSINESS_AS_NAME LIKE '%' || :value || '%'
            OR C.CLIENT_IDENTIFICATION = :value
            OR UTL_MATCH.JARO_WINKLER_SIMILARITY(C.LEGAL_MIDDLE_NAME, :value) >= 90
            OR C.LEGAL_MIDDLE_NAME LIKE '%' || :value || '%'
            OR (
                C.CLIENT_TYPE_CODE = 'I' AND (
                    UTL_MATCH.JARO_WINKLER_SIMILARITY(
                        TRIM(
                            COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' || 
                            COALESCE(C.LEGAL_MIDDLE_NAME, '') || 
                            COALESCE(C.CLIENT_NAME, '')
                        ), :value
                    ) >= 90
                    OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                        TRIM(
                            COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' || 
                            COALESCE(C.CLIENT_NAME, '')
                        ), :value
                    ) >= 90
                    OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                        TRIM(
                            COALESCE(C.CLIENT_NAME, '') || ' ' || 
                            COALESCE(C.LEGAL_MIDDLE_NAME, '') || 
                            COALESCE(C.LEGAL_FIRST_NAME, '')
                        ), :value
                    ) >= 90
                    OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                        TRIM(
                            COALESCE(C.CLIENT_NAME, '') || ' ' || 
                            COALESCE(C.LEGAL_FIRST_NAME, '')
                        ), :value
                    ) >= 90
                )
            )
        )
        AND CL.CLIENT_LOCN_CODE = '00'
        """;

  public static final String FIND_BY_PREDICTIVE_SEARCH_WITH_LIKE =
      FIND_BY_PREDICTIVE_SEARCH_SELECT
      + FIND_BY_PREDICTIVE_SEARCH_SCORE_LIKE
      + FIND_BY_PREDICTIVE_SEARCH_FROM
      + FIND_BY_PREDICTIVE_SEARCH_LIKE_WHERE
      + ORDER_BY
      + ORACLE_PAGINATION;

  public static final String COUNT_BY_PREDICTIVE_SEARCH_WITH_LIKE =
      SELECT_COUNT_C_CLIENT_NUMBER
      + FIND_BY_PREDICTIVE_SEARCH_FROM
      + FIND_BY_PREDICTIVE_SEARCH_LIKE_WHERE;

  public static final String FIND_BY_PREDICTIVE_SEARCH_WITH_SIMILARITY =
      FIND_BY_PREDICTIVE_SEARCH_SELECT
      + FIND_BY_PREDICTIVE_SEARCH_SCORE_SIMILARITY
      + FIND_BY_PREDICTIVE_SEARCH_FROM
      + FIND_BY_PREDICTIVE_SEARCH_SIMILARITY_WHERE
      + ORDER_BY
      + ORACLE_PAGINATION;

  public static final String COUNT_BY_PREDICTIVE_SEARCH_WITH_SIMILARITY =
      SELECT_COUNT_C_CLIENT_NUMBER
      + FIND_BY_PREDICTIVE_SEARCH_FROM
      + FIND_BY_PREDICTIVE_SEARCH_SIMILARITY_WHERE;

  public static final String FIND_BY_EMPTY_FULL_SEARCH = """
      SELECT
          C.CLIENT_NUMBER,
          C.CLIENT_ACRONYM AS CLIENT_ACRONYM,
          C.CLIENT_NAME,
          C.LEGAL_FIRST_NAME AS CLIENT_FIRST_NAME,
          DBA.DOING_BUSINESS_AS_NAME AS DOING_BUSINESS_AS,
          C.CLIENT_IDENTIFICATION,
          C.LEGAL_MIDDLE_NAME AS CLIENT_MIDDLE_NAME,
          CL.CITY AS CITY,
          CTC.DESCRIPTION AS CLIENT_TYPE,
          CSC.DESCRIPTION AS CLIENT_STATUS,
          100 AS SCORE
      FROM THE.FOREST_CLIENT C
          LEFT JOIN THE.CLIENT_DOING_BUSINESS_AS DBA ON C.CLIENT_NUMBER = DBA.CLIENT_NUMBER
          LEFT JOIN THE.CLIENT_TYPE_CODE CTC ON C.CLIENT_TYPE_CODE = CTC.CLIENT_TYPE_CODE
          LEFT JOIN THE.CLIENT_LOCATION CL ON C.CLIENT_NUMBER = CL.CLIENT_NUMBER
          LEFT JOIN THE.CLIENT_STATUS_CODE CSC ON C.CLIENT_STATUS_CODE = CSC.CLIENT_STATUS_CODE
      WHERE
        CL.CLIENT_LOCN_CODE = '00'
        AND (C.UPDATE_TIMESTAMP >= :date OR C.ADD_TIMESTAMP >= :date)
      ORDER BY C.ADD_TIMESTAMP DESC
      OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
      """;

  public static final String COUNT_BY_EMPTY_FULL_SEARCH = """
      SELECT
          COUNT(C.CLIENT_NUMBER)
      FROM THE.FOREST_CLIENT C
      LEFT JOIN THE.CLIENT_DOING_BUSINESS_AS DBA ON C.CLIENT_NUMBER = DBA.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_TYPE_CODE CTC ON C.CLIENT_TYPE_CODE = CTC.CLIENT_TYPE_CODE
      LEFT JOIN THE.CLIENT_LOCATION CL ON C.CLIENT_NUMBER = CL.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_STATUS_CODE CSC ON C.CLIENT_STATUS_CODE = CSC.CLIENT_STATUS_CODE
      WHERE
        CL.CLIENT_LOCN_CODE = '00'
        AND (C.UPdate_TIMESTAMP >= :date OR C.ADD_TIMESTAMP >= :date)
      """;

  public static final String SEARCH_OTHER_CORP_NUMBER = """
      SELECT *
      FROM FOREST_CLIENT fc
      WHERE
        fc.CLIENT_NUMBER != :clientNumber
        AND fc.REGISTRY_COMPANY_TYPE_CODE = (NVL(:companyType,(SELECT REGISTRY_COMPANY_TYPE_CODE FROM FOREST_CLIENT WHERE CLIENT_NUMBER = :clientNumber)))
        AND fc.CORP_REGN_NMBR = (NVL(:companyNumber,(SELECT CORP_REGN_NMBR FROM FOREST_CLIENT WHERE CLIENT_NUMBER = :clientNumber)))""";

  public static final String RELATED_CLIENT_LIST = """
      SELECT
        rc.CLIENT_NUMBER,
        CASE
            WHEN fc.LEGAL_FIRST_NAME IS NOT NULL AND fc.LEGAL_MIDDLE_NAME IS NOT NULL THEN
            fc.LEGAL_FIRST_NAME || ' ' || fc.LEGAL_MIDDLE_NAME || ' ' || fc.CLIENT_NAME
            WHEN fc.LEGAL_FIRST_NAME IS NOT NULL THEN
              fc.LEGAL_FIRST_NAME || ' ' || fc.CLIENT_NAME
            ELSE
              NVL(fc.LEGAL_FIRST_NAME, '') || NVL(fc.LEGAL_MIDDLE_NAME, '') || fc.CLIENT_NAME
          END AS CLIENT_NAME,
        rc.CLIENT_LOCN_CODE,
        cl.CLIENT_LOCN_NAME AS client_locn_name,
        rc.RELATED_CLNT_NMBR,
        CASE
            WHEN rfc.LEGAL_FIRST_NAME IS NOT NULL AND rfc.LEGAL_MIDDLE_NAME IS NOT NULL THEN
            rfc.LEGAL_FIRST_NAME || ' ' || rfc.LEGAL_MIDDLE_NAME || ' ' || rfc.CLIENT_NAME
            WHEN rfc.LEGAL_FIRST_NAME IS NOT NULL THEN
              rfc.LEGAL_FIRST_NAME || ' ' || rfc.CLIENT_NAME
            ELSE
              NVL(rfc.LEGAL_FIRST_NAME, '') || NVL(rfc.LEGAL_MIDDLE_NAME, '') || rfc.CLIENT_NAME
          END AS RELATED_CLNT_NAME,
        rc.RELATED_CLNT_LOCN,
        rcl.CLIENT_LOCN_NAME AS related_clnt_locn_name,
        rc.RELATIONSHIP_CODE,
        crc.DESCRIPTION AS relationship_name,
        rc.SIGNING_AUTH_IND,
        rc.PERCENT_OWNERSHIP,
        CASE
          WHEN rc.CLIENT_NUMBER = :clientNumber THEN 'TRUE'
          WHEN rc.RELATED_CLNT_NMBR = :clientNumber THEN 'FALSE'
        END AS PRIMARY_CLIENT
      FROM RELATED_CLIENT rc
      LEFT JOIN THE.FOREST_CLIENT fc ON fc.CLIENT_NUMBER = rc.CLIENT_NUMBER
      LEFT JOIN THE.FOREST_CLIENT rfc ON rfc.CLIENT_NUMBER = rc.RELATED_CLNT_NMBR
      LEFT JOIN THE.CLIENT_LOCATION cl ON cl.CLIENT_NUMBER = rc.CLIENT_NUMBER AND cl.CLIENT_LOCN_CODE = rc.CLIENT_LOCN_CODE
      LEFT JOIN THE.CLIENT_LOCATION rcl ON rcl.CLIENT_NUMBER = rc.RELATED_CLNT_NMBR AND rcl.CLIENT_LOCN_CODE = rc.RELATED_CLNT_LOCN
      LEFT JOIN THE.CLIENT_RELATIONSHIP_CODE crc ON crc.CLIENT_RELATIONSHIP_CODE = rc.RELATIONSHIP_CODE
      WHERE rc.CLIENT_NUMBER = :clientNumber OR rc.RELATED_CLNT_NMBR = :clientNumber""";

  public static final String RELATED_CLIENT_AUTOCOMPLETE_SELECT = """
      SELECT
        C.CLIENT_NUMBER,
        C.CLIENT_ACRONYM AS CLIENT_ACRONYM,
        C.CLIENT_NAME,
        C.LEGAL_FIRST_NAME AS CLIENT_FIRST_NAME,
        DBA.DOING_BUSINESS_AS_NAME AS DOING_BUSINESS_AS,
        C.CLIENT_IDENTIFICATION,
        C.LEGAL_MIDDLE_NAME AS CLIENT_MIDDLE_NAME,
        CL.CITY AS CITY,
        CTC.DESCRIPTION AS CLIENT_TYPE,
        CSC.DESCRIPTION AS CLIENT_STATUS,
      """;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_LIKE_ORDER = """
      (
          CASE
              WHEN UPPER(C.CLIENT_NUMBER) LIKE '%' || :value || '%' THEN 100
              WHEN UPPER(C.CLIENT_ACRONYM) LIKE '%' || :value || '%' THEN 100
              WHEN UPPER(C.CLIENT_NAME) LIKE '%' || :value || '%' THEN 100
              WHEN UPPER(C.LEGAL_FIRST_NAME) LIKE '%' || :value || '%' THEN 90
              WHEN UPPER(C.LEGAL_MIDDLE_NAME) LIKE '%' || :value || '%' THEN 50
              WHEN UPPER(DBA.DOING_BUSINESS_AS_NAME) LIKE '%' || :value || '%' THEN 75
              WHEN UPPER(C.CLIENT_IDENTIFICATION) LIKE '%' || :value || '%' THEN 70
              WHEN UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE '%' || :value || '%' THEN 90
              WHEN UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE '%' || :value || '%' THEN 90
              WHEN UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE '%' || :value || '%' THEN 50
              WHEN UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE '%' || :value || '%' THEN 50
              WHEN UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') ||
                  COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE '%' || :value || '%' THEN 70
              WHEN UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') || ' ' ||
                  COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE '%' || :value || '%' THEN 40
              ELSE 0
          END
      ) AS SCORE
      """;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_FROM = """
      FROM THE.FOREST_CLIENT C
      LEFT JOIN THE.CLIENT_DOING_BUSINESS_AS DBA ON C.CLIENT_NUMBER = DBA.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_TYPE_CODE CTC ON C.CLIENT_TYPE_CODE = CTC.CLIENT_TYPE_CODE
      LEFT JOIN THE.CLIENT_STATUS_CODE CSC ON C.CLIENT_STATUS_CODE = CSC.CLIENT_STATUS_CODE
      LEFT JOIN THE.CLIENT_LOCATION CL ON C.CLIENT_NUMBER = CL.CLIENT_NUMBER
      """;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_LIKE = """
      WHERE
        C.CLIENT_NUMBER != :mainClientNumber
        AND C.CLIENT_TYPE_CODE IN (
          SELECT DISTINCT(SECONDARY_CLIENT_TYPE_CODE) FROM THE.CLIENT_RELATIONSHIP_TYPE_XREF
          WHERE PRIMARY_CLIENT_TYPE_CODE = (SELECT CLIENT_TYPE_CODE FROM FOREST_CLIENT WHERE CLIENT_NUMBER = :mainClientNumber) AND (
            NVL(:relationType,'NOVALUE') = 'NOVALUE' OR CLIENT_RELATIONSHIP_CODE = :relationType
          )
        )
        AND (
            UPPER(C.CLIENT_NUMBER) LIKE '%' || :value || '%'
            OR UPPER(C.CLIENT_ACRONYM) LIKE '%' || :value || '%'
            OR UPPER(C.CLIENT_NAME) LIKE '%' || :value || '%'
            OR UPPER(C.LEGAL_FIRST_NAME) LIKE '%' || :value || '%'
            OR UPPER(C.LEGAL_MIDDLE_NAME) LIKE '%' || :value || '%'
            OR UPPER(DBA.DOING_BUSINESS_AS_NAME) LIKE '%' || :value || '%'
            OR UPPER(C.CLIENT_IDENTIFICATION) LIKE '%' || :value || '%'
            OR UPPER(TRIM(
                COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                COALESCE(C.CLIENT_NAME, '')
            )) LIKE '%' || :value || '%'
            OR UPPER(TRIM(
                COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                COALESCE(C.CLIENT_NAME, '')
            )) LIKE '%' || :value || '%'
            OR UPPER(TRIM(
                COALESCE(C.CLIENT_NAME, '') || ' ' ||
                COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                COALESCE(C.LEGAL_FIRST_NAME, '')
            )) LIKE '%' || :value || '%'
            OR UPPER(TRIM(
                COALESCE(C.CLIENT_NAME, '') || ' ' ||
                COALESCE(C.LEGAL_FIRST_NAME, '')
            )) LIKE '%' || :value || '%'
            OR UPPER(TRIM(
                COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') ||
                COALESCE(C.CORP_REGN_NMBR, '')
            )) LIKE '%' || :value || '%'
            OR UPPER(TRIM(
                    COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') || ' ' ||
                    COALESCE(C.CORP_REGN_NMBR, '')
            )) LIKE '%' || :value || '%'
        )
        AND CL.CLIENT_LOCN_CODE = '00'
      """;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_SIMILARITY = """
      WHERE
        C.CLIENT_NUMBER != :mainClientNumber
        AND C.CLIENT_TYPE_CODE IN (
          SELECT DISTINCT(SECONDARY_CLIENT_TYPE_CODE) FROM THE.CLIENT_RELATIONSHIP_TYPE_XREF
          WHERE PRIMARY_CLIENT_TYPE_CODE = (SELECT CLIENT_TYPE_CODE FROM FOREST_CLIENT WHERE CLIENT_NUMBER = :mainClientNumber) AND (
            NVL(:relationType,'NOVALUE') = 'NOVALUE' OR CLIENT_RELATIONSHIP_CODE = :relationType
          )
        )
        AND
        (
          C.CLIENT_NUMBER = :value
          OR C.CLIENT_ACRONYM = :value
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(C.CLIENT_NAME, :value) >= 90
          OR C.CLIENT_NAME LIKE '%' || :value || '%'
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(C.LEGAL_FIRST_NAME, :value) >= 90
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(DBA.DOING_BUSINESS_AS_NAME, :value) >= 90
          OR DBA.DOING_BUSINESS_AS_NAME LIKE '%' || :value || '%'
          OR C.CLIENT_IDENTIFICATION = :value
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(C.LEGAL_MIDDLE_NAME, :value) >= 90
          OR C.LEGAL_MIDDLE_NAME LIKE '%' || :value || '%'
          OR (
              C.CLIENT_TYPE_CODE = 'I' AND (
                  UTL_MATCH.JARO_WINKLER_SIMILARITY(
                      TRIM(
                          COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||\s
                          COALESCE(C.LEGAL_MIDDLE_NAME, '') ||\s
                          COALESCE(C.CLIENT_NAME, '')
                      ), :value
                  ) >= 90
                  OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                      TRIM(
                          COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||\s
                          COALESCE(C.CLIENT_NAME, '')
                      ), :value
                  ) >= 90
                  OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                      TRIM(
                          COALESCE(C.CLIENT_NAME, '') || ' ' ||\s
                          COALESCE(C.LEGAL_MIDDLE_NAME, '') ||\s
                          COALESCE(C.LEGAL_FIRST_NAME, '')
                      ), :value
                  ) >= 90
                  OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                      TRIM(
                          COALESCE(C.CLIENT_NAME, '') || ' ' ||\s
                          COALESCE(C.LEGAL_FIRST_NAME, '')
                      ), :value
                  ) >= 90
              )
          )
        )
      """;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_COUNT_WITH_LIKE =
      SELECT_COUNT_C_CLIENT_NUMBER
      + RELATED_CLIENT_AUTOCOMPLETE_FROM
      + RELATED_CLIENT_AUTOCOMPLETE_LIKE;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_WITH_LIKE =
      RELATED_CLIENT_AUTOCOMPLETE_SELECT
      + RELATED_CLIENT_AUTOCOMPLETE_LIKE_ORDER
      + RELATED_CLIENT_AUTOCOMPLETE_FROM
      + RELATED_CLIENT_AUTOCOMPLETE_LIKE
      + ORDER_BY
      + ORACLE_PAGINATION;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_COUNT_WITH_SIMILARITY =
      SELECT_COUNT_C_CLIENT_NUMBER
      + RELATED_CLIENT_AUTOCOMPLETE_FROM
      + RELATED_CLIENT_AUTOCOMPLETE_SIMILARITY;

  public static final String RELATED_CLIENT_AUTOCOMPLETE_WITH_SIMILARITY =
      RELATED_CLIENT_AUTOCOMPLETE_SELECT
      + FIND_BY_PREDICTIVE_SEARCH_SCORE_SIMILARITY
      + RELATED_CLIENT_AUTOCOMPLETE_FROM
      + RELATED_CLIENT_AUTOCOMPLETE_SIMILARITY
      + ORDER_BY
      + ORACLE_PAGINATION;

  public static final String RELATED_EXACT_SEARCH = """
      SELECT * FROM THE.RELATED_CLIENT WHERE
      CLIENT_NUMBER = :clientNumber
      AND CLIENT_LOCN_CODE = :clientLocationCode
      AND RELATED_CLNT_NMBR = :relatedClientNumber
      AND RELATED_CLNT_LOCN  = :relatedClientLocationCode
      AND RELATIONSHIP_CODE = :relationshipType""";

  public static final String LOCATION_TO_REACTIVATE = """
      WITH ForestClientAudit AS (
      	SELECT
      		fca.client_number,
      		fca.UPDATE_TIMESTAMP
      	FROM FOR_CLI_AUDIT fca
      	WHERE fca.client_number = :client_number
      	AND fca.CLIENT_STATUS_CODE = :client_status
      	ORDER BY fca.FOREST_CLIENT_AUDIT_ID DESC
      	FETCH NEXT 1 ROWS ONLY
      )
      SELECT
      	DISTINCT cla.client_locn_code as code,
        cla.client_locn_name as name
      FROM cli_locn_audit cla
      LEFT JOIN ForestClientAudit fca ON fca.client_number = cla.client_number
      WHERE cla.client_number = :client_number
      AND cla.UPDATE_TIMESTAMP = fca.UPDATE_TIMESTAMP""";
}
