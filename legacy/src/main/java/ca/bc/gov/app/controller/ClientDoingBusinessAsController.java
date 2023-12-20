package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.service.ClientDoingBusinessAsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/dba", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientDoingBusinessAsController {

  private final ClientDoingBusinessAsService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ClientDoingBusinessAsDto dto) {
    return service.saveAndGetIndex(dto);
  }

}
