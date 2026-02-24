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
  c.UPDATE_TIMESTAMP = SYSDATE,
  c.UPDATE_USERID = 'DATAFIX_FSADT1_2096',
  c.UPDATE_ORG_UNIT = 70
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
