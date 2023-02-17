package ca.bc.gov.app.routes.client;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import ca.bc.gov.app.handlers.client.ClientCountryCodeHandler;
import ca.bc.gov.app.handlers.client.ClientProvinceCodeHandler;
import ca.bc.gov.app.handlers.client.ClientSubmissionHandler;
import ca.bc.gov.app.handlers.client.ClientTypeCodeHandler;
import ca.bc.gov.app.handlers.client.ContactTypeCodeHandler;
import ca.bc.gov.app.routes.BaseRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
@RequiredArgsConstructor
public class ClientRouter implements BaseRouter {

  private final ClientProvinceCodeHandler provinceCodeHandler;
  private final ClientTypeCodeHandler clientHandler;
  private final ClientCountryCodeHandler countryCodeHandler;
  private final ContactTypeCodeHandler contactTypeCodeHandler;
  private final ClientSubmissionHandler clientSubmissionHandler;

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
    return "The FSA Client endpoint, responsible for handling client data";
  }

  @Override
  public RouterFunction<ServerResponse> routerRoute() {
    return route()
        .GET(
            "/activeClientTypeCodes",
            accept(MediaType.ALL),
            clientHandler::handle,
            clientHandler.documentation(routeTagName())
        )
        .GET(
            "/activeCountryCodes",
            accept(MediaType.ALL),
            countryCodeHandler::handle,
            countryCodeHandler.documentation(routeTagName())
        )
        .GET(
            "/activeCountryCodes/{countryCode}",
            accept(MediaType.ALL),
            provinceCodeHandler::handle,
            provinceCodeHandler.documentation(routeTagName())
        )
        .GET(
            "/activeContactTypeCodes",
            accept(MediaType.ALL),
            contactTypeCodeHandler::handle,
            contactTypeCodeHandler.documentation(routeTagName())
        )
        .POST(
            "/submit",
            accept(MediaType.ALL),
            clientSubmissionHandler::handle,
            clientSubmissionHandler.documentation(routeTagName())
        )
        .build();
  }
}
