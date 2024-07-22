package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
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
public class BusinessInformationBusinessTypeValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    if (StringUtils.isAllBlank(target.businessType())) {
      return Mono.just(
          new ValidationError("businessInformation.businessType", "You must choose an option"));
    }

    if (BusinessTypeEnum.fromValue(target.businessType()) == null) {
      return Mono.just(
          new ValidationError("businessInformation.businessType",
              "Business type has an invalid value"
          )
      );
    }
    return Mono.empty();
  }


}
