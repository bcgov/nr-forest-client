package ca.bc.gov.app.configuration;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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
  private BcRegistryConfiguration bcregistry;
  @NestedConfigurationProperty
  private AddressCompleteConfiguration addressComplete;
  @NestedConfigurationProperty
  private SecurityConfiguration security;

  private Duration submissionLimit;

  /**
   * The Common hosted email service configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChesConfiguration {

    private String uri;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String scope;
    private List<String> copyEmail;
  }

  /**
   * The Front end configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FrontEndConfiguration {

    private String url;
    @NestedConfigurationProperty
    private FrontEndCorsConfiguration cors;

  }

  /**
   * The Front end cors configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FrontEndCorsConfiguration {

    private List<String> headers;
    private List<String> methods;
    private Duration age;
  }

  /**
   * The BC Registry configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BcRegistryConfiguration {

    private String uri;
    private String apiKey;
    private String accountId;
  }

  /**
   * The Legacy service (AKA Oracle) configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LegacyConfiguration {

    private String url;
  }

  /**
   * The Address Complete service from Canada Post configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AddressCompleteConfiguration {

    private String url;
    private String apiKey;
  }

  /**
   * The Security / Authentication configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SecurityConfiguration {

    private String region;
    private String userPool;
    private String environment;
    private List<NameSecretDto> serviceAccounts;

    public String getDomainUrl(){
      return String.format("https://cognito-idp.%s.amazonaws.com/", region);
    }

    public String getJwksUrl() {
      return String.format("%s%s/.well-known/jwks.json", getDomainUrl(), userPool);
    }
  }

  public record NameSecretDto(String name, String secret) {}
}
