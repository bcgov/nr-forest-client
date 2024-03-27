package ca.bc.gov.app.security;

import ca.bc.gov.app.ApplicationConstant;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.stereotype.Component;

/**
 * This class customizes the authorization rules for different API endpoints. It implements the
 * Customizer interface and overrides the customize method to set the authorization rules.
 */
@Component
public class ApiAuthorizationCustomizer implements Customizer<AuthorizeExchangeSpec> {

  /**
   * This method customizes the AuthorizeExchangeSpec by setting the authorization rules for
   * different API endpoints. The rules specify which roles can access which endpoints.
   *
   * @param authorize The AuthorizeExchangeSpec to be customized.
   */
  @Override
  public void customize(AuthorizeExchangeSpec authorize) {
    // Begin authorization rules configuration

    // Allow all access to metrics and health endpoints
    // This is due to the internal platform checks that require access to these endpoints
    authorize.pathMatchers(HttpMethod.GET, "/metrics/**", "/health/**").permitAll();

    // Only service users can POST to the email endpoint
    authorize.pathMatchers(HttpMethod.POST, "/api/ches/email")
        .hasAnyRole(ApplicationConstant.USERTYPE_SERVICE_USER);

    // Only service users can send OPTIONS request to the email endpoint
    authorize.pathMatchers(HttpMethod.OPTIONS, "/api/ches/email")
        .hasAnyRole(ApplicationConstant.USERTYPE_SERVICE_USER);

    // Only BCeIDBusiness and BCSC users can POST to the duplicate endpoint
    authorize.pathMatchers(HttpMethod.POST, "/api/ches/duplicate")
        .hasAnyRole(ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER);

    // Only BCeIDBusiness and BCSC users can send OPTIONS request to the duplicate endpoint
    authorize.pathMatchers(HttpMethod.OPTIONS, "/api/ches/duplicate")
        .hasAnyRole(ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER);

    // Only BCeIDBusiness and BCSC users can GET from the addresses endpoint
    authorize.pathMatchers(HttpMethod.GET, "/api/addresses/**")
        .hasAnyRole(ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER);

    // Viewer, editor, admin, BCeIDBusiness and BCSC users can GET from the codes endpoint
    authorize.pathMatchers(HttpMethod.GET, "/api/codes/**")
        .hasAnyRole(ApplicationConstant.ROLE_VIEWER, ApplicationConstant.ROLE_EDITOR,
            ApplicationConstant.ROLE_ADMIN, ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER);

    // Viewer, editor, admin, BCeIDBusiness, BCSC and service users can GET from the districts endpoint
    authorize.pathMatchers(HttpMethod.GET, "/api/districts/**")
        .hasAnyRole(ApplicationConstant.ROLE_VIEWER, ApplicationConstant.ROLE_EDITOR,
            ApplicationConstant.ROLE_ADMIN, ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER, ApplicationConstant.USERTYPE_SERVICE_USER);

    // Viewer, editor, admin, BCeIDBusiness, BCSC and service users can GET from the countries endpoint
    authorize.pathMatchers(HttpMethod.GET, "/api/countries/**")
        .hasAnyRole(ApplicationConstant.ROLE_VIEWER, ApplicationConstant.ROLE_EDITOR,
            ApplicationConstant.ROLE_ADMIN, ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER, ApplicationConstant.USERTYPE_SERVICE_USER);

    // Only editor and admin can POST to the clients submissions endpoint with a specific id
    authorize.pathMatchers(HttpMethod.POST, "/api/clients/submissions/{id:[0-9]+}")
        .hasAnyRole(ApplicationConstant.ROLE_EDITOR, ApplicationConstant.ROLE_ADMIN);

    // Viewer, editor and admin can GET from the clients submissions endpoint with a specific id
    authorize.pathMatchers(HttpMethod.GET, "/api/clients/submissions/{id:[0-9]+}")
        .hasAnyRole(ApplicationConstant.ROLE_VIEWER, ApplicationConstant.ROLE_EDITOR,
            ApplicationConstant.ROLE_ADMIN);

    // Only BCeIDBusiness and BCSC users can POST to the clients submissions endpoint
    authorize.pathMatchers(HttpMethod.POST, "/api/clients/submissions/**")
        .hasAnyRole(ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER);

    // Viewer, editor and admin can GET from the clients submissions endpoint
    authorize.pathMatchers(HttpMethod.GET, "/api/clients/submissions/**")
        .hasAnyRole(ApplicationConstant.ROLE_VIEWER, ApplicationConstant.ROLE_EDITOR,
            ApplicationConstant.ROLE_ADMIN);

    // BCeIDBusiness, BCSC, viewer, editor and admin users can GET from the clients endpoint
    authorize.pathMatchers(HttpMethod.GET, "/api/clients/**")
        .hasAnyRole(ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER,
            ApplicationConstant.USERTYPE_BCSC_USER, ApplicationConstant.ROLE_VIEWER,
            ApplicationConstant.ROLE_EDITOR, ApplicationConstant.ROLE_ADMIN);

    // Deny all other requests
    authorize.anyExchange().denyAll();
  }
}