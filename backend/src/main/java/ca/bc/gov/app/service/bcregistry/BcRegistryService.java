package ca.bc.gov.app.service.bcregistry;

import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryIdentificationDto;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j

public class BcRegistryService {

  private final WebClient bcRegistryApi;

  public BcRegistryService(@Qualifier("bcRegistryApi") WebClient bcRegistryApi) {
    this.bcRegistryApi = bcRegistryApi;
  }


  /**
   * Retrieves the standing of a business with the given client number from the BC Registry API.
   *
   * @param clientNumber the client number of the business to retrieve standing for
   * @return a {@link Mono} emitting a {@link BcRegistryBusinessDto}
   *     representing the standing of the business
   * @throws NoClientDataFound if no client data could be found with the given client number
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Mono<BcRegistryBusinessDto> getCompanyStanding(String clientNumber) {
    // make a request to the BC Registry API to retrieve the business data
    return bcRegistryApi
        .get()
        .uri("/business/api/v2/businesses/{clientNumber}",
            Map.of("clientNumber", clientNumber)
        )
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        // handle different HTTP error codes
        .onStatus(
            statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(404)),
            exception -> Mono.error(new NoClientDataFound(clientNumber))
        )
        .onStatus(
            statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
            exception -> Mono.error(new InvalidAccessTokenException())
        )
        .onStatus(
            statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
            exception -> Mono.error(new InvalidAccessTokenException())
        )
        .bodyToMono(BcRegistryIdentificationDto.class)
        // transform the response into a BcRegistryBusinessDto
        .map(BcRegistryIdentificationDto::business);

  }

  /**
   * Retrieves the addresses for a business with the given client number from the BC Registry API.
   *
   * @param clientNumber the client number of the business to retrieve addresses for
   * @return a {@link Mono} emitting a {@link BcRegistryBusinessAdressesDto}
   *     representing the addresses of the business
   * @throws NoClientDataFound if no client data could be found with the given client number
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Mono<BcRegistryBusinessAdressesDto> getAddresses(String clientNumber) {
    // make a request to the BC Registry API to retrieve the addresses of the business
    return bcRegistryApi
        .get()
        .uri("/business/api/v2/businesses/{clientNumber}/addresses",
            Map.of("clientNumber", clientNumber))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        // handle different HTTP error codes
        .onStatus(
            statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(404)),
            exception -> Mono.error(new NoClientDataFound(clientNumber))
        )
        .onStatus(
            statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
            exception -> Mono.error(new InvalidAccessTokenException())
        )
        .onStatus(
            statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
            exception -> Mono.error(new InvalidAccessTokenException())
        )
        .bodyToMono(BcRegistryIdentificationDto.class)
        // transform the response into a BcRegistryBusinessAdressesDto
        .map(BcRegistryIdentificationDto::businessOffice);
  }

}
