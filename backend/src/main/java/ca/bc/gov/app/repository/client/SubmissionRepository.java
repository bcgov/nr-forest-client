package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionEntity;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<SubmissionEntity, Integer> {

  Mono<Long> countBySubmissionDateBetweenAndCreateUserIgnoreCase(
              LocalDateTime startTime, 
              LocalDateTime endTime, 
              String createdBy);

}
