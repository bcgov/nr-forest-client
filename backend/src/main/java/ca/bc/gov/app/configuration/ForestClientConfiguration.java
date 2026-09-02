package ca.bc.gov.app.configuration;

import java.time.Duration;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * The Forest client configuration.
 * This file is a representation of the yml/properties file
 */
@Data
@Builder
@NoArgsConstructor
@Component
@ConfigurationProperties("ca.bc.gov.nrs")
public class ForestClientConfiguration {

  @NestedConfigurationProperty
  private ChesConfiguration ches;
  @NestedConfigurationProperty
  private FrontEndConfiguration frontend;
  @NestedConfigurationProperty
  private LegacyConfiguration legacy;
  @NestedConfigurationProperty
  private LegacyConfiguration processor;
  @NestedConfigurationProperty
  private BcRegistryConfiguration bcregistry;
  @NestedConfigurationProperty
  private AddressCompleteConfiguration addressComplete;
  @NestedConfigurationProperty
  private SecurityConfiguration security;
  private Duration submissionLimit;
  private Duration idirSubmissionTimeWindow;
  private int idirMaxSubmissions;
  private Duration otherSubmissionTimeWindow;
  private int otherMaxSubmissions;
  @NestedConfigurationProperty
  private OpenDataConfiguration openData;

  public ForestClientConfiguration(
      ChesConfiguration ches,
      FrontEndConfiguration frontend,
      LegacyConfiguration legacy,
      LegacyConfiguration processor,
      BcRegistryConfiguration bcregistry,
      AddressCompleteConfiguration addressComplete,
      SecurityConfiguration security,
      Duration submissionLimit,
      Duration idirSubmissionTimeWindow,
      int idirMaxSubmissions,
      Duration otherSubmissionTimeWindow,
      int otherMaxSubmissions,
      OpenDataConfiguration openData) {
    this.ches = ches;
    this.frontend = frontend;
    this.legacy = legacy;
    this.processor = processor;
    this.bcregistry = bcregistry;
    this.addressComplete = addressComplete;
    this.security = security;
    this.submissionLimit = submissionLimit;
    this.idirSubmissionTimeWindow = idirSubmissionTimeWindow;
    this.idirMaxSubmissions = idirMaxSubmissions;
    this.otherSubmissionTimeWindow = otherSubmissionTimeWindow;
    this.otherMaxSubmissions = otherMaxSubmissions;
    this.openData = openData;
  }

  /**
   * The Common hosted email service configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class ChesConfiguration {

    private String uri;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String scope;
    private List<String> copyEmail;

    public ChesConfiguration(
        String uri,
        String tokenUrl,
        String clientId,
        String clientSecret,
        String scope,
        List<String> copyEmail) {
      this.uri = uri;
      this.tokenUrl = tokenUrl;
      this.clientId = clientId;
      this.clientSecret = clientSecret;
      this.scope = scope;
      this.copyEmail = copyEmail;
    }
  }

  /**
   * The Front end configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class FrontEndConfiguration {

    private String url;
    @NestedConfigurationProperty
    private FrontEndCorsConfiguration cors;

    public FrontEndConfiguration(String url, FrontEndCorsConfiguration cors) {
      this.url = url;
      this.cors = cors;
    }
  }

  /**
   * The Front end cors configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class FrontEndCorsConfiguration {

    private List<String> headers;
    private List<String> methods;
    private Duration age;

    public FrontEndCorsConfiguration(List<String> headers, List<String> methods, Duration age) {
      this.headers = headers;
      this.methods = methods;
      this.age = age;
    }
  }

  /**
   * The BC Registry configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class BcRegistryConfiguration {

    private String uri;
    private String apiKey;
    private String accountId;

    public BcRegistryConfiguration(String uri, String apiKey, String accountId) {
      this.uri = uri;
      this.apiKey = apiKey;
      this.accountId = accountId;
    }
  }

  /**
   * The Legacy service (AKA Oracle) configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class LegacyConfiguration {

    private String url;

    public LegacyConfiguration(String url) {
      this.url = url;
    }
  }

  /**
   * The Address Complete service from Canada Post configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class AddressCompleteConfiguration {

    private String url;
    private String apiKey;

    public AddressCompleteConfiguration(String url, String apiKey) {
      this.url = url;
      this.apiKey = apiKey;
    }
  }

  /**
   * The Security / Authentication configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class SecurityConfiguration {

    private String region;
    private String userPool;
    private String environment;
    private List<NameSecretDto> serviceAccounts;

    public SecurityConfiguration(
        String region, String userPool, String environment, List<NameSecretDto> serviceAccounts) {
      this.region = region;
      this.userPool = userPool;
      this.environment = environment;
      this.serviceAccounts = serviceAccounts;
    }

    public String getDomainUrl() {
      return String.format("https://cognito-idp.%s.amazonaws.com/", region);
    }

    public String getJwksUrl() {
      return String.format("%s%s/.well-known/jwks.json", getDomainUrl(), userPool);
    }
  }

  /**
   * The Open Data configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  public static class OpenDataConfiguration {

    private String sacBandUrl;
    private String sacTribeUrl;
    private String openMapsBandUrl;
    private String openMapsTribeUrl;

    public OpenDataConfiguration(
        String sacBandUrl, String sacTribeUrl, String openMapsBandUrl, String openMapsTribeUrl) {
      this.sacBandUrl = sacBandUrl;
      this.sacTribeUrl = sacTribeUrl;
      this.openMapsBandUrl = openMapsBandUrl;
      this.openMapsTribeUrl = openMapsTribeUrl;
    }
  }

  public record NameSecretDto(String name, String secret) {}
}
