package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.SubmissionDetailEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SubmissionDetailRepository extends
    ReactiveCrudRepository<SubmissionDetailEntity, Integer> {

  Mono<SubmissionDetailEntity> findBySubmissionId(Integer submissionId);
}
