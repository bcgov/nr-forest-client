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
  private OpenMapsConfiguration openmaps;
  @NestedConfigurationProperty
  private OrgBookConfiguration orgbook;
  @NestedConfigurationProperty
  private LegacyConfiguration legacy;
  @NestedConfigurationProperty
  private BcRegistryConfiguration bcregistry;
  @NestedConfigurationProperty
  private AddressCompleteConfiguration addressComplete;
  @NestedConfigurationProperty
  private CognitoConfiguration cognito;

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

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FrontEndConfiguration {
    private String url;
    @NestedConfigurationProperty
    private FrontEndCorsConfiguration cors;

  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FrontEndCorsConfiguration {
    private List<String> headers;
    private List<String> methods;
    private Duration age;
  }


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OpenMapsConfiguration {
    private String uri;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrgBookConfiguration {
    private String uri;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BcRegistryConfiguration {
    private String uri;
    private String apiKey;
    private String accountId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LegacyConfiguration {
    private String url;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AddressCompleteConfiguration {
    private String url;
    private String apiKey;
  }

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

    public String getUrl(){
      if(StringUtils.isNotBlank(this.url)){
        return this.url;
      }
      return "https://"+domain+".auth."+region+".amazoncognito.com";
    }
  }
}
