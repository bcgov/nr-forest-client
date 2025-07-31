package ca.bc.gov.app.controller.filters;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


/**
 * A web filter that extracts the user ID from the security context and sets it in the MDC (Mapped
 * Diagnostic Context). This filter is applied globally to all incoming requests. It ensures that
 * the user ID is available in the MDC, facilitating logging and tracing of requests by user ID. The
 * filter operates in both reactive and non-reactive contexts, ensuring compatibility across
 * different parts of the application.
 */
//@Component
@Slf4j
@Order(-1)
public class UserIdWebFilter extends ContextPropagatorWebFilter {

  /**
   * Retrieves the context key for the user ID. This key is used to store the user ID in the MDC
   * (Mapped Diagnostic Context).
   *
   * @return The context key for the user ID.
   */
  @Override
  protected String getContextKey() {
    return ApplicationConstant.MDC_USERID;
  }

  /**
   * Extracts the user ID from the authentication object. This method processes the authentication
   * object to extract the user's ID based on the type of principal. It supports extraction from
   * {@link JwtAuthenticationToken}, {@link Jwt}, and {@link UserDetails} principals. The user ID is
   * formatted as "Provider\\UserID" for JWT tokens, or simply the username for {@link UserDetails}.
   * If the authentication object is null, not authenticated, or the principal type is not
   * supported, "no-user-id-found" is returned.
   *
   * @return The extracted user ID in the format "Provider\\UserID" or the username, or
   * "no-user-id-found" if it cannot be extracted.
   */
  @Override
  protected Function<Authentication, String> getContextValueExtractor() {
    return authentication -> {
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
    };
  }

}
