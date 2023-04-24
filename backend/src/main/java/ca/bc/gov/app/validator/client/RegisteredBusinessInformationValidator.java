package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.util.ClientValidationUtils.getClientType;

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

  private static String BUSINESS_NAME_FIELD = "businessName";

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientBusinessInformationDto.class.equals(clazz);
  }

  @SneakyThrows
  @Override
  public void validate(Object target, Errors errors) {
	
    errors.pushNestedPath("businessInformation");

    ClientBusinessInformationDto businessInformation = (ClientBusinessInformationDto) target;

    validateClientType(businessInformation.clientType(), businessInformation.legalType(), errors);

    ValidationUtils.rejectIfEmpty(errors, BUSINESS_NAME_FIELD,
            fieldIsMissingErrorMessage(BUSINESS_NAME_FIELD));
    
    validateIncorporationNumberAndLegalType(
        businessInformation.incorporationNumber(),
        businessInformation.legalType(),
        errors);

    errors.popNestedPath();
  }

  private void validateClientType(
      ClientTypeEnum clientType, LegalTypeEnum legalType, Errors errors) {
    String clientTypeField = "clientType";

    if (clientType == null) {
      errors.rejectValue(clientTypeField, fieldIsMissingErrorMessage(clientTypeField));
      return;
    }

    if(ClientTypeEnum.I.equals(clientType)) {
      errors.rejectValue(clientTypeField, clientTypeField + "invalid value");
    }

    ClientTypeEnum expectedClientType = getClientType(legalType);
    if(!clientType.equals(expectedClientType)) {
      errors.rejectValue(
          clientTypeField,
          clientTypeField +
              "value for clientType does not match the expected value for legalType");
    }
  }

  private void validateIncorporationNumberAndLegalType(
      String incorporationNumber, LegalTypeEnum legalType, Errors errors) {

	String incorporationNumberField = "incorporationNumber";

    if (StringUtils.isBlank(incorporationNumber)) {
      errors.rejectValue(
          incorporationNumberField,
          fieldIsMissingErrorMessage(incorporationNumberField));
      return;
    }

    if(legalType == null) {
      String legalTypeField = "legalType";
      errors.rejectValue(
          legalTypeField,
          fieldIsMissingErrorMessage(legalTypeField));
    }

    if(LegalTypeEnum.SP.equals(legalType) || LegalTypeEnum.GP.equals(legalType)) {
      bcRegistryService.requestDocumentData(incorporationNumber)
          .doOnError(ResponseStatusException.class, e -> errors.rejectValue(
              "businessName", "Incorporation Number was not found in BC Registry"))
          .doOnNext(bcRegistryBusinessDto -> {
            if (Boolean.FALSE.equals(bcRegistryBusinessDto.business().goodStanding())) {
              errors.rejectValue(incorporationNumberField,
                  "Company is not in goodStanding in BC Registry");
            }
          })
          .subscribe();
    }
  }
}
