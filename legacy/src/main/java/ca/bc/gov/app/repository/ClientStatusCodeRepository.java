package ca.bc.gov.app.repository;

import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientStatusCodeEntity;
import reactor.core.publisher.Flux;

@Repository
public interface ClientStatusCodeRepository
  extends ReactiveCrudRepository<ClientStatusCodeEntity, String> {

  @Query("""
      SELECT CLIENT_STATUS_CODE, DESCRIPTION AS NAME
      FROM THE.CLIENT_STATUS_CODE
      WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
      AND EFFECTIVE_DATE <= :activeDate
      """)
  Flux<CodeNameDto> findActiveClientStatusCodes(LocalDate activeDate);
  
}
