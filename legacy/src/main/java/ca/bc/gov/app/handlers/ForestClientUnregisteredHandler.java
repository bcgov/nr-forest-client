package ca.bc.gov.app.handlers;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.service.ForestClientService;
import ca.bc.gov.app.util.HandlerUtil;
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
@RequiredArgsConstructor
@Slf4j
public class ForestClientUnregisteredHandler implements BaseHandler {

  private final ForestClientService service;
  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return ServerResponse
        .ok()
        .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
        .body(service.getUnregisteredCompanies(), ClientPublicViewDto.class)
        .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
        .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops.tag(tag)
        .description("List all clients that are unregistered")
        .beanClass(ForestClientUnregisteredHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .response(
            responseBuilder()
                .responseCode("200")
                .description("Found")
                .content(
                    contentBuilder()
                        .array(
                            arraySchemaBuilder()
                                .schema(
                                    schemaBuilder()
                                        .name("ClientInformation")
                                        .implementation(ClientPublicViewDto.class)
                                )
                        )
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        );
  }
}
