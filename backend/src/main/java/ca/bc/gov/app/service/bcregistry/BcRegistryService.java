package ca.bc.gov.app.service.bcregistry;

import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultsDto;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
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
    return
        bcRegistryApi
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
    return
        bcRegistryApi
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
            .map(BcRegistryIdentificationDto::businessOffice);
  }

  /**
   * Searches the BC Registry API for {@link BcRegistryFacetSearchResultEntryDto}
   * instances matching the given value.
   *
   * @param value the value to search for
   * @return a {@link Flux} of matching {@link BcRegistryFacetSearchResultEntryDto} instances
   * @throws NoClientDataFound           if no matching data is found
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Flux<BcRegistryFacetSearchResultEntryDto> searchByFacets(String value) {
    return
        bcRegistryApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/registry-search/api/v1/businesses/search/facets")
                    .queryParam("query", String.format("value:%s", value))
                    .queryParam("start", "0")
                    .queryParam("rows", "100")
                    .queryParam("category", "status:Active")
                    .build(Map.of())
            )
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            // handle different HTTP error codes
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(404)),
                exception -> Mono.error(new NoClientDataFound(value))
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .bodyToMono(BcRegistryFacetResponseDto.class)
            .map(BcRegistryFacetResponseDto::searchResults)
            .flatMapIterable(BcRegistryFacetSearchResultsDto::results)
            .doOnNext(content -> log.info("Found entry on BC Registry {}", content));
  }
}
