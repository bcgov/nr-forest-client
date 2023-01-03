package ca.bc.gov.app.handlers.openmaps;

import ca.bc.gov.app.dto.openmaps.PropertyDTO;
import ca.bc.gov.app.service.openmaps.OpenMapsService;
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
public class OpenMapsHandler {

  private final OpenMapsService openMapsService;

  public Mono<ServerResponse> getFirstNation(ServerRequest serverRequest) {
    return
        openMapsService
            .getFirstNation(serverRequest.pathVariable("id"))
            .flatMap(
                companyId ->
                    ServerResponse.ok()
                        .body(Mono.just(companyId), PropertyDTO.class)
            )
            .doOnError(ResponseStatusException.class, HandlerUtils.handleStatusResponse())
            .doOnError(HandlerUtils.handleError());
  }
}
