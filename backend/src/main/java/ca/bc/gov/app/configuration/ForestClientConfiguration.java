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
}
