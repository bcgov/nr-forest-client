package ca.bc.gov.app.configuration;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import ca.bc.gov.app.dto.DistrictDto;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessRequestDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessTypeDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestBodyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryExceptionMessageDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
@RegisterReflectionForBinding({
    SubmissionInformationDto.class,
    Integer.class,
    MessagingWrapper.class,
    MatcherResult.class,
    EmailRequestDto.class,
    DistrictDto.class,
    ForestClientLocationDto.class,
    ForestClientDto.class,
    ForestClientContactDto.class,
    ClientDoingBusinessAsDto.class,
    BcRegistryRoleDto.class,
    BcRegistryPartyDto.class,
    BcRegistryOfficerDto.class,
    BcRegistryExceptionMessageDto.class,
    BcRegistryDocumentRequestBodyDto.class,
    BcRegistryDocumentRequestDocumentDto.class,
    BcRegistryDocumentRequestResponseDto.class,
    BcRegistryDocumentDto.class,
    BcRegistryDocumentAccessTypeDto.class,
    BcRegistryDocumentAccessRequestDto.class
})
public class GlobalServiceConfiguration {

  @Bean
  public WebClient forestClientApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getBackend().getUri())
        .filter(
            basicAuthentication(
                configuration.getSecurity().getServiceAccountName(),
                configuration.getSecurity().getServiceAccountSecret()
            )
        )
        .build();
  }

  @Bean
  public WebClient legacyClientApi(
      ForestClientConfiguration configuration,
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getLegacy().getUri())
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
      WebClient.Builder webClientBuilder
  ) {
    return webClientBuilder
        .baseUrl(configuration.getBcregistry().getUri())
        .defaultHeader("x-apikey", configuration.getBcregistry().getApiKey())
        .defaultHeader("Account-Id", configuration.getBcregistry().getAccountId())
        .build();
  }

}
