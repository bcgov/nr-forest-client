package ca.bc.gov.app.repository.legacy;

import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientRepository extends ReactiveCrudRepository<ForestClientEntity, String>,
    ReactiveQueryByExampleExecutor<ForestClientEntity>,
    ReactiveSortingRepository<ForestClientEntity, String> {

  @Query("""
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
      UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(CLIENT_NAME),UPPER(:companyName)) >= 95
      AND CLIENT_STATUS_CODE = 'ACT'
      ORDER BY CLIENT_NUMBER""")
  Flux<ForestClientEntity> matchBy(String companyName);

  @Query("""
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
      (REGISTRY_COMPANY_TYPE_CODE || CORP_REGN_NMBR) = :incorporationNumber
      AND CLIENT_STATUS_CODE = 'ACT'
      ORDER BY CLIENT_NUMBER""")
  Flux<ForestClientEntity> findByIncorporationNumber(String incorporationNumber);

  @Query("""
      SELECT *
      FROM THE.FOREST_CLIENT
      WHERE
      UPPER(LEGAL_FIRST_NAME) = UPPER(:firstName)
      AND UPPER(CLIENT_NAME) = UPPER(:lastName)
      AND BIRTHDATE = :dob
      AND CLIENT_STATUS_CODE = 'ACT'
      AND CLIENT_TYPE_CODE = 'I'
      ORDER BY CLIENT_NUMBER""")
  Flux<ForestClientEntity> findByIndividual(String firstName, String lastName, LocalDateTime dob);

}
