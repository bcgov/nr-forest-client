package ca.bc.gov.app.configuration;


import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * The Forest client configuration. This file is a representation of the yml/properties file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties("ca.bc.gov.nrs")
public class ForestClientConfiguration {

  @NestedConfigurationProperty
  private ProcessorConfiguration processor;

  @NestedConfigurationProperty
  private BackendConfiguration backend;

  @NestedConfigurationProperty
  private BackendConfiguration legacy;

  @NestedConfigurationProperty
  private BcRegistryConfiguration bcregistry;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProcessorConfiguration {
    private Duration poolTime;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BackendConfiguration {
    private String uri;
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

}
