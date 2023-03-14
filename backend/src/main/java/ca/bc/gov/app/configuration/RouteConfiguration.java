package ca.bc.gov.app.configuration;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;

import ca.bc.gov.app.routes.BaseRouter;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * <p><b>Base Router Class</b></p>
 * This configuration class is responsible for configuring the routing for webflux.
 * It also configures the OpenAPI part of the code with some default data.
 */
@Configuration
@Slf4j
public class RouteConfiguration {

  @Bean
  public RouterFunction<ServerResponse> routes(List<BaseRouter> routes) {
    return
        routes
            .stream()
            .map(route -> nest(
                path(String.format("/api%s", route.basePath())),
                route.routerRoute())
            )
            .reduce(RouterFunction::and)
            .orElseThrow();

  }

  @Bean
  public OpenApiCustomizer tagCustomizer(List<BaseRouter> routes) {
    return openAPI -> {
      routes.forEach(route ->
          openAPI
              .addTagsItem(
                  new Tag()
                      .name(route.routeTagName())
                      .description(route.routeTagDescription())
              )
      );

      openAPI
          .getPaths()
          .values()
          .forEach(pathItem ->
              pathItem
                  .addParametersItem(
                      new Parameter()
                          .schema(new StringSchema())
                          .required(true)
                          .examples(
                              Map.of(
                                  MediaType.APPLICATION_JSON_VALUE,
                                  new Example().value(MediaType.APPLICATION_JSON_VALUE),
                                  MediaType.TEXT_EVENT_STREAM_VALUE,
                                  new Example().value(MediaType.TEXT_EVENT_STREAM_VALUE)
                              )
                          )
                          .in("header")
                          .name("Content-Type")
                  )
                  .addParametersItem(
                      new Parameter()
                          .schema(new StringSchema())
                          .required(true)
                          .examples(
                              Map.of(
                                  MediaType.APPLICATION_JSON_VALUE,
                                  new Example().value(MediaType.APPLICATION_JSON_VALUE),
                                  MediaType.TEXT_EVENT_STREAM_VALUE,
                                  new Example().value(MediaType.TEXT_EVENT_STREAM_VALUE)
                              )
                          )
                          .in("header")
                          .name("accept")
                  )
          );
    };
  }

}
