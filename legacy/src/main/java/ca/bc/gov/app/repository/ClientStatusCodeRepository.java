package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientStatusCodeEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for managing {@link ClientStatusCodeEntity} entities.
 * <p>
 * Provides CRUD operations and a custom query to retrieve active client status codes using
 * non-blocking, reactive database access.
 * </p>
 */
@Repository
public interface ClientStatusCodeRepository
  extends ReactiveCrudRepository<ClientStatusCodeEntity, String> {

  /**
   * Finds all active client status codes for the specified date.
   * <p>
   * A status code is active if its expiry date is null or after {@code activeDate}, and its
   * effective date is on or before {@code activeDate}.
   * </p>
   *
   * @param activeDate the date to check for active status codes
   * @return a {@link Flux} of {@link CodeNameDto} representing active client status codes
   */
  @Query("""
    SELECT CLIENT_STATUS_CODE, DESCRIPTION AS NAME
    FROM THE.CLIENT_STATUS_CODE
    WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
    AND EFFECTIVE_DATE <= :activeDate
    ORDER BY DESCRIPTION
    """)
  Flux<CodeNameDto> findActiveClientStatusCodes(LocalDate activeDate);

}
