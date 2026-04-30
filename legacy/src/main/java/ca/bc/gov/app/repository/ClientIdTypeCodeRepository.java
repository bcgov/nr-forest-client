package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientIdTypeCodeEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for accessing and managing client ID type codes.
 * <p>
 * Provides CRUD operations for {@link ClientIdTypeCodeEntity} and includes a custom query to
 * retrieve all active client ID type codes as of a specified date, ordered alphabetically by name.
 * Uses {@link ReactiveCrudRepository} for non-blocking, reactive database interactions.
 * </p>
 */
@Repository
public interface ClientIdTypeCodeRepository 
  extends ReactiveCrudRepository<ClientIdTypeCodeEntity, String> {
  
  /**
   * Retrieves all active client ID type codes as of the given date, ordered by name.
   * <p>
   * Uses a custom SQL query to select codes where the effective date is on or before, and the expiry
   * date is after, the provided date. Results are ordered alphabetically by description (name).
   * </p>
   *
   * @param activeDate the date to check for active codes
   * @return a Flux of {@link CodeNameDto} representing active client ID type codes, ordered by name
   */
  @Query("""
      SELECT CLIENT_ID_TYPE_CODE, DESCRIPTION AS NAME
      FROM THE.CLIENT_ID_TYPE_CODE
      WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
      AND EFFECTIVE_DATE <= :activeDate
      ORDER BY DESCRIPTION
      """)
  Flux<CodeNameDto> findActiveClientIdTypeCodes(LocalDate activeDate);

}
