package ca.bc.gov.app.routes;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;

import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public abstract class BaseRouter {
  public abstract String basePath();

  public abstract RouterFunction<ServerResponse> routerRoute();

  protected abstract String routeTagName();

  protected abstract String routeTagDescription();


  @Bean
  public RouterFunction<ServerResponse> routes() {
    return nest(path(basePath()), routerRoute());
  }

  @Bean
  public OpenApiCustomizer tagCustomizer() {
    return openAPI ->
        openAPI
            .addTagsItem(
                new Tag()
                    .name(routeTagName())
                    .description(routeTagDescription())
            );

  }
}
