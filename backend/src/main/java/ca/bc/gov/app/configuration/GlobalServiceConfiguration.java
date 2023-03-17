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

/**
 * <p><b>Global Service Configuration</b></p>
 * This class is responsible for configuring basic beans to be used by the services.
 * It creates and holds the external API webclients and the cors filter.
 */
@Configuration
@Slf4j
public class GlobalServiceConfiguration {

  /**
   * Creates and configures a WebClient instance for accessing OrgBook API based on the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration The {@link ForestClientConfiguration} containing the OrgBook API URI.
   * @return A {@link WebClient} instance configured for accessing OrgBook API.
   */
  @Bean
  public WebClient orgBookApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getOrgbook().getUri()).build();
  }

  /**
   * Returns a configured instance of WebClient to communicate with the CHES API
   * based on the provided configuration.
   *
   * @param configuration the ForestClientConfiguration containing the CHES API base URI
   * @return a WebClient instance configured with the CHES API base URI
   */
  @Bean
  public WebClient chesApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getChes().getUri()).build();
  }

  /**
   * Creates a WebClient instance for making HTTP requests to the OpenMaps API based on the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration the configuration object for the Forest client
   * @return a WebClient instance configured for the OpenMaps API
   */
  @Bean
  public WebClient openMapsApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getOpenmaps().getUri()).build();
  }

  /**
   * Returns a configured instance of WebClient for accessing the BC Registry API.
   *
   * @param configuration The configuration for the ForestClient.
   * @return A configured instance of WebClient for accessing the BC Registry API.
   */
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
