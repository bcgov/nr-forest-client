package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ClientDoingBusinessAsRepository extends
    ReactiveCrudRepository<ClientDoingBusinessAsEntity, String>,
    ReactiveSortingRepository<ClientDoingBusinessAsEntity, String> {

  Flux<ClientDoingBusinessAsEntity> findBy(Pageable page);
}
