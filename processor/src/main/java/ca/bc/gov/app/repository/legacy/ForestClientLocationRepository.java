package ca.bc.gov.app.repository.legacy;

import ca.bc.gov.app.entity.legacy.ForestClientLocationEntity;
import ca.bc.gov.app.entity.legacy.ForestClientLocationEntityKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForestClientLocationRepository
    extends ReactiveCrudRepository<ForestClientLocationEntity, ForestClientLocationEntityKey> {

}
