package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ClientTypeCodeRepository
    extends ReactiveCrudRepository<ClientTypeCodeEntity, String> {

  @Query("""
      SELECT * FROM
        nrfc.client_type_code 
      WHERE 
        (expiry_date is null or expiry_date > :activeDate)
        and effective_date <= :activeDate
      ORDER BY 
        description
      """)
  Flux<ClientTypeCodeEntity> findActiveAt(LocalDate activeDate);

  Mono<ClientTypeCodeEntity> findByCode(String code);
  
}
