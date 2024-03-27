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
public class ApiAuthorizationCustomizer implements Customizer<AuthorizeExchangeSpec> {

  /**
   * This method customizes the AuthorizeExchangeSpec by setting the authorization rules for different API endpoints.
   * The rules specify which roles can access which endpoints.
   *
   * @param authorize The AuthorizeExchangeSpec to be customized.
   */
  @Override
  public void customize(AuthorizeExchangeSpec authorize) {
    // Begin authorization rules configuration
authorize

    // Allow all access to metrics and health endpoints
    .pathMatchers("/metrics/**", "/health/**").permitAll()

    // Only service users can access the email endpoint
    .pathMatchers("/api/ches/email")
    .hasAnyRole(
        ApplicationConstant.USERTYPE_SERVICE_USER
    )

    // Only BCeIdBusiness and BCSC users can access the duplicate endpoint
    .pathMatchers("/api/ches/duplicate")
    .hasAnyRole(
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER
    )

    // Only BCeIdBusiness and BCSC users can access the addresses endpoint
    .pathMatchers("/api/addresses/**")
    .hasAnyRole(
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER
    )

    // Viewer, editor, admin, BCeIdBusiness and BCSC users can access the codes endpoint
    .pathMatchers("/api/codes/**")
    .hasAnyRole(
        ApplicationConstant.ROLE_VIEWER,
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN,
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER
    )

    // Viewer, editor, admin, BCeIdBusiness, BCSC and service users can access the districts endpoint
    .pathMatchers("/api/districts/**")
    .hasAnyRole(
        ApplicationConstant.ROLE_VIEWER,
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN,
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER,
        ApplicationConstant.USERTYPE_SERVICE_USER
    )

    // Viewer, editor, admin, BCeIdBusiness, BCSC and service users can access the countries endpoint
    .pathMatchers("/api/countries/**")
    .hasAnyRole(
        ApplicationConstant.ROLE_VIEWER,
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN,
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER,
        ApplicationConstant.USERTYPE_SERVICE_USER
    )

    // Only editor and admin can POST to the clients submissions endpoint with a specific id
    .pathMatchers(HttpMethod.POST,"/api/clients/submissions/{id:[0-9]+}")
    .hasAnyRole(
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN
    )

    // Viewer, editor and admin can access the clients submissions endpoint with a specific id
    .pathMatchers("/api/clients/submissions/{id:[0-9]+}")
    .hasAnyRole(
        ApplicationConstant.ROLE_VIEWER,
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN
    )

    // Only BCeIdBusiness and BCSC users can POST to the clients submissions endpoint
    .pathMatchers(HttpMethod.POST, "/api/clients/submissions/**")
    .hasAnyRole(
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER
    )

    // Viewer, editor and admin can GET from the clients submissions endpoint
    .pathMatchers(HttpMethod.GET, "/api/clients/submissions/**")
    .hasAnyRole(
        ApplicationConstant.ROLE_VIEWER,
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN
    )

    // BCeIdBusiness, BCSC, viewer, editor and admin users can access the clients endpoint
    .pathMatchers("/api/clients/**")
    .hasAnyRole(
        ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
        ApplicationConstant.USERTYPE_BCSC_USER,
        ApplicationConstant.ROLE_VIEWER,
        ApplicationConstant.ROLE_EDITOR,
        ApplicationConstant.ROLE_ADMIN
    )

    // Deny all other requests
    .anyExchange().denyAll();

  }
}