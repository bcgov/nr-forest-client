package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SubmissionMatchDetailRepository extends
    ReactiveCrudRepository<SubmissionMatchDetailEntity, Integer> {

  Mono<SubmissionMatchDetailEntity> findBySubmissionId(Integer submissionId);

}
