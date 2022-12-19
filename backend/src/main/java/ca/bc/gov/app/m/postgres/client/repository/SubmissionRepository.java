package ca.bc.gov.app.m.postgres.client.repository;

import ca.bc.gov.app.repository.CoreRepository;
import ca.bc.gov.app.m.postgres.client.entity.SubmissionEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends CoreRepository<SubmissionEntity> {

}
