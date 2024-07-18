package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class BusinessInformationClientTypeValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  private final ClientTypeCodeRepository clientTypeCodeRepository;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    String clientTypeCode = target.clientType();

    if (StringUtils.isBlank(clientTypeCode)) {
      return Mono.just(
          new ValidationError("businessInformation.clientType", "Client does not have a type")
      );
    }

    return
        clientTypeCodeRepository
            .findByCode(clientTypeCode)
            .map(entity -> new ValidationError("", ""))
            .defaultIfEmpty(
                new ValidationError("businessInformation.clientType", "Client type is invalid"))
            .filter(ValidationError::isValid);
  }

}
