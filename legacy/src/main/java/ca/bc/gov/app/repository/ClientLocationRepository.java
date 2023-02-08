package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ClientLocationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientLocationRepository
    extends ReactiveCrudRepository<ClientLocationEntity, String> {
}
