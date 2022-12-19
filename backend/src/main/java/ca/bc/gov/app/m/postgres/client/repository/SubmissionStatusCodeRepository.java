package ca.bc.gov.app.m.postgres.client.repository;

import ca.bc.gov.app.repository.CoreRepository;
import ca.bc.gov.app.m.postgres.client.entity.SubmissionStatusCodeEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionStatusCodeRepository extends CoreRepository<SubmissionStatusCodeEntity> {

}
