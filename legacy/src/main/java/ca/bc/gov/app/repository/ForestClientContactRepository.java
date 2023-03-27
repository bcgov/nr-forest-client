package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientContactEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientContactRepository
    extends ReactiveCrudRepository<ForestClientContactEntity, Long>,
    ReactiveQueryByExampleExecutor<ForestClientContactEntity>,
    ReactiveSortingRepository<ForestClientContactEntity, Long> {

  Flux<ForestClientContactEntity> findByClientNumber(String clientNumber);

  Flux<ForestClientContactEntity> findByClientNumberAndClientLocnCode(
      String clientNumber,
      String locationCode
  );
}
