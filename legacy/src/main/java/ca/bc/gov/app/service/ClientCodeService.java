package ca.bc.gov.app.service;

import java.time.LocalDate;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.repository.ClientUpdateReasonCodeRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCodeService {

  private final ClientUpdateReasonCodeRepository clientUpdateReasonCodeRepository;
  
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
}
