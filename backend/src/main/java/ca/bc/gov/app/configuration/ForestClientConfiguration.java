package ca.bc.gov.app.configuration;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties("ca.bc.gov.nrs")
public class ForestClientConfiguration {

  private ChesConfiguration ches;
  private FrontEndConfiguration frontend;
  private OpenMapsConfiguration openmaps;
  private OrgBookConfiguration orgbook;
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
}
