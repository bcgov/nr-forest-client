package ca.bc.gov.app.validator.contact;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.util.ClientValidationUtils;
import ca.bc.gov.app.validator.ForestClientValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import io.micrometer.observation.annotation.Observed;

@Component
@Observed
@Slf4j
public class ContactPhoneNumberExternalValidator implements ForestClientValidator<ClientContactDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.EXTERNAL.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientContactDto target, Integer index) {

    String fieldName = "location.contacts[" + index + "].";

    if (StringUtils.isBlank(target.phoneNumber())) {
      return Mono.just(new ValidationError(fieldName + "phoneNumber",
          "The phone number must be a 10-digit number"));
    }

    return
        ClientValidationUtils
            .validatePhoneNumber(
                fieldName + "phoneNumber",
                target.phoneNumber()
            );
  }

}
