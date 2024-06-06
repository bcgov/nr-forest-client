package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.service.client.ClientMatchService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/clients/matches", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientMatchController {

  private final ClientMatchService matchService;

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> fuzzyMatchClients(
      @RequestBody ClientSubmissionDto dto,
      @RequestHeader int step
  ) {
    return matchService.matchClients(dto, step);
  }

}
