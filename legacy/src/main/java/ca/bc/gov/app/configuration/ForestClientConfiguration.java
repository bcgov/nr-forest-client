package ca.bc.gov.app.configuration;

import java.time.Duration;
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
  private LegacyDataConfiguration data;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LegacyDataConfiguration {

    private Duration predictiveCap;
  }

}
