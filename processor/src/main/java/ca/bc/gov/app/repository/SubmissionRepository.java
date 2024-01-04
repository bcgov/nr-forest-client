package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.SubmissionEntity;
import ca.bc.gov.app.entity.SubmissionTypeCodeEnum;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<SubmissionEntity, Integer> {
  Flux<SubmissionEntity> findBySubmissionType(SubmissionTypeCodeEnum submissionType);
}
