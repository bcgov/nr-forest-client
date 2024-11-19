package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.service.ClientSearchService;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.util.List;
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

  @GetMapping("/individual")
  public Flux<ForestClientDto> findIndividuals(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam LocalDate dob,
      @RequestParam(required = false) String identification
  ) {
    log.info("Receiving request to search by individual {} {} {} {}", firstName, lastName, dob,
        identification);
    return service.findByIndividual(firstName, lastName, dob, identification,true);
  }

  @GetMapping("/match")
  public Flux<ForestClientDto> matchBy(
      @RequestParam String companyName
  ) {
    log.info("Receiving request to match by company name {}", companyName);
    return service.matchBy(companyName);
  }

  @GetMapping("/idAndLastName")
  public Flux<ForestClientDto> findByIdAndLastName(
      @RequestParam String clientId,
      @RequestParam String lastName
  ) {
    log.info("Receiving request to search by ID {} and Last Name {}", clientId, lastName);
    return service.findByIdAndLastName(clientId, lastName);
  }
  
  @GetMapping("/clientNumber")
  public Mono<ForestClientDto> findByClientNumber(
      @RequestParam String clientNumber
  ) {
    log.info("Receiving request to search by ID {} and Last Name", clientNumber);
    return service.findByClientNumber(clientNumber);
  }

  @GetMapping("/id/{idType}/{identification}")
  public Flux<ForestClientDto> findByIdentification(
      @PathVariable String idType,
      @PathVariable String identification
  ) {
    log.info("Receiving request to search by ID with type {} and value {}", idType, identification);
    return service.findByIdentification(idType, identification);
  }

  @GetMapping("/email")
  public Flux<ForestClientDto> findByGeneralEmail(
      @RequestParam String email
  ) {
    log.info("Receiving request to search by email {}", email);
    return service.findByGeneralEmail(email);
  }

  @GetMapping("/phone")
  public Flux<ForestClientDto> findByGeneralPhone(
      @RequestParam String phone
  ) {
    log.info("Receiving request to search by phone {}", phone);
    return service.findByGeneralPhoneNumber(phone);
  }

  @PostMapping("/address")
  public Flux<ForestClientDto> findByLocation(
      @RequestBody AddressSearchDto address
  ) {
    log.info("Receiving request to search by address {}", address);
    return service.findByEntireAddress(address);
  }

  @PostMapping("/contact")
  public Flux<ForestClientDto> findByContact(
      @RequestBody ContactSearchDto contact
  ) {
    log.info("Receiving request to search by contact {}", contact);
    return service.findByContact(contact);
  }

  @GetMapping("/acronym")
  public Flux<ForestClientDto> findByAcronym(
      @RequestParam String acronym
  ) {
    log.info("Receiving request to search by acronym {}", acronym);
    return service.findByAcronym(acronym);
  }

  @GetMapping("/doingBusinessAs")
  public Flux<ForestClientDto> findByDoingBusinessAs(
      @RequestParam String dbaName,
      @RequestParam(required = false,defaultValue = "true") Boolean isFuzzy
  ) {
    log.info("Receiving request to search by doing business as name {} being a {} match",
        dbaName, BooleanUtils.toString(isFuzzy,"fuzzy","full")
    );
    return service.findByDoingBusinessAs(dbaName,isFuzzy);
  }

  @GetMapping("/clientName")
  public Flux<ForestClientDto> findByClientName(
      @RequestParam String clientName
  ) {
    log.info("Receiving request to match by company name {}", clientName);
    return service.findByClientName(clientName);
  }
  
  @GetMapping
  public Flux<PredictiveSearchResultDto> findByComplexSearch(
      @RequestParam(required = false) String value,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "5") Integer size,
      ServerHttpResponse serverResponse) {
    if (StringUtils.isNotBlank(value)) {
      log.info("Receiving request to do a complex search by {}", value);
      return service
          .complexSearch(value, PageRequest.of(page, size))
          .doOnNext(pair -> serverResponse.getHeaders()
              .putIfAbsent("X-Total-Count", List.of(pair.getValue().toString()))
          )
          .map(Pair::getKey);
    } else {
      log.info("Receiving request to search the latest entries");
      return service
          .latestEntries(PageRequest.of(page, size))
          .doOnNext(pair -> serverResponse.getHeaders()
              .putIfAbsent("X-Total-Count", List.of(pair.getValue().toString()))
          )
          .map(Pair::getKey);
    }
  }
  
}
