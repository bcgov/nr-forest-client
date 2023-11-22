package ca.bc.gov.app.service.bcregistry;

import static ca.bc.gov.app.ApplicationConstant.BUSINESS_SUMMARY_FILING_HISTORY;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestResponseDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryExceptionMessageDto;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
                        .map(message -> message.contains("not found"))
                        .filter(message -> message)
                        .switchIfEmpty(Mono.error(new InvalidAccessTokenException()))
                        .flatMap(message -> Mono.error(new NoClientDataFound(value)))

            )
            .bodyToMono(BcRegistryDocumentRequestResponseDto.class)
            .flatMapIterable(BcRegistryDocumentRequestResponseDto::documents)
            .map(BcRegistryDocumentRequestDocumentDto::documentKey)
            .doOnNext(documentKey -> log.info("Loading document {} for identifier {}", documentKey,
                value))
            .flatMap(documentKey -> getDocumentData(value, documentKey));
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
            .doOnNext(
                document -> log.info("Document loaded for {} {} as {}", identifier, documentKey,
                    document));
  }
}
