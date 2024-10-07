package ca.bc.gov.app.repository;

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
      	(
      		CASE WHEN c.client_number = :value THEN 112 ELSE 0 END +
      		CASE WHEN c.CLIENT_ACRONYM = :value THEN 111 ELSE 0 END +
      		(UTL_MATCH.JARO_WINKLER_SIMILARITY(c.client_name, :value)+10) +
      		(UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_first_name, :value)+9) +
      		(UTL_MATCH.JARO_WINKLER_SIMILARITY(dba.doing_business_as_name, :value)+7) +
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
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.client_name,:value) >= 90
          OR c.client_name LIKE '%' || :value || '%'
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_first_name,:value) >= 90
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(dba.doing_business_as_name,:value) >= 90
          OR dba.doing_business_as_name LIKE '%' || :value || '%'
          OR c.client_identification = :value
          OR UTL_MATCH.JARO_WINKLER_SIMILARITY(c.legal_middle_name,:value) >= 90
        )  AND
        cl.CLIENT_LOCN_CODE = '00'
      ORDER BY score DESC
      FETCH FIRST 5 ROWS ONLY""")
  Flux<PredictiveSearchResultDto> findByPredictiveSearch(String value);

}
