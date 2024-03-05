package ca.bc.gov.app.security;

import ca.bc.gov.app.ApplicationConstant;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.stereotype.Component;

/**
 * This class customizes the authorization rules for different API endpoints.
 * It implements the Customizer interface and overrides the customize method to set the authorization rules.
 */
@Component
public class ForestExchangeCustomizer implements Customizer<AuthorizeExchangeSpec> {

  /**
   * This method customizes the AuthorizeExchangeSpec by setting the authorization rules for different API endpoints.
   * The rules specify which roles can access which endpoints.
   *
   * @param authorize The AuthorizeExchangeSpec to be customized.
   */
  @Override
  public void customize(AuthorizeExchangeSpec authorize) {
    authorize
        // Metrics and health endpoints are open to all
        .pathMatchers("/metrics/**", "/health/**").permitAll()

        // Only service users can access the email endpoint
        .pathMatchers("/api/ches/email")
        .hasAnyRole(
            ApplicationConstant.ROLE_SERVICE_USER
        )
        // Only BCEID business users and BCSC users can access the duplicate endpoint
        .pathMatchers("/api/ches/duplicate")
        .hasAnyRole(
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER
        )
        // Only BCEID business users and BCSC users can access the addresses endpoint
        .pathMatchers("/api/addresses/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER
        )
        // IDIR users, BCEID business users, and BCSC users can access the codes endpoint
        .pathMatchers("/api/codes/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_IDIR_USER,
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER
        )
        // IDIR users, BCEID business users, BCSC users, and service users can access the districts endpoint
        .pathMatchers("/api/districts/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_IDIR_USER,
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER,
            ApplicationConstant.ROLE_SERVICE_USER
        )
        // IDIR users, BCEID business users, BCSC users, and service users can access the countries endpoint
        .pathMatchers("/api/countries/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_IDIR_USER,
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER,
            ApplicationConstant.ROLE_SERVICE_USER
        )
        // Only IDIR users can get details and approve/reject submissions
        .pathMatchers("/api/clients/submissions/{id:[0-9]+}")
        .hasAnyRole(
            ApplicationConstant.ROLE_IDIR_USER
        )
        // Only IDIR users can access the list of submissions, and other users can create submissions
        .pathMatchers(HttpMethod.POST, "/api/clients/submissions/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER
        )
        .pathMatchers(HttpMethod.GET, "/api/clients/submissions/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_IDIR_USER
        )
        // All BCSC, BCEID, and IDIR users can access the client APIs
        .pathMatchers("/api/clients/**")
        .hasAnyRole(
            ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
            ApplicationConstant.ROLE_BCSC_USER,
            ApplicationConstant.ROLE_IDIR_USER
        )
        // All other exchanges are denied by default
        .anyExchange().denyAll();
  }
}