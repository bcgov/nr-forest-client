
/****************************************************************************
* CLIENT -                                                                  *
* DATAFIX: Update duplicated contacts                                       *
*                                                                           *
* DATE CREATED : 2026-02-04                                                 *
* AUTHOR : Maria Martinez                                                   *
* System : CLIENT                                                           *
*                                                                           *
*****************************************************************************
INSTRUCTIONS:
Environments include:  DBQ01, DBP01

1) Log in to database as THE, via SQL*Plus.
Execute the SET statements before running the scripts
$ sqlplus username/password@Environment
SET DEFINE ON;
SET SQLBL ON;
2) From the SQL Prompt, run the script
https://github.com/bcgov/nr-forest-client/blob/main/legacy/src/test/resources/db/migration/v6.0.1__update_duplicated_contacts.sql
 
3) Verify the query results with expected results detailed in the Excel file  'Results-2026-02-24.xlsx' attached to the parent RFC https://apps.nrs.gov.bc.ca/int/jira/browse/FSADT1-2096
 
  Commit the results if verification OK.
4) Exit SQL*Plus
EXIT

*****************************************************************************
****  ROLLBACK -                                                         ****
****                                                                     ****
*****************************************************************************
*
UPDATE THE.CLIENT_CONTACT c
SET c.CONTACT_NAME = REGEXP_REPLACE(c.CONTACT_NAME, ' [0-9]+$', '');
*/

UPDATE THE.CLIENT_CONTACT c
SET
  c.CONTACT_NAME = (
    SELECT
      CASE 
        WHEN rn = 1 THEN CONTACT_NAME
        ELSE CONTACT_NAME || ' ' || (rn-1)
      END
    FROM
    -- BEGIN rn query
    (
      SELECT
        CLIENT_NUMBER,
        CONTACT_NAME,
        BUS_CONTACT_CODE,
        BUSINESS_PHONE,
        CELL_PHONE,
        FAX_NUMBER,
        EMAIL_ADDRESS,
        ROW_NUMBER() OVER(
          PARTITION BY
            CLIENT_NUMBER,
            CONTACT_NAME
          ORDER BY
            MAX(UPDATE_TIMESTAMP) DESC,
            BUS_CONTACT_CODE,
            BUSINESS_PHONE,
            CELL_PHONE,
            FAX_NUMBER,
            EMAIL_ADDRESS
        ) rn
      FROM THE.CLIENT_CONTACT
      GROUP BY
        CLIENT_NUMBER,
        CONTACT_NAME,
        BUS_CONTACT_CODE,
        BUSINESS_PHONE,
        CELL_PHONE,
        FAX_NUMBER,
        EMAIL_ADDRESS
    ) dg
    WHERE
      dg.CLIENT_NUMBER = c.CLIENT_NUMBER
      AND dg.CONTACT_NAME = c.CONTACT_NAME
      AND DECODE(dg.BUS_CONTACT_CODE, c.BUS_CONTACT_CODE, 1, 0) = 1
      AND DECODE(dg.BUSINESS_PHONE, c.BUSINESS_PHONE, 1, 0) = 1
      AND DECODE(dg.CELL_PHONE, c.CELL_PHONE, 1, 0) = 1
      AND DECODE(dg.FAX_NUMBER, c.FAX_NUMBER, 1, 0) = 1
      AND DECODE(dg.EMAIL_ADDRESS, c.EMAIL_ADDRESS, 1, 0) = 1
    -- END rn query
  ),
  c.REVISION_COUNT = c.REVISION_COUNT + 1,
  c.UPDATE_TIMESTAMP = SYSTIMESTAMP + (ROWNUM/1000000) -- tiny offset
WHERE
  (
    SELECT rn FROM
    -- BEGIN the exact same rn query
    (
      SELECT
        CLIENT_NUMBER,
        CONTACT_NAME,
        BUS_CONTACT_CODE,
        BUSINESS_PHONE,
        CELL_PHONE,
        FAX_NUMBER,
        EMAIL_ADDRESS,
        ROW_NUMBER() OVER(
          PARTITION BY
            CLIENT_NUMBER,
            CONTACT_NAME
          ORDER BY
            MAX(UPDATE_TIMESTAMP) DESC,
            BUS_CONTACT_CODE,
            BUSINESS_PHONE,
            CELL_PHONE,
            FAX_NUMBER,
            EMAIL_ADDRESS
        ) rn
      FROM THE.CLIENT_CONTACT
      GROUP BY
        CLIENT_NUMBER,
        CONTACT_NAME,
        BUS_CONTACT_CODE,
        BUSINESS_PHONE,
        CELL_PHONE,
        FAX_NUMBER,
        EMAIL_ADDRESS
    ) dg
    WHERE
      dg.CLIENT_NUMBER = c.CLIENT_NUMBER
      AND dg.CONTACT_NAME = c.CONTACT_NAME
      AND DECODE(dg.BUS_CONTACT_CODE, c.BUS_CONTACT_CODE, 1, 0) = 1
      AND DECODE(dg.BUSINESS_PHONE, c.BUSINESS_PHONE, 1, 0) = 1
      AND DECODE(dg.CELL_PHONE, c.CELL_PHONE, 1, 0) = 1
      AND DECODE(dg.FAX_NUMBER, c.FAX_NUMBER, 1, 0) = 1
      AND DECODE(dg.EMAIL_ADDRESS, c.EMAIL_ADDRESS, 1, 0) = 1
    -- END the exact same rn query
  ) > 1
