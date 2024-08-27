package ca.bc.gov.app.service.client.matches;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.service.client.ClientLegacyService;
import io.micrometer.observation.annotation.Observed;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Observed
@RequiredArgsConstructor
public class LocationStepMatcher implements StepMatcher {

  private static final String PHONE_CONSTANT = "phone";
  private static final String FIELD_NAME_PREFIX = "location.addresses[";

  /**
   * The ClientLegacyService used to search for individuals and documents.
   */
  private final ClientLegacyService legacyService;

  /**
   * This method is used to get the logger for this class. This is just to allow the default methods
   * to access the logger.
   *
   * @return The Logger object used for logging in this class.
   */
  public Logger getLogger() {
    return log;
  }

  /**
   * This method returns the step matcher enumeration value for location steps.
   *
   * @return The StepMatchEnum value representing the location step.
   */
  public StepMatchEnum getStepMatcher() {
    return StepMatchEnum.STEP2;
  }

  /**
   * <p>This method matches the client submission data to the location step. It performs three
   * searches:</p>
   * <ol>
   * <li>A full match for a combination of all address-related fields</li>
   * <li>A full match for the email address (both on location and contact)</li>
   * <li>A full match for each phone number (both on location and contact)</li>
   * </ol>
   * <p>The results of these searches are then processed and reduced to a single result.</p>
   *
   * @param dto The ClientSubmissionDto object containing the client data to be matched.
   * @return A Mono<Void> indicating when the matching process is complete.
   */
  public Mono<Void> matchStep(ClientSubmissionDto dto) {

    // Check if the location information is empty
    if (dto.location().addresses() == null || dto.location().addresses().isEmpty()) {
      return Mono.error(new InvalidRequestObjectException("Invalid address information"));
    }

    // Check if any of the addresses are invalid
    if (
        BooleanUtils.isFalse(
            dto
                .location()
                .addresses()
                .stream()
                .map(ClientAddressDto::isValid)
                .reduce(true, Boolean::logicalAnd)
        )
    ) {
      return Mono.error(new InvalidRequestObjectException("Invalid address information"));
    }

    // This is just to make sure index is filled in
    AtomicInteger indexCounter = new AtomicInteger(0);

    return
        dto
            .location()
            .addresses()
            // For each address in location
            .stream()
            // Fix nonexistent index
            .map(address -> address.withIndexed(indexCounter.getAndIncrement()))
            //Concat all the results for each location
            .map(this::processSingleLocation)
            .reduce(Flux.empty(), Flux::concat)
            .as(this::reduceMatchResults);
  }

  private Flux<MatchResult> processSingleLocation(ClientAddressDto location) {

    Mono<MatchResult> contactEmailFull = processResult(
        legacyService
            .searchGeneric(
                "email",
                location.emailAddress()
            ),
        FIELD_NAME_PREFIX + location.index() + "].emailAddress",
        true,
        false
    );

    Mono<MatchResult> businessPhoneFull = processResult(
        legacyService
            .searchGeneric(
                PHONE_CONSTANT,
                location.businessPhoneNumber()
            ),
        FIELD_NAME_PREFIX + location.index() + "].businessPhoneNumber",
        true,
        false
    );

    Mono<MatchResult> secondaryPhoneFull = processResult(
        legacyService
            .searchGeneric(
                PHONE_CONSTANT,
                location.secondaryPhoneNumber()
            ),
        FIELD_NAME_PREFIX + location.index() + "].secondaryPhoneNumber",
        true,
        false
    );

    Mono<MatchResult> faxPhoneFull = processResult(
        legacyService
            .searchGeneric(
                PHONE_CONSTANT,
                location.faxNumber()
            ),
        FIELD_NAME_PREFIX + location.index() + "].faxNumber",
        true,
        false
    );

    Mono<MatchResult> locationFull = processResult(
        legacyService
            .searchLocation(
                new AddressSearchDto(
                    location.streetAddress(),
                    location.city(),
                    location.province().value(),
                    location.postalCode(),
                    location.country().text()
                )
            ),
        FIELD_NAME_PREFIX + location.index() + "].streetAddress",
        true,
        false
    );

    return Flux.concat(
        contactEmailFull,
        businessPhoneFull,
        secondaryPhoneFull,
        faxPhoneFull,
        locationFull
    );


  }

}
