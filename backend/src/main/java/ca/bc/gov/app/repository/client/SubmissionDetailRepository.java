package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionDetailRepository extends
    ReactiveCrudRepository<SubmissionDetailEntity, Integer> {
}
