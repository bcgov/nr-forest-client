package ca.bc.gov.app.handlers.fsa;

import ca.bc.gov.app.service.fsa.FsaClientService;
import ca.bc.gov.app.util.HandlerUtils;
import java.util.List;
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
public class FsaClientHandler {

  private final FsaClientService fsaClientService;

  public Mono<ServerResponse> findActiveClientTypeCodes(ServerRequest serverRequest) {
    return
        fsaClientService
            .findActiveClientTypeCodes()
            .flatMap(
                response -> ServerResponse.ok()
                    .body(Mono.just(response), List.class)
            )
            .doOnError(ResponseStatusException.class, HandlerUtils.handleStatusResponse())
            .doOnError(HandlerUtils.handleError());
  }

}
