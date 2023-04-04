package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.validator.common.CommonValidator.fieldIsMissingErrorMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

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

    errors.pushNestedPath("businessInformation");

    validateIncorporationNumber(target, errors);

    ValidationUtils.rejectIfEmpty(errors, "businessName",
        fieldIsMissingErrorMessage("businessName"));

    errors.popNestedPath();
  }

  private void validateIncorporationNumber(Object target, Errors errors) {
    ClientBusinessInformationDto businessInformation = (ClientBusinessInformationDto) target;

    if (StringUtils.isBlank(businessInformation.incorporationNumber())) {
      errors.rejectValue(
          "incorporationNumber", fieldIsMissingErrorMessage("incorporationNumber"));

      return;
    }

    bcRegistryService.getCompanyStanding(businessInformation.incorporationNumber())
        .doOnError(ResponseStatusException.class, e -> errors.rejectValue(
            "incorporationNumber", "IncomporationNumber was not found in BC Registry"))
        .doOnNext(bcRegistryBusinessDto -> {
          if (!bcRegistryBusinessDto.goodStanding()) {
            errors.rejectValue(
                "goodStanding", "Company is not in goodStanding in BC Registry");
          }
        })
        .subscribe();
  }
}
