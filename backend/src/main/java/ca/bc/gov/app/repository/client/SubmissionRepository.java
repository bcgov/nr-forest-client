package ca.bc.gov.app.repository.client;

import ca.bc.gov.app.entity.client.SubmissionEntity;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<SubmissionEntity, Integer> {
  
  @Query(""" 
         select count(*) from nrfc.submission
         where submission_date >= :startTime
         and submission_date <= :endTime
         and upper(create_user) = upper(:createdBy)
         """)
  Mono<Long> countSubmissionsByTimeRangeAndCreatedBy(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     @Param("createdBy") String createdBy);
  
}
