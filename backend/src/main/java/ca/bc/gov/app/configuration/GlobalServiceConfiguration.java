package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessRequestDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessTypeDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestBodyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryExceptionMessageDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultsDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryIdentificationDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.ches.ChesMailErrorResponse;
import ca.bc.gov.app.dto.ches.ChesMailRequest;
import ca.bc.gov.app.dto.ches.ChesMailResponse;
import ca.bc.gov.app.dto.ches.ChesRequestDto;
import ca.bc.gov.app.dto.ches.CommonExposureJwtDto;
import ca.bc.gov.app.dto.client.AddressCompleteFindDto;
import ca.bc.gov.app.dto.client.AddressCompleteFindListDto;
import ca.bc.gov.app.dto.client.AddressCompleteRetrieveDto;
import ca.bc.gov.app.dto.client.AddressCompleteRetrieveListDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * <p><b>Global Service Configuration</b></p>
 * This class is responsible for configuring basic beans to be used by the services. It creates and
 * holds the external API webclients and the cors filter.
 */
@Configuration
@Slf4j
@RegisterReflectionForBinding({
    ValidationError.class,
    AddressCompleteFindDto.class,
    AddressCompleteFindListDto.class,
    AddressCompleteRetrieveDto.class,
    AddressCompleteRetrieveListDto.class,
    ClientAddressDto.class,
    ClientBusinessInformationDto.class,
    ClientContactDto.class,
    ClientLocationDto.class,
    ClientLookUpDto.class,
    ClientSubmissionDto.class,
    ClientValueTextDto.class,
    CodeNameDto.class,
    BcRegistryAddressDto.class,
    BcRegistryBusinessAdressesDto.class,
    BcRegistryBusinessDto.class,
    BcRegistryFacetResponseDto.class,
    BcRegistryFacetSearchResultEntryDto.class,
    BcRegistryFacetSearchResultsDto.class,
    BcRegistryIdentificationDto.class,
    BcRegistryExceptionMessageDto.class,
    ClientDetailsDto.class,
    CommonExposureJwtDto.class,
    ChesRequestDto.class,
    ChesMailRequest.class,
    ChesMailResponse.class,
    ChesMailErrorResponse.class,
    BcRegistryDocumentRequestBodyDto.class,
    BcRegistryDocumentAccessRequestDto.class,
    BcRegistryDocumentAccessTypeDto.class,
    BcRegistryDocumentRequestResponseDto.class,
    BcRegistryDocumentDto.class,
    BcRegistryDocumentRequestDocumentDto.class,
    BcRegistryBusinessDto.class,
    BcRegistryOfficesDto.class,
    BcRegistryPartyDto.class,
    BcRegistryAddressDto.class,
    BcRegistryBusinessAdressesDto.class,
    BcRegistryOfficerDto.class,
    BcRegistryRoleDto.class,
    ForestClientDto.class
})
public class GlobalServiceConfiguration {

  /**
   * Returns a configured instance of WebClient to communicate with the CHES API based on the
   * provided configuration.
   *
   * @param configuration the ForestClientConfiguration containing the CHES API base URI
   * @return a WebClient instance configured with the CHES API base URI
   */
  @Bean
  public WebClient chesApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getChes().getUri()).build();
  }

  /**
   * Creates a WebClient instance for making HTTP requests to the CHES Auth API on the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration the configuration object for the Forest client
   * @return a WebClient instance configured for the CHES Auth API
   */
  @Bean
  public WebClient authApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getChes().getTokenUrl())
        .filter(
            ExchangeFilterFunctions
                .basicAuthentication(
                    configuration.getChes().getClientId(),
                    configuration.getChes().getClientSecret()
                )
        )
        .build();
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
   * Creates a WebClient instance for making HTTP requests to the Oracle Legacy API based on the
   * provided {@link ForestClientConfiguration}.
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

  /**
   * Returns a configured instance of WebClient for accessing the Canada Post AddressComplete API.
   *
   * @param configuration The configuration for the ForestClient.
   * @return A configured instance of WebClient for accessing the AddressComplete API.
   */
  @Bean
  public WebClient addressCompleteApi(ForestClientConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getAddressComplete().getUrl()).build();
  }

  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.build();
  }
}
