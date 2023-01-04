package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientCodeTypeDto;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ClientService {
  private final ClientTypeCodeRepository clientTypeCodeRepository;

  public Mono<List<ClientCodeTypeDto>> findActiveClientTypeCodes(LocalDate targetDate) {

    return
        clientTypeCodeRepository
            .findActiveAt(targetDate)
            .map(entity -> new ClientCodeTypeDto(
                entity.getCode(),
                entity.getDescription(),
                entity.getEffectiveAt(),
                entity.getExpiredAt(),
                null)
            )
            .collectList();
  }

}
