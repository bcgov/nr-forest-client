package ca.bc.gov.app.routes.ches;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.handlers.ches.ChesHandler;
import ca.bc.gov.app.routes.BaseRouter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class ChesRouter implements BaseRouter {

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
                handler::handle,
                handler.documentation(routeTagName())
            )
            .build();
  }


}
