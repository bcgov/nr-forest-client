package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientBusinessTypeDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientSubmitRequestValidator implements Validator {
  private final ClientBusinessInformationDtoValidator businessInformationDtoValidator;
  private final ClientLocationDtoValidator locationDtoValidator;
  private final ClientSubmitterInformationDtoValidator submitterInformationDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmissionDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientSubmissionDto request = (ClientSubmissionDto) target;

    String businessType = validateBusinessType(request.businessType(), errors);

    validateBusinessInformation(businessType, request.businessInformation(), errors);

    validateLocation(request.location(), errors);

    validateSubmitterInformation(request.submitterInformation(), errors);
  }

  private String validateBusinessType(ClientBusinessTypeDto businessType, Errors errors) {
	String clientTypeField = "businessType.clientType";
	  
    if (businessType == null) {
      errors.rejectValue("businessType", "businessType is missing");
      return StringUtils.EMPTY;
    }
    else if (null == businessType.clientType() || StringUtils.isBlank(businessType.clientType().value())) {
      errors.rejectValue(clientTypeField, "clientType is missing");
      return StringUtils.EMPTY;
    }

    return businessType.clientType().value();
  }

  private void validateBusinessInformation(
      String businessType, ClientBusinessInformationDto businessInformation, Errors errors) {

    if (StringUtils.isBlank(businessType) || "I".equalsIgnoreCase(businessType)) {
      return;
    }

    if (businessInformation == null) {
      errors.rejectValue("businessInformation", "businessInformation is missing");
      return;
    }

    ValidationUtils
        .invokeValidator(businessInformationDtoValidator, businessInformation, errors);
  }

  private void validateLocation(ClientLocationDto location, Errors errors) {
    if (location == null) {
      errors.rejectValue("location", "location is missing");
      return;
    }

    ValidationUtils
        .invokeValidator(locationDtoValidator, location, errors);
  }

  private void validateSubmitterInformation(
      ClientSubmitterInformationDto submitterInformation, Errors errors) {
    if (submitterInformation == null) {
      errors.rejectValue("submitterInformation", "submitterInformation is missing");
      return;
    }

    ValidationUtils
        .invokeValidator(submitterInformationDtoValidator, submitterInformation, errors);
  }
	
}
