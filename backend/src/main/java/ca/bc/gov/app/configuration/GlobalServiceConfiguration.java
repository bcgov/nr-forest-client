package ca.bc.gov.app.configuration;

import ca.bc.gov.app.converters.ForestClientDetailsSerializerModifier;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAlternateNameDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessRequestDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessTypeDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestBodyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryExceptionMessageDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetRequestBodyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetRequestQueryDto;
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
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.client.RelatedClientDto;
import ca.bc.gov.app.dto.client.RelatedClientEntryDto;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ClientRelatedProjection;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.opendata.Crs;
import ca.bc.gov.app.dto.opendata.CrsProperties;
import ca.bc.gov.app.dto.opendata.Feature;
import ca.bc.gov.app.dto.opendata.FeatureProperties;
import ca.bc.gov.app.dto.opendata.Geometry;
import ca.bc.gov.app.dto.opendata.OpenData;
import ca.bc.gov.app.health.ManualHealthIndicator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flipkart.zjsonpatch.JsonPatch;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.database.postgresql.TransactionalModel;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

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
    ForestClientDto.class,
    OpenData.class,
    Feature.class,
    Crs.class,
    CrsProperties.class,
    Geometry.class,
    FeatureProperties.class,
    AddressSearchDto.class,
    ContactSearchDto.class,
    Crs.class,
    CrsProperties.class,
    Feature.class,
    FeatureProperties.class,
    Geometry.class,
    OpenData.class,
    MatchResult.class,
    BcRegistryAlternateNameDto.class,
    BcRegistryFacetPartyDto.class,
    BcRegistryFacetRequestBodyDto.class,
    BcRegistryFacetRequestQueryDto.class,
    JsonPatch.class,
    JsonNode.class,
    ForestClientContactDetailsDto.class,
    TransactionalModel.class,
    ClientRelatedProjection.class,
    RelatedClientEntryDto.class,
    RelatedClientDto.class
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
  public WebClient chesApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder.baseUrl(configuration.getChes().getUri()).build();
  }

  /**
   * Creates a WebClient instance for making HTTP requests to the CHES Auth API on the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration the configuration object for the Forest client
   * @return a WebClient instance configured for the CHES Auth API
   */
  @Bean
  public WebClient authApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
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
  public WebClient bcRegistryApi(
      ForestClientConfiguration configuration,
      @Qualifier("bcRegistryApiHealthIndicator") ManualHealthIndicator bcRegistryApiHealthIndicator,
      WebClient.Builder webClientBuilder
  ) {

    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMinutes(2))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(120))
            .addHandlerLast(new WriteTimeoutHandler(120)))
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofMinutes(2).toMillis());

    return webClientBuilder
        .baseUrl(configuration.getBcregistry().getUri())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .defaultHeader("x-apikey", configuration.getBcregistry().getApiKey())
        .defaultHeader("Account-Id", configuration.getBcregistry().getAccountId())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
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
  public WebClient legacyApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getLegacy().getUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Returns a configured instance of WebClient for accessing the Canada Post AddressComplete API.
   *
   * @param configuration The configuration for the ForestClient.
   * @return A configured instance of WebClient for accessing the AddressComplete API.
   */
  @Bean
  public WebClient addressCompleteApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getAddressComplete().getUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Configures and provides a WebClient for accessing the Open Data SAC API. This WebClient is
   * pre-configured with the base URL for the SAC API, as specified in the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration    The configuration containing the SAC API URL and other settings.
   * @param webClientBuilder A builder for creating WebClient instances.
   * @return A WebClient instance configured for the Open Data SAC API.
   */
  @Bean
  public WebClient openDataSacBandApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getOpenData().getSacBandUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Configures and provides a WebClient for accessing the Open Data SAC Tribe API. This WebClient
   * is pre-configured with the base URL for the SAC Tribe API, as specified in the provided
   * {@link ForestClientConfiguration}.
   *
   * @param configuration    The configuration containing the SAC Tribe API URL and other settings.
   * @param webClientBuilder A builder for creating WebClient instances.
   * @return A WebClient instance configured for the Open Data SAC Tribe API.
   */
  @Bean
  public WebClient openDataSacTribeApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getOpenData().getSacTribeUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Configures and provides a WebClient for accessing the Open Data BC Maps API. Similar to the SAC
   * API WebClient, this WebClient is configured with the base URL for the BC Maps API, as defined
   * in the {@link ForestClientConfiguration}.
   *
   * @param configuration    The configuration containing the BC Maps API URL and other necessary
   *                         settings.
   * @param webClientBuilder A builder for creating WebClient instances.
   * @return A WebClient instance configured for the Open Data BC Maps API.
   */
  @Bean
  public WebClient openDataBcMapsBandApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getOpenData().getOpenMapsBandUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Configures and provides a WebClient for accessing the Open Data BC Maps Tribe API. This
   * WebClient is pre-configured with the base URL for the BC Maps Tribe API, as specified in the
   * provided {@link ForestClientConfiguration}.
   *
   * @param configuration    The configuration containing the BC Maps Tribe API URL and other
   *                         settings.
   * @param webClientBuilder A builder for creating WebClient instances.
   * @return A WebClient instance configured for the Open Data BC Maps Tribe API.
   */
  @Bean
  public WebClient openDataBcMapsTribeApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getOpenData().getOpenMapsTribeUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Configures and provides a {@link WebClient} instance for making HTTP requests to the Processor
   * API. This method utilizes the {@link ForestClientConfiguration} to retrieve the Processor API's
   * base URL and configures a {@link WebClient.Builder} with this URL. The configured
   * {@link WebClient} is then built and returned. This WebClient can be used throughout the
   * application to interact with the Processor API, facilitating operations such as sending
   * requests and receiving responses.
   *
   * @param configuration    The {@link ForestClientConfiguration} containing the Processor API's
   *                         configuration details.
   * @param webClientBuilder A pre-configured {@link WebClient.Builder} for creating WebClient
   *                         instances.
   * @return A {@link WebClient} instance ready for interacting with the Processor API.
   */
  @Bean
  public WebClient processorApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getProcessor().getUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity")
        .build();
  }

  /**
   * Configures and provides an ObjectMapper bean. This ObjectMapper is built using the provided
   * Jackson2ObjectMapperBuilder and is configured with the JavaTimeModule and a custom
   * ForestClientDetailsSerializerModifier module.
   *
   * @param builder The Jackson2ObjectMapperBuilder used to build the ObjectMapper.
   * @return A configured ObjectMapper instance.
   */
  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {

    ObjectMapper mapper = builder.build();
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(forestClientDetailsDtoModule());

    return mapper;
  }

  /**
   * Creates and configures a SimpleModule for customizing the serialization of ForestClientDetails.
   * This module registers a custom serializer modifier, ForestClientDetailsSerializerModifier.
   *
   * @return A configured SimpleModule instance with the custom serializer modifier.
   */
  SimpleModule forestClientDetailsDtoModule() {
    SimpleModule module = new SimpleModule();

    // Register the serializer modifier
    module.setSerializerModifier(new ForestClientDetailsSerializerModifier());
    return module;
  }

}
