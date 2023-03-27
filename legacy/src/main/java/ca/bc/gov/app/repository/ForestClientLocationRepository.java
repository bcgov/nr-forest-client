package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientLocationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientLocationRepository
    extends ReactiveCrudRepository<ForestClientLocationEntity, String>,
    ReactiveQueryByExampleExecutor<ForestClientLocationEntity>,
    ReactiveSortingRepository<ForestClientLocationEntity, String> {

  Flux<ForestClientLocationEntity> findByClientNumber(String clientNumber);
}
