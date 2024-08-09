package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.util.ClientValidationUtils;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class BusinessInformationAcronymValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.STAFF.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    String fieldName = "businessInformation.clientAcronym";

    return
        Mono
            .justOrEmpty(Optional.ofNullable(target.clientAcronym()))
            .filter(StringUtils::isNotBlank)
            .flatMap(value -> ClientValidationUtils.validateBySize(
                    value,
                    fieldName,
                    8,
                    3,
                    "acronym"
                )
            );
  }

}
