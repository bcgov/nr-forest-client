package ca.bc.gov.app.routes.orgbook;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.handlers.orgbook.OrgBookIncorporationHandler;
import ca.bc.gov.app.handlers.orgbook.OrgBookNameHandler;
import ca.bc.gov.app.routes.BaseRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class OrgBookRouter implements BaseRouter {

  private final OrgBookIncorporationHandler handler;
  private final OrgBookNameHandler nameHandler;

  @Override
  public String basePath() {
    return "/orgbook";
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
                "/incorporation/{incorporationId}",
                accept(MediaType.ALL),
                handler::handle,
                handler.documentation(routeTagName())
            )

            .GET(
                "/name/{name}",
                accept(MediaType.ALL),
                nameHandler::handle,
                nameHandler.documentation(routeTagName())
            )
            .build();
  }

}
