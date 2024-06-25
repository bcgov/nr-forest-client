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

/**
 * This class is a REST controller for client matching operations.
 * It uses the ClientMatchService to perform the matching operations.
 */
@RestController
@RequestMapping(value = "/api/clients/matches", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientMatchController {

  /**
   * The ClientMatchService used to perform the matching operations.
   */
  private final ClientMatchService matchService;

  /**
   * This method is a POST endpoint for fuzzy matching clients.
   * It takes a ClientSubmissionDto object and a step number as input.
   * It uses the ClientMatchService to perform the matching operation.
   *
   * @param dto The ClientSubmissionDto object containing the client data to be matched.
   * @param step The step number for the matching operation.
   * @return A Mono<Void> indicating when the matching process is complete.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> fuzzyMatchClients(
      @RequestBody ClientSubmissionDto dto,
      @RequestHeader(name = "X-STEP") int step
  ) {
    return matchService.matchClients(dto, step);
  }

}