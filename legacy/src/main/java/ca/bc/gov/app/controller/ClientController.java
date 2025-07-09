package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.HistoryLogDto;
import ca.bc.gov.app.entity.ClientRelatedProjection;
import ca.bc.gov.app.service.ClientRelatedService;
import ca.bc.gov.app.service.ClientService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping(value = "/api/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientController {

  private final ClientService service;
  private final ClientRelatedService relatedService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ForestClientDto dto) {
    log.info("Receiving request to save client {}: {}", dto.clientNumber(), dto.clientName());
    return service.saveAndGetIndex(dto);
  }

  @GetMapping("/history-logs/{clientNumber}")
  public Flux<HistoryLogDto> findHistoryLogsByClientNumber(
      @PathVariable String clientNumber,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "5") Integer size,
      @RequestParam(required = false) List<String> sources,
      ServerHttpResponse serverResponse
  ) {
    log.info("Receiving request to search client history by client number {}", 
             clientNumber);
    return service
        .findHistoryLogsByClientNumber(clientNumber, PageRequest.of(page, size), sources)
        .doOnNext(pair -> serverResponse.getHeaders()
            .putIfAbsent("X-Total-Count", List.of(pair.getValue().toString()))
        )
        .map(Pair::getKey);
  }

  @GetMapping("/{clientNumber}/related-clients")
  public Flux<ClientRelatedProjection> getRelatedClients(
      @PathVariable String clientNumber
  ) {
    log.info("Receiving request to fetch related clients for client number {}", clientNumber);
    return relatedService.getRelatedClientList(clientNumber);
  }
  
}
