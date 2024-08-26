package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class BusinessInformationBusinessRegisteredNumberValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  private final BcRegistryService bcRegistryService;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {
    String fieldName = "businessInformation.registrationNumber";

    if (BusinessTypeEnum.R.equals(BusinessTypeEnum.fromValue(target.businessType()))) {

      if (StringUtils.isBlank(target.registrationNumber())) {
        return Mono.just(
            new ValidationError(
                fieldName,
                fieldIsMissingErrorMessage("registrationNumber")
            )
        );
      }

      if (StringUtils.length(target.registrationNumber()) > 13) {
        return Mono.just(
            new ValidationError(
                fieldName,
                "has more than 13 characters"
            )
        );
      }

      LegalTypeEnum legalType = LegalTypeEnum.fromValue(target.legalType());

      if (LegalTypeEnum.SP.equals(legalType) || LegalTypeEnum.GP.equals(legalType)) {
        return
            bcRegistryService
                .requestDocumentData(target.registrationNumber())
                .map(checkSoleProprietor(fieldName))
                .onErrorReturn(ResponseStatusException.class,
                    new ValidationError(fieldName,
                        "Incorporation Number was not found in BC Registry")
                )
                .defaultIfEmpty(
                    new ValidationError(fieldName,
                        "Incorporation Number was not found in BC Registry")
                )
                .filter(ValidationError::isValid)
                .next();
      }
    }
    return Mono.empty();
  }

  private Function<BcRegistryDocumentDto, ValidationError> checkSoleProprietor(
      String fieldName) {
    return bcRegistryBusinessDto ->
        Stream.of(
            checkStanding(fieldName),
            checkSoleProprietorOwner(fieldName)
        )
            .map(f -> f.apply(bcRegistryBusinessDto))
            .filter(ValidationError::isValid)
            .findFirst()
            .orElse(new ValidationError("", ""));

  }

  private Function<BcRegistryDocumentDto, ValidationError> checkStanding(
      String fieldName) {
    return bcRegistryBusinessDto ->
        Boolean.FALSE.equals(bcRegistryBusinessDto.business().goodStanding())
            ? new ValidationError(fieldName,
            "Company is not in goodStanding in BC Registry")
            : new ValidationError("", "");
  }

  private Function<BcRegistryDocumentDto, ValidationError> checkSoleProprietorOwner(
      String fieldName) {
    return bcRegistryBusinessDto ->
        Boolean.FALSE.equals(bcRegistryBusinessDto.isOwnedByPerson())
            ? new ValidationError(
            fieldName,
            String.format("%s sole proprietor is not owned by a person",
                bcRegistryBusinessDto.business().getResolvedLegalName()
            )
        )
            : new ValidationError("", "");
  }

}
