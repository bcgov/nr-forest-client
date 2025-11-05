package ca.bc.gov.app.validator;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.micrometer.observation.annotation.Observed;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
public class SubmissionValidatorService {

  public static final String LOG_MESSAGE = "Executing validator {}";
  private final List<ForestClientValidator<ClientBusinessInformationDto>> businessValidators;
  private final List<ForestClientValidator<ClientAddressDto>> addressValidators;
  private final List<ForestClientValidator<ClientContactDto>> contactValidators;
  private final List<ForestClientValidator<ClientLocationDto>> locationValidators;
  private final SubmissionRepository submissionRepository;
  private final ForestClientConfiguration configuration;

  /**
   * Validates the overall submission data and structure of a {@link ClientSubmissionDto} object.
   * <p>
   * This method serves as the primary entry point for validating a client submission. It first
   * performs basic structural validation on the {@link ClientSubmissionDto} object to ensure it
   * contains all necessary information for further processing. If the basic structure is valid, it
   * proceeds to validate the comprehensive data within the submission against various validation
   * rules.
   * </p>
   * <p>
   * The validation process is conducted in two stages:
   * <ul>
   *   <li>Initial validation of the submission's basic structure through {@code validateSubmissionDto}.</li>
   *   <li>If the initial validation passes (i.e., the submission DTO is structurally sound), further
   *       detailed validation of the submission data is performed by {@code validateSubmissionData}.</li>
   * </ul>
   * </p>
   * <p>
   * If the initial structural validation fails, the method immediately returns with the validation
   * errors encountered. If the structural validation passes but the detailed validation fails, the
   * detailed validation errors are returned. If both validations pass, a {@link Mono} containing the
   * original {@link ClientSubmissionDto} object is returned, indicating the submission is valid.
   * </p>
   *
   * @param request The client submission DTO to be validated.
   * @param source  The source of validation, determining which validators are applicable for
   *                detailed validation.
   * @return A {@link Mono} containing the original {@link ClientSubmissionDto} if valid, or a
   * {@link Mono} error with a {@link ValidationException} detailing the validation errors.
   */
  public Mono<ClientSubmissionDto> validate(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return validateSubmissionDto(request).switchIfEmpty(validateSubmissionData(request, source));
  }

  public Mono<Void> validateSubmissionLimit(JwtAuthenticationToken principal) {
    String userId = JwtPrincipalUtil.getUserId(principal);
    return checkSubmissionLimit(userId);
  }

  public Mono<Void> checkSubmissionLimit(String userId) {
    String provider = extractPrefix(userId);
    LocalDateTime endTime = LocalDateTime.now();
    LocalDateTime startTime;
    long maxSubmissions;
    String submissionTimeWindow;

    if ("IDIR".equalsIgnoreCase(provider)) {
      startTime = endTime.minus(configuration.getIdirSubmissionTimeWindow());
      maxSubmissions = configuration.getIdirMaxSubmissions();
      submissionTimeWindow = configuration.getIdirSubmissionTimeWindow().toHours() + " hours";
    }
    else if ("BCSC".equalsIgnoreCase(provider) || "BCEIDBUSINESS".equalsIgnoreCase(provider)) {
      startTime = endTime.minus(configuration.getOtherSubmissionTimeWindow());
      maxSubmissions = configuration.getOtherMaxSubmissions();
      submissionTimeWindow = configuration.getOtherSubmissionTimeWindow().toDays() + " days";
    }
    else {
      return Mono.error(new IllegalArgumentException("Invalid provider " + provider));
    }

    return submissionRepository
        .countBySubmissionDateBetweenAndCreatedByIgnoreCase(
          startTime,
          endTime,
          userId
        )
        .doOnNext(count -> log.info("User {} has made {} submissions in the last {}", userId, count, submissionTimeWindow))
        .filter(count -> count >= maxSubmissions)
        .flatMap(count -> Mono.error(
                new ValidationException(
                    List.of(
                        new ValidationError(
                            "submissionLimit",
                            "You can make up to " + maxSubmissions + " submissions in "
                                + submissionTimeWindow +
                                ". Resubmit this application in " + submissionTimeWindow + "."
                        )
                    )
                )
            )
        );
  }

  private String extractPrefix(String input) {
      int backslashIndex = input.indexOf('\\');
      if (backslashIndex != -1) {
          return input.substring(0, backslashIndex);
      } else {
          return input;
      }
  }


  /**
   * Validates the comprehensive data of a {@link ClientSubmissionDto} against various validation
   * rules.
   * <p>
   * This method orchestrates the validation process by invoking separate validation methods for
   * business information, location information, address information, and contact information within
   * the client submission. It utilizes the Reactor framework to execute these validations
   * concurrently, aggregating all validation errors into a single list.
   * </p>
   * <p>
   * The validation process is as follows:
   * <ul>
   *   <li>Concurrently validate business, location, address, and contact data.</li>
   *   <li>Aggregate all validation errors into a single list.</li>
   *   <li>If any errors are found, a {@link ValidationException} is thrown, encapsulating the errors.</li>
   *   <li>If no errors are found, the original {@link ClientSubmissionDto} is returned, indicating a successful validation.</li>
   * </ul>
   * </p>
   * <p>
   * This method demonstrates the use of Reactor's {@link Flux} and {@link Mono} for asynchronous data processing
   * and error handling within a Spring WebFlux application.
   * </p>
   *
   * @param request The client submission DTO to be validated.
   * @param source  The source of validation, determining which validators are applicable.
   * @return A {@link Mono} containing the original {@link ClientSubmissionDto} if valid, or a
   * {@link Mono} error with a {@link ValidationException} detailing the validation errors.
   */
  private Mono<ClientSubmissionDto> validateSubmissionData(ClientSubmissionDto request,
      ValidationSourceEnum source) {

    // We combine all the validation errors and return them as a single Mono
    return Flux.concat(
            businessValidationErrors(request, source),
            locationValidationErrors(request, source),
            addressValidationErrors(request, source),
            contactValidationErrors(request, source)
        )
        // We flatten the list of errors by reducing it
        .reduce(new ArrayList<ValidationError>(), (acc, errors) -> {
          acc.addAll(errors);
          return acc;
        })
        // We filter out the empty lists of errors, as it means no error
        .filter(errors -> !errors.isEmpty())
        // We throw a ValidationException if there are errors
        .flatMap(errors -> Mono.error(new ValidationException(errors)))
        // Cast is here due to the nature of the type erasure in Java
        .cast(ClientSubmissionDto.class)
        // Otherwise, return the request if there are no errors
        .defaultIfEmpty(request);
  }


  /**
   * Validates the basic structure of a {@link ClientSubmissionDto} object.
   * <p>
   * This method performs preliminary checks on the provided {@link ClientSubmissionDto} object to
   * ensure that it contains all necessary information for further processing. Specifically, it
   * checks for:
   * <ul>
   *   <li>Non-nullity of the request object itself.</li>
   *   <li>Presence of business information.</li>
   *   <li>Presence of location information.</li>
   *   <li>Non-emptiness of the contacts list within the location information.</li>
   *   <li>Non-emptiness of the addresses list within the location information.</li>
   * </ul>
   * If any of these checks fail, a {@link Mono} error containing a {@link ValidationException} is returned,
   * which encapsulates the specific validation errors encountered. If all checks pass, an empty {@link Mono}
   * is returned, indicating that the {@link ClientSubmissionDto} object is valid for further processing.
   * </p>
   *
   * @param request The {@link ClientSubmissionDto} object to validate.
   * @return A {@link Mono} containing the original {@link ClientSubmissionDto} object if valid, or
   * a {@link Mono} error with a {@link ValidationException} detailing the validation errors.
   */
  private Mono<ClientSubmissionDto> validateSubmissionDto(ClientSubmissionDto request) {
    if (request == null) {
      return Mono.error(
          new ValidationException(
              List.of(
                  new ValidationError(
                      "",
                      "no request body was provided"
                  )
              )
          )
      );
    }

    if (request.businessInformation() == null) {
      return Mono.error(
          new ValidationException(
              List.of(
                  new ValidationError(
                      "businessInformation",
                      fieldIsMissingErrorMessage("businessInformation")
                  )
              )
          )
      );
    }

    if (request.location() == null) {
      return Mono.error(
          new ValidationException(
              List.of(
                  new ValidationError(
                      "location",
                      fieldIsMissingErrorMessage("location")
                  )
              )
          )
      );
    }

    if (request.location().contacts() == null || request.location().contacts().isEmpty()) {
      return Mono.error(
          new ValidationException(
              List.of(
                  new ValidationError(
                      "location.contacts",
                      "contacts are missing"
                  )
              )
          )
      );
    }

    if (request.location().addresses() == null || request.location().addresses().isEmpty()) {
      return Mono.error(
          new ValidationException(
              List.of(
                  new ValidationError(
                      "location.addresses",
                      "addresses are missing"
                  )
              )
          )
      );
    }

    return checkSubmissionLimit(request.userId()).then().cast(ClientSubmissionDto.class);
  }

  /**
   * Validates business information within a client submission against a set of predefined rules.
   * <p>
   * This method leverages the Reactor framework to asynchronously validate business information
   * contained within the {@link ClientSubmissionDto}. It iterates over a list of business
   * validators, applying each validator that supports the specified validation source. Each
   * validator performs specific checks on the business information, and any validation errors
   * encountered are collected into a list.
   * </p>
   * <p>
   * The validation process involves the following steps:
   * <ul>
   *   <li>Iterating over the list of business validators.</li>
   *   <li>Filtering validators based on whether they support the current validation source.</li>
   *   <li>Logging the name of each validator being executed for traceability.</li>
   *   <li>Applying each applicable validator to the business information from the submission.</li>
   *   <li>Collecting any validation errors into a list.</li>
   * </ul>
   * </p>
   * <p>
   * If validation errors are found, they are returned as a {@link Mono} wrapping a list of
   * {@link ValidationError} objects. If no errors are found, an empty list is returned, wrapped
   * in a {@link Mono}.
   * </p>
   *
   * @param request The client submission DTO containing the business information to be validated.
   * @param source  The source of validation, used to determine which validators are applicable.
   * @return A {@link Mono} wrapping a list of {@link ValidationError}s, if any, otherwise an empty
   * list.
   */
  private Mono<List<ValidationError>> businessValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return
        Flux
            .fromIterable(businessValidators)
            .filter(validator -> validator.supports(source))
            .doOnNext(
                validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName())
            )
            .flatMap(validator -> validator.validate(request.businessInformation(), null))
            .filter(ValidationError::isValid)
            .collectList();
  }

  /**
   * Validates location information within a client submission against a set of predefined rules.
   * <p>
   * This method iterates over a list of location validators, applying each validator to the
   * location information found within the provided {@link ClientSubmissionDto}. It filters
   * validators based on the validation source to ensure that only relevant validators are applied.
   * Validation errors for the location are collected and aggregated into a list. If any validation
   * errors are found, the list of errors is returned wrapped in a {@link Mono}. Otherwise, an empty
   * list indicates successful validation with no errors.
   * </p>
   * <p>
   * The validation process involves:
   * <ul>
   *   <li>Filtering validators based on the validation source.</li>
   *   <li>Logging the validator being executed for debugging purposes.</li>
   *   <li>Applying each validator to the location in the submission, collecting any validation errors.</li>
   *   <li>Aggregating all errors into a list, which is then filtered to exclude empty lists, indicating
   *       successful validation with no errors.</li>
   * </ul>
   * </p>
   *
   * @param request The client submission DTO containing the location to be validated.
   * @param source  The source of validation, used to determine applicable validators.
   * @return A {@link Mono} wrapping a list of {@link ValidationError}s, if any, otherwise an empty
   * list.
   */
  private Mono<List<ValidationError>> locationValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return
        Flux
            .fromIterable(locationValidators)
            .filter(validator -> validator.supports(source))
            .doOnNext(
                validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
            .flatMap(validator -> validator.validate(request.location(), null))
            .filter(ValidationError::isValid)
            .collectList();
  }

  /**
   * Validates address information within a client submission against a set of predefined rules.
   * <p>
   * This method iterates over a list of address validators, applying each validator to the
   * addresses found within the provided {@link ClientSubmissionDto}. It filters validators based on
   * the validation source to ensure that only relevant validators are applied. Validation errors
   * for each address are collected and aggregated into a list. If any validation errors are found,
   * the list of errors is returned wrapped in a {@link Mono}. Otherwise, an empty list indicates
   * successful validation with no errors.
   * </p>
   * <p>
   * The validation process involves:
   * <ul>
   *   <li>Filtering validators based on the validation source.</li>
   *   <li>Logging the validator being executed for debugging purposes.</li>
   *   <li>Applying each validator to each address in the submission, collecting any validation errors.</li>
   *   <li>Aggregating all errors into a list, which is then filtered to exclude empty lists, indicating
   *       successful validation with no errors.</li>
   * </ul>
   * </p>
   *
   * @param request The client submission DTO containing the addresses to be validated.
   * @param source  The source of validation, used to determine applicable validators.
   * @return A {@link Mono} wrapping a list of {@link ValidationError}s, if any, otherwise an empty
   * list.
   */
  private Mono<List<ValidationError>> addressValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return
        Flux
            .fromIterable(addressValidators)
            .filter(validator -> validator.supports(source))
            .doOnNext(
                validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
            .flatMap(validator ->
                Flux
                    .fromIterable(request.location().addresses())
                    .flatMap(address -> validator.validate(address, address.index()))
                    .filter(ValidationError::isValid)
            )
            .collectList()
            .filter(errors -> !errors.isEmpty());
  }


  /**
   * Validates contact information within a client submission against a set of rules.
   * <p>
   * This method iterates over a list of contact validators, applying each to the contacts found
   * within the provided {@link ClientSubmissionDto}. Each validator is checked for applicability to
   * the current validation source, ensuring that only relevant validators are applied. Validation
   * errors are collected and returned as a list wrapped in a {@link Mono}.
   * </p>
   * <p>
   * The process involves:
   * <ul>
   *   <li>Filtering validators based on the validation source.</li>
   *   <li>Logging the validator being executed.</li>
   *   <li>Applying each validator to each contact, collecting any validation errors.</li>
   *   <li>Aggregating all errors into a list, which is then filtered to exclude empty lists,
   *       indicating successful validation with no errors.</li>
   * </ul>
   * </p>
   *
   * @param request The client submission DTO containing the contacts to be validated.
   * @param source  The source of validation, used to determine applicable validators.
   * @return A {@link Mono} wrapping a list of {@link ValidationError}s, if any, otherwise an empty
   * list.
   */
  private Mono<List<ValidationError>> contactValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return
        Flux
            .fromIterable(contactValidators)
            .filter(validator -> validator.supports(source))
            .doOnNext(
                validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
            .flatMap(validator ->
                Flux
                    .fromIterable(request.location().contacts())
                    .flatMap(contact -> validator.validate(contact, contact.index()))
                    .filter(ValidationError::isValid)
            )
            .collectList()
            .filter(errors -> !errors.isEmpty());
  }
  
  public Mono<Void> validateSubmissionDuplicationForUnregiteredBusinesses(
      JwtAuthenticationToken principal) {
    
    String fullUsername = JwtPrincipalUtil.getUserId(principal);

    return submissionRepository.countSubmissionUnregiteredBusinessesByUsername(fullUsername)
        .doOnNext(
            count -> log.info("User {} has {} active submissions", fullUsername, count)
        ) 
        .flatMap(count -> {
          if (count > 0) {
            return Mono.error(new ValidationException(
                List.of(
                    new ValidationError(
                        "duplicatedSubmission", 
                        "It looks like the client you're trying to create already has a submission in progress."
                    )
                ))
            );
          } 
          else {
            return Mono.empty();
          }
        });
  
  }

  public Mono<Void> validateSubmissionDuplicationForRegiteredBusinesses(
      String registrationNumber) {
    
    return submissionRepository.countSubmissionRegiteredBusinessesByRegistrationNumber(registrationNumber)
        .doOnNext(
            count -> log.info("There are {} active submissions for {}", count, registrationNumber)
        ) 
        .flatMap(count -> {
          if (count > 0) {
            return Mono.error(new ValidationException(
                List.of(
                    new ValidationError(
                        "duplicatedSubmission", 
                        "It looks like the client you're trying to create already has a submission in progress."
                    )
                ))
            );
          } 
          else {
            return Mono.empty();
          }
        });
  
  }

}
