package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactIdEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionLocationContactRepository extends
    ReactiveCrudRepository<SubmissionLocationContactEntity, SubmissionLocationContactIdEntity> {

  Flux<SubmissionLocationContactEntity> findBySubmissionContactId(Integer id);

  Flux<SubmissionLocationContactEntity> findBySubmissionLocationId(Integer id);
}
