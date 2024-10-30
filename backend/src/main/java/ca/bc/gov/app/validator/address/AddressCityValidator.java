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
public class AddressCityValidator implements ForestClientValidator<ClientAddressDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {
    String fieldName = "location.addresses[" + index + "].city";

    if (StringUtils.isBlank(target.city())) {
      return Mono.just(
          new ValidationError(fieldName, "You must enter the name of a city or town.")
      );
    }

    if (!US7ASCII_PATTERN.matcher(target.city()).matches()) {
      return Mono.just(new ValidationError(fieldName,
          String.format("%s has an invalid character.", target.city())));
    }

    if (StringUtils.length(target.city()) > 30) {
      return Mono.just(new ValidationError(fieldName, "This field has a 30 character limit."));
    }
    return Mono.empty();
  }
}
