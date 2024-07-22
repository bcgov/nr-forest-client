package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.repository.client.DistrictCodeRepository;
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
public class BusinessInformationDistrictValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  private final DistrictCodeRepository districtCodeRepository;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.EXTERNAL.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    if (StringUtils.isBlank(target.district())) {
      return Mono.just(
          new ValidationError("businessInformation.district", "Client does not have a district")
      );
    }
    return
        districtCodeRepository
            .findByCode(target.district())
            .map(entity -> new ValidationError("", ""))
            .defaultIfEmpty(
                new ValidationError("businessInformation.district", "district is invalid"))
            .filter(ValidationError::isValid);

  }

}
