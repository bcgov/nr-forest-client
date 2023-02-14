package ca.bc.gov.app.handlers.client;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.client.ClientCodeTypeDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.handlers.BaseHandler;
import ca.bc.gov.app.service.client.ClientService;
import ca.bc.gov.app.util.HandlerUtil;
import java.time.LocalDate;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ClientTypeCodeHandler implements BaseHandler {

  private final ClientService clientService;

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        ServerResponse
            .ok()
            .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
            .body(
                clientService
                    .findActiveClientTypeCodes(LocalDate.now()),
                ClientNameCodeDto.class
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("List active clients with their type codes")
        .beanClass(ClientTypeCodeHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .requestBody(requestBodyBuilder())
        .response(
            responseBuilder()
                .responseCode("200")
                .description("OK - Data was found based on the provided incorporation")
                .content(
                    contentBuilder()
                        .array(
                            arraySchemaBuilder()
                                .schema(
                                    schemaBuilder()
                                        .name("NameCode")
                                        .implementation(ClientNameCodeDto.class)
                                )
                        )
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        );
  }
}
