package ca.bc.gov.app.handlers.ches;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.header.Builder.headerBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.handlers.AbstractHandler;
import ca.bc.gov.app.handlers.BaseHandler;
import ca.bc.gov.app.service.ches.ChesCommonServicesService;
import ca.bc.gov.app.util.HandlerUtil;
import ca.bc.gov.app.validator.ches.ChesRequestValidator;
import java.net.URI;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ChesHandler extends AbstractHandler<ChesRequest, ChesRequestValidator> implements
    BaseHandler {

  private final ChesCommonServicesService service;

  public ChesHandler(ChesCommonServicesService service, ChesRequestValidator validator) {
    super(ChesRequest.class, validator);
    this.service = service;
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
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
                    ServerResponse
                        .created(
                            URI
                                .create(String.format("/api/mail/%s", companyId))
                        )
                        .contentType(request.headers().contentType().orElse(MediaType.APPLICATION_JSON))
                        .build()
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());

  }


  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("Send an email to one or more email addresses")
        .beanClass(ChesHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .requestBody(requestBodyBuilder().implementation(ChesRequest.class))
        .response(
            responseBuilder()
                .responseCode("201")
                .description("Mail was sent")
                .content(contentBuilder())
                .header(
                    headerBuilder()
                        .name("Location")
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)
                                .example("/api/mail/00000000-0000-0000-0000-000000000000")
                        )
                )
        )
        .response(
            responseBuilder()
                .responseCode("400")
                .description("Something went wrong, a required parameter wasn't being sent")
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)
                                .example("Destination email is required")
                        )
                )
        );
  }
}
