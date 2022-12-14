package ca.bc.gov.app.m.postgres.client.repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.postgres.client.entity.SubmissionDetailEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionDetailRepository extends CoreRepository<SubmissionDetailEntity> {

}
