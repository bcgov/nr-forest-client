package ca.bc.gov.app.configuration;

import java.net.URI;
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
@ConfigurationProperties("ca.bc.gov.nrs.ches")
public class ChesConfiguration {
  private String uri;
  private String tokenUrl;
  private String clientId;
  private String clientSecret;
  private String scope;
}
