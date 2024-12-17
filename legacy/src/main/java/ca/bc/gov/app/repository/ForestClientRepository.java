package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.AuditLogDto;
import ca.bc.gov.app.dto.ForestClientDetailsDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ForestClientRepository extends ReactiveCrudRepository<ForestClientEntity, String>,
    ReactiveQueryByExampleExecutor<ForestClientEntity>,
    ReactiveSortingRepository<ForestClientEntity, String> {

  Flux<ForestClientEntity> findBy(Pageable page);

  @Query("""
      SELECT * FROM FOREST_CLIENT x
       WHERE (UPPER(x.REGISTRY_COMPANY_TYPE_CODE) || x.CORP_REGN_NMBR) = UPPER(:registrationNumber)
       OR UPPER(x.CLIENT_NAME) = UPPER(:companyName)
       OR x.CLIENT_IDENTIFICATION = UPPER(:registrationNumber)""")
  Flux<ForestClientEntity> findClientByIncorporationOrName(
      @Param("registrationNumber") String registrationNumber,
      @Param("companyName") String companyName
  );

  @Query("""
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
      BIRTHDATE = :dob
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
      ORDER BY CLIENT_NUMBER""")
  Flux<ForestClientEntity> findByIndividualFuzzy(String name, LocalDateTime dob);

  @Query("""
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
      UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(CLIENT_NAME),UPPER(:companyName)) >= 95
      ORDER BY CLIENT_NUMBER""")
  Flux<ForestClientEntity> matchBy(String companyName);

  Mono<ForestClientEntity> findByClientNumber(String clientNumber);
  
  @Query("""
      select
          c.client_number,
          c.client_name,
          c.legal_first_name,
          c.legal_middle_name,
          c.client_status_code,
          s.description as client_status_desc,
          c.client_type_code,
          t.description as client_type_desc,
          c.client_id_type_code,
          it.description as client_id_type_desc,
          c.client_identification,
          c.registry_company_type_code,
          c.corp_regn_nmbr,
          c.client_acronym,
          c.wcb_firm_number,
          c.client_comment,
          fca.update_userid as latest_update_userid,
          fca.update_timestamp as latest_update_timestamp,
          '' as good_standing_ind,
          c.birthdate
      from the.forest_client c
        inner join the.client_status_code s on c.client_status_code = s.client_status_code
        inner join the.client_type_code t on c.client_type_code = t.client_type_code
        left join the.client_id_type_code it on c.client_id_type_code = it.client_id_type_code
        left join (
            select
                client_number,
                update_userid,
                update_timestamp
            from (
                select
                    client_number,
                    update_userid,
                    update_timestamp,
                    row_number() over (partition by client_number order by update_timestamp desc) as rn
                from
                    the.for_cli_audit
                where
                    client_comment is not null
            )
            where rn = 1
        ) fca on c.client_number = fca.client_number
      where c.client_number = :clientNumber""")
  Mono<ForestClientDetailsDto> findDetailsByClientNumber(String clientNumber);

  @Query("""
      SELECT
          c.client_number,
          c.CLIENT_ACRONYM AS client_acronym,
          c.client_name,
          c.legal_first_name AS client_first_name,
          dba.doing_business_as_name AS doing_business_as,
          c.client_identification,
          c.legal_middle_name AS client_middle_name,
          cl.city AS city,
          ctc.description AS client_type,
          csc.description AS client_status,
          (
              CASE WHEN c.client_number = :value THEN 112 ELSE 0 END +
              CASE WHEN c.CLIENT_ACRONYM = :value THEN 111 ELSE 0 END +
              (UTL_MATCH.JARO_WINKLER_SIMILARITY(
                  c.client_name || ' ' || c.legal_first_name, :value
              ) + 10) +
              (UTL_MATCH.JARO_WINKLER_SIMILARITY(dba.doing_business_as_name, :value) + 7) +
              CASE WHEN c.client_identification = :value THEN 106 ELSE 0 END +
              UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_middle_name, :value)
          ) AS score
      FROM the.forest_client c
      LEFT JOIN the.CLIENT_DOING_BUSINESS_AS dba ON c.client_number = dba.client_number
      LEFT JOIN the.CLIENT_TYPE_CODE ctc ON c.client_type_code = ctc.client_type_code
      LEFT JOIN the.CLIENT_LOCATION cl ON c.client_number = cl.client_number
      LEFT JOIN the.CLIENT_STATUS_CODE csc ON c.client_status_code = csc.client_status_code
      WHERE
          (
              c.client_number = :value
              OR c.CLIENT_ACRONYM = :value
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.client_name, :value) >= 90
              OR c.client_name LIKE '%' || :value || '%'
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_first_name, :value) >= 90
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(dba.doing_business_as_name, :value) >= 90
              OR dba.doing_business_as_name LIKE '%' || :value || '%'
              OR c.client_identification = :value
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_middle_name, :value) >= 90
              OR c.legal_middle_name LIKE '%' || :value || '%'
              OR (
                  c.client_type_code = 'I' AND (
                      UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.legal_first_name, '') || ' ' || 
                              COALESCE(c.legal_middle_name, '') || 
                              COALESCE(c.client_name, '')
                          ), :value
                      ) >= 90
                      OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.legal_first_name, '') || ' ' || 
                              COALESCE(c.client_name, '')
                          ), :value
                      ) >= 90
                      OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.client_name, '') || ' ' || 
                              COALESCE(c.legal_middle_name, '') || 
                              COALESCE(c.legal_first_name, '')
                          ), :value
                      ) >= 90
                      OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.client_name, '') || ' ' || 
                              COALESCE(c.legal_first_name, '')
                          ), :value
                      ) >= 90
                  )
              )
          )
          AND cl.CLIENT_LOCN_CODE = '00'
      ORDER BY score DESC
      OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY""")
  Flux<PredictiveSearchResultDto> findByPredictiveSearch(String value, int limit, long offset);

  @Query("""
      SELECT
          count(c.client_number)
      FROM the.forest_client c
      LEFT JOIN the.CLIENT_DOING_BUSINESS_AS dba ON c.client_number = dba.client_number
      LEFT JOIN the.CLIENT_TYPE_CODE ctc ON c.client_type_code = ctc.client_type_code
      LEFT JOIN the.CLIENT_LOCATION cl ON c.client_number = cl.client_number
      LEFT JOIN the.CLIENT_STATUS_CODE csc ON c.client_status_code = csc.client_status_code
      WHERE
          (
              c.client_number = :value
              OR c.CLIENT_ACRONYM = :value
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.client_name, :value) >= 90
              OR c.client_name LIKE '%' || :value || '%'
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_first_name, :value) >= 90
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(dba.doing_business_as_name, :value) >= 90
              OR dba.doing_business_as_name LIKE '%' || :value || '%'
              OR c.client_identification = :value
              OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_middle_name, :value) >= 90
              OR c.legal_middle_name LIKE '%' || :value || '%'
              OR (
                  c.client_type_code = 'I' AND (
                      UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.legal_first_name, '') || ' ' || 
                              COALESCE(c.legal_middle_name, '') || 
                              COALESCE(c.client_name, '')
                          ), :value
                      ) >= 90
                      OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.legal_first_name, '') || ' ' || 
                              COALESCE(c.client_name, '')
                          ), :value
                      ) >= 90
                      OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.client_name, '') || ' ' || 
                              COALESCE(c.legal_middle_name, '') || 
                              COALESCE(c.legal_first_name, '')
                          ), :value
                      ) >= 90
                      OR UTL_MATCH.JARO_WINKLER_SIMILARITY(
                          TRIM(
                              COALESCE(c.client_name, '') || ' ' || 
                              COALESCE(c.legal_first_name, '')
                          ), :value
                      ) >= 90
                  )
              )
          )
          AND cl.CLIENT_LOCN_CODE = '00'""")
  Mono<Long> countByPredictiveSearch(String value);

  @Query("""
      SELECT
          c.client_number,
          c.CLIENT_ACRONYM as client_acronym,
          c.client_name,
          c.legal_first_name as client_first_name,
          dba.doing_business_as_name as doing_business_as,
          c.client_identification,
          c.legal_middle_name as client_middle_name,
          cl.city as city,
          ctc.description as client_type,
          csc.description as client_status,
          100 as score
      FROM the.forest_client c
      LEFT JOIN the.CLIENT_DOING_BUSINESS_AS dba ON c.client_number = dba.client_number
      LEFT JOIN the.CLIENT_TYPE_CODE ctc ON c.client_type_code = ctc.client_type_code
      LEFT JOIN the.CLIENT_LOCATION cl ON c.client_number = cl.client_number
      LEFT JOIN the.CLIENT_STATUS_CODE csc ON c.client_status_code = csc.client_status_code
      WHERE
        cl.CLIENT_LOCN_CODE = '00'
        AND (c.update_timestamp >= :date OR c.add_timestamp >= :date)
      ORDER BY c.ADD_TIMESTAMP DESC
      OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY""") 
  Flux<PredictiveSearchResultDto> findByEmptyFullSearch(int limit, long offset, LocalDateTime date);
  
  @Query("""
      SELECT
          count(c.client_number)
      FROM the.forest_client c
      LEFT JOIN the.CLIENT_DOING_BUSINESS_AS dba ON c.client_number = dba.client_number
      LEFT JOIN the.CLIENT_TYPE_CODE ctc ON c.client_type_code = ctc.client_type_code
      LEFT JOIN the.CLIENT_LOCATION cl ON c.client_number = cl.client_number
      LEFT JOIN the.CLIENT_STATUS_CODE csc ON c.client_status_code = csc.client_status_code
      WHERE
        cl.CLIENT_LOCN_CODE = '00'
        AND (c.update_timestamp >= :date OR c.add_timestamp >= :date)""")
  Mono<Long> countByEmptyFullSearch(LocalDateTime date);

  @Query("""
WITH AUDIT_DATA AS (
  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'CLIENT_LOCN_NAME' AS COLUMN_NAME, 
    CLIENT_LOCN_NAME AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY CLIENT_LOCN_NAME 
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber
  
  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'HDBS_COMPANY_CODE' AS COLUMN_NAME, 
    HDBS_COMPANY_CODE AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY HDBS_COMPANY_CODE  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber
  
  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'ADDRESS_1' AS COLUMN_NAME, 
    ADDRESS_1 AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY ADDRESS_1  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber
  
  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'ADDRESS_2' AS COLUMN_NAME, 
    ADDRESS_2 AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY ADDRESS_2  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber
  
  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'ADDRESS_3' AS COLUMN_NAME, 
    ADDRESS_3 AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY ADDRESS_3  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber
  
  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'CITY' AS COLUMN_NAME, 
    CITY AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY CITY  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'PROVINCE' AS COLUMN_NAME,
    PROVINCE AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY PROVINCE  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'POSTAL_CODE' AS COLUMN_NAME, 
    POSTAL_CODE AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY POSTAL_CODE  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'COUNTRY' AS COLUMN_NAME, 
    COUNTRY AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY COUNTRY  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'BUSINESS_PHONE' AS COLUMN_NAME, 
    BUSINESS_PHONE AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY BUSINESS_PHONE  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'HOME_PHONE' AS COLUMN_NAME, 
    HOME_PHONE AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY HOME_PHONE  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'CELL_PHONE' AS COLUMN_NAME, 
    CELL_PHONE AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY CELL_PHONE  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'FAX_NUMBER' AS COLUMN_NAME, 
    FAX_NUMBER AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY FAX_NUMBER  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'EMAIL_ADDRESS' AS COLUMN_NAME, 
    EMAIL_ADDRESS AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY EMAIL_ADDRESS  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber

  UNION ALL

  SELECT 
    'CLI_LOCN_AUDIT' AS TABLE_NAME,
    TO_CHAR(CLIENT_LOCN_CODE) AS IDX,
    'LOCN_EXPIRED_IND' AS COLUMN_NAME, 
    LOCN_EXPIRED_IND AS VALUE, 
    UPDATE_TIMESTAMP, 
    UPDATE_USERID, 
    ROW_NUMBER() OVER (
      PARTITION BY LOCN_EXPIRED_IND  
      ORDER BY UPDATE_TIMESTAMP
    ) AS ROW_NUM 
  FROM 
    THE.CLI_LOCN_AUDIT 
  WHERE 
    CLIENT_NUMBER = :clientNumber
)

SELECT 
  A1.TABLE_NAME,
  A1.IDX,
  A1.COLUMN_NAME, 
  A1.VALUE AS OLD_VALUE, 
  A2.VALUE AS NEW_VALUE, 
  A1.UPDATE_TIMESTAMP, 
  A1.UPDATE_USERID, 
  CASE 
    WHEN A1.VALUE IS NULL AND A2.VALUE IS NOT NULL THEN 'INSERT'
    WHEN A1.VALUE IS NOT NULL AND A2.VALUE IS NULL THEN 'DELETE'
    WHEN A1.VALUE <> A2.VALUE THEN 'UPDATE'
    ELSE 'NO CHANGE'
  END AS CHANGE_TYPE
FROM 
  AUDIT_DATA A1 
  JOIN AUDIT_DATA A2 ON A1.TABLE_NAME = A2.TABLE_NAME 
  AND TO_CHAR(A1.IDX) = TO_CHAR(A2.IDX) 
  AND A1.COLUMN_NAME = A2.COLUMN_NAME 
  AND A1.ROW_NUM = A2.ROW_NUM - 1
WHERE 
  (
    A1.VALUE <> A2.VALUE 
    OR (
      A1.VALUE IS NULL 
      AND A2.VALUE IS NOT NULL
    ) 
    OR (
      A1.VALUE IS NOT NULL 
      AND A2.VALUE IS NULL
    )
  )
ORDER BY 
  A1.UPDATE_TIMESTAMP DESC,
  A1.COLUMN_NAME
""")
  Flux<AuditLogDto> findAuditLogsByClientNumber(String clientNumber);
  
}