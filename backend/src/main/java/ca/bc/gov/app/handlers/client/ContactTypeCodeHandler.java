package ca.bc.gov.app.handlers.client;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.handlers.BaseHandler;
import ca.bc.gov.app.service.client.ClientService;
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
@Slf4j
@RequiredArgsConstructor
public class ContactTypeCodeHandler implements BaseHandler {

  private final ClientService clientService;


  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        ServerResponse
            .ok()
            .body(
                clientService
                    .listClientContactTypeCodes(
                        serverRequest
                            .queryParam("page")
                            .map(Integer::parseInt)
                            .orElse(0),
                        serverRequest
                            .queryParam("size")
                            .map(Integer::parseInt)
                            .orElse(5)
                    ),
                ContactTypeCodeHandler.class
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("List contact type codes")
        .beanClass(ContactTypeCodeHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .requestBody(requestBodyBuilder())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("page")
                .description("""
                    0 index page number, for example:
                     0 for the first page, 4 for the fifth page.
                     Defaults to 0 (first page)""")
                .allowEmptyValue(true)
                .schema(schemaBuilder().implementation(String.class))
                .example("4")
        )
        .parameter(
            parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("size")
                .description("Amount of entries per page,default to 5")
                .allowEmptyValue(true)
                .schema(schemaBuilder().implementation(String.class))
                .example("5")
        )
        .response(
            responseBuilder()
                .responseCode("200")
                .description("OK - List a page of contact type codes")
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
