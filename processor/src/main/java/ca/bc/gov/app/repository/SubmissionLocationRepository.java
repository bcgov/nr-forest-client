package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.SubmissionLocationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionLocationRepository extends
    ReactiveCrudRepository<SubmissionLocationEntity, Integer> {

  Flux<SubmissionLocationEntity> findBySubmissionId(Integer submissionId);
}
