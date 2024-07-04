package ca.bc.gov.app.service.client.matches;

import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
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
public class ContactStepMatcher implements StepMatcher {

  private static final String PHONE_CONSTANT = "phone";
  private static final String FIELD_NAME_PREFIX = "location.contacts[";

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
   * This method returns the step matcher enumeration value for contact steps.
   *
   * @return The StepMatchEnum value representing the location step.
   */
  public StepMatchEnum getStepMatcher() {
    return StepMatchEnum.STEP3;
  }

  /**
   * <p>This method matches the client submission data to the contact step. It performs three
   * searches:</p>
   * <ol>
   * <li>A fuzzy match for a combination of name, email and phone numbers fields</li>
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
    if (dto.location().contacts() == null || dto.location().contacts().isEmpty()) {
      return Mono.error(new InvalidRequestObjectException("Invalid contact information"));
    }

    // Check if any of the addresses are invalid
    if (
        BooleanUtils.isFalse(dto
            .location()
            .contacts()
            .stream()
            .map(ClientContactDto::isValid)
            .reduce(true, Boolean::logicalAnd)
        )
    ) {
      return Mono.error(new InvalidRequestObjectException("Invalid contact information"));
    }

    // This is just to make sure index is filled in
    AtomicInteger indexCounter = new AtomicInteger(0);

    return
        dto
            .location()
            .contacts()
            // For each address in location
            .stream()
            // Fix nonexistent index
            .map(address -> address.withIndexed(indexCounter.getAndIncrement()))
            .map(contact ->
                //Concat all the results for each address
                Flux.concat(
                    processResult(
                        legacyService
                            .searchGeneric(
                                "email",
                                contact.email()
                            ),
                        FIELD_NAME_PREFIX + contact.index() + "].emailAddress",
                        false
                    ).as(Flux::from),
                    processResult(
                        legacyService
                            .searchGeneric(
                                PHONE_CONSTANT,
                                contact.phoneNumber()
                            ),
                        FIELD_NAME_PREFIX + contact.index() + "].businessPhoneNumber",
                        false
                    ).as(Flux::from),
                    processResult(
                        legacyService
                            .searchGeneric(
                                PHONE_CONSTANT,
                                contact.secondaryPhoneNumber()
                            ),
                        FIELD_NAME_PREFIX + contact.index() + "].secondaryPhoneNumber",
                        false
                    ).as(Flux::from),
                    processResult(
                        legacyService
                            .searchGeneric(
                                PHONE_CONSTANT,
                                contact.faxNumber()
                            ),
                        FIELD_NAME_PREFIX + contact.index() + "].faxNumber",
                        false
                    ).as(Flux::from),
                    processResult(
                        legacyService
                            .searchContact(
                                new ContactSearchDto(
                                    contact.firstName(),
                                    null,
                                    contact.lastName(),
                                    contact.email(),
                                    contact.phoneNumber(),
                                    contact.secondaryPhoneNumber(),
                                    contact.faxNumber()
                                )
                            ),
                        FIELD_NAME_PREFIX + contact.index() + "].firstName",
                        true
                    ).as(Flux::from)
                )
            )
            .reduce(Flux.empty(), Flux::concat)
            .as(this::reduceMatchResults);
  }
}
