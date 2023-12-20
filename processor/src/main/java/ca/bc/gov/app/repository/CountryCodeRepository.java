package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.CountryCodeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
/**
 * CountryCodeRepository
 * Repository responsible for retrieving CountryCodeEntity objects from the database.
 */
public interface CountryCodeRepository extends ReactiveCrudRepository<CountryCodeEntity, String>,
    ReactiveSortingRepository<CountryCodeEntity, String> {

  Flux<CountryCodeEntity> findBy(Pageable pageable);

  Mono<CountryCodeEntity> findByCountryCode(String code);

  Mono<CountryCodeEntity> findByDescription(String description);
}
