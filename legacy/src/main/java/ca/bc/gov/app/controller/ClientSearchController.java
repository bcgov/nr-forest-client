package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.service.ClientSearchService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequestMapping(value = "/api/search", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientSearchController {

  private final ClientSearchService service;

  @GetMapping("/incorporationOrName")
  public Flux<ForestClientDto> findByIncorporationOrName(
      @RequestParam(required = false) String incorporationNumber,
      @RequestParam(required = false) String companyName
  ) {
    return service
        .findByIncorporationOrName(incorporationNumber, companyName);
  }

  @GetMapping("/individual")
  public Flux<ForestClientDto> findIndividuals(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam LocalDate dob
  ) {
    return service.findByIndividual(firstName, lastName, dob);
  }

  @GetMapping("/match")
  public Flux<ForestClientDto> matchBy(
      @RequestParam String companyName
  ) {
    return service.matchBy(companyName);
  }

}
