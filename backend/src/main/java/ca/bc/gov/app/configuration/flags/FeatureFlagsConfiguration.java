package ca.bc.gov.app.configuration.flags;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
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
    log.info("Feature :: Multi address is disabled");
    return addressDto -> addressDto.addressType().equalsIgnoreCase("mailing");
  }

  @Bean
  @ConditionalOnProperty(name = "features.bcregistry.multiaddress", matchIfMissing = true)
  public Predicate<BcRegistryAddressDto> isMultiAddressEnabled() {
    return addressDto -> true;
  }

}
