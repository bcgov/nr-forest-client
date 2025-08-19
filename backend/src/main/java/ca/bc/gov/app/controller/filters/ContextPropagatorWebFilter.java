package ca.bc.gov.app.controller.filters;

import io.micrometer.context.ContextRegistry;
import java.util.List;
import java.util.function.Function;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * This class is a web filter that extracts a value from the security context and sets it in the MDC
 * (Mapped Diagnostic Context) for logging and tracing purposes. The value is then propagated down
 * the filter chain and set in the reactive context to ensure it is available for logging and
 * tracing in both reactive and non-reactive parts of the application.
 */
public abstract class ContextPropagatorWebFilter implements WebFilter {

  private static final List<String> SKIP_PATHS = List.of("/metrics", "/health");

  protected abstract String getContextKey();

  protected abstract Function<Authentication, String> getContextValueExtractor();

  /**
   * Filters each incoming request to extract from the security context a {@link String} value using
   * {@link #getContextValueExtractor()} and set it in the MDC. The value is then propagated down
   * the filter chain and set in the reactive context to ensure it is available for logging and
   * tracing in both reactive and non-reactive parts of the application.
   *
   * @param exchange The current server web exchange that contains information about the request and
   *                 response.
   * @param chain    The web filter chain that allows the filter to pass on the request to the next
   *                 entity in the chain.
   * @return A {@link Mono} of {@link Void} that indicates when request handling is complete.
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // This is to be able to tackle non-reactive context
    contextLoad();

    // If the request is for metrics or health, we skip the context propagation
    if (SKIP_PATHS.contains(exchange.getRequest().getPath().toString())) {
      return chain.filter(exchange);
    }
    return
        // Here we are getting the user id from the security context
        ReactiveSecurityContextHolder
            .getContext()
            .map(SecurityContext::getAuthentication)
            .map(getContextValueExtractor())
            // Then we set it to the MDC
            .doOnNext(userId -> MDC.put(getContextKey(), userId))
            // Then we chain the filter, passing the context down
            .flatMap(userId -> chain
                .filter(exchange)
                .contextWrite(Context.of(getContextKey(), userId))
                // While we are at it, we also set the context for the reactive part
                .doOnNext(v -> contextLoad())
            );
  }

  /**
   * Initializes and registers a thread-local context for the current thread. This method configures
   * the {@link ContextRegistry} to handle the value within the MDC (Mapped Diagnostic Context),
   * allowing for the propagation of the value across different parts of the application that run on
   * the same thread. It specifically sets up accessors for getting, setting, and removing the value
   * from the MDC. This setup is crucial for maintaining state across the reactive and non-reactive
   * parts of the application.
   */
  private void contextLoad() {
    ContextRegistry
        .getInstance()
        .registerThreadLocalAccessor(
            getContextKey(),
            () -> MDC.get(getContextKey()),
            contextValue -> MDC.put(getContextKey(), contextValue),
            () -> MDC.remove(getContextKey())
        );
  }

}