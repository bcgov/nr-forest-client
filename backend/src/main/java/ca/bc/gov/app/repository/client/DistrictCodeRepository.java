package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.DistrictCodeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DistrictCodeRepository
    extends ReactiveCrudRepository<DistrictCodeRepository, String>,
    ReactiveSortingRepository<DistrictCodeRepository, String> {

  Flux<DistrictCodeEntity> findBy(Pageable pageable);

  Mono<DistrictCodeEntity> findByDistrictCode(String code);

}
