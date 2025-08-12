package ca.bc.gov.app.validator.contact;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.util.ClientValidationUtils;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class ContactEmailExternalValidator implements ForestClientValidator<ClientContactDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.EXTERNAL.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientContactDto target, Integer index) {

    String fieldName = "location.contacts[" + index + "].email";

    return ClientValidationUtils.validateEmail(target.email(), fieldName);
  }

}
