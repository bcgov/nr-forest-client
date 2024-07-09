package ca.bc.gov.app.repository.client;

import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ca.bc.gov.app.entity.client.IdentificationTypeCodeEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IdentificationTypeCodeRepository extends ReactiveCrudRepository<IdentificationTypeCodeEntity, String> {

  @Query("""
      select * 
      from nrfc.identification_type_code 
      where (expiry_date is null or expiry_date > :activeDate)
          and effective_date <= :activeDate
      order by description
      """)
  Flux<IdentificationTypeCodeEntity> findActiveAt(LocalDate activeDate);

  Mono<IdentificationTypeCodeEntity> findByCode(String code);
  
  Flux<IdentificationTypeCodeEntity> findByCountryCode(String countryCode);

}
