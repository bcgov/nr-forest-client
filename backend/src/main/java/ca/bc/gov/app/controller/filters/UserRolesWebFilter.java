package ca.bc.gov.app.controller.filters;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Extracts the user roles from the security context and sets them in the MDC (Mapped Diagnostic
 * Context). This filter is applied globally to all incoming requests. It ensures that the user
 * roles are available in the MDC, facilitating logging and tracing of requests by user roles. The
 * filter operates in both reactive and non-reactive contexts, ensuring compatibility across
 * different parts.
 */
@Component
@Slf4j
@Order(-2)
public class UserRolesWebFilter extends ContextPropagatorWebFilter {

  /**
   * Retrieves the context key for the user roles. This key is used to store the user roles in the
   * MDC (Mapped Diagnostic Context).
   *
   * @return The context key for the user roles.
   */
  @Override
  protected String getContextKey() {
    return ApplicationConstant.MDC_USERROLES;
  }

  /**
   * Extracts the context value for the user roles from the given Authentication object. This
   * function retrieves the roles from different types of principals (JwtAuthenticationToken, Jwt,
   * UserDetails) and concatenates them into a comma-separated string.
   *
   * @return A function that takes an Authentication object and returns a comma-separated string of
   * user roles.
   */
  @Override
  protected Function<Authentication, String> getContextValueExtractor() {
    return authentication -> {
      Set<String> roles = Set.of();
      if (authentication != null && authentication.isAuthenticated()) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtAuthenticationToken jwt) {
          roles = JwtPrincipalUtil.getGroups(jwt);
        }

        if (principal instanceof Jwt jwt) {
          roles = JwtPrincipalUtil.getGroups(jwt);
        }

        if (principal instanceof UserDetails userDetails) {
          roles = userDetails
              .getAuthorities()
              .stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toSet());

        }
      }
      return String.join(",", roles);
    };
  }

}