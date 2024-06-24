package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientEntity;
import java.time.LocalDateTime;
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

}
