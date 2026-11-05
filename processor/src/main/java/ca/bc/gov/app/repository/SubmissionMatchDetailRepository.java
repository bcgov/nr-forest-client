package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SubmissionMatchDetailRepository extends
    ReactiveCrudRepository<SubmissionMatchDetailEntity, Integer> {

  Mono<SubmissionMatchDetailEntity> findBySubmissionId(Integer submissionId);

  Flux<SubmissionMatchDetailEntity> findAllBySubmissionId(Integer submissionId);

  @Modifying
  @Query("""
      UPDATE nrfc.submission_matching_detail
      SET submission_matching_processed_time = now()
      WHERE submission_id = :submissionId
        AND (submission_matching_processed_time IS NULL 
             OR submission_matching_processed_time < now() - interval '2 minutes')
        AND (submission_matching_processed IS NULL 
             OR submission_matching_processed = false)
      """)
  Mono<Long> claimSubmissionForProcessing(Integer submissionId);

}
