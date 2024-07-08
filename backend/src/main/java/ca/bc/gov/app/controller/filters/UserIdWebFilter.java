package ca.bc.gov.app.controller.filters;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import io.micrometer.context.ContextRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;


/**
 * A web filter that extracts the user ID from the security context and sets it in the MDC (Mapped
 * Diagnostic Context). This filter is applied globally to all incoming requests. It ensures that
 * the user ID is available in the MDC, facilitating logging and tracing of requests by user ID. The
 * filter operates in both reactive and non-reactive contexts, ensuring compatibility across
 * different parts of the application.
 */
@Component
@Slf4j
@Order(-1)
public class UserIdWebFilter implements WebFilter {

  /**
   * Filters each incoming request to extract the user ID from the security context and set it in
   * the MDC. The user ID is then propagated down the filter chain and set in the reactive context
   * to ensure it is available for logging and tracing in both reactive and non-reactive parts of
   * the application.
   *
   * @param exchange The current server web exchange that contains information about the request and
   *                 response.
   * @param chain    The web filter chain that allows the filter to pass on the request to the next
   *                 entity in the chain.
   * @return A Mono<Void> that indicates when request handling is complete.
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // This is to be able to tackle non-reactive context
    contextLoad();

    return
        // Here we are getting the user id from the security context
        ReactiveSecurityContextHolder
            .getContext()
            .map(SecurityContext::getAuthentication)
            .map(this::extractUserId)
            // Then we set it to the MDC
            .doOnNext(userId -> MDC.put(ApplicationConstant.MDC_USERID, userId))
            // Then we chain the filter, passing the context down
            .flatMap(userId -> chain
                .filter(exchange)
                .contextWrite(Context.of(ApplicationConstant.MDC_USERID, userId))
                // While we are at it, we also set the context for the reactive part
                .doOnNext(v -> contextLoad())
            );
  }

  /**
   * Initializes and registers a thread-local context for the current thread. This method configures
   * the {@link ContextRegistry} to handle the user ID within the MDC (Mapped Diagnostic Context),
   * allowing for the propagation of the user ID across different parts of the application that run
   * on the same thread. It specifically sets up accessors for getting, setting, and removing the
   * user ID from the MDC. This setup is crucial for maintaining state across the reactive and
   * non-reactive parts of the application.
   */
  private void contextLoad() {
    ContextRegistry
        .getInstance()
        .registerThreadLocalAccessor(
            ApplicationConstant.MDC_USERID,
            () -> MDC.get(ApplicationConstant.MDC_USERID),
            userId -> MDC.put(ApplicationConstant.MDC_USERID, userId),
            () -> MDC.remove(ApplicationConstant.MDC_USERID)
        );
  }

  /**
   * Extracts the user ID from the authentication object. This method processes the authentication
   * object to extract the user's ID based on the type of principal. It supports extraction from
   * {@link JwtAuthenticationToken}, {@link Jwt}, and {@link UserDetails} principals. The user ID is
   * formatted as "Provider\\UserID" for JWT tokens, or simply the username for {@link UserDetails}.
   * If the authentication object is null, not authenticated, or the principal type is not
   * supported, "no-user-id-found" is returned.
   *
   * @param authentication The authentication object from which to extract the user ID.
   * @return The extracted user ID in the format "Provider\\UserID" or the username, or
   * "no-user-id-found" if it cannot be extracted.
   */
  private String extractUserId(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      Object principal = authentication.getPrincipal();

      if (principal instanceof JwtAuthenticationToken jwt) {
        return JwtPrincipalUtil.getUserId(jwt);
      }

      if (principal instanceof Jwt jwt) {
        return JwtPrincipalUtil.getUserId(jwt);
      }

      if (principal instanceof UserDetails userDetails) {
        return userDetails.getUsername();
      }
    }
    return "no-user-id-found";
  }

}
