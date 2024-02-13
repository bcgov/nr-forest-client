package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.util.ClientValidationUtils.getClientType;
import static ca.bc.gov.app.util.ClientValidationUtils.isValidEnum;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class RegisteredBusinessInformationValidator implements Validator {

  private final BcRegistryService bcRegistryService;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientBusinessInformationDto.class.equals(clazz);
  }

  @SneakyThrows
  @Override
  public void validate(Object target, Errors errors) {

    errors.pushNestedPath("businessInformation");

    String businessNameField = "businessName";
    ValidationUtils.rejectIfEmpty(errors, businessNameField,
        "You must select your B.C. registered business name from the list.");

    ClientBusinessInformationDto businessInformation = (ClientBusinessInformationDto) target;
    
    LegalTypeEnum legalType = isValidLegalType(businessInformation.legalType(), errors);

    validateClientType(businessInformation.clientType(), legalType, errors);

    validateRegistrationNumberAndLegalType(
        businessInformation.registrationNumber(),
        legalType,
        errors);

    errors.popNestedPath();
  }

  private LegalTypeEnum isValidLegalType(String legalType, Errors errors) {
    if (!isValidEnum(legalType, "legalType", LegalTypeEnum.class, errors)) {
      return null;
    }
    return LegalTypeEnum.valueOf(legalType);
  }

  private void validateClientType(
      String clientType, LegalTypeEnum legalType, Errors errors) {
    String clientTypeField = "clientType";

    if (!isValidEnum(clientType, clientTypeField, ClientTypeEnum.class, errors)) {
      return;
    }

    if (legalType == null) {
      return;
    }

    ClientTypeEnum expectedClientType = getClientType(legalType);
    if (!ClientTypeEnum.valueOf(clientType).equals(expectedClientType)) {
      errors.rejectValue(
          clientTypeField,
          clientTypeField +
              "value for clientType does not match the expected value for legalType");
    }
  }

  private void validateRegistrationNumberAndLegalType(
      String registrationNumber, LegalTypeEnum legalType, Errors errors) {

    String registrationNumberField = "registrationNumber";

    if (StringUtils.isBlank(registrationNumber)) {
      errors.rejectValue(
          registrationNumberField,
          fieldIsMissingErrorMessage(registrationNumberField));
      return;
    }

    if (legalType == null) {
      return;
    }

    if (LegalTypeEnum.SP.equals(legalType) || LegalTypeEnum.GP.equals(legalType)) {
      bcRegistryService.requestDocumentData(registrationNumber)
          .doOnError(ResponseStatusException.class, e -> errors.rejectValue(
              "businessName", "Incorporation Number was not found in BC Registry"))
          .doOnNext(bcRegistryBusinessDto -> {
            if (Boolean.FALSE.equals(bcRegistryBusinessDto.business().goodStanding())) {
              errors.rejectValue(registrationNumberField,
                  "Company is not in goodStanding in BC Registry");
            }
          })
          .subscribe();
    }
  }
}
