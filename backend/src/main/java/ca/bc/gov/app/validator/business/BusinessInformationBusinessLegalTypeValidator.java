package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class BusinessInformationBusinessLegalTypeValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {
    String fieldName = "businessInformation.legalType";

    return
        Mono
            .justOrEmpty(
                Optional
                    .ofNullable(target.legalType())
                    .map(LegalTypeEnum::fromValue)
                    .filter(Objects::nonNull) // Not true, if no value is found, it will return null
                    .map(value -> new ValidationError("", ""))
                    .orElse(new ValidationError(
                            fieldName,
                            String.format("Legal type has an invalid value [%s]", target.legalType())
                        )
                    )
            )
            .filter(ValidationError::isValid);
  }

}
