package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.RelatedClientEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RelatedClientRepository
    extends ReactiveCrudRepository<RelatedClientEntity, String> {

  Flux<RelatedClientEntity> findByClientNumberAndClientLocationCode(
      String clientNumber, String clientLocationCode);

  @Query(ForestClientQueries.RELATED_EXACT_SEARCH)
  Mono<RelatedClientEntity> findToUpdate(
      String clientNumber,
      String clientLocationCode,
      String relatedClientNumber,
      String relatedClientLocationCode
  );

}
