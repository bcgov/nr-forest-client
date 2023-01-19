package ca.bc.gov.app.routes;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import ca.bc.gov.app.handlers.ClientSearchIncorporationHandler;
import ca.bc.gov.app.handlers.ClientSearchNameBirthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class ClientSearchRouter implements BaseRouter {

  private final ClientSearchNameBirthHandler nameBirthHandler;
  private final ClientSearchIncorporationHandler incorporationHandler;

  @Override
  public String basePath() {
    return "/search";
  }

  @Override
  public String routeTagName() {
    return "Search Client";
  }

  @Override
  public String routeTagDescription() {
    return "Search clients based on some criterias";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return
        route()
            .GET(
                "/incorporationOrName",
                accept(MediaType.ALL)
                    .and(contentType(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)),
                incorporationHandler::handle,
                incorporationHandler.documentation(routeTagName())
            )
            .GET(
                "/nameAndBirth",
                accept(MediaType.ALL)
                    .and(contentType(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)),
                nameBirthHandler::handle,
                nameBirthHandler.documentation(routeTagName())
            )
            .build();
  }

}
