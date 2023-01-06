package ca.bc.gov.app.validator.ches;

import ca.bc.gov.app.dto.ches.ChesRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@Slf4j
public class ChesRequestValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ChesRequest.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "emailBody", "ches.request.emailBody.empty");
    ValidationUtils.rejectIfEmpty(errors, "emailTo", "ches.request.emailTo.empty");
    ChesRequest request = (ChesRequest) target;
    if (request.emailTo() != null && request.emailTo().isEmpty()) {
      errors.rejectValue("emailTo", "ches.request.emailTo.empty");
    }
  }
}
