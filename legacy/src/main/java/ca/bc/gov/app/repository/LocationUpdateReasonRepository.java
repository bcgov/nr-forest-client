package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.LocationUpdateReasonEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface LocationUpdateReasonRepository extends
    ReactiveCrudRepository<LocationUpdateReasonEntity, Long> {

  @Query(ForestClientQueries.LOCATION_TO_REACTIVATE)
  Flux<CodeNameDto> findAllLocationUpdatedWithClient(String clientNumber, String clientStatus);
}
