package ca.bc.gov.app.validator.address;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
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
public class AddressEmailValidator implements ForestClientValidator<ClientAddressDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.STAFF.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientAddressDto target, Integer index) {

    String fieldName = "location.addresses[" + index + "].emailAddress";

    return
        Mono
            .justOrEmpty(Optional.ofNullable(target.emailAddress()))
            .filter(StringUtils::isNotBlank)
            .flatMap(email -> ClientValidationUtils.validateEmail(email,fieldName));
  }

}
