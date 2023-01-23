package ca.bc.gov.app.handlers;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import ca.bc.gov.app.ApplicationConstants;
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
public class ClientSearchNameBirthHandler implements BaseHandler {

  private final ClientSearchService service;

  @Override
  public Mono<ServerResponse> handle(ServerRequest serverRequest) {
    return
        ServerResponse
            .ok()
            .contentType(serverRequest.headers().contentType().orElse(MediaType.APPLICATION_JSON))
            .body(
                service
                    .findByNameAndBirth(
                        serverRequest
                            .queryParam(ApplicationConstants.CLIENT_SEARCH_FIRST_NAME),
                        serverRequest
                            .queryParam(ApplicationConstants.CLIENT_SEARCH_LAST_NAME),
                        serverRequest
                            .queryParam(ApplicationConstants.CLIENT_SEARCH_BIRTHDATE)
                    ),
                ForestClientDto.class)
            .doOnError(ResponseStatusException.class, HandlerUtil.handleStatusResponse())
            .doOnError(HandlerUtil.handleError());
  }

  @Override
  public Consumer<Builder> documentation(String tag) {
    return ops -> ops
        .tag(tag)
        .description("List forest client entries by first name, last name and date of birth")
        .beanClass(ClientSearchNameBirthHandler.class)
        .beanMethod("handle")
        .operationId("handle")
        .parameter(
            parameterBuilder()
                .description("First name to lookup. For companies, you can pass an empty value")
                .allowEmptyValue(true)
                .example("Jhon")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.QUERY)
                .name(ApplicationConstants.CLIENT_SEARCH_FIRST_NAME)
        )
        .parameter(
            parameterBuilder()
                .description("Last name to lookup. For companies, this will be the company name")
                .allowEmptyValue(false)
                .example("Doh")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.QUERY)
                .name(ApplicationConstants.CLIENT_SEARCH_LAST_NAME)
        )
        .parameter(
            parameterBuilder()
                .description("""                        
                    Date of birth to lookup in an ISO Local Date format, 
                    such as YYYY-MM-dd.<br> 
                    For companies, you can pass an empty value""")
                .allowEmptyValue(true)
                .example("1955-05-09")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.QUERY)
                .name(ApplicationConstants.CLIENT_SEARCH_BIRTHDATE)
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
        );
  }
}
