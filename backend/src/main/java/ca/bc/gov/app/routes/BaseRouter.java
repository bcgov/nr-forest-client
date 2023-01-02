package ca.bc.gov.app.routes;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public interface BaseRouter {
  public abstract String basePath();

  public abstract RouterFunction<ServerResponse> routerRoute();

  public abstract String routeTagName();

  public abstract String routeTagDescription();

}
