package ca.bc.gov.app.m.postgres.client.service;

import ca.bc.gov.app.core.dto.CodeDescrDTO;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import ca.bc.gov.app.m.postgres.client.repository.ClientTypeCodeRepository;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
  private final ClientTypeCodeRepository clientTypeCodeRepository;

  public List<CodeDescrDTO> findActiveClientTypeCodes() {
    return
        clientTypeCodeRepository
            .findActiveAt(
                Calendar
                    .getInstance()
                    .getTime()
            )
            .stream()
            .sorted(Comparator.comparing(ClientTypeCodeEntity::getDescription))
            .map(entity -> new CodeDescrDTO(
                entity.getCode(),
                entity.getDescription(),
                entity.getEffectiveDate(),
                entity.getExpiryDate(),
                null)
            )
            .collect(Collectors.toList());
  }

}
