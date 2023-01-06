package ca.bc.gov.app.routes.openmaps;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.dto.openmaps.PropertyDto;
import ca.bc.gov.app.handlers.openmaps.OpenMapsHandler;
import ca.bc.gov.app.routes.BaseRouter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class OpenMapsRouter implements BaseRouter {

  private final OpenMapsHandler openMapsHandler;

  @Override
  public String basePath() {
    return "/maps";
  }

  @Override
  public String routeTagName() {
    return "OpenMaps";
  }

  @Override
  public String routeTagDescription() {
    return "Exposes OpanMaps consumption for First nation data extraction";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return route()
        .GET(
            "/firstNation/{id}",
            accept(MediaType.ALL),
            openMapsHandler::getFirstNation,
            getFirstNationOps()
        )
        .build();
  }

  private Consumer<Builder> getFirstNationOps() {
    return ops -> ops
        .tag(routeTagName())
        .description("Get First Nation data by ID")
        .beanClass(OpenMapsHandler.class)
        .beanMethod("getFirstNation")
        .operationId("getFirstNation")
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
