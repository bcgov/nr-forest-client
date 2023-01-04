package ca.bc.gov.app.handlers.client;

import ca.bc.gov.app.service.client.ClientService;
import ca.bc.gov.app.util.HandlerUtil;
import java.time.LocalDate;
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
public class ClientHandler {

  private final ClientService clientService;

  public Mono<ServerResponse> findActiveClientTypeCodes(ServerRequest serverRequest) {
    return
        clientService
            .findActiveClientTypeCodes(LocalDate.now())
            .flatMap(
                response -> ServerResponse.ok()
                    .body(Mono.just(response), List.class)
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

}
