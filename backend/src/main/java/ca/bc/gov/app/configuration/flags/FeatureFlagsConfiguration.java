package ca.bc.gov.app.configuration.flags;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeatureFlagsConfiguration {

  @Bean
  @ConditionalOnProperty(name = "features.bcregistry.multiaddress", havingValue = "false")
  public Predicate<BcRegistryAddressDto> isMultiAddressDisabled() {
    return addressDto -> addressDto.addressType().equalsIgnoreCase("mailing");
  }

  @Bean
  @ConditionalOnProperty(name = "features.bcregistry.multiaddress", matchIfMissing = true)
  public Predicate<BcRegistryAddressDto> isMultiAddressEnabled() {
    return addressDto -> true;
  }

  @Bean
  @ConditionalOnProperty(name = "features.staff.match", havingValue = "true")
  public Predicate<ClientSubmissionDto> isMatcherEnabled() {
    return dto -> true;
  }

  @Bean
  @ConditionalOnProperty(name = "features.staff.match", matchIfMissing = true)
  public Predicate<ClientSubmissionDto> isMatcherDisabled() {
    return dto -> false;
  }


}
