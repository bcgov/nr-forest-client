package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.validator.common.CommonValidator.fieldIsMissingErrorMessage;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
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
public class ClientBusinessInformationDtoValidator implements Validator {

  private final BcRegistryService bcRegistryService;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientBusinessInformationDto.class.equals(clazz);
  }

  @SneakyThrows
  @Override
  public void validate(Object target, Errors errors) {
	String businessNameField = "businessName";
	
    errors.pushNestedPath("businessInformation");

    ValidationUtils.rejectIfEmpty(errors, businessNameField,
            fieldIsMissingErrorMessage(businessNameField));
    
    validateIncorporationNumber(target, errors);

    errors.popNestedPath();
  }

  private void validateIncorporationNumber(Object target, Errors errors) {
	String businessNameField = "businessName";
    ClientBusinessInformationDto businessInformation = (ClientBusinessInformationDto) target;

    if (StringUtils.isBlank(businessInformation.incorporationNumber())) {
      errors.rejectValue(businessNameField, businessNameField + " is invalid");

      return;
    }

    bcRegistryService.requestDocumentData(businessInformation.incorporationNumber())
        .doOnError(ResponseStatusException.class, e -> errors.rejectValue(
            "businessName", "Incorporation Number was not found in BC Registry"))
        .doOnNext(bcRegistryBusinessDto -> {
          if (Boolean.FALSE.equals(bcRegistryBusinessDto.business().goodStanding())) {
            errors.rejectValue(businessNameField, 
                               "Company is not in goodStanding in BC Registry");
          }
        })
        .subscribe();
  }
}
