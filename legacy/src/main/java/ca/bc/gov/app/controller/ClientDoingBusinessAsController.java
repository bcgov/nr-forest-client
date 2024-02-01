package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.exception.NoValueFoundException;
import ca.bc.gov.app.service.ClientDoingBusinessAsService;
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
@RequestMapping(value = "/api/dba", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientDoingBusinessAsController {

  private final ClientDoingBusinessAsService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ClientDoingBusinessAsDto dto) {
    log.info("Receiving request to save client doing business as for {}: {}", dto.clientNumber(),
        dto.doingBusinessAsName());
    return service.saveAndGetIndex(dto);
  }

  @GetMapping("/search")
  public Flux<ClientDoingBusinessAsDto> search(@RequestParam String dbaName) {
    log.info("Receiving request to search client doing business as {}", dbaName);
    return service.search(dbaName).switchIfEmpty(Flux.error(new NoValueFoundException(dbaName)));

  }

}
