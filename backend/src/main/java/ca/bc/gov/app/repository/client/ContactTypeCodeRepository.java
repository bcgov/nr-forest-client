package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.ContactTypeCodeEntity;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ContactTypeCodeRepository
    extends ReactiveCrudRepository<ContactTypeCodeEntity, String>,
    ReactiveSortingRepository<ContactTypeCodeEntity, String> {

  @Query("""
      SELECT * FROM
      nrfc.contact_type_code
      WHERE
      (expiry_date is null or expiry_date > :activeDate)
      and effective_date <= :activeDate
      ORDER BY description""")
  Flux<ContactTypeCodeEntity> findActiveAt(LocalDate activeDate, Pageable pageable);

  Mono<ContactTypeCodeEntity> findByOrDescription(String id, String description);
}
