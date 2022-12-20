package ca.bc.gov.app.routes.ches;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.handlers.ches.ChesHandler;
import ca.bc.gov.app.routes.BaseRouter;
import ca.bc.gov.app.util.SwaggerUtils;
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
    return "/api/mail";
  }

  @Override
  protected String routeTagName() {
    return "Ches - Common Services";
  }

  @Override
  protected String routeTagDescription() {
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

    return SwaggerUtils
        .buildApi(
            "sendEmail",
            "Send an email to one or more email addresses",
            routeTagName(),
            ChesHandler.class
        )
        //TODO: Example is not showing
        .andThen(SwaggerUtils.createdOps(
            "/api/mail/00000000-0000-0000-0000-000000000000"))
        //TODO: Example is not showing
        .andThen(SwaggerUtils.badRequestOps(
            "Destination email is required"))
        .andThen(SwaggerUtils.requestBodyOps(ChesRequest.class));
  }

}
