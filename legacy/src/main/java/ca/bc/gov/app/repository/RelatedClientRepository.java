package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.RelatedClientEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RelatedClientRepository
    extends ReactiveCrudRepository<RelatedClientEntity, String> {

  Flux<RelatedClientEntity> findByClientNumberAndClientLocationCode(
      String clientNumber, String clientLocationCode);


}
