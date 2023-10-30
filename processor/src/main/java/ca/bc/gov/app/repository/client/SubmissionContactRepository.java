package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SubmissionContactRepository extends
    ReactiveCrudRepository<SubmissionContactEntity, Integer> {

  Flux<SubmissionContactEntity> findBySubmissionId(Integer submissionId);
  Mono<SubmissionContactEntity> findFirstBySubmissionId(Integer submissionId);
}
