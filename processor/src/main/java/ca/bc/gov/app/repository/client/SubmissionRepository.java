package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.entity.client.SubmissionStatusEnum;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<SubmissionEntity, Integer> {
  Flux<SubmissionEntity> findBySubmissionStatus(SubmissionStatusEnum status);
}
