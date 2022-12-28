package ca.bc.gov.app.routes.orgbook;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.dto.orgbook.OrgBookResultListResponse;
import ca.bc.gov.app.dto.orgbook.OrgBookTopicListResponse;
import ca.bc.gov.app.handlers.orgbook.OrgBookHandler;
import ca.bc.gov.app.routes.BaseRouter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class OrgBookRouter extends BaseRouter {

  private final OrgBookHandler handler;

  @Override
  public String basePath() {
    return "/api/orgbook";
  }

  @Override
  public String routeTagName() {
    return "OrgBook";
  }

  @Override
  public String routeTagDescription() {
    return "A route for BC OrgBook data access and consumption";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return
        route()
            .GET(
                "/incorporation/{id}",
                accept(MediaType.ALL),
                handler::findByIncorporationId,
                incorporationOps()
            )

            .GET(
                "/name/{id}",
                accept(MediaType.ALL),
                handler::findByName,
                nameLookUpOps()
            )
            .build();
  }

  private Consumer<Builder> incorporationOps() {

    return ops -> ops
        .tag(routeTagName())
        .description("Search the OrgBook based on the incorporation id")
        .beanClass(OrgBookHandler.class)
        .beanMethod("findByIncorporationId")
        .operationId("findByIncorporationId")
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .description("The incorporation ID to lookup")
                .schema(schemaBuilder().implementation(String.class))
                .example("BC0772006")
        )
        .requestBody(requestBodyBuilder())
        .parameter(
            parameterBuilder()
                .description("The incorporation ID to lookup")
                .allowEmptyValue(false)
                .example("BC0772006")
                .schema(schemaBuilder().implementation(String.class))
                .in(ParameterIn.PATH)
                .name("id")
        )
        .response(
            responseBuilder()
                .responseCode("200")
                .description("OK - Data was found based on the provided incorporation")
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder()
                                .name("TopicListResponse")
                                .implementation(OrgBookTopicListResponse.class)
                        )
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )

        );
  }


  private Consumer<Builder> nameLookUpOps() {
    return
        ops -> ops
            .tag(routeTagName())
            .description("Search the OrgBook based on the name")
            .beanClass(OrgBookHandler.class)
            .beanMethod("findByName")
            .operationId("findByName")
            .parameter(
                parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("id")
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
                            .schema(
                                schemaBuilder()
                                    .name("NameListResponse")
                                    .implementation(OrgBookResultListResponse.class)
                            )
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                    )
            );
  }

}
