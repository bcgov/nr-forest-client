package ca.bc.gov.app.validator.address;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class AddressProvinceValidator implements ForestClientValidator<ClientAddressDto> {

  private final ProvinceCodeRepository provinceCodeRepository;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {
    String fieldName = "location.addresses[" + index + "].province";

    if (target.province() == null || StringUtils.isBlank(target.province().value())) {
      return Mono.just(new ValidationError(fieldName,
          "You must select a " + regionByCountry(target.country()) + "."));
    }

    return
        Mono
            .justOrEmpty(Optional.ofNullable(target.country()))
            .map(ClientValueTextDto::value)
            .flatMap(countryCode ->
                provinceCodeRepository
                    .findByCountryCodeAndProvinceCode(
                        countryCode,
                        target.province().value()
                    )
                    .map(entity -> new ValidationError("", ""))
            )
            .defaultIfEmpty(
                new ValidationError(fieldName,
                    regionByCountry(target.country()) + " doesn't belong to country"))
            .filter(ValidationError::isValid);
  }

  private String regionByCountry(ClientValueTextDto countryCode) {
    if (countryCode == null) {
      return regionByCountry("XXX");
    }
    return regionByCountry(countryCode.value());
  }

  private String regionByCountry(String countryCode) {
    return switch (countryCode) {
      case "CA" -> "province or territory";
      case "US" -> "state";
      default -> "province, territory or state";
    };
  }

}
