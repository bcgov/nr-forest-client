package ca.bc.gov.app.validator.contact;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
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
public class ContactTypeValidator implements ForestClientValidator<ClientContactDto> {

  private final ContactTypeCodeRepository typeCodeRepository;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientContactDto target, Integer index) {
    String fieldName = "location.contacts[" + index + "].contactType";

    if (target.contactType() == null || StringUtils.isBlank(target.contactType().value())) {
      return Mono.just(new ValidationError(fieldName, "All contacts must select a role."));
    }

    return typeCodeRepository
        .findById(target.contactType().value())
        .map(entity -> new ValidationError("", ""))
        .defaultIfEmpty(
            new ValidationError(fieldName,
                "Contact Type " + target.contactType().text() + " is invalid")
        )
        .filter(ValidationError::isValid);
  }

}
