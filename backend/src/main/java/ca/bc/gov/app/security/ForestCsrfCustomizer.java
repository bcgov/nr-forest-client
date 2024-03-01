package ca.bc.gov.app.security;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.stereotype.Component;


/**
 * This class customizes the CSRF (Cross-Site Request Forgery) protection for the application.
 * It implements the Customizer interface and overrides the customize method to set the CSRF token repository.
 * The CSRF token repository is set to a CookieServerCsrfTokenRepository with HttpOnly set to false.
 * This means that the CSRF token is stored in a cookie that can be accessed by client-side scripts.
 */
@Component
public class ForestCsrfCustomizer implements Customizer<CsrfSpec> {

  /**
   * This method customizes the CSRF protection by setting the CSRF token repository.
   * The CSRF token repository is set to a CookieServerCsrfTokenRepository with HttpOnly set to false.
   * This allows the CSRF token to be accessed by client-side scripts.
   *
   * @param csrfSpec The CSRF specification to be customized.
   */
  @Override
  public void customize(CsrfSpec csrfSpec) {
    csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
  }
}
