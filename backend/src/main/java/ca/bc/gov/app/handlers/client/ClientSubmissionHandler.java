package ca.bc.gov.app.handlers.client;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.header.Builder.headerBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.handlers.BaseHandler;
import ca.bc.gov.app.service.client.ClientService;
import ca.bc.gov.app.util.HandlerUtil;
import java.net.URI;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClientSubmissionHandler implements BaseHandler {

  private final ClientService clientService;

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        serverRequest
            .bodyToMono(ClientSubmissionDto.class)
            .flatMap(clientService::submit)
            .flatMap(submissionId ->
                ServerResponse
                    .created(
                        URI.create(String.format("/api/clients/submissions/%d", submissionId))
                    )
                    .header("x-sub-id",String.valueOf(submissionId))
                    .build()
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("Submit client data")
        .beanClass(ClientSubmissionHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .requestBody(
            requestBodyBuilder()
                .implementation(ClientSubmissionDto.class))
        .response(
            responseBuilder()
                .responseCode("201")
                .description("New client submission posted")
                .content(contentBuilder())
                .header(
                    headerBuilder()
                        .name("Location")
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)
                                .example("/api/clients/submissions/0")
                        )
                )
                .header(
                    headerBuilder()
                        .name("x-sub-id")
                        .description("ID of the submission that was created")
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)
                                .example("0")
                        )
                )
        );
  }
}
