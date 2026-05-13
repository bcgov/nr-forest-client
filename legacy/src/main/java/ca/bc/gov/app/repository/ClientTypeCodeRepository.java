package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientTypeCodeEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for managing {@link ClientTypeCodeEntity} entities.
 * <p>
 * Provides non-blocking CRUD operations and custom queries for client type codes.
 * Utilizes Spring Data R2DBC for reactive database access.
 * </p>
 */
@Repository
public interface ClientTypeCodeRepository
  extends ReactiveCrudRepository<ClientTypeCodeEntity, String> {
    
    /**
     * Retrieves all active client type codes as of the specified date.
     * <p>
     * A client type code is considered active if its expiry date is null or after the given date,
     * and its effective date is on or before the given date.
     * </p>
     *
     * @param activeDate the date to check for active client type codes
     * @return a {@link Flux} emitting {@link CodeNameDto} objects representing active client type codes
     */
    @Query("""
        SELECT CLIENT_TYPE_CODE, DESCRIPTION AS NAME
        FROM THE.CLIENT_TYPE_CODE
        WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
        AND EFFECTIVE_DATE <= :activeDate
        ORDER BY DESCRIPTION
        """)
    Flux<CodeNameDto> findActiveClientTypeCodes(LocalDate activeDate);
    
}
