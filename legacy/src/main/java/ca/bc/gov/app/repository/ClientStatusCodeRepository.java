package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientStatusCodeEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repository interface for managing client status codes.
 * <p>
 * This repository provides reactive CRUD operations for {@link ClientStatusCodeEntity} 
 * and includes a custom query method to retrieve active client status codes.
 * </p>
 * <p>
 * The repository uses {@link ReactiveCrudRepository}, making it suitable for 
 * non-blocking, reactive database interactions.
 * </p>
 */
@Repository
public interface ClientStatusCodeRepository
    extends ReactiveCrudRepository<ClientStatusCodeEntity, String> {

  /**
   * Retrieves active client status codes based on the given active date.
   * <p>
   * A client status code is considered active if:
   * <ul>
   *   <li>Its expiry date is either null or greater than the provided {@code activeDate}.</li>
   *   <li>Its effective date is less than or equal to the provided {@code activeDate}.</li>
   * </ul>
   * </p>
   *
   * @param activeDate The date used to filter active client status codes.
   * @return A {@link Flux} containing {@link CodeNameDto} objects representing 
   *         active client status codes.
   */
  @Query("""
      SELECT CLIENT_STATUS_CODE, DESCRIPTION AS NAME
      FROM THE.CLIENT_STATUS_CODE
      WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
      AND EFFECTIVE_DATE <= :activeDate
      """)
  Flux<CodeNameDto> findActiveClientStatusCodes(LocalDate activeDate);
  
}
