package ca.bc.gov.app.configuration;

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
public class LegacyConfiguration {

  private ForestClientConfiguration forest;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ForestClientConfiguration {

    private String uri;
  }
}
