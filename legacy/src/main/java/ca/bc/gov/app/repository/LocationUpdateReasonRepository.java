package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.LocationUpdateReasonEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationUpdateReasonRepository extends
    ReactiveCrudRepository<LocationUpdateReasonEntity, Long> {

}
