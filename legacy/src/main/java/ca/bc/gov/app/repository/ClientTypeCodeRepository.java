package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientTypeCodeEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repository interface for managing client type codes.
 * <p>
 * This repository provides reactive CRUD operations for {@link ClientTypeCodeEntity} 
 * and includes a custom query method to retrieve active client type codes.
 * </p>
 * <p>
 * The repository uses {@link ReactiveCrudRepository}, making it suitable for 
 * non-blocking, reactive database interactions.
 * </p>
 */
@Repository
public interface ClientTypeCodeRepository
  extends ReactiveCrudRepository<ClientTypeCodeEntity, String> {
    
    @Query("""
        SELECT CLIENT_TYPE_CODE, DESCRIPTION AS NAME
        FROM THE.CLIENT_TYPE_CODE
        WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
        AND EFFECTIVE_DATE <= :activeDate
        """)
    Flux<CodeNameDto> findActiveClientTypeCodes(LocalDate activeDate);
    
}
