package ca.bc.gov.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
/**
 * <p><b>Global Service Configuration</b></p>
 * This class is responsible for configuring basic beans to be used by the services.
 * It creates and holds the external API webclients and the cors filter.
 */
public class GlobalServiceConfiguration {

  @Bean
  public WebClient orgBookApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getOrgbook().getUri()).build();
  }

  @Bean
  public WebClient chesApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getChes().getUri()).build();
  }

  @Bean
  public WebClient openMapsApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getOpenmaps().getUri()).build();
  }

  @Bean
  public WebClient bcRegistryApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getBcregistry().getUri())
        .defaultHeader("x-apikey", configuration.getBcregistry().getApiKey())
        .defaultHeader("Account-Id", configuration.getBcregistry().getAccountId())
        .build();
  }


  /**
   * <p><b>CORS Filter</b></p>
   * Creates the CORS (Cross-Origin Resource Sharing) filter to enable external requests from
   * the frontend application.
   * It consumes from the configuration file, AKA <b>application.yml</b> file and add as a default
   * filter when receiving a CORS request.
   */
  @Bean
  public WebFilter corsFilter(ForestClientConfiguration configuration) {
    return (ServerWebExchange ctx, WebFilterChain chain) -> {

      ServerHttpRequest request = ctx.getRequest();

      if (CorsUtils.isCorsRequest(request)) {
        ServerHttpResponse response = ctx.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", configuration.getFrontend().getUrl());
        headers.add("Access-Control-Max-Age",
            String.valueOf(configuration.getFrontend().getCors().getAge().getSeconds())
        );
        headers.add("Access-Control-Allow-Methods",
            String.join(",", configuration.getFrontend().getCors().getMethods())
        );
        headers.add("Access-Control-Allow-Headers",
            String.join(",", configuration.getFrontend().getCors().getHeaders())
        );

        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
          response.setStatusCode(HttpStatus.OK);
          return Mono.empty();
        }
      }
      return chain.filter(ctx);

    };
  }

}
