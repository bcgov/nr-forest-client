package ca.bc.gov.app.configuration;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
  private CognitoConfiguration cognito;

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
   * The AWS Cognito configuration.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CognitoConfiguration {

    private String region;
    private String clientId;
    private String userPool;
    private String domain;
    private String url;
    private String environment;
    private String redirectUri;
    private String logoutUri;
    private String cookieDomain;

    /**
     * Gets the cognito URL.
     *
     * @return A string representing the cognito URL.
     */
    public String getUrl() {
      if (StringUtils.isNotBlank(this.url)) {
        return this.url;
      }
      return "https://" + domain + ".auth." + region + ".amazoncognito.com";
    }
  }
}
