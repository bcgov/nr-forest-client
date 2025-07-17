package ca.bc.gov.app.repository;

import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.ClientRelationshipCodeEntity;
import reactor.core.publisher.Flux;


@Repository
public interface ClientRelationshipCodeRepository
  extends ReactiveCrudRepository<ClientRelationshipCodeEntity, String> {
  
  @Query("""
      SELECT DISTINCT CRC.CLIENT_RELATIONSHIP_CODE, CRC.DESCRIPTION AS NAME
      FROM THE.CLIENT_RELATIONSHIP_CODE CRC
      INNER JOIN THE.CLIENT_RELATIONSHIP_TYPE_XREF CRTX
          ON CRC.CLIENT_RELATIONSHIP_CODE = CRTX.CLIENT_RELATIONSHIP_CODE
      WHERE 
          (CRC.EXPIRY_DATE IS NULL OR CRC.EXPIRY_DATE > :activeDate)
          AND CRC.EFFECTIVE_DATE <= :activeDate
          AND CRTX.PRIMARY_CLIENT_TYPE_CODE = :clientTypeCode
      """)
  Flux<CodeNameDto> findActiveRelationshipCodesByClientTypeCode(
      String clientTypeCode,
      LocalDate now);

}
