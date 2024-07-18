package ca.bc.gov.app.validator.location;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class LocationAssociationValidator implements ForestClientValidator<ClientLocationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientLocationDto target, Integer index) {

    Set<String> locationNames = target
        .addresses()
        .stream()
        .map(ClientAddressDto::locationName)
        .collect(Collectors.toSet());

    log.info("Validating location association for location: {}", locationNames);

    // Keep in mind that it will return the first error found
    // If there's more than one, it will have to run again to point out the next one
    return
        Flux
            .fromIterable(target.contacts())
            .map(contact -> hasValidAssociation(locationNames, contact))
            .filter(ValidationError::isValid)
            .next();
  }

  private boolean isLocationAssociated(Set<String> locationNames,
      Set<String> contactLocationNames) {
    log.info("Location names: {}", locationNames);
    log.info("Contact location names: {}", contactLocationNames);
    log.info("Location names contains all contact location names: {}",
        locationNames.containsAll(contactLocationNames));
    return locationNames.containsAll(contactLocationNames);
  }

  private ValidationError hasValidAssociation(Set<String> locationNames, ClientContactDto contact) {

    String fieldName = "location.contacts[" + contact.index() + "].locationNames";

    if (contact.locationNames() == null || contact.locationNames().isEmpty()) {
      return new ValidationError(fieldName, "Contact has no locations associated to it");
    }

    Set<String> contactLocationNames =
        contact
            .locationNames()
            .stream()
            .map(ClientValueTextDto::text)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());

    if (contactLocationNames.isEmpty()) {
      return new ValidationError(
          fieldName,
          "Contact has no locations associated to it"
      );
    }

    if (isLocationAssociated(locationNames, contactLocationNames)) {
      return new ValidationError("", ""); // no error
    } else {
      return new ValidationError(
          fieldName,
          "Contact has invalid address association"
      );
    }
  }

}
