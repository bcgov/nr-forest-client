package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.legacy.HistoryLogDto;
import ca.bc.gov.app.service.client.ClientLegacyService;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api/clients/history-logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Observed
@RequiredArgsConstructor
public class HistoryLogController {

  private final ClientLegacyService clientLegacyService;

  /**
   * Retrieves a paginated list of history logs for a given client number.
   *
   * @param clientNumber the unique identifier of the client
   * @param page the page number to retrieve (zero-based), defaults to 0 if not provided
   * @param size the number of items per page, defaults to 5 if not provided
   * @param sources optional list of source filters to apply; empty by default
   * @param principal the authenticated user making the request
   */
  @GetMapping("/{clientNumber}")
  public Flux<HistoryLogDto> getHistoryLogsByClientNumber(
      @PathVariable String clientNumber,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "5") Integer size,
      @RequestParam(required = false, defaultValue = "") List<String> sources,
      JwtAuthenticationToken principal
  ) {
    log.info("Getting history logs by client number {}, page {}, size {}, sources {}", 
             clientNumber, page, size, sources);
    return clientLegacyService.retrieveHistoryLogs(
             clientNumber, page, size, sources);
  }

}