package ca.bc.gov.app.handlers.openmaps;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.dto.openmaps.PropertyDto;
import ca.bc.gov.app.handlers.BaseHandler;
import ca.bc.gov.app.service.openmaps.OpenMapsService;
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
public class OpenMapsHandler implements BaseHandler {

  private final OpenMapsService openMapsService;

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        ServerResponse
            .ok()
            .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
            .body(
                openMapsService
                    .getFirstNation(serverRequest.pathVariable("id")),
                PropertyDto.class
            )
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("Get First Nation data by ID")
        .beanClass(OpenMapsHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .requestBody(requestBodyBuilder())
        .parameter(
            parameterBuilder()
                .description("The first nation federal ID to lookup")
                .allowEmptyValue(false)
                .example("656")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.PATH)
                .name("id")
        )
        .response(
            responseBuilder()
                .responseCode("200")
                .description("OK - Data was found based on the provided id")
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder()
                                .name("FirstNationResponse")
                                .implementation(PropertyDto.class)
                        )
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        )
        .response(
            responseBuilder()
                .responseCode("400")
                .description("No first nation found with provided federal id ")
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)

                        )
                        .example(exampleOjectBuilder()
                            .value("No first nation found with federal id 99999"))
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        );
  }
}
