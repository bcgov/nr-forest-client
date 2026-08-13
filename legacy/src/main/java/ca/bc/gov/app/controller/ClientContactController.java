package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.service.ClientContactService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Exposes the endpoints used to create, search and validate forest client contacts.
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientContactController {

  private final ClientContactService service;

  /**
   * Saves the provided forest client contact.
   *
   * @param dto the contact to be saved
   * @return a {@link Mono} emitting the client number of the saved contact
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ForestClientContactDto dto) {
    log.info("Receiving request to save contact for {}: {}", dto.clientNumber(), dto.contactName());
    return service.saveAndGetIndex(dto);
  }

  /**
   * Searches for forest client contacts matching the provided name, email and phone.
   *
   * @param firstName the first name of the contact
   * @param lastName the last name of the contact
   * @param email the email address of the contact
   * @param phone the phone number of the contact
   * @return a {@link Flux} emitting the matching contacts
   */
  @GetMapping("/search")
  public Flux<ForestClientContactDto> findIndividuals(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String email,
      @RequestParam String phone
  ) {
    log.info("Receiving request to search for contact: {} {} {} {}", firstName, lastName, email,
        phone);
    return service.search(firstName, lastName, email, phone);
  }

  /**
   * Checks whether a contact is being used (referenced) by another system, such as EMS, GAS2,
   * LEXIS, or SCS.
   *
   * @param contactId the id of the contact to check
   * @return a {@link Mono} emitting {@code true} if the contact is in use, {@code false}
   *     otherwise
   */
  @GetMapping("/{contactId}/in-use")
  public Mono<Boolean> isContactInUse(@PathVariable Long contactId) {
    log.info("Receiving request to check if contact {} is in use by another system", contactId);
    return service.isContactInUse(contactId);
  }

}
