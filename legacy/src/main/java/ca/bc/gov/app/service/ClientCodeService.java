package ca.bc.gov.app.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.repository.ClientIdTypeCodeRepository;
import ca.bc.gov.app.repository.ClientRelationshipCodeRepository;
import ca.bc.gov.app.repository.ClientStatusCodeRepository;
import ca.bc.gov.app.repository.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.ClientUpdateReasonCodeRepository;
import ca.bc.gov.app.repository.RegistryCompanyTypeCodeRepository;
import io.micrometer.observation.annotation.Observed;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCodeService {

  private final ClientUpdateReasonCodeRepository clientUpdateReasonCodeRepository;
  private final ClientStatusCodeRepository clientStatusCodeRepository;
  private final RegistryCompanyTypeCodeRepository registryCompanyTypeCodeRepository;
  private final ClientTypeCodeRepository clientTypeCodeRepository;
  private final ClientIdTypeCodeRepository clientIdTypeCodeRepository;
  private final ClientRelationshipCodeRepository clientRelationshipCodeRepository;
  
  public Flux<CodeNameDto> findActiveByClientTypeAndActionCode(
      String clientTypeCode,
      String actionCode) {
    
    log.info("Loading active update reason codes for {} {}", clientTypeCode, actionCode);
    
    return clientUpdateReasonCodeRepository
        .findActiveByClientTypeAndActionCode(
            clientTypeCode,
            actionCode, 
            LocalDate.now()
        );
  }

  public Flux<CodeNameDto> findActiveClientStatusCodes() {
    log.info("Loading active client status codes");
    
    return clientStatusCodeRepository
        .findActiveClientStatusCodes(
            LocalDate.now()
        );
  }

  public Flux<CodeNameDto> findActiveRegistryTypeCodes() {
    log.info("Loading active registry type codes");
    
    return registryCompanyTypeCodeRepository
        .findActiveRegistryTypeCodes(
            LocalDate.now()
        );
  }

  public Flux<CodeNameDto> findActiveRegistryTypeCodesByClientTypeCode(String clientTypeCode) {
    log.info("Loading active registry type codes");
    
    return registryCompanyTypeCodeRepository
        .findActiveRegistryTypeCodesByClientTypeCode(
            clientTypeCode,
            LocalDate.now()
        );
  }

  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Loading active client type codes");
    
    return clientTypeCodeRepository
        .findActiveClientTypeCodes(
            LocalDate.now()
        );
  }

  public Flux<CodeNameDto> findActiveClientIdTypeCodes() {
    log.info("Loading active client ID type codes");
    
    return clientIdTypeCodeRepository
        .findActiveClientIdTypeCodes(
            LocalDate.now()
        );
  }
  
  public Flux<CodeNameDto> findActiveRelationshipCodesByClientTypeCode(String clientTypeCode) {
    log.info("Loading active relationship type codes");
    
    return clientRelationshipCodeRepository
        .findActiveRelationshipCodesByClientTypeCode(
            clientTypeCode,
            LocalDate.now()
        );
  }
  
}
