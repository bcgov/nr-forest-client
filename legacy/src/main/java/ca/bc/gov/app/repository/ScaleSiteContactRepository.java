package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ScaleSiteContactEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository for the {@link ScaleSiteContactEntity}.
 *
 * <p>Provides methods to query the SCALE_SITE_CONTACT table, which is used by other
 * systems (EMS, GAS2, LEXIS, SCS) to reference client contacts.</p>
 */
@Repository
public interface ScaleSiteContactRepository
    extends ReactiveCrudRepository<ScaleSiteContactEntity, Long> {

  /**
   * Checks whether a record exists in SCALE_SITE_CONTACT for the given client contact id.
   *
   * @param clientContactId the client contact id to check
   * @return a {@link Mono} emitting {@code true} if a matching record exists, {@code false}
   *     otherwise
   */
  Mono<Boolean> existsByClientContactId(Long clientContactId);

}
