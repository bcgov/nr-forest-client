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
    log.warn("Multi address feature is disabled due to property set as false");
    return addressDto -> addressDto.addressType().equalsIgnoreCase("mailing");
  }

  @Bean
  @ConditionalOnProperty(name = "features.bcregistry.multiaddress", matchIfMissing = true)
  public Predicate<BcRegistryAddressDto> isMultiAddressEnabled() {
    log.warn("Multi address feature is enabled");
    return addressDto -> true;
  }


  @Bean
  @ConditionalOnProperty(prefix = "features", name = "staff.match", matchIfMissing = true)
  public Predicate<ClientSubmissionDto> isMatcherDisabled() {
    log.warn("Staff match feature is enabled");
    return dto -> true;
  }

  @Bean
  @ConditionalOnProperty(prefix = "features", name = "staff.match", havingValue = "false")
  public Predicate<ClientSubmissionDto> isMatcherDisabledByValue() {
    log.warn("Staff match feature is disabled due to property set as false");
    return dto -> false;
  }

}
