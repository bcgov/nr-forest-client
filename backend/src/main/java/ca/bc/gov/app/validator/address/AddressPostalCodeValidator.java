package ca.bc.gov.app.validator.address;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class AddressPostalCodeValidator implements ForestClientValidator<ClientAddressDto> {

  private static final Pattern CA_POSTAL_CODE_FORMAT = Pattern.compile("[A-Z]\\d[A-Z]\\d[A-Z]\\d");
  private static final Pattern US_ZIP_CODE_FORMAT = Pattern.compile("^\\d{5}(?:-\\d{4})?$");

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {
    String fieldName = "location.addresses[" + index + "].postalCode";

    // Skip validation if country is not set
    if (target.country() == null || StringUtils.isBlank(target.country().value())) {
      return Mono.empty();
    }

    if (StringUtils.isBlank(target.postalCode())) {
      return
          Mono.just(
              new ValidationError(fieldName,
                  "You must include a " + postalNameByCountry(target.country()) + " "
                      + postalFormatByCountry(target.country()) + ".")
          );
    }

    if (isCA(target.country())) {
      // For CA, postal code should be up to 6 characters
      if (StringUtils.length(target.postalCode()) > 6) {
        return Mono.just(
            new ValidationError(fieldName, "has more than 6 characters")
        );
      }
      // CA postal code format is A9A9A9
      if (!CA_POSTAL_CODE_FORMAT.matcher(target.postalCode()).matches()) {
        return Mono.just(
            new ValidationError(fieldName, "invalid Canadian postal code format")
        );
      }
    }

    // US postal code format is 00000 or 00000-0000
    if (isUS(target.country()) && !US_ZIP_CODE_FORMAT.matcher(target.postalCode()).matches()) {
      return Mono.just(
          new ValidationError(fieldName, "invalid US zip code format")
      );
    }

    // For other countries postal code should be up to 10 characters
    if (StringUtils.length(target.postalCode()) > 10) {
      return Mono.just(
          new ValidationError(fieldName, "has more than 10 characters")
      );
    }

    return Mono.empty();

  }

  private String postalNameByCountry(ClientValueTextDto countryCode) {
    return countryCode.value().equalsIgnoreCase("US") ? "zip code" : "postal code";
  }

  private String postalFormatByCountry(ClientValueTextDto countryCode) {
    return switch (countryCode.value()) {
      case "CA" -> "in the format A9A9A9";
      case "US" -> "in the format 000000 or 00000-0000";
      default -> "up to 10 characters";
    };
  }

  private boolean isCA(ClientValueTextDto countryCode) {
    return "CA".equalsIgnoreCase(countryCode.value());
  }

  private boolean isUS(ClientValueTextDto countryCode) {
    return "US".equalsIgnoreCase(countryCode.value());
  }

}
