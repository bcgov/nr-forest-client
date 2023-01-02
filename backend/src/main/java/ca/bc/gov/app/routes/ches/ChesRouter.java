package ca.bc.gov.app.routes.ches;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.header.Builder.headerBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.handlers.ches.ChesHandler;
import ca.bc.gov.app.routes.BaseRouter;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class ChesRouter extends BaseRouter {

  private final ChesHandler handler;

  @Override
  public String basePath() {
    return "/mail";
  }

  @Override
  public String routeTagName() {
    return "Ches";
  }

  @Override
  public String routeTagDescription() {
    return "A route for common services consumption";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return
        route()
            .POST(
                StringUtils.EMPTY,
                accept(MediaType.APPLICATION_JSON),
                handler::sendEmail,
                sendEmailOps()
            )
            .build();
  }


  private Consumer<Builder> sendEmailOps() {

    return ops -> ops
        .tag(routeTagName())
        .description("Send an email to one or more email addresses")
        .beanClass(ChesHandler.class)
        .beanMethod("sendEmail")
        .operationId("sendEmail")
        .requestBody(requestBodyBuilder().implementation(ChesRequest.class))
        .response(
            responseBuilder()
                .responseCode("201")
                .description("Mail was sent")
                .content(contentBuilder())
                .header(
                    headerBuilder()
                        .name("Location")
                        .schema(
                            schemaBuilder()
                                .implementation(String.class)
                                .example("/api/mail/00000000-0000-0000-0000-000000000000")
                        )
                )
        )
        .response(
            responseBuilder()
                .responseCode("400")
                .description("Something went wrong, a required parameter wasn't being sent")
                .content(
                    contentBuilder()
                        .example(exampleOjectBuilder().value("Destination email is required"))
                )

        );
  }

}
