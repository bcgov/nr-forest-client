package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.util.ClientValidationUtils.isValidEnum;

import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientSubmitRequestValidator implements Validator {
  private final RegisteredBusinessInformationValidator registeredBusinessInformationValidator;
  private final UnregisteredBusinessInformationValidator unregisteredBusinessInformationValidator;
  private final ClientLocationDtoValidator locationDtoValidator;
  private final ClientSubmitterInformationDtoValidator submitterInformationDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmissionDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientSubmissionDto request = (ClientSubmissionDto) target;



    validateBusinessInformation(request.businessInformation(), errors);

    validateLocation(request.location(), errors);

    validateSubmitterInformation(request.submitterInformation(), errors);
  }

  private void validateBusinessInformation(
      ClientBusinessInformationDto businessInformation, Errors errors) {

    String businessInformationField = "businessInformation";
    if (businessInformation == null) {
      errors.rejectValue(
          businessInformationField,
          fieldIsMissingErrorMessage(businessInformationField));
      return;
    }

    String businessType = businessInformation.businessType();

    errors.pushNestedPath(businessInformationField);
    if (!isValidEnum(businessType, "businessType", BusinessTypeEnum.class, errors)) {
      errors.popNestedPath();
      return;
    }
    errors.popNestedPath();

    if (BusinessTypeEnum.U.toString().equals(businessType)) {
      ValidationUtils
          .invokeValidator(unregisteredBusinessInformationValidator, businessInformation, errors);
    } else {
      //Only option left is Business Type == R (Registered)
      ValidationUtils
          .invokeValidator(registeredBusinessInformationValidator, businessInformation, errors);
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