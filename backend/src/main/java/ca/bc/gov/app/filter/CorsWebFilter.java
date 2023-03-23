package ca.bc.gov.app.filter;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * <p><b>CORS Filter</b></p>
 * Creates the CORS (Cross-Origin Resource Sharing) filter to enable external requests from
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class CorsWebFilter implements WebFilter {

  private final ForestClientConfiguration configuration;

  /**
   * <p><b>CORS Filter</b></p>
   * Creates the CORS (Cross-Origin Resource Sharing) filter to enable external requests from
   * the frontend application.
   * It consumes from the configuration file, AKA <b>application.yml</b> file and add as a default
   * filter when receiving a CORS request.
   */
  @Override
  public Mono<Void> filter(ServerWebExchange ctx, WebFilterChain chain) {

    ServerHttpRequest request = ctx.getRequest();
    ServerHttpResponse response = ctx.getResponse();
    HttpHeaders headers = response.getHeaders();

    ForestClientConfiguration.FrontEndCorsConfiguration corsSettings = configuration
        .getFrontend()
        .getCors();


    headers.add("Access-Control-Allow-Origin",
        configuration.getFrontend().getUrl());

    headers.add("Access-Control-Max-Age",
        String.valueOf(corsSettings.getAge().getSeconds()));

    headers.add("Access-Control-Allow-Methods",
        String.join(",", corsSettings.getMethods()));

    headers.add("Access-Control-Allow-Headers",
        String.join(",", corsSettings.getHeaders()));

    if (CorsUtils.isPreFlightRequest(request)) {
      response.setStatusCode(HttpStatus.NO_CONTENT);
      return Mono.empty();
    }

    return chain.filter(ctx);

  }
}
