package ca.bc.gov.app.repository.legacy;

import ca.bc.gov.app.entity.legacy.ForestClientContactEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ForestClientContactRepository
    extends ReactiveCrudRepository<ForestClientContactEntity,String> {

}
