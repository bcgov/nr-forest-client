package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.service.ClientLocationService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/locations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientLocationController {

  private final ClientLocationService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ForestClientLocationDto dto) {
    log.info("Receiving request to save location for {}: {}", dto.clientNumber(),
        dto.clientLocnName());
    return service.saveAndGetIndex(dto);
  }

  @GetMapping("/search")
  public Flux<ForestClientLocationDto> findLocations(
      @RequestParam String address,
      @RequestParam String postalCode
  ) {
    log.info("Receiving request to search by address {} and postal code {}", address, postalCode);
    return service.search(address, postalCode);
  }

}
