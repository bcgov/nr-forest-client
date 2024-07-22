package ca.bc.gov.app.validator.address;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
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
public class AddressCountryValidator implements ForestClientValidator<ClientAddressDto> {

  private final CountryCodeRepository countryCodeRepository;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {
    String fieldName = "location.addresses[" + index + "].country";

    if (target.country() == null || StringUtils.isBlank(target.country().value())) {
      return Mono.just(new ValidationError(fieldName, "You must select a country from the list."));
    }

    return countryCodeRepository
        .findByCountryCode(target.country().value())
        .map(entity -> new ValidationError("", ""))
        .defaultIfEmpty(new ValidationError(fieldName, "Country is invalid"))
        .filter(ValidationError::isValid);
  }
}
