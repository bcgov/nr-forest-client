package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionEntity;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<SubmissionEntity, Integer> {

  Mono<Long> countBySubmissionDateBetweenAndCreatedByIgnoreCase(
              LocalDateTime startTime, 
              LocalDateTime endTime, 
              String createdBy);

  @Query("""
      select count(s.submission_id)
      from nrfc.submission s
      inner join nrfc.submission_detail sd
      on s.submission_id = sd.submission_id
      where 
          s.submission_status_code != 'R' 
          and s.submission_status_code != 'A'
          and sd.business_type_code = 'U' 
          and s.create_user = :fullUsername 
        """)
  Mono<Long> countSubmissionUnregiteredBusinessesByUsername(String fullUsername);

  @Query("""
      select count(s.submission_id)
      from nrfc.submission s
      inner join nrfc.submission_detail sd
      on s.submission_id = sd.submission_id
      where 
          s.submission_status_code != 'R' 
          and s.submission_status_code != 'A'
          and sd.business_type_code = 'R' 
          and sd.incorporation_number = :registrationNumber
        """)
  Mono<Long> countSubmissionRegiteredBusinessesByRegistrationNumber(String registrationNumber);

}
