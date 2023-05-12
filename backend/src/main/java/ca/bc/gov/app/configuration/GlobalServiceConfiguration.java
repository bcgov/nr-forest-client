package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultsDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryIdentificationDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientCodeTypeDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientDetailsAddressDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * <p><b>Global Service Configuration</b></p>
 * This class is responsible for configuring basic beans to be used by the services.
 * It creates and holds the external API webclients and the cors filter.
 */
@Configuration
@Slf4j
@RegisterReflectionForBinding({
    ValidationError.class,
    ClientAddressDto.class,
    ClientBusinessInformationDto.class,
    ClientCodeTypeDto.class,
    ClientContactDto.class,
    ClientDetailsAddressDto.class,
    ClientLocationDto.class,
    ClientLookUpDto.class,
    ClientNameCodeDto.class,
    ClientSubmissionDto.class,
    ClientValueTextDto.class,
    BcRegistryAddressDto.class,
    BcRegistryBusinessAdressesDto.class,
    BcRegistryBusinessDto.class,
    BcRegistryFacetResponseDto.class,
    BcRegistryFacetSearchResultEntryDto.class,
    BcRegistryFacetSearchResultsDto.class,
    BcRegistryIdentificationDto.class,
    ClientDetailsDto.class
})
public class GlobalServiceConfiguration {

  /**
   * Returns a configured instance of WebClient to communicate with the CHES API
   * based on the provided configuration.
   *
   * @param configuration the ForestClientConfiguration containing the CHES API base URI
   * @return a WebClient instance configured with the CHES API base URI
   */
  @Bean
  public WebClient chesApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getChes().getUri()).build();
  }

  /**
   * Creates a WebClient instance for making HTTP requests to the OpenMaps API based on the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration the configuration object for the Forest client
   * @return a WebClient instance configured for the OpenMaps API
   */
  @Bean
  public WebClient openMapsApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getOpenmaps().getUri()).build();
  }

  /**
   * Returns a configured instance of WebClient for accessing the BC Registry API.
   *
   * @param configuration The configuration for the ForestClient.
   * @return A configured instance of WebClient for accessing the BC Registry API.
   */
  @Bean
  public WebClient bcRegistryApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getBcregistry().getUri())
        .defaultHeader("x-apikey", configuration.getBcregistry().getApiKey())
        .defaultHeader("Account-Id", configuration.getBcregistry().getAccountId())
        .build();
  }

  /**
   * Creates a WebClient instance for making HTTP requests to the Oracle Legacy API
   * based on the provided {@link ForestClientConfiguration}.
   *
   * @param configuration the configuration object for the Forest client
   * @return a WebClient instance configured for the Oracle Legacy API
   */
  @Bean
  public WebClient legacyApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getLegacy().getUrl())
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

}
