package ca.bc.gov.app.validator.contact;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.util.ClientValidationUtils;
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
public class ContactPhoneNumberValidator implements ForestClientValidator<ClientContactDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientContactDto target, Integer index) {
    String fieldName = "location.contacts[" + index + "].";

    return validatePhoneNumber(
            fieldName + "phoneNumber", 
            target.phoneNumber()
        )
        .switchIfEmpty(validatePhoneNumber(
            fieldName + "secondaryPhoneNumber", 
            target.secondaryPhoneNumber())
        )
        .switchIfEmpty(validatePhoneNumber(
            fieldName + "faxNumber", 
            target.faxNumber())
        );
  }

  private Mono<ValidationError> validatePhoneNumber(String fieldName, String phoneNumber) {
    return
        Mono
            .justOrEmpty(Optional.ofNullable(phoneNumber))
            .filter(StringUtils::isNotBlank)
            .flatMap(value -> ClientValidationUtils.validatePhoneNumber(fieldName, value));
  }

}
