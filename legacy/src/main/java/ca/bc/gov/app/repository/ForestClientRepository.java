package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.ForestClientInformationDto;
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
          100 AS SCORE
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
          )
          AND CL.CLIENT_LOCN_CODE = '00'
      OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY""")
  Flux<PredictiveSearchResultDto> findByPredictiveSearch(String value, int limit, long offset);

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
      )
      AND CL.CLIENT_LOCN_CODE = '00'""")
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
  
}