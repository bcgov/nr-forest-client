package ca.bc.gov.app.handlers.orgbook;

import ca.bc.gov.app.dto.orgbook.OrgBookTopicListResponse;
import ca.bc.gov.app.service.orgbook.OrgBookApiService;
import ca.bc.gov.app.util.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrgBookHandler {

  private final OrgBookApiService service;

  public Mono<ServerResponse> findByIncorporationId(ServerRequest serverRequest) {
    return
        service
            .findByIncorporationNumber(serverRequest.pathVariable("incorporationId"))
            .flatMap(
                response -> ServerResponse.ok()
                    .body(Mono.just(response), OrgBookTopicListResponse.class)
            )
            .doOnError(ResponseStatusException.class, HandlerUtils.handleStatusResponse())
            .doOnError(HandlerUtils.handleError());
  }

  public Mono<ServerResponse> findByName(ServerRequest serverRequest) {
    return
        service
            .findByClientName(serverRequest.pathVariable("name"))
            .flatMap(
                response -> ServerResponse.ok()
                    .body(Mono.just(response), OrgBookTopicListResponse.class)
            )
            .doOnError(ResponseStatusException.class, HandlerUtils.handleStatusResponse())
            .doOnError(HandlerUtils.handleError());
  }
}
