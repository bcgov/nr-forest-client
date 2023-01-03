package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientCodeTypeDTO;
import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FsaClientService {
  private final ClientTypeCodeRepository clientTypeCodeRepository;

  public Mono<List<ClientCodeTypeDTO>> findActiveClientTypeCodes() {

    return
        clientTypeCodeRepository
            .findActiveAt(LocalDate.now())
            .sort(Comparator.comparing(ClientTypeCodeEntity::getDescription))
            .map(entity -> new ClientCodeTypeDTO(
                entity.getCode(),
                entity.getDescription(),
                entity.getEffectiveAt(),
                entity.getExpiredAt(),
                null)
            )
            .collectList();
  }

}
