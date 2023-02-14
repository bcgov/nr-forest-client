package ca.bc.gov.app.handlers.orgbook;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.handlers.BaseHandler;
import ca.bc.gov.app.service.orgbook.OrgBookApiService;
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
public class OrgBookNameHandler implements BaseHandler {

  private final OrgBookApiService service;

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        ServerResponse
            .ok()
            .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
            .body(
                service
                    .findByClientName(serverRequest.pathVariable("name")),
                ClientNameCodeDto.class
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return
        ops -> ops
            .tag(tag)
            .description("Search the OrgBook based on the name")
            .beanClass(OrgBookIncorporationHandler.class)
            .beanMethod("handle")
            .operationId("handle")
            .parameter(
                parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("name")
                    .schema(schemaBuilder().implementation(String.class))
                    .description("The name to lookup")
                    .example("Power Corp")
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
                                            .name("NameCode")
                                            .implementation(ClientNameCodeDto.class)
                                    )
                            )
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                    )
            );
  }
}
