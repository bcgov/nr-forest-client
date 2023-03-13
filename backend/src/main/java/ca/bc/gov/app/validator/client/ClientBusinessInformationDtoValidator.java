package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ClientBusinessInformationDtoValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientBusinessInformationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientBusinessInformationDto businessInformation = (ClientBusinessInformationDto) target;

    if(StringUtils.isBlank(businessInformation.firstName())) {
      errors.rejectValue("firstName", "firstName.empty");
    }

    if(StringUtils.isBlank(businessInformation.lastName())) {
      errors.rejectValue("lastName", "lastName.empty");
    }

    //Todo: validate birthdate format?
    if(StringUtils.isBlank(businessInformation.birthdate())) {
      errors.rejectValue("birthdate", "birthdate.empty");
    }

    if(StringUtils.isBlank(businessInformation.incorporationNumber())) {
      errors.rejectValue("incorporationNumber", "incorporationNumber.empty");
    }

    if(StringUtils.isBlank(businessInformation.doingBusinessAsName())) {
      errors.rejectValue("doingBusinessAsName", "doingBusinessAsName.empty");
    }

    if(StringUtils.isBlank(businessInformation.businessName())) {
      errors.rejectValue("businessName", "businessName.empty");
    }
  }
}
