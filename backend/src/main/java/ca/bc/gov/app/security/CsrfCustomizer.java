package ca.bc.gov.app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.stereotype.Component;

/**
 * This class is a customizer for CSRF (Cross-Site Request Forgery) protection in a Spring Security context.
 * It is annotated with @Component, meaning it is a Spring-managed bean and will be automatically detected and instantiated by Spring.
 * It is also annotated with @RequiredArgsConstructor, a Lombok annotation that generates a constructor requiring all final fields.
 * In this case, the constructor will require an instance of CookieCsrfRequestAttributeHandler.
 */
@Component
@RequiredArgsConstructor
public class CsrfCustomizer implements Customizer<CsrfSpec> {

  /**
   * This is a final field of type CookieCsrfRequestAttributeHandler.
   * It is used to handle CSRF token requests.
   */
  private final CookieCsrfRequestAttributeHandler cookieCsrfRequestAttributeHandler;

  /**
   * This method customizes the CSRF protection settings.
   * It sets the CSRF token repository to a CookieServerCsrfTokenRepository with HTTP only set to false.
   * This means that the CSRF token will be stored in a cookie that can be accessed by client-side scripts.
   * It also sets the CSRF token request handler to the CookieCsrfRequestAttributeHandler instance provided in the constructor.
   *
   * @param csrfSpec The CSRF specification to be customized.
   */
  // Suppress SonarQube warning about HttpOnly=false for CSRF cookie.
  // This is intentional: the front-end needs to read the CSRF token from the cookie.
  @SuppressWarnings("java:S3330")
  @Override
  public void customize(CsrfSpec csrfSpec) {
    CookieServerCsrfTokenRepository repo = 
        CookieServerCsrfTokenRepository.withHttpOnlyFalse();
    
    repo.setCookieCustomizer(cookie -> {
      cookie.sameSite("Lax");
      cookie.secure(true);
      cookie.path("/");
    });
    
    csrfSpec
        .csrfTokenRepository(repo)
        .csrfTokenRequestHandler(cookieCsrfRequestAttributeHandler);
  }

}