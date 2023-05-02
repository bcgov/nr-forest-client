package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.ContactTypeCodeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ContactTypeCodeRepository
    extends ReactiveCrudRepository<ContactTypeCodeEntity, String>,
    ReactiveSortingRepository<ContactTypeCodeEntity, String> {

  Flux<ContactTypeCodeEntity> findBy(Pageable pageable);
}
