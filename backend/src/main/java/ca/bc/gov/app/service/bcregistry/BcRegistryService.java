package ca.bc.gov.app.service.bcregistry;

import static ca.bc.gov.app.ApplicationConstant.BUSINESS_SUMMARY_FILING_HISTORY;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryExceptionMessageDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultsDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficesDto;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Observed
public class BcRegistryService {

  private final WebClient bcRegistryApi;
  private final ObservationRegistry registry;

  public BcRegistryService(
      @Qualifier("bcRegistryApi") WebClient bcRegistryApi,
      ObservationRegistry registry
  ) {
    this.bcRegistryApi = bcRegistryApi;
    this.registry = registry;
  }


  /**
   * Searches the BC Registry API for {@link BcRegistryFacetSearchResultEntryDto} instances matching
   * the given value.
   *
   * @param value the value to search for
   * @return a {@link Flux} of matching {@link BcRegistryFacetSearchResultEntryDto} instances
   * @throws NoClientDataFound           if no matching data is found
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Flux<BcRegistryFacetSearchResultEntryDto> searchByFacets(String value) {
    log.info("Searching BC Registry for {}", value);
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
            .name("request.bcregistry")
            .tag("kind", "facet")
            .tap(Micrometer.observation(registry))
            .map(BcRegistryFacetResponseDto::searchResults)
            .flatMapIterable(BcRegistryFacetSearchResultsDto::results)
            .filter(entry -> entry.status().equalsIgnoreCase("active"))
            .doOnNext(content -> log.info("Found entry on BC Registry [{}] {}", content.identifier(),content.name()));
  }

  /**
   * Sends a request to retrieve the document data for a given value using the BC Registry API. The
   * method returns a Flux of {@link BcRegistryDocumentDto}, which represents the document data.
   *
   * @param value the value used to identify the document data
   * @return a Flux of {@link BcRegistryDocumentDto} representing the requested document data
   * @throws NoClientDataFound           if the API responds with a 404 status code indicating that
   *                                     no data was found for the given value
   * @throws InvalidAccessTokenException if the API responds with a 401 status code indicating that
   *                                     the access token used for the request is invalid
   */
  public Flux<BcRegistryDocumentDto> requestDocumentData(String value) {
    log.info("Requesting document for {}", value);
    return
        bcRegistryApi
            .post()
            .uri("/registry-search/api/v1/businesses/{identifier}/documents/requests",
                Map.of("identifier", value)
            )
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(BUSINESS_SUMMARY_FILING_HISTORY))
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
                exception ->
                    exception
                        .bodyToMono(BcRegistryExceptionMessageDto.class)
                        .map(BcRegistryExceptionMessageDto::rootCause)
                        .doOnNext(
                            message -> log.error("Error while requesting data for {} -- {}", value,
                                message))
                        .filter(message -> message.contains("not found"))
                        .switchIfEmpty(Mono.error(new InvalidAccessTokenException()))
                        .flatMap(message -> Mono.error(new NoClientDataFound(value)))

            )
            .bodyToMono(BcRegistryDocumentRequestResponseDto.class)
            .name("request.bcregistry")
            .tag("kind", "docreq")
            .tap(Micrometer.observation(registry))
            .flatMapIterable(BcRegistryDocumentRequestResponseDto::documents)
            .map(BcRegistryDocumentRequestDocumentDto::documentKey)
            .doOnNext(documentKey -> log.info("Loading document {} for identifier {}", documentKey,
                value))
            .flatMap(documentKey -> getDocumentData(value, documentKey))
            //This will try to load the standing and business data for entries with no documents
            .onErrorResume(NoClientDataFound.class, exception ->
                searchByFacets(value)
                    .next()
                    .map(facet -> new BcRegistryBusinessDto(
                            facet.goodStanding(),
                            false,
                            false,
                            false,
                            facet.identifier(),
                            facet.name(),
                            facet.legalType(),
                            facet.status()
                        )
                    )
                    .map(business -> new BcRegistryDocumentDto(
                            business,
                            new BcRegistryOfficesDto(
                                new BcRegistryBusinessAdressesDto(
                                    new BcRegistryAddressDto(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                    ),
                                    new BcRegistryAddressDto(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                    )
                                )
                            ),
                            List.of()
                        )
                    )
            );
  }

  private Mono<BcRegistryDocumentDto> getDocumentData(String identifier, String documentKey) {
    return
        bcRegistryApi
            .get()
            .uri("/registry-search/api/v1/businesses/{identifier}/documents/{documentKey}",
                Map.of("identifier", identifier, "documentKey", documentKey)
            )
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            // handle different HTTP error codes
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(404)),
                exception -> Mono.error(new NoClientDataFound(identifier))
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .bodyToMono(BcRegistryDocumentDto.class)
            .name("request.bcregistry")
            .tag("kind", "docget")
            .tap(Micrometer.observation(registry))
            .doOnNext(
                document -> log.info("Document loaded for {} {} as {}", identifier, documentKey,
                    document.business().legalName()));
  }
}
