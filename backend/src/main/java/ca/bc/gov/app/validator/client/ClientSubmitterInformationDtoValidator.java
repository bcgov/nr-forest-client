package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ClientSubmitterInformationDtoValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmitterInformationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils
        .rejectIfEmpty(errors, "submitterInformation", "submitterInformation.empty");

    ClientSubmitterInformationDto submitterInformation = (ClientSubmitterInformationDto) target;

    if(StringUtils.isBlank(submitterInformation.submitterFirstName())) {
      errors.rejectValue("submitterFirstName", "submitterFirstName.empty");
    }

    if(StringUtils.isBlank(submitterInformation.submitterLastName())) {
      errors.rejectValue("submitterLastName", "submitterLastName.empty");
    }

    if(StringUtils.isBlank(submitterInformation.submitterPhoneNumber())) {
      errors.rejectValue("submitterPhoneNumber", "submitterPhoneNumber.empty");
    }

    //Todo: validate email format?
    if(StringUtils.isBlank(submitterInformation.submitterEmail())) {
      errors.rejectValue("submitterEmail", "submitterEmail.empty");
    }
  }
}
