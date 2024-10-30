package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class BusinessInformationBirthdayValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  public static final String BIRTHDATE = "businessInformation.birthdate";

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {
    ClientTypeEnum clientTypeEnum = ClientTypeEnum.fromValue(target.clientType());

    if (
        ClientTypeEnum.RSP.equals(clientTypeEnum) ||
            ClientTypeEnum.USP.equals(clientTypeEnum) ||
            ClientTypeEnum.I.equals(clientTypeEnum)
    ) {
      if (target.birthdate() == null) {
        return Mono.just(
            new ValidationError(BIRTHDATE,
                fieldIsMissingErrorMessage("Birthdate"))
        );
      }

      LocalDate minAgeDate = LocalDate.now().minusYears(19);
      if (target.birthdate().isAfter(minAgeDate)) {
        return Mono.just(
            new ValidationError(BIRTHDATE,
                "Sole proprietorship must be at least 19 years old")
        );
      }
    }
    return Mono.empty();
  }

}
