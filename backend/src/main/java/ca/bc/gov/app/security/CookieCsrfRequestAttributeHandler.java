package ca.bc.gov.app.security;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * This class is a custom CSRF token request attribute handler that extends the ServerCsrfTokenRequestAttributeHandler.
 * It is annotated with @Component, meaning it is a Spring-managed bean and can be autowired into other components.
 */
@Component
public class CookieCsrfRequestAttributeHandler extends ServerCsrfTokenRequestAttributeHandler {

  /**
   * This method is used to resolve the CSRF token value from the request.
   * It first checks if the exchange and csrfToken are not null.
   * Then, it tries to get the CSRF token value from the form data.
   * If it cannot find the CSRF token in the form data, it tries to get it from the request headers.
   *
   * @param exchange The current server web exchange.
   * @param csrfToken The CSRF token.
   * @return A Mono that emits the CSRF token value if it is found, or empty if it is not found.
   */
  @Override
  public Mono<String> resolveCsrfTokenValue(ServerWebExchange exchange, CsrfToken csrfToken) {
    Assert.notNull(exchange, "exchange cannot be null");
    Assert.notNull(csrfToken, "csrfToken cannot be null");
    return exchange.getFormData()
        .flatMap((data) -> Mono.justOrEmpty(data.getFirst(csrfToken.getParameterName())))
        .switchIfEmpty(Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(csrfToken.getHeaderName())));
  }
}
