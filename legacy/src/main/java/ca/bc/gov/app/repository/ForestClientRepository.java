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
          fca.update_userid as client_comment_update_user,
          fca.update_timestamp as client_comment_update_date,
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
                   CLIENT_LOCN_CODE AS IDX,
                   'CLIENT_LOCN_NAME' AS COLUMN_NAME,
                   CLIENT_LOCN_NAME AS NEW_VALUE,
                   LAG(CLIENT_LOCN_NAME) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'HDBS_COMPANY_CODE' AS COLUMN_NAME,
                   HDBS_COMPANY_CODE AS NEW_VALUE,
                   LAG(HDBS_COMPANY_CODE) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'ADDRESS_1' AS COLUMN_NAME,
                   ADDRESS_1 AS NEW_VALUE,
                   LAG(ADDRESS_1) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'ADDRESS_2' AS COLUMN_NAME,
                   ADDRESS_2 AS NEW_VALUE,
                   LAG(ADDRESS_2) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'ADDRESS_3' AS COLUMN_NAME,
                   ADDRESS_3 AS NEW_VALUE,
                   LAG(ADDRESS_3) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'CITY' AS COLUMN_NAME,
                   CITY AS NEW_VALUE,
                   LAG(CITY) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'PROVINCE' AS COLUMN_NAME,
                   PROVINCE AS NEW_VALUE,
                   LAG(PROVINCE) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'POSTAL_CODE' AS COLUMN_NAME,
                   POSTAL_CODE AS NEW_VALUE,
                   LAG(POSTAL_CODE) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'COUNTRY' AS COLUMN_NAME,
                   COUNTRY AS NEW_VALUE,
                   LAG(COUNTRY) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'BUSINESS_PHONE' AS COLUMN_NAME,
                   BUSINESS_PHONE AS NEW_VALUE,
                   LAG(BUSINESS_PHONE) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'HOME_PHONE' AS COLUMN_NAME,
                   HOME_PHONE AS NEW_VALUE,
                   LAG(HOME_PHONE) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'CELL_PHONE' AS COLUMN_NAME,
                   CELL_PHONE AS NEW_VALUE,
                   LAG(CELL_PHONE) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'FAX_NUMBER' AS COLUMN_NAME,
                   FAX_NUMBER AS NEW_VALUE,
                   LAG(FAX_NUMBER) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'EMAIL_ADDRESS' AS COLUMN_NAME,
                   EMAIL_ADDRESS AS NEW_VALUE,
                   LAG(EMAIL_ADDRESS) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber

               UNION ALL

               SELECT
                   'CLI_LOCN_AUDIT' AS TABLE_NAME,
                   CLIENT_LOCN_CODE AS IDX,
                   'CLI_LOCN_COMMENT' AS COLUMN_NAME,
                   CLI_LOCN_COMMENT AS NEW_VALUE,
                   LAG(CLI_LOCN_COMMENT) OVER (PARTITION BY CLIENT_LOCN_CODE ORDER BY AL.UPDATE_TIMESTAMP) AS OLD_VALUE,
                   AL.UPDATE_TIMESTAMP,
                   UPDATE_USERID,
                   RC.DESCRIPTION AS REASON
               FROM THE.CLI_LOCN_AUDIT AL
               LEFT OUTER JOIN THE.CLIENT_UPDATE_REASON_CODE RC
                   ON AL.CLIENT_UPDATE_REASON_CODE = RC.CLIENT_UPDATE_REASON_CODE
               WHERE CLIENT_NUMBER = :clientNumber
           )

           SELECT
               TABLE_NAME,
               IDX,
               L.CLIENT_LOCN_NAME AS IDENTIFIER_LABEL,
               COLUMN_NAME,
               CASE
                   WHEN TRIM(OLD_VALUE) = 'Y' THEN 'Yes'
                   WHEN TRIM(OLD_VALUE) = 'N' THEN 'No'
                   ELSE OLD_VALUE
               END AS OLD_VALUE,
               CASE
                   WHEN TRIM(NEW_VALUE) = 'Y' THEN 'Yes'
                   WHEN TRIM(NEW_VALUE) = 'N' THEN 'No'
                   ELSE NEW_VALUE
               END AS NEW_VALUE,
               A.UPDATE_TIMESTAMP,
               A.UPDATE_USERID,
               CASE
                   WHEN OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL THEN 'INSERT'
                   WHEN OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL THEN 'DELETE'
                   WHEN TRIM(OLD_VALUE) <> TRIM(NEW_VALUE) THEN 'UPDATE'
               END AS CHANGE_TYPE,
               REASON
           FROM AUDIT_DATA A
           INNER JOIN (
               SELECT *
               FROM THE.CLIENT_LOCATION
               WHERE CLIENT_NUMBER = :clientNumber
           ) L
               ON A.IDX = L.CLIENT_LOCN_CODE
           WHERE
               (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL)
               OR (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL)
               OR (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
           ORDER BY A.UPDATE_TIMESTAMP DESC
             """)
  Flux<AuditLogDto> findLocationAuditLogsByClientNumber(String clientNumber);

  @Query("""
      WITH AUDIT_DATA AS (
          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_NAME' AS COLUMN_NAME,
            CLIENT_NAME AS NEW_VALUE,
            LAG(CLIENT_NAME) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'LEGAL_FIRST_NAME' AS COLUMN_NAME,
            LEGAL_FIRST_NAME AS NEW_VALUE,
            LAG(LEGAL_FIRST_NAME) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'LEGAL_MIDDLE_NAME' AS COLUMN_NAME,
            LEGAL_MIDDLE_NAME AS NEW_VALUE,
            LAG(LEGAL_MIDDLE_NAME) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_STATUS_CODE' AS COLUMN_NAME,
            CLIENT_STATUS_CODE AS NEW_VALUE,
            LAG(CLIENT_STATUS_CODE) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_TYPE_CODE' AS COLUMN_NAME,
            CLIENT_TYPE_CODE AS NEW_VALUE,
            LAG(CLIENT_TYPE_CODE) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'BIRTHDATE' AS COLUMN_NAME,
            TO_CHAR(BIRTHDATE, 'YYYY-MM-DD') AS NEW_VALUE,
            LAG(TO_CHAR(BIRTHDATE, 'YYYY-MM-DD')) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_ID_TYPE_CODE' AS COLUMN_NAME,
            CLIENT_ID_TYPE_CODE AS NEW_VALUE,
            LAG(CLIENT_ID_TYPE_CODE) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_IDENTIFICATION' AS COLUMN_NAME,
            CLIENT_IDENTIFICATION AS NEW_VALUE,
            LAG(CLIENT_IDENTIFICATION) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

            SELECT 
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'REGISTRATION_NUMBER' AS COLUMN_NAME,
            REGISTRY_COMPANY_TYPE_CODE || CORP_REGN_NMBR AS NEW_VALUE,
            LAG(REGISTRY_COMPANY_TYPE_CODE) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) 
            ||
            LAG(CORP_REGN_NMBR) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
        FROM THE.FOR_CLI_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_ACRONYM' AS COLUMN_NAME,
            CLIENT_ACRONYM AS NEW_VALUE,
            LAG(CLIENT_ACRONYM) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'WCB_FIRM_NUMBER' AS COLUMN_NAME,
            WCB_FIRM_NUMBER AS NEW_VALUE,
            LAG(WCB_FIRM_NUMBER) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'OCG_SUPPLIER_NMBR' AS COLUMN_NAME,
            OCG_SUPPLIER_NMBR AS NEW_VALUE,
            LAG(OCG_SUPPLIER_NMBR) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber

          UNION ALL

          SELECT
            'FOR_CLI_AUDIT' AS TABLE_NAME,
            '' AS IDX,
            'CLIENT_COMMENT' AS COLUMN_NAME,
            CLIENT_COMMENT AS NEW_VALUE,
            LAG(CLIENT_COMMENT) OVER (PARTITION BY CLIENT_NUMBER ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
            UPDATE_TIMESTAMP,
            UPDATE_USERID
          FROM THE.FOR_CLI_AUDIT
          WHERE CLIENT_NUMBER = :clientNumber
        )

        SELECT
          TABLE_NAME,
          IDX,
          '' AS IDENTIFIER_LABEL,
          COLUMN_NAME,
          CASE
            WHEN TRIM(OLD_VALUE) = 'Y' THEN 'Yes'
            WHEN TRIM(OLD_VALUE) = 'N' THEN 'No'
            ELSE OLD_VALUE
          END AS OLD_VALUE,
          CASE
            WHEN TRIM(NEW_VALUE) = 'Y' THEN 'Yes'
            WHEN TRIM(NEW_VALUE) = 'N' THEN 'No'
            ELSE NEW_VALUE
          END AS NEW_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID,
          CASE
            WHEN OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL THEN 'INSERT'
            WHEN OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL THEN 'DELETE'
            WHEN TRIM(OLD_VALUE) <> TRIM(NEW_VALUE) THEN 'UPDATE'
          END AS CHANGE_TYPE,
          '' AS REASON
        FROM AUDIT_DATA A
        WHERE
          (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL)
          OR (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL)
          OR (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
        ORDER BY UPDATE_TIMESTAMP DESC
      """)
  Flux<AuditLogDto> findClientInformationAuditLogsByClientNumber(String clientNumber);

  @Query("""
      WITH AUDIT_DATA AS (
        SELECT
          'CLI_CON_AUDIT' AS TABLE_NAME,
          CLIENT_CONTACT_ID AS IDX,
          'BUS_CONTACT_CODE' AS COLUMN_NAME,
          BUS_CONTACT_CODE AS NEW_VALUE,
          LAG(BUS_CONTACT_CODE) OVER (PARTITION BY CLIENT_CONTACT_ID ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID
        FROM THE.CLI_CON_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber

        UNION ALL

        SELECT
          'CLI_CON_AUDIT' AS TABLE_NAME,
          CLIENT_CONTACT_ID AS IDX,
          'CONTACT_NAME' AS COLUMN_NAME,
          CONTACT_NAME AS NEW_VALUE,
          LAG(CONTACT_NAME) OVER (PARTITION BY CLIENT_CONTACT_ID ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID
        FROM THE.CLI_CON_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber

        UNION ALL

        SELECT
          'CLI_CON_AUDIT' AS TABLE_NAME,
          CLIENT_CONTACT_ID AS IDX,
          'BUSINESS_PHONE' AS COLUMN_NAME,
          BUSINESS_PHONE AS NEW_VALUE,
          LAG(BUSINESS_PHONE) OVER (PARTITION BY CLIENT_CONTACT_ID ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID
        FROM THE.CLI_CON_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber

        UNION ALL

        SELECT
          'CLI_CON_AUDIT' AS TABLE_NAME,
          CLIENT_CONTACT_ID AS IDX,
          'CELL_PHONE' AS COLUMN_NAME,
          CELL_PHONE AS NEW_VALUE,
          LAG(CELL_PHONE) OVER (PARTITION BY CLIENT_CONTACT_ID ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID
        FROM THE.CLI_CON_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber

        UNION ALL

        SELECT
          'CLI_CON_AUDIT' AS TABLE_NAME,
          CLIENT_CONTACT_ID AS IDX,
          'FAX_NUMBER' AS COLUMN_NAME,
          FAX_NUMBER AS NEW_VALUE,
          LAG(FAX_NUMBER) OVER (PARTITION BY CLIENT_CONTACT_ID ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID
        FROM THE.CLI_CON_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber

        UNION ALL

        SELECT
          'CLI_CON_AUDIT' AS TABLE_NAME,
          CLIENT_CONTACT_ID AS IDX,
          'EMAIL_ADDRESS' AS COLUMN_NAME,
          EMAIL_ADDRESS AS NEW_VALUE,
          LAG(EMAIL_ADDRESS) OVER (PARTITION BY CLIENT_CONTACT_ID ORDER BY UPDATE_TIMESTAMP) AS OLD_VALUE,
          UPDATE_TIMESTAMP,
          UPDATE_USERID
        FROM THE.CLI_CON_AUDIT
        WHERE CLIENT_NUMBER = :clientNumber
      )

      SELECT
        TABLE_NAME,
        IDX,
        COLUMN_NAME,
        OLD_VALUE,
        NEW_VALUE,
        UPDATE_TIMESTAMP,
        UPDATE_USERID,
        CASE
          WHEN OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL THEN 'INSERT'
          WHEN OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL THEN 'DELETE'
          WHEN TRIM(OLD_VALUE) <> TRIM(NEW_VALUE) THEN 'UPDATE'
        END AS CHANGE_TYPE
      FROM AUDIT_DATA
      WHERE
        (OLD_VALUE IS NULL AND TRIM(NEW_VALUE) IS NOT NULL)
        OR (OLD_VALUE IS NOT NULL AND NEW_VALUE IS NULL)
        OR (TRIM(OLD_VALUE) <> TRIM(NEW_VALUE))
      ORDER BY UPDATE_TIMESTAMP DESC
      """)
  Flux<AuditLogDto> findConctactAuditLogsByClientNumber(String clientNumber);
  
}