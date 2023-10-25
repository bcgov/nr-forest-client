package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionLocationRepository extends
    ReactiveCrudRepository<SubmissionLocationEntity, Integer> {

  Flux<SubmissionLocationEntity> findBySubmissionId(Integer submissionId);
}
