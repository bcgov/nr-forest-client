package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.ProvinceCodeEntity;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProvinceCodeRepository extends ReactiveCrudRepository<ProvinceCodeEntity, String>,
    ReactiveSortingRepository<ProvinceCodeEntity, String> {

  Flux<ProvinceCodeEntity> findByCountryCode(String countryCode, Pageable pageable);

  Mono<ProvinceCodeEntity> findByCountryCodeAndProvinceCode(String countryCode,
                                                            String provinceCode);

  Mono<ProvinceCodeEntity> findByProvinceCode(String provinceCode);
}
