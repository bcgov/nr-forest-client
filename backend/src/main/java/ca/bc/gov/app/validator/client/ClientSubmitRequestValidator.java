package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientSubmitRequestValidator implements Validator {
  private ClientBusinessTypeDtoValidator businessTypeDtoValidator;
  private ClientBusinessInformationDtoValidator businessInformationDtoValidator;
  private ClientLocationDtoValidator locationDtoValidator;
  private ClientSubmitterInformationDtoValidator submitterInformationDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmissionDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmpty(
        errors, "businessType", "businessType.empty");

    ValidationUtils.rejectIfEmpty(
        errors, "businessInformation", "businessInformation.empty");

    ValidationUtils.rejectIfEmpty(
        errors, "location", "location.empty");

    ValidationUtils.rejectIfEmpty(
        errors, "submitterInformation", "submitterInformation.empty");

    ClientSubmissionDto request = (ClientSubmissionDto) target;

    ValidationUtils
        .invokeValidator(businessTypeDtoValidator, request.businessType(), errors);

    ValidationUtils
        .invokeValidator(businessInformationDtoValidator, request.businessInformation(), errors);

    ValidationUtils
        .invokeValidator(locationDtoValidator, request.location(), errors);

    ValidationUtils
        .invokeValidator(submitterInformationDtoValidator, request.submitterInformation(), errors);
  }
}
