package ca.bc.gov.app.handlers.ches;

import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.handlers.AbstractHandler;
import ca.bc.gov.app.service.ches.ChesCommonServicesService;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ChesHandler extends AbstractHandler<String> {

  private final ChesCommonServicesService service;

  public ChesHandler(ChesCommonServicesService service) {
    super(String.class);
    this.service = service;
  }

  public Mono<ServerResponse> sendEmail(ServerRequest request) {
    return
        request
            .bodyToMono(ChesRequest.class)
            .doOnNext(requestBody -> log.info("Requesting an email to be sent {}", requestBody))
            .flatMap(service::sendEmail)
            .flatMap(
                companyId ->
                    ServerResponse.created(
                            URI
                                .create(String.format("/api/mail/%s", companyId))
                        )
                        .build()
            )
            .doOnError(ResponseStatusException.class, handleStatusResponse())
            .doOnError(handleError());

  }

}
