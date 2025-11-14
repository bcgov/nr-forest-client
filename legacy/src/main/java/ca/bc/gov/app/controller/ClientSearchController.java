package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientDetailsDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.service.ClientSearchService;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/search", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientSearchController {

  private final ClientSearchService service;

  /**
   * Handles the HTTP GET request to search for clients by registration number or company name.
   *
   * @param registrationNumber the registration number to search for (optional)
   * @param companyName        the company name to search for (optional)
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/registrationOrName")
  public Flux<ForestClientDto> findByIncorporationOrName(
      @RequestParam(required = false) String registrationNumber,
      @RequestParam(required = false) String companyName
  ) {
    log.info("Receiving request to search by registration number {} or company name {}",
        registrationNumber, companyName);
    return service
        .findByRegistrationNumberOrCompanyName(registrationNumber, companyName);
  }

  /**
   * Handles the HTTP GET request to search for individuals by their first name, last name, date of
   * birth, and optional identification.
   *
   * @param firstName      the first name of the individual to search for
   * @param lastName       the last name of the individual to search for
   * @param dob            the date of birth of the individual to search for
   * @param identification the optional identification of the individual to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/individual")
  public Flux<ForestClientDto> findIndividuals(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam(required = false) LocalDate dob,
      @RequestParam(required = false) String identification
  ) {
    log.info("Receiving request to search by individual {} {} {} {}", firstName, lastName, dob,
        identification);
    return service.findByIndividual(firstName, lastName, dob, identification, true);
  }

  /**
   * Handles the HTTP GET request to perform a fuzzy match search by company name.
   *
   * @param companyName the company name to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/match")
  public Flux<ForestClientDto> matchBy(
      @RequestParam String companyName
  ) {
    log.info("Receiving request to fuzzy match by company name {}", companyName);
    return service.matchBy(companyName);
  }

  /**
   * Handles the HTTP GET request to search for clients by client ID and last name.
   *
   * @param clientId the client ID to search for
   * @param lastName the last name to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/idAndLastName")
  public Flux<ForestClientDto> findByIdAndLastName(
      @RequestParam String clientId,
      @RequestParam String lastName
  ) {
    log.info("Receiving request to search by ID {} and Last Name {}", clientId, lastName);
    return service.findByIdAndLastName(clientId, lastName);
  }

  /**
   * Handles the HTTP GET request to retrieve client details by client number.
   *
   * @param clientNumber the client number to search for
   * @return a Mono containing the client details if found, or an empty Mono if not found
   */
  @GetMapping("/clientNumber/{clientNumber}")
  public Mono<ForestClientDetailsDto> findByClientNumber(@PathVariable String clientNumber) {
    log.info("Receiving request to search by ID {}", clientNumber);
    return service.findByClientNumber(clientNumber);
  }

  /**
   * Handles the HTTP GET request to search for clients by identification type and value.
   *
   * @param idType         the type of identification to search for
   * @param identification the identification value to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/id/{idType}/{identification}")
  public Flux<ForestClientDto> findByIdentification(
      @PathVariable String idType,
      @PathVariable String identification
  ) {
    log.info("Receiving request to search by ID with type {} and value {}", idType, identification);
    return service.findByIdentification(idType, identification);
  }

  /**
   * Handles the HTTP GET request to search for clients by email.
   *
   * @param email the email address to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/email")
  public Flux<ForestClientDto> findByGeneralEmail(
      @RequestParam String email
  ) {
    log.info("Receiving request to search by email {}", email);
    return service.findByGeneralEmail(email);
  }

  /**
   * Handles the HTTP GET request to search for clients by phone number.
   *
   * @param phone the phone number to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/phone")
  public Flux<ForestClientDto> findByGeneralPhone(
      @RequestParam String phone
  ) {
    log.info("Receiving request to search by phone {}", phone);
    return service.findByGeneralPhoneNumber(phone);
  }

  /**
   * Handles the HTTP POST request to search for clients by address.
   *
   * @param address the address to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @PostMapping("/address")
  public Flux<ForestClientDto> findByLocation(
      @RequestBody AddressSearchDto address
  ) {
    log.info("Receiving request to search by address {}", address);
    return service.findByEntireAddress(address);
  }

  /**
   * Handles the HTTP POST request to search for clients by contact information.
   *
   * @param contact the contact information to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @PostMapping("/contact")
  public Flux<ForestClientDto> findByContact(
      @RequestBody ContactSearchDto contact
  ) {
    log.info("Receiving request to search by contact {}", contact);
    return service.findByContact(contact);
  }

  /**
   * Handles the HTTP GET request to search for clients by acronym.
   *
   * @param acronym the acronym to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/acronym")
  public Flux<ForestClientDto> findByAcronym(
      @RequestParam String acronym
  ) {
    log.info("Receiving request to search by acronym {}", acronym);
    return service.findByAcronym(acronym);
  }

  /**
   * Handles the HTTP GET request to search for clients by doing business as (DBA) name.
   *
   * @param dbaName the DBA name to search for
   * @param isFuzzy whether to perform a fuzzy match (default is true)
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/doingBusinessAs")
  public Flux<ForestClientDto> findByDoingBusinessAs(
      @RequestParam String dbaName,
      @RequestParam(required = false, defaultValue = "true") Boolean isFuzzy
  ) {
    log.info("Receiving request to search by doing business as name {} being a {} match",
        dbaName, BooleanUtils.toString(isFuzzy, "fuzzy", "full")
    );
    return service.findByDoingBusinessAs(dbaName, isFuzzy);
  }

  /**
   * Handles the HTTP GET request to search for clients by client name.
   *
   * @param clientName the client name to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  @GetMapping("/clientName")
  public Flux<ForestClientDto> findByClientName(
      @RequestParam String clientName
  ) {
    log.info("Receiving request to match by company name {}", clientName);
    return service.findByClientName(clientName);
  }

  /**
   * Handles the HTTP GET request to perform a complex search or retrieve the latest entries. If a
   * value is provided, it performs a complex search; otherwise, it retrieves the latest entries.
   * The total count of results is added to the response headers.
   *
   * @param value          the search value (optional)
   * @param page           the page number for pagination (default is 0)
   * @param size           the page size for pagination (default is 5)
   * @param serverResponse the server response to add headers to
   * @return a Flux containing the matching PredictiveSearchResultDto objects
   */
  @GetMapping
  public Flux<PredictiveSearchResultDto> findByComplexSearch(
      @RequestParam(required = false) String value,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "5") Integer size,
      ServerHttpResponse serverResponse) {

    PageRequest pageRequest = PageRequest.of(page, size);

    if (StringUtils.isNotBlank(value)) {
      log.info("Receiving request to do a complex search by {}", value);
      return service
          .complexSearch(value, pageRequest)
          .collectList()
          .flatMapMany(list -> {
            if (list.isEmpty()) {
              serverResponse.getHeaders().set("X-Total-Count", "0");
              return Flux.empty();
            }
            serverResponse
                .getHeaders()
                .set("X-Total-Count", list.get(0).getValue().toString());
            return Flux.fromIterable(list).map(Pair::getKey);
          });

    } else {
      log.info("Receiving request to search the latest entries");
      return service
          .latestEntries(pageRequest)
          .collectList()
          .flatMapMany(list -> {
            if (list.isEmpty()) {
              serverResponse
                .getHeaders()
                .set("X-Total-Count", "0");
              return Flux.empty();
            }
            serverResponse
                .getHeaders()
                .set("X-Total-Count", list.get(0).getValue().toString());
            return Flux
                .fromIterable(list)
                .map(Pair::getKey);
          });
    }
  }

  @GetMapping("/corporationValues/{clientNumber}")
  public Flux<ForestClientDto> searchByCorporationValues(
      @PathVariable String clientNumber,
      @RequestParam(required = false) String registryCompanyTypeCode,
      @RequestParam(required = false) String corpRegnNmbr
  ){
    log.info("Receiving request to search by corporation values {}{} that is not from {}",registryCompanyTypeCode, corpRegnNmbr, clientNumber);
    return service.searchByCorporationValues(clientNumber, registryCompanyTypeCode, corpRegnNmbr);
  }

  @GetMapping("/relation/{clientNumber}")
  public Flux<PredictiveSearchResultDto> searchByRelation(
      @PathVariable String clientNumber,
      @RequestParam(required = false) String type,
      @RequestParam String value,
      ServerHttpResponse serverResponse
  ) {
    log.info(
        "Searching related clients with relation type {} search term {} and excluding client {}",
        type, value, clientNumber);
    return service
        .searchByRelation(clientNumber, type, value)
        .switchOnFirst((signal, flux) -> {
          if (signal.hasValue()) {
            long total = Optional.ofNullable(signal.get()).map(Pair::getValue).orElse(0L);
            serverResponse.getHeaders().add("X-Total-Count", String.valueOf(total));
          } else {
            serverResponse.getHeaders().add("X-Total-Count", "0");
          }
          return flux.map(Pair::getKey);
        });
  }

}
