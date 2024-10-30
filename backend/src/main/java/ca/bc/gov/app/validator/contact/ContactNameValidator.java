package ca.bc.gov.app.validator.contact;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class ContactNameValidator implements ForestClientValidator<ClientContactDto> {

  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s'-]+$");

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientContactDto target, Integer index) {

    String fieldName = "location.contacts[" + index + "].";

    return
        validateField(
            "first name",
            fieldName + "firstName",
            target.firstName()
        )
            .switchIfEmpty(
                validateField(
                    "last name",
                    fieldName + "lastName",
                    target.lastName()
                )
            );
  }

  private Mono<ValidationError> validateField(
      String messageName,
      String fieldName,
      String fieldValue
  ) {

    if (StringUtils.isBlank(fieldValue)) {
      return Mono.just(
          new ValidationError(fieldName,
              String.format("All contacts must have a %s.", messageName)));
    }

    if (!NAME_PATTERN.matcher(fieldValue).matches()) {
      return Mono.just(new ValidationError(fieldName,
          String.format("%s has an invalid character.", fieldValue))
      );
    }

    if (StringUtils.length(fieldValue) > 30) {
      return Mono.just(new ValidationError(fieldName, "This field has a 30 character limit."));
    }

    return Mono.empty();
  }

}
