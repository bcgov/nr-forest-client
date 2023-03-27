package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientDoingBusinessAsEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientDoingBusinessAsRepository extends
    ReactiveCrudRepository<ForestClientDoingBusinessAsEntity, String>,
    ReactiveSortingRepository<ForestClientDoingBusinessAsEntity, String> {

  Flux<ForestClientDoingBusinessAsEntity> findByClientNumber(String clientNumber);

}
