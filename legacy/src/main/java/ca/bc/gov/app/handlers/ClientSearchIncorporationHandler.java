package ca.bc.gov.app.handlers;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.service.ClientSearchService;
import ca.bc.gov.app.util.HandlerUtil;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
public class ClientSearchIncorporationHandler implements BaseHandler {
  private final ClientSearchService service;

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        ServerResponse
            .ok()
            .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
            .body(
                service
                    .findByIncorporationOrName(
                        serverRequest.queryParam("incorporationNumber").orElse(null),
                        serverRequest.queryParam("companyName").orElse(null)
                    ),
                ForestClientDto.class
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("List forest client entries by incorporation number or name")
        .beanClass(ClientSearchIncorporationHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .parameter(
            parameterBuilder()
                .description("The incorporation ID to lookup")
                .allowEmptyValue(true)
                .example("BC0772006")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.QUERY)
                .name("incorporationNumber")
        )
        .parameter(
            parameterBuilder()
                .description("The name to lookup")
                .allowEmptyValue(true)
                .example("Power Corp")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.QUERY)
                .name("companyName")
        )
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
                                        .name("ForestClient")
                                        .implementation(ForestClientDto.class)
                                )
                        )
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        )
        .response(
            responseBuilder()
                .responseCode("412")
                .description("Missing value for parameter incorporationNumber or companyName")
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)
                                .example(
                                    "Missing value for parameter incorporationNumber or companyName")
                        )

                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        );
  }
}

