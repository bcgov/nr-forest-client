package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientContactEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ForestClientContactRepository
    extends ReactiveCrudRepository<ForestClientContactEntity,String> {

}
