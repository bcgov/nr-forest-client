package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDoingBusinessAsRepository extends
    ReactiveCrudRepository<ClientDoingBusinessAsEntity, String> {
}
