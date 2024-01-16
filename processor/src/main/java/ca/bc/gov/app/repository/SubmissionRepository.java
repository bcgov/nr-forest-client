package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.SubmissionEntity;
import ca.bc.gov.app.entity.SubmissionTypeCodeEnum;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<SubmissionEntity, Integer> {
  Flux<SubmissionEntity> findBySubmissionType(SubmissionTypeCodeEnum submissionType);

  @Query("""
        SELECT nrfc.submission.submission_id
        FROM nrfc.submission
        WHERE nrfc.submission.submission_type_code = 'SPP'"""
  )
  Flux<Integer> loadProcessingSubmissions();

  @Query("""
        SELECT nrfc.submission.submission_id
        FROM nrfc.submission
        LEFT JOIN nrfc.submission_matching_detail 
        ON nrfc.submission_matching_detail.submission_id = nrfc.submission.submission_id
        WHERE
        nrfc.submission.submission_status_code in ('R','A')
        AND ( nrfc.submission_matching_detail.submission_matching_processed  is null or
        nrfc.submission_matching_detail.submission_matching_processed = false)"""
  )
  Flux<Integer> loadProcessedSubmissions();
}
