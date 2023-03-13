package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientBusinessTypeDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ClientBusinessTypeDtoValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientBusinessTypeDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmpty(errors, "businessType", "businessType.empty");

    ClientBusinessTypeDto businessType = (ClientBusinessTypeDto) target;

    if(businessType.clientType() == null) {
      errors.rejectValue("clientType", "clientType.empty");
      return;
    }

    if(StringUtils.isBlank(businessType.clientType().value())) {
      errors.rejectValue("clientType.value", "clientType.value.empty");
    }

    //Todo: validate value?
    /*if(StringUtils.isBlank(businessType.clientType().value())) {
      errors.rejectValue("clientType.value", "clientType.value.empty");
    }*/
  }
}
