package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.CountryCodeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CountryCodeRepository extends ReactiveCrudRepository<CountryCodeEntity, String>,
    ReactiveSortingRepository<CountryCodeEntity, String> {

  Flux<CountryCodeEntity> findAllBy(Pageable pageable);

  Mono<CountryCodeEntity> findByCountryCode(String code);

  Mono<CountryCodeEntity> findByDescription(String description);

}
