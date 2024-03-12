package ca.bc.gov.app.security;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.HeaderSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HeaderSpec.XssProtectionSpec;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.stereotype.Component;
/**
 * This class is a customizer for HTTP headers in a Spring Boot application.
 * It is annotated with @Component, which means it is a Spring Bean and will be automatically instantiated by Spring.
 * It implements the Customizer interface with a type parameter of HeaderSpec, which is a specification for HTTP headers in Spring Security.
 */
@Component
@RequiredArgsConstructor
public class HeadersCustomizer implements Customizer<HeaderSpec> {

  /**
   * The self URI of the application, which is injected from the application properties.
   */
  @Value("${ca.bc.gov.nrs.self-uri}")
  String selfUri;

  /**
   * The environment of the application, which is injected from the application properties.
   * The default value is "PROD".
   */
  @Value("${ca.bc.gov.nrs.security.environment:PROD}")
  String environment;

  /**
   * This method customizes the HTTP headers for the application.
   * It sets various security-related headers such as Content-Security-Policy, Strict-Transport-Security, X-XSS-Protection, X-Content-Type-Options, Referrer-Policy, and Permissions-Policy.
   *
   * @param headerSpec The specification for the HTTP headers.
   */
  @Override
  public void customize(HeaderSpec headerSpec) {

    // Define the policy directives for the Content-Security-Policy header.
    String policyDirectives = String.join("; ",
        "default-src 'none'",
        "connect-src 'self' " + selfUri,
        "script-src 'strict-dynamic' 'nonce-" + UUID.randomUUID()
        + "' " + ( "local".equalsIgnoreCase(environment) ? "http: " : StringUtils.EMPTY) + "https:",
        "object-src 'none'",
        "base-uri 'none'",
        "frame-ancestors 'none'",
        "require-trusted-types-for 'script'",
        "report-uri " + selfUri
    );

    // Customize the HTTP headers.
    headerSpec
        .frameOptions(frameOptionsSpec -> frameOptionsSpec.mode(Mode.DENY)) // Set the X-Frame-Options header to "DENY".
        .contentSecurityPolicy(
            contentSecurityPolicySpec -> contentSecurityPolicySpec.policyDirectives(
                policyDirectives)) // Set the Content-Security-Policy header.
        .hsts(hstsSpec -> hstsSpec.maxAge(Duration.ofDays(30)).includeSubdomains(true)) // Set the Strict-Transport-Security header.
        .xssProtection(XssProtectionSpec::disable) // Disable the X-XSS-Protection header.
        .contentTypeOptions(Customizer.withDefaults()) // Set the X-Content-Type-Options header to its default value.
        .referrerPolicy(referrerPolicySpec -> referrerPolicySpec.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)) // Set the Referrer-Policy header.
        .permissionsPolicy(permissionsPolicySpec -> permissionsPolicySpec.policy(
            "geolocation=(), microphone=(), camera=(), speaker=(), usb=(), bluetooth=(), payment=(), interest-cohort=()")) // Set the Permissions-Policy header.
    ;
  }
}