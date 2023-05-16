package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionMatchDetailRepository extends
    ReactiveCrudRepository<SubmissionMatchDetailEntity, Integer> {
}
