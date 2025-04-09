package ca.bc.gov.app.controller.client;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.dto.legacy.HistoryLogDto;
import ca.bc.gov.app.service.client.ClientLegacyService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api/clients/history-log", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Observed
@RequiredArgsConstructor
public class HistoryLogController {

  private final ClientLegacyService clientLegacyService;

  @GetMapping("/{clientNumber}")
  public Flux<HistoryLogDto> getHistoryLogsByClientNumber(
      @PathVariable String clientNumber,
      JwtAuthenticationToken principal
  ) {
    log.info("Getting history logs by client muber {}", clientNumber);
    return clientLegacyService.retrieveHistoryLog(clientNumber);
  }

}