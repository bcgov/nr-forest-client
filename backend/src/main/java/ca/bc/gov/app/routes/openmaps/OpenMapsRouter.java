package ca.bc.gov.app.routes.openmaps;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.handlers.openmaps.OpenMapsHandler;
import ca.bc.gov.app.routes.BaseRouter;
import lombok.RequiredArgsConstructor;
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
            openMapsHandler::handle,
            openMapsHandler.documentation(routeTagName())
        )
        .build();
  }

}
