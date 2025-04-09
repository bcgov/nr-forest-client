package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.ForestClientInformationDto;
import ca.bc.gov.app.dto.HistoryLogDto;
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
                    row_number() over (
                        partition by client_number order by update_timestamp desc
                    ) as rn
                from
                    the.for_cli_audit
                where
                    client_comment is not null
            )
            where rn = 1
        ) fca on c.client_number = fca.client_number
      where c.client_number = :clientNumber""")
  Mono<ForestClientInformationDto> findDetailsByClientNumber(String clientNumber);

  @Query("""
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
      FROM THE.FOREST_CLIENT C
      LEFT JOIN THE.CLIENT_DOING_BUSINESS_AS DBA ON C.CLIENT_NUMBER = DBA.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_TYPE_CODE CTC ON C.CLIENT_TYPE_CODE = CTC.CLIENT_TYPE_CODE
      LEFT JOIN THE.CLIENT_LOCATION CL ON C.CLIENT_NUMBER = CL.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_STATUS_CODE CSC ON C.CLIENT_STATUS_CODE = CSC.CLIENT_STATUS_CODE
      WHERE
          (
              UPPER(C.CLIENT_NUMBER) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.CLIENT_ACRONYM) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.CLIENT_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.LEGAL_FIRST_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.LEGAL_MIDDLE_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(DBA.DOING_BUSINESS_AS_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.CLIENT_IDENTIFICATION) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') ||
                  COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                      COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') || ' ' ||
                      COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(CL.ADDRESS_1) LIKE UPPER('%' || :value || '%')
              OR UPPER(CL.POSTAL_CODE) LIKE UPPER('%' || :value || '%')
              OR UPPER(CL.EMAIL_ADDRESS) LIKE UPPER('%' || :value || '%')
          )
          AND CL.CLIENT_LOCN_CODE = '00'
      ORDER BY SCORE DESC
      OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY""")
  Flux<PredictiveSearchResultDto> findByPredictiveSearchWithLike(
      String value, int limit, long offset);

  @Query("""
      SELECT
          COUNT(C.CLIENT_NUMBER)
      FROM THE.FOREST_CLIENT C
      LEFT JOIN THE.CLIENT_DOING_BUSINESS_AS DBA ON C.CLIENT_NUMBER = DBA.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_TYPE_CODE CTC ON C.CLIENT_TYPE_CODE = CTC.CLIENT_TYPE_CODE
      LEFT JOIN THE.CLIENT_LOCATION CL ON C.CLIENT_NUMBER = CL.CLIENT_NUMBER
      LEFT JOIN THE.CLIENT_STATUS_CODE CSC ON C.CLIENT_STATUS_CODE = CSC.CLIENT_STATUS_CODE
      WHERE
          (
              UPPER(C.CLIENT_NUMBER) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.CLIENT_ACRONYM) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.CLIENT_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.LEGAL_FIRST_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.LEGAL_MIDDLE_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(DBA.DOING_BUSINESS_AS_NAME) LIKE UPPER('%' || :value || '%')
              OR UPPER(C.CLIENT_IDENTIFICATION) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.LEGAL_FIRST_NAME, '') || ' ' ||
                  COALESCE(C.CLIENT_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_MIDDLE_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.CLIENT_NAME, '') || ' ' ||
                  COALESCE(C.LEGAL_FIRST_NAME, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                  COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') ||
                  COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(TRIM(
                      COALESCE(C.REGISTRY_COMPANY_TYPE_CODE, '') || ' ' ||
                      COALESCE(C.CORP_REGN_NMBR, '')
              )) LIKE UPPER('%' || :value || '%')
              OR UPPER(CL.ADDRESS_1) LIKE UPPER('%' || :value || '%')
              OR UPPER(CL.POSTAL_CODE) LIKE UPPER('%' || :value || '%')
              OR UPPER(CL.EMAIL_ADDRESS) LIKE UPPER('%' || :value || '%')
          )
      AND CL.CLIENT_LOCN_CODE = '00'""")
  Mono<Long> countByPredictiveSearchWithLike(String value);
  
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
  Flux<PredictiveSearchResultDto> findByPredictiveSearchWithSimilarity(
      String value, int limit, long offset);

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
  Mono<Long> countByPredictiveSearchWithSimilarity(String value);

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
             
          """)
  Flux<HistoryLogDto> findClientInformationHistoryLogsByClientNumber(String clientNumber);
 
  Flux<HistoryLogDto> findLocationHistoryLogsByClientNumber(String clientNumber);
  
  Flux<HistoryLogDto> findContactHistoryLogsByClientNumber(String clientNumber);
  
  Flux<HistoryLogDto> findDoingBusinessAsHistoryLogsByClientNumber(String clientNumber);
  
}