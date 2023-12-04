package ca.bc.gov.app.repository.client;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ca.bc.gov.app.entity.client.EmailLogEntity;
import reactor.core.publisher.Flux;

@Repository
public interface EmailLogRepository extends
  ReactiveCrudRepository<EmailLogEntity, Integer> {

  Flux<EmailLogEntity> findByEmailSentInd(String emailSentInd);
  
}
