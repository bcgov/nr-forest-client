package ca.bc.gov.app.handlers.ches;

import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.handlers.AbstractHandler;
import ca.bc.gov.app.service.ches.ChesCommonServicesService;
import ca.bc.gov.app.util.HandlerUtils;
import ca.bc.gov.app.validator.ches.ChesRequestValidator;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ChesHandler extends AbstractHandler<ChesRequest, ChesRequestValidator> {

  private final ChesCommonServicesService service;

  public ChesHandler(ChesCommonServicesService service, ChesRequestValidator validator) {
    super(ChesRequest.class, validator);
    this.service = service;
  }

  public Mono<ServerResponse> sendEmail(ServerRequest request) {
    return
        request
            .bodyToMono(ChesRequest.class)
            .switchIfEmpty(
                Mono.error(new InvalidRequestObjectException("no request body was provided"))
            )
            .doOnNext(this::validate)
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
            .doOnError(ResponseStatusException.class, HandlerUtils.handleStatusResponse())
            .doOnError(HandlerUtils.handleError());

  }

}
