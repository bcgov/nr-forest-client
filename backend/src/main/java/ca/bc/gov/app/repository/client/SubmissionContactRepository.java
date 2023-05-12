package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionContactRepository extends
    ReactiveCrudRepository<SubmissionContactEntity, Integer> {
}
