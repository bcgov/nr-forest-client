package ca.bc.gov.app.validator.address;

import static ca.bc.gov.app.util.ClientValidationUtils.US7ASCII_PATTERN;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class AddressComplementaryInformationValidator implements
    ForestClientValidator<ClientAddressDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.STAFF.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {
    String fieldName = "location.addresses[" + index + "].complementaryAddress";

    // Return empty if both fields are blank
    if (StringUtils.isAllBlank(target.complementaryAddressOne(),
        target.complementaryAddressTwo())) {
      return Mono.empty();
    }

    // Validate the first complementary address
    if (StringUtils.isBlank(target.complementaryAddressOne()) && StringUtils.isNotBlank(
        target.complementaryAddressTwo())) {
      return Mono.just(
          new ValidationError(fieldName+"One", "You must enter the first complementary address."));
    }

    // Validate characters and length for both addresses if present
    return validateAddressField(target.complementaryAddressOne(), fieldName+"One")
        .switchIfEmpty(validateAddressField(target.complementaryAddressTwo(), fieldName+"Two"));
  }

  private Mono<ValidationError> validateAddressField(String content, String fieldName) {
    if (StringUtils.isNotBlank(content)) {
      if (!US7ASCII_PATTERN.matcher(content).matches()) {
        return Mono.just(
            new ValidationError(fieldName, String.format("%s has an invalid character.", content)));
      }
      
      if (StringUtils.length(content) < 4) {
        return Mono.just(new ValidationError(fieldName, "The address must be between 4 and 40 characters."));
      }
      
      if (StringUtils.length(content) > 40) {
        return Mono.just(new ValidationError(fieldName, "The address must be between 4 and 40 characters."));
      }
    }
    return Mono.empty();
  }

}
