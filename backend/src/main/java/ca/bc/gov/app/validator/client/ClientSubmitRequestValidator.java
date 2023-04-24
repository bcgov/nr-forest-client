package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;

import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
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
  private final RegisteredBusinessInformationValidator registeredBusinessInformationValidator;
  private final ClientLocationDtoValidator locationDtoValidator;
  private final ClientSubmitterInformationDtoValidator submitterInformationDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmissionDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientSubmissionDto request = (ClientSubmissionDto) target;

    String userId = "userId";

    ValidationUtils
        .rejectIfEmptyOrWhitespace(
            errors,
            userId,
            fieldIsMissingErrorMessage(userId));

    validateBusinessInformation(request.businessInformation(), errors);

    validateLocation(request.location(), errors);

    validateSubmitterInformation(request.submitterInformation(), errors);
  }

  private void validateBusinessInformation(
      ClientBusinessInformationDto businessInformation, Errors errors) {

    if (businessInformation == null) {
      String businessInformationField = "businessInformation";
      errors.rejectValue(
          businessInformationField,
          fieldIsMissingErrorMessage(businessInformationField));
      return;
    }

    if(BusinessTypeEnum.R.equals(businessInformation.businessType())) {
      ValidationUtils
          .invokeValidator(registeredBusinessInformationValidator, businessInformation, errors);
      return;
    }

    //Only option left is Business Type == U (Unregistered)
    if (StringUtils.isBlank(businessInformation.businessName())) {
      errors.pushNestedPath("businessInformation");
      String businessNameField = "businessName";
      errors.rejectValue(businessNameField, fieldIsMissingErrorMessage(businessNameField));
      errors.popNestedPath();
    }
  }

  private void validateLocation(ClientLocationDto location, Errors errors) {

    String locationField = "location";

    if (location == null) {
      errors.rejectValue(locationField, fieldIsMissingErrorMessage(locationField));
      return;
    }

    ValidationUtils
        .invokeValidator(locationDtoValidator, location, errors);
  }

  private void validateSubmitterInformation(
      ClientSubmitterInformationDto submitterInformation, Errors errors) {

    String submitterInformationField = "submitterInformation";

    if (submitterInformation == null) {
      errors.rejectValue(
          submitterInformationField,
          fieldIsMissingErrorMessage(submitterInformationField));
      return;
    }

    ValidationUtils
        .invokeValidator(submitterInformationDtoValidator, submitterInformation, errors);
  }
}