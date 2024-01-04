package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntityKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForestClientLocationRepository
    extends ReactiveCrudRepository<ForestClientLocationEntity, ForestClientLocationEntityKey> {

}
