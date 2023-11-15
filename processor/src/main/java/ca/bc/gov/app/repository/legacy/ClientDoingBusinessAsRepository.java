package ca.bc.gov.app.repository.legacy;

import ca.bc.gov.app.entity.legacy.ClientDoingBusinessAsEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ClientDoingBusinessAsRepository extends
    ReactiveCrudRepository<ClientDoingBusinessAsEntity, Integer>,
    ReactiveSortingRepository<ClientDoingBusinessAsEntity, Integer> {

  Mono<Boolean> existsByClientNumber(String clientNumber);
}
