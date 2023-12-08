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

@Repository
public interface ForestClientRepository extends ReactiveCrudRepository<ForestClientEntity, String>,
    ReactiveQueryByExampleExecutor<ForestClientEntity>,
    ReactiveSortingRepository<ForestClientEntity, String> {

  @Query("""
      SELECT * FROM FOREST_CLIENT x
       WHERE (UPPER(x.REGISTRY_COMPANY_TYPE_CODE) || x.CORP_REGN_NMBR) = UPPER(:incorporationNumber)
       OR UPPER(x.CLIENT_NAME) = UPPER(:companyName)
       OR (x.CLIENT_ID_TYPE_CODE = 'BCRE' and x.CLIENT_IDENTIFICATION = UPPER(:incorporationNumber))
       AND x.CLIENT_STATUS_CODE = 'ACT'""")
  Flux<ForestClientEntity> findClientByIncorporationOrName(
      @Param("incorporationNumber") String incorporationNumber,
      @Param("companyName") String companyName
  );

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
