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
public class AddressAddressValidator implements ForestClientValidator<ClientAddressDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {
    String fieldName = "location.addresses[" + index + "].streetAddress";
    String fieldValue = target.streetAddress();

    if (StringUtils.isBlank(fieldValue)) {
      return Mono.just(
          new ValidationError(fieldName, "You must enter a street address or PO box number."));
    }
    
    if (!US7ASCII_PATTERN.matcher(fieldValue).matches()) {
      return Mono.just(new ValidationError(fieldName,
          String.format("%s has an invalid character.", fieldValue)));
    }
    
    if (StringUtils.length(fieldValue) < 4) {
      return Mono.just(new ValidationError(fieldName, "The address must be between 4 and 40 characters."));
    }
    
    if (StringUtils.length(fieldValue) > 40) {
      return Mono.just(new ValidationError(fieldName, "The address must be between 4 and 40 characters."));
    }
    
    return Mono.empty();
  }
}
