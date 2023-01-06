package ca.bc.gov.app.routes.client;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.dto.client.ClientCodeTypeDto;
import ca.bc.gov.app.handlers.client.ClientHandler;
import ca.bc.gov.app.routes.BaseRouter;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class ClientRouter implements BaseRouter {

  private final ClientHandler clientHandler;

  @Override
  public String basePath() {
    return "/clients";
  }

  @Override
  public String routeTagName() {
    return "FSA Clients";
  }

  @Override
  public String routeTagDescription() {
    return "The FSA Client endpoint, responsible for returning client data";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return route()
        .GET(
            "/active",
            accept(MediaType.ALL),
            clientHandler::findActiveClientTypeCodes,
            activeClientTypeCodesOps()
        )
        .build();
  }

  private Consumer<Builder> activeClientTypeCodesOps() {
    return ops -> ops
        .tag(routeTagName())
        .description("List active clients with their type codes")
        .beanClass(ClientHandler.class)
        .beanMethod("findActiveClientTypeCodes")
        .operationId("findActiveClientTypeCodes")
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
                                        .name("ClientCodeType")
                                        .implementation(ClientCodeTypeDto.class)
                                )
                        )
                        .mediaType(MediaType.APPLICATION_JSON_VALUE)
                )
        );

  }


}
