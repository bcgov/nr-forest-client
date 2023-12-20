package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ClientDoingBusinessAsRepository extends
    ReactiveCrudRepository<ClientDoingBusinessAsEntity, String>,
    ReactiveSortingRepository<ClientDoingBusinessAsEntity, String> {

  Mono<Boolean> existsByClientNumber(String clientNumber);

  @Query("""
      SELECT *
      FROM THE.CLIENT_DOING_BUSINESS_AS
      WHERE
      UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(DOING_BUSINESS_AS_NAME),UPPER(:companyName)) >= 95
      ORDER BY CLIENT_NUMBER""")
  Flux<ClientDoingBusinessAsEntity> matchBy(String companyName);
}
