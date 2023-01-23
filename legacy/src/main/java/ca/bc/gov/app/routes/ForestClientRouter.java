package ca.bc.gov.app.routes;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import ca.bc.gov.app.handlers.ForestClientBandsHandler;
import ca.bc.gov.app.handlers.ForestClientBusinessHandler;
import ca.bc.gov.app.handlers.ForestClientUnregisteredHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class ForestClientRouter implements BaseRouter {

  private final ForestClientBusinessHandler businessHandler;
  private final ForestClientBandsHandler bandsHandler;

  private final ForestClientUnregisteredHandler unregisteredHandler;

  @Override
  public String basePath() {
    return "/client";
  }

  @Override
  public String routeTagName() {
    return "Forest Client";
  }

  @Override
  public String routeTagDescription() {
    return "Aggregation and other reports for forest clients";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return route()
        .GET(
            "/bands",
            accept(MediaType.ALL)
                .and(contentType(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)),
            bandsHandler::handle,
            bandsHandler.documentation(routeTagName())
        )
        .GET(
            "/business",
            accept(MediaType.ALL)
                .and(contentType(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)),
            businessHandler::handle,
            businessHandler.documentation(routeTagName())
        )
        .GET(
            "/unregistered",
            accept(MediaType.ALL)
                .and(contentType(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)),
            unregisteredHandler::handle,
            unregisteredHandler.documentation(routeTagName())
        )
        .build();
  }


}
