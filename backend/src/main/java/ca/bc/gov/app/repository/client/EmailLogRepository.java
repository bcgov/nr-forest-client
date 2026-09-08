package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.EmailLogEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EmailLogRepository extends
  ReactiveCrudRepository<EmailLogEntity, Integer> {

  Flux<EmailLogEntity> findByEmailSentInd(String emailSentInd);
  
}
