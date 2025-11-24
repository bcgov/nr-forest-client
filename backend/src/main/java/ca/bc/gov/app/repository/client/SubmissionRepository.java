package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.dto.client.ClientSubmissionDistrictListDto;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
  
  @Query("""
      select s.submission_id as id, dc.description as district, dc.email_address as emails
      from nrfc.submission s
      inner join nrfc.submission_detail sd
      on s.submission_id = sd.submission_id
      inner join nrfc.district_code dc
      on dc.district_code = sd.district_code
      where 
          s.submission_status_code = 'N'
          and s.submission_date < NOW() - CAST(:submissionLimit AS INTERVAL)
        """)
  Flux<ClientSubmissionDistrictListDto> retrievePendingSubmissions(String submissionLimit);

}
