package ca.bc.gov.app.repository;

import java.time.LocalDate;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientUpdateReasonCodeEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

@Repository
public interface ClientUpdateReasonCodeRepository 
  extends ReactiveCrudRepository<ClientUpdateReasonCodeEntity, String> {

  @Query("""
      SELECT CLIENT_UPDATE_REASON_CODE, DESCRIPTION AS NAME
      FROM THE.CLIENT_UPDATE_REASON_CODE
      WHERE CLIENT_UPDATE_REASON_CODE IN (
          SELECT CLIENT_UPDATE_REASON_CODE
          FROM THE.CLIENT_ACTION_REASON_XREF
          WHERE UPPER(CLIENT_UPDATE_ACTION_CODE) = UPPER(:actionCode)
            AND UPPER(CLIENT_TYPE_CODE) = UPPER(:clientTypeCode)
      )
      AND (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
      AND EFFECTIVE_DATE <= :activeDate
      AND CLIENT_UPDATE_REASON_CODE != 'UND'
      ORDER BY DESCRIPTION""")
  Flux<CodeNameDto> findActiveByClientTypeAndActionCode(
      String clientTypeCode,
      String actionCode,
      LocalDate activeDate);

}
