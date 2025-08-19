package ca.bc.gov.app.service.bcregistry;

import static ca.bc.gov.app.ApplicationConstant.BUSINESS_SUMMARY_FILING_HISTORY;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAlternateNameDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryExceptionMessageDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetRequestBodyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetRequestQueryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultsDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import ca.bc.gov.app.exception.BadRequestException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.exception.UnexpectedErrorException;
import ca.bc.gov.app.util.ClientMapper;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
   * @param name       the name value to search for
   * @param identifier the identifier value to search for
   * @return a {@link Flux} of matching {@link BcRegistryFacetSearchResultEntryDto} instances
   * @throws NoClientDataFound           if no matching data is found
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Flux<BcRegistryFacetSearchResultEntryDto> searchByFacets(String name, String identifier) {
    log.info("Searching BC Registry for {}", Objects.toString(name, identifier));
    return
        bcRegistryApi
            .post()
            .uri("/registry-search/api/v2/search/businesses")
            .body(BodyInserters.fromValue(
                    new BcRegistryFacetRequestBodyDto(
                        new BcRegistryFacetRequestQueryDto(Objects.toString(name, identifier), name,
                            identifier),
                        Map.of("status", List.of("ACTIVE")),
                        100,
                        0
                    )
                )
            )
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            // handle different HTTP error codes
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)),
                exception -> Mono.error(new InvalidAccessTokenException())
            )
            .onStatus(
                statusCode -> statusCode.isSameCodeAs(HttpStatusCode.valueOf(400)),
                exception -> exception
                    .bodyToMono(BcRegistryExceptionMessageDto.class)
                    .flatMap(ex -> Mono.error(new BadRequestException(ex.errorMessage())))
            )
            .onStatus(
                HttpStatusCode::isError,
                exception -> exception
                    .bodyToMono(BcRegistryExceptionMessageDto.class)
                    .flatMap(ex -> Mono.error(
                            new UnexpectedErrorException(
                                exception.statusCode().value(),
                                ex.errorMessage()
                            )
                        )
                    )
            )
            .bodyToMono(BcRegistryFacetResponseDto.class)
            .name(ApplicationConstant.REQUEST_BCREGISTRY)
            .tag("kind", "facet")
            .tap(Micrometer.observation(registry))
            .map(BcRegistryFacetResponseDto::searchResults)
            .flatMapIterable(BcRegistryFacetSearchResultsDto::results)
            .filter(entry -> entry.status().equalsIgnoreCase("active"))
            .doOnNext(
                content -> log.info("Found entry on BC Registry [{}] {}",
                    content.identifier(),
                    content.name()));
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
                            message -> log.error("Error while requesting data for {} -- {}",
                                value,
                                message))
                        .filter(message -> message.contains("not found"))
                        .switchIfEmpty(Mono.error(new InvalidAccessTokenException()))
                        .flatMap(message -> Mono.error(new NoClientDataFound(value)))

            )
            .onStatus(
                HttpStatusCode::isError,
                exception -> exception
                    .bodyToMono(BcRegistryExceptionMessageDto.class)
                    .flatMap(ex -> Mono.error(
                            new UnexpectedErrorException(
                                exception.statusCode().value(),
                                ex.errorMessage()
                            )
                        )
                    )
            )
            .bodyToMono(BcRegistryDocumentRequestResponseDto.class)
            .name(ApplicationConstant.REQUEST_BCREGISTRY)
            .tag("kind", "docreq")
            .tap(Micrometer.observation(registry))
            .flatMapIterable(BcRegistryDocumentRequestResponseDto::documents)
            .doOnNext(entry -> log.info("Found document entry for {}: {}", value, entry))
            .map(BcRegistryDocumentRequestDocumentDto::documentKey)
            .flatMap(documentKey -> getDocumentData(value, documentKey))
            //This will try to load the standing and business data for entries with no documents
            .onErrorResume(NoClientDataFound.class, exception ->
                searchByFacets(null, value).next().map(this::buildDocumentData)
            );
  }

  private Mono<BcRegistryDocumentDto> getDocumentData(String identifier, String documentKey) {
    log.info("Requesting document {} for identifier {} for details", documentKey, identifier);
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
            .name(ApplicationConstant.REQUEST_BCREGISTRY)
            .tag("kind", "docget")
            .tap(Micrometer.observation(registry))
            .doOnNext(
                document -> log.info("Document loaded for {} {} as {}", identifier, documentKey,
                    document.business().legalName()));
  }

  private BcRegistryDocumentDto buildDocumentData(
      BcRegistryFacetSearchResultEntryDto facet
  ) {
    // We have no address data in the facet, so we'll just create empty address objects
    BcRegistryAddressDto address =
        new BcRegistryAddressDto(null, null, null, null, null, null, null, null);

    // We build the alternate name that's being used down the line
    BcRegistryAlternateNameDto alternateName =
        new BcRegistryAlternateNameDto(
            facet.legalType(),
            facet.identifier(),
            facet.name(),
            null,
            null
        );

    // We can have the parties value, so we will build the parties list
    List<BcRegistryPartyDto> parties = new ArrayList<>();

    // and we fill it with the parties from facet
    if (!CollectionUtils.isEmpty(facet.parties())) {
      facet
          .parties()
          .stream()
          .map(this::buildBcRegistryPartyDto)
          .filter(Objects::nonNull)
          .forEachOrdered(parties::add);
    }

    BcRegistryBusinessDto business =
        new BcRegistryBusinessDto(
            List.of(alternateName),
            facet.goodStanding(),
            false,
            false,
            false,
            facet.identifier(),
            facet.name(),
            facet.legalType(),
            facet.status()
        );

    return
        new BcRegistryDocumentDto(
            business,
            new BcRegistryOfficesDto(
                new BcRegistryBusinessAdressesDto(address, address)
            ),
            parties
        );

  }

  private BcRegistryPartyDto buildBcRegistryPartyDto(BcRegistryFacetPartyDto party) {

    if (StringUtils.isEmpty(party.partyType()) || !party.partyType().equalsIgnoreCase("person")) {
      return null;
    }

    List<BcRegistryRoleDto> roles =
        Optional
            .ofNullable(party.partyRoles())
            .map(partyRoles ->
                partyRoles
                    .stream()
                    .map(StringUtils::capitalize)
                    .map(role -> new BcRegistryRoleDto(null, null, role))
                    .toList()
            )
            .orElse(List.of());

    Map<String, String> naming = ClientMapper.parseName(party.partyName(), "BCREGISTRY");

    return new BcRegistryPartyDto(
        null,
        null,
        new BcRegistryOfficerDto(
            null,
            naming.get("firstName"),
            naming.get("lastName"),
            null,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            party.partyType()
        ),
        roles
    );
  }
}
