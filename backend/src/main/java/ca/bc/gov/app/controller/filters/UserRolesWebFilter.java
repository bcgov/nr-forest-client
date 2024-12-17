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

@Component
@Slf4j
@Order(-2)
public class UserRolesWebFilter extends ContextPropagatorWebFilter {

  @Override
  protected String getContextKey() {
    return ApplicationConstant.MDC_USERROLES;
  }

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
