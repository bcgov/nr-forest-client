package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
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

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmissionDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientSubmissionDto request = (ClientSubmissionDto) target;

    validateBusinessInformation(request.businessInformation(), errors);

    validateLocation(request.location(), errors);
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
    errors.pushNestedPath(businessInformationField);

    String businessType = businessInformation.businessType();
    if (StringUtils.isAllBlank(businessType)) {
      errors.rejectValue("businessType", "You must choose an option");
      errors.popNestedPath();
      return;
    } else if (!EnumUtils.isValidEnum(BusinessTypeEnum.class, businessType)) {
      errors.rejectValue("businessType", String.format("%s has an invalid value", "Business type"));
      errors.popNestedPath();
      return;
    }

    String clientType = businessInformation.clientType();

    if (StringUtils.isBlank(clientType)) {
      errors.rejectValue("clientType", "Client does not have a type");
      errors.popNestedPath();
      return;
    }

    if (ApplicationConstant.REG_SOLE_PROPRIETORSHIP_CLIENT_TYPE_CODE.equals(clientType)
        || ApplicationConstant.UNREG_SOLE_PROPRIETORSHIP_CLIENT_TYPE_CODE.equals(clientType)
        || ApplicationConstant.INDIVIDUAL_CLIENT_TYPE_CODE.equals(clientType)
    ) {
      validateBirthdate(businessInformation.birthdate(), errors);
    }

    if (!List.of("A", "I", "S", "USP", "RSP","SP","BC","GP").contains(clientType)) {
      errors.rejectValue("businessType",
          String.format("%s %s is not supported at the moment", "Business type",clientType));
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

  private void validateBirthdate(LocalDate birthdate, Errors errors) {
    String dobFieldName = "birthdate";
    if (birthdate == null) {
      errors.rejectValue(dobFieldName, fieldIsMissingErrorMessage("Birthdate"));
    } else {
      LocalDate minAgeDate = LocalDate.now().minusYears(19);
      if (birthdate.isAfter(minAgeDate)) {
        errors.rejectValue(dobFieldName, "Sole proprietorship must be at least 19 years old");
      }
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

}