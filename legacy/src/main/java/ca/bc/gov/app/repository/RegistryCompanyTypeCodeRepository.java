package ca.bc.gov.app.repository;

import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.entity.RegistryCompanyTypeCodeEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RegistryCompanyTypeCodeRepository 
  extends ReactiveCrudRepository<RegistryCompanyTypeCodeEntity, String> {
  
  @Query("""
      SELECT REGISTRY_COMPANY_TYPE_CODE, DESCRIPTION AS NAME
      FROM THE.REGISTRY_COMPANY_TYPE_CODE
      WHERE (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :activeDate)
      AND EFFECTIVE_DATE <= :activeDate
      """)
  Flux<CodeNameDto> findActiveRegistryTypeCodes(
      LocalDate activeDate);

  @Query("""
      SELECT RCTC.REGISTRY_COMPANY_TYPE_CODE, RCTC.DESCRIPTION AS NAME
      FROM THE.REGISTRY_COMPANY_TYPE_CODE RCTC
      INNER JOIN THE.CLIENT_TYPE_COMPANY_XREF CTRX
          ON RCTC.REGISTRY_COMPANY_TYPE_CODE = CTRX.REGISTRY_COMPANY_TYPE_CODE
      WHERE 
          (RCTC.EXPIRY_DATE IS NULL OR RCTC.EXPIRY_DATE > :activeDate)
          AND RCTC.EFFECTIVE_DATE <= :activeDate
          AND CTRX.CLIENT_TYPE_CODE = :clientTypeCode
      """)
  Flux<CodeNameDto> findActiveRegistryTypeCodesByClientTypeCode(
      String clientTypeCode,
      LocalDate now);

  @Query("""
      SELECT COUNT(*) AS CNT
      FROM THE.CLIENT_TYPE_COMPANY_XREF CTRX
      INNER JOIN THE.REGISTRY_COMPANY_TYPE_CODE RCTC
          ON CTRX.REGISTRY_COMPANY_TYPE_CODE = RCTC.REGISTRY_COMPANY_TYPE_CODE
      WHERE CTRX.CLIENT_TYPE_CODE = :clientTypeCode
          AND CTRX.REGISTRY_COMPANY_TYPE_CODE = :registryCompanyTypeCode
          AND (RCTC.EXPIRY_DATE IS NULL OR RCTC.EXPIRY_DATE > :activeDate)
          AND RCTC.EFFECTIVE_DATE <= :activeDate
      """)
  Mono<Long> countByClientTypeCodeAndRegistryCompanyTypeCode(
      String clientTypeCode,
      String registryCompanyTypeCode,
      LocalDate activeDate);
  
}
