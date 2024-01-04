package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SubmissionMatchDetailRepository extends
    ReactiveCrudRepository<SubmissionMatchDetailEntity, Integer> {

  Mono<SubmissionMatchDetailEntity> findBySubmissionId(Integer submissionId);

  Flux<SubmissionMatchDetailEntity> findAllBySubmissionId(Integer submissionId);

}
