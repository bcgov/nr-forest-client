package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
import ca.bc.gov.app.entity.client.DistrictCodeEntity;
import ca.bc.gov.app.exception.ClientAlreadyExistException;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.DistrictCodeRepository;
import ca.bc.gov.app.service.client.ClientService;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientSubmitRequestValidator implements ReactiveValidator {

  private final RegisteredBusinessInformationValidator registeredBusinessInformationValidator;
  private final UnregisteredBusinessInformationValidator unregisteredBusinessInformationValidator;
  private final ClientLocationDtoValidator locationDtoValidator;
  private final ClientTypeCodeRepository clientTypeCodeRepository;
  private final DistrictCodeRepository districtCodeRepository;
  private final ClientService clientService;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmissionDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientSubmissionDto request = (ClientSubmissionDto) target;
    
    validateBusinessInformation(
        request.businessInformation(), 
        request.userId(), 
        errors);

    validateLocation(request.location(), errors);
  }
  
  @Override
  public Mono<Errors> validateReactive(Object target, Errors errors) {
      
      ClientSubmissionDto request = (ClientSubmissionDto) target;
      String clientTypeCode = request.businessInformation().clientType();
      String userId = request.userId();
      return Mono.just(errors);
      /*if (ApplicationConstant.INDIVIDUAL_CLIENT_TYPE_CODE.equals(clientTypeCode)) {
          
          return clientService
              .findByIndividual(userId, "MARTINEZ")
              .then(Mono.just(errors))
              .onErrorResume(error -> {
                  if (error instanceof ClientAlreadyExistException) {
                      String errorMessage = error.getMessage();
                      String clientNumberRegex = ".*client number (\\d+).*";
                      Matcher matcher = Pattern.compile(clientNumberRegex).matcher(errorMessage);
                      String clientNumber = null;
                      if (matcher.find()) {
                          clientNumber = matcher.group(1);
                      }
                      log.info("\n" + clientNumber);
                      errors.pushNestedPath("businessInformation");
                      errors.rejectValue("", "Client already exists with the client number");
                      errors.popNestedPath();
                      return Mono.just(errors);
                  } else {
                      log.error("\nError while checking client existence for userId {} and lastName {}: {}", 
                                userId,
                                "MARTINEZ", 
                                error.getMessage());
                      return Mono.error(error);
                  }
              });
      } else {
          return Mono.just(errors);
      }*/
  }

  @SneakyThrows
  private void validateBusinessInformation(
      ClientBusinessInformationDto businessInformation, 
      String userId, 
      Errors errors) {

    String businessInformationField = "businessInformation";
    if (businessInformation == null) {
      errors.rejectValue(
          businessInformationField,
          fieldIsMissingErrorMessage(businessInformationField));
      return;
    }
    errors.pushNestedPath(businessInformationField);
    
    clientService.findByUserIdAndLastName(userId, "5")
    .subscribe(
        result -> {
            System.out.println("Received result: " + result);
            // Handle the result here
        },
        error -> {
            System.err.println("An error occurred: " + error.getMessage());
            // Handle the error here
        },
        () -> {
            System.out.println("Completed");
            // Handle completion here if necessary
        }
    );

    
    //TODO: Remove this code
    if (1 == 1) {
      errors.rejectValue("businessType", "Test");
      errors.popNestedPath();
      return;
    }
    
    String businessType = businessInformation.businessType();
    if (StringUtils.isAllBlank(businessType)) {
      errors.rejectValue("businessType", "You must choose an option");
      errors.popNestedPath();
      return;
    } 
    else if (!EnumUtils.isValidEnum(BusinessTypeEnum.class, businessType)) {
      errors.rejectValue("businessType", String.format("%s has an invalid value", "Business type"));
      errors.popNestedPath();
      return;
    }

    String clientTypeCode = businessInformation.clientType();

    if (StringUtils.isBlank(clientTypeCode)) {
      errors.rejectValue("clientType", "Client does not have a type");
      errors.popNestedPath();
      return;
    }

    if (StringUtils.isBlank(businessInformation.district())) {
      errors.rejectValue("district", "Client does not have a district");
      errors.popNestedPath();
      return;
    }
    else {
      DistrictCodeEntity districtCodeEntity = districtCodeRepository
          .findByCode(businessInformation.district()).toFuture().get();

      if (districtCodeEntity == null) {
        errors.rejectValue("district", "district is invalid");
        errors.popNestedPath();
        return;
      }
    }
    
    if (ApplicationConstant.REG_SOLE_PROPRIETORSHIP_CLIENT_TYPE_CODE.equals(clientTypeCode)
        || ApplicationConstant.UNREG_SOLE_PROPRIETORSHIP_CLIENT_TYPE_CODE.equals(clientTypeCode)
        || ApplicationConstant.INDIVIDUAL_CLIENT_TYPE_CODE.equals(clientTypeCode)
    ) {
      validateBirthdate(businessInformation.birthdate(), errors);
    }
    
    if (!ApplicationConstant.AVAILABLE_CLIENT_TYPES.contains(clientTypeCode)) {
      ClientTypeCodeEntity clientTypeCodeEntity = clientTypeCodeRepository
          .findByCode(clientTypeCode)
          .toFuture()
          .get();
      
      errors.rejectValue("clientType",
          String.format("'%s' is not supported at the moment", clientTypeCodeEntity.getDescription()));
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
    } 
    else if (!isValidBirthdate(birthdate)) {
      errors.rejectValue(dobFieldName, "Date of birth is invalid");
    }
    else {
      LocalDate minAgeDate = LocalDate.now().minusYears(19);
      if (birthdate.isAfter(minAgeDate)) {
        errors.rejectValue(dobFieldName, "Sole proprietorship must be at least 19 years old");
      }
    }
  }
  
  private boolean isValidBirthdate(LocalDate birthdate) {
    int year = birthdate.getYear();
    int month = birthdate.getMonthValue();
    int day = birthdate.getDayOfMonth();

    if (month < 1 || month > 12) {
      return false;
    }

    // Check day validity based on month (considering leap year)
    int maxDays = getMaxDaysInMonth(month, year);
    if (day < 1 || day > maxDays) {
      return false;
    }

    return true;
  }
  
  private int getMaxDaysInMonth(int month, int year) {
    switch (month) {
      case 2:
        // Check for leap year
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0 ? 29 : 28;
      case 4:
      case 6:
      case 9:
      case 11:
        return 30;
      default:
        return 31;
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