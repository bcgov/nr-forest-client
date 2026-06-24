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
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Validates client submissions and submission limits.
 */
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
   * Validates the submission structure and its business data.
   *
   * @param request The client submission DTO to be validated.
   * @param source The source of validation, used to determine applicable validators.
   * @return A {@link Mono} containing the original {@link ClientSubmissionDto} if valid, or a
   *     validation error when validation fails.
   */
  public Mono<ClientSubmissionDto> validate(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return validateSubmissionDto(request).switchIfEmpty(validateSubmissionData(request, source));
  }

  /**
   * Validates the authenticated user's submission limit.
   *
   * @param principal the authenticated principal
   * @return a completion signal when the user is within the configured limit
   */
  public Mono<Void> validateSubmissionLimit(JwtAuthenticationToken principal) {
    String userId = JwtPrincipalUtil.getUserId(principal);
    return checkSubmissionLimit(userId);
  }

  /**
   * Validates the submission limit for the given user id.
   *
   * @param userId the user id to validate
   * @return a completion signal when the user is within the configured limit
   */
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
    } else if (
        "BCSC".equalsIgnoreCase(provider)
            || "BCEIDBUSINESS".equalsIgnoreCase(provider)
    ) {
      startTime = endTime.minus(configuration.getOtherSubmissionTimeWindow());
      maxSubmissions = configuration.getOtherMaxSubmissions();
      submissionTimeWindow = configuration.getOtherSubmissionTimeWindow().toDays() + " days";
    } else {
      return Mono.error(new IllegalArgumentException("Invalid provider " + provider));
    }

    return submissionRepository
        .countBySubmissionDateBetweenAndCreatedByIgnoreCase(
            startTime,
            endTime,
            userId
        )
        .doOnNext(
            count ->
                log.info(
                    "User {} has made {} submissions in the last {}",
                    userId,
                    count,
                    submissionTimeWindow
                )
        )
        .filter(count -> count >= maxSubmissions)
        .flatMap(
            count -> Mono.error(submissionLimitExceeded(maxSubmissions, submissionTimeWindow))
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

  private Mono<ClientSubmissionDto> validateSubmissionData(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return Flux.concat(
            businessValidationErrors(request, source),
            locationValidationErrors(request, source),
            addressValidationErrors(request, source),
            contactValidationErrors(request, source)
        )
        .reduce(new ArrayList<ValidationError>(), (acc, errors) -> {
          acc.addAll(errors);
          return acc;
        })
        .filter(errors -> !errors.isEmpty())
        .flatMap(errors -> Mono.error(new ValidationException(errors)))
        .cast(ClientSubmissionDto.class)
        .defaultIfEmpty(request);
  }

  private Mono<ClientSubmissionDto> validateSubmissionDto(ClientSubmissionDto request) {
    if (request == null) {
      return validationError("", "no request body was provided");
    }

    if (request.businessInformation() == null) {
      return validationError(
          "businessInformation",
          fieldIsMissingErrorMessage("businessInformation")
      );
    }

    if (request.location() == null) {
      return validationError("location", fieldIsMissingErrorMessage("location"));
    }

    if (request.location().contacts() == null || request.location().contacts().isEmpty()) {
      return validationError("location.contacts", "contacts are missing");
    }

    if (request.location().addresses() == null || request.location().addresses().isEmpty()) {
      return validationError("location.addresses", "addresses are missing");
    }

    return checkSubmissionLimit(request.userId()).then(Mono.just(request));
  }

  private Mono<List<ValidationError>> businessValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return Flux.fromIterable(businessValidators)
        .filter(validator -> validator.supports(source))
        .doOnNext(validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
        .flatMap(validator -> validator.validate(request.businessInformation(), null))
        .filter(ValidationError::isValid)
        .collectList();
  }

  private Mono<List<ValidationError>> locationValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return Flux.fromIterable(locationValidators)
        .filter(validator -> validator.supports(source))
        .doOnNext(validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
        .flatMap(validator -> validator.validate(request.location(), null))
        .filter(ValidationError::isValid)
        .collectList();
  }

  private Mono<List<ValidationError>> addressValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return Flux.fromIterable(addressValidators)
        .filter(validator -> validator.supports(source))
        .doOnNext(validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
        .flatMap(
            validator ->
                Flux.fromIterable(request.location().addresses())
                    .flatMap(address -> validator.validate(address, address.index()))
                    .filter(ValidationError::isValid)
        )
        .collectList()
        .filter(errors -> !errors.isEmpty());
  }

  private Mono<List<ValidationError>> contactValidationErrors(
      ClientSubmissionDto request,
      ValidationSourceEnum source
  ) {
    return Flux.fromIterable(contactValidators)
        .filter(validator -> validator.supports(source))
        .doOnNext(validator -> log.info(LOG_MESSAGE, validator.getClass().getSimpleName()))
        .flatMap(
            validator ->
                Flux.fromIterable(request.location().contacts())
                    .flatMap(contact -> validator.validate(contact, contact.index()))
                    .filter(ValidationError::isValid)
        )
        .collectList()
        .filter(errors -> !errors.isEmpty());
  }

  /**
   * Validates whether an unregistered business already has an active submission.
   *
   * @param principal the authenticated principal
   * @return a completion signal when no duplicate submission exists
   */
  public Mono<Void> validateSubmissionDuplicationForUnregiteredBusinesses(
      JwtAuthenticationToken principal
  ) {
    String fullUsername = JwtPrincipalUtil.getUserId(principal);

    return submissionRepository.countSubmissionUnregiteredBusinessesByUsername(fullUsername)
        .doOnNext(count -> log.info("User {} has {} active submissions", fullUsername, count))
        .flatMap(count -> {
          if (count > 0) {
            return Mono.error(duplicateSubmissionError());
          } else {
            return Mono.empty();
          }
        });
  }

  /**
   * Validates whether a registered business already has an active submission.
   *
   * @param registrationNumber the registration number to validate
   * @return a completion signal when no duplicate submission exists
   */
  public Mono<Void> validateSubmissionDuplicationForRegiteredBusinesses(
      String registrationNumber
  ) {
    return submissionRepository
        .countSubmissionRegiteredBusinessesByRegistrationNumber(registrationNumber)
        .doOnNext(
            count -> log.info("There are {} active submissions for {}", count, registrationNumber)
        )
        .flatMap(count -> {
          if (count > 0) {
            return Mono.error(duplicateSubmissionError());
          } else {
            return Mono.empty();
          }
        });
  }

  private Mono<ClientSubmissionDto> validationError(String field, String message) {
    return Mono.error(new ValidationException(List.of(new ValidationError(field, message))));
  }

  private ValidationException submissionLimitExceeded(
      long maxSubmissions,
      String submissionTimeWindow
  ) {
    String message =
        "You can make up to "
            + maxSubmissions
            + " submissions in "
            + submissionTimeWindow
            + ". Resubmit this application in "
            + submissionTimeWindow
            + ".";
    return new ValidationException(
        List.of(new ValidationError("submissionLimit", message))
    );
  }

  private ValidationException duplicateSubmissionError() {
    return new ValidationException(
        List.of(
            new ValidationError(
                "duplicatedSubmission",
                "It looks like the client you're trying to create already has a submission in "
                    + "progress."
            )
        )
    );
  }

}
