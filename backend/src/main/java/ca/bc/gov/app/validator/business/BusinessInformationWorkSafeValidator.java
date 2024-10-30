package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
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
public class BusinessInformationWorkSafeValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.STAFF.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    String fieldName = "businessInformation.workSafeBcNumber";

    return
        Mono
            .justOrEmpty(Optional.ofNullable(target.workSafeBcNumber()))
            .filter(StringUtils::isNotBlank)
            .flatMap(value ->
                isOnlyNumber(value, fieldName)
                    .switchIfEmpty(isMaxSize(value, fieldName, 6))
            );
  }

  private Mono<ValidationError> isOnlyNumber(String value, String fieldName) {
    if (value.matches("[0-9]+")) {
      return Mono.empty();
    } else {
      return Mono.just(new ValidationError(fieldName, "WorkSafeBC number should contain only numbers"));
    }
  }

  private Mono<ValidationError> isMaxSize(String value, String fieldName, int maxSize) {
    if (value.length() <= maxSize) {
      return Mono.empty();
    } else {
      return Mono.just(new ValidationError(fieldName, "WorkSafeBC number should be less than " + maxSize + " characters"));
    }
  }

}
