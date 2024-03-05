package ca.bc.gov.app.security;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.HeaderSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.HeaderSpec.XssProtectionSpec;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForestHeadersCustomizer implements Customizer<HeaderSpec> {

  @Value("${ca.bc.gov.nrs.self-uri}")
  String selfUri;

  @Override
  public void customize(HeaderSpec headerSpec) {

    String policyDirectives = String.join("; ",
        "default-src 'none'",
        "connect-src 'self' " + selfUri,
        "script-src 'strict-dynamic' 'unsafe-inline' 'nonce-" + UUID.randomUUID()
        + "' http: https:",
        "object-src 'none'",
        "base-uri 'none'",
        "require-trusted-types-for 'script'",
        "report-uri " + selfUri
    );

    headerSpec
        .frameOptions(frameOptionsSpec -> frameOptionsSpec.mode(Mode.DENY))
        .contentSecurityPolicy(
            contentSecurityPolicySpec -> contentSecurityPolicySpec.policyDirectives(
                policyDirectives))
        .hsts(hstsSpec -> hstsSpec.maxAge(Duration.ofDays(30)).includeSubdomains(true))
        .xssProtection(XssProtectionSpec::disable)
        .contentTypeOptions(Customizer.withDefaults())
        .referrerPolicy(referrerPolicySpec -> referrerPolicySpec.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        .permissionsPolicy(permissionsPolicySpec -> permissionsPolicySpec.policy(
            "geolocation=(), microphone=(), camera=(), speaker=(), usb=(), bluetooth=(), payment=(), interest-cohort=()"))
    ;
  }
}
