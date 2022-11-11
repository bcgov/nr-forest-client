package ca.bc.gov.app.m.postgres.client.repository;

import org.springframework.stereotype.Repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.postgres.client.entity.SubmissionDetailEntity;

@Repository
public interface SubmissionDetailRepository extends CoreRepository<SubmissionDetailEntity> {

}
