package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ClientAdvancedSearchCriteriaDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.service.ClientAdvancedSearchService;
import ca.bc.gov.app.service.ClientSearchService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller that exposes the advanced client search endpoint.
 *
 * <p>Provides a single {@code POST /api/clients/advanced-search} endpoint that
 * accepts optional query parameters {@code page} and {@code size}, and a JSON request body
 * represented by {@link ClientAdvancedSearchCriteriaDto}. When no valid criteria are supplied,
 * the endpoint falls back to returning the most recently created client entries.
 *
 * <p>Every response includes an {@code x-total-count} header with the total
 * number of matching records so the caller can implement pagination controls.
 *
 * @see ClientAdvancedSearchService
 * @see ClientSearchService
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/clients/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientAdvancedSearchController {
  
  private final ClientSearchService searchService;
  private final ClientAdvancedSearchService advancedSearchService;
  
  private static final String X_TOTAL_COUNT = "x-total-count";

  /**
   * Performs an advanced search for clients based on the provided criteria.
   * <p>
   * This endpoint is available at {@code POST /api/clients/advanced-search}.
   * The search criteria are provided in the request body as a {@link ClientAdvancedSearchCriteriaDto}.
   * Optional pagination parameters {@code page} and {@code size} can be supplied as query parameters.
   * <p>
   * If the {@code criteria} is null or contains no valid search parameters, the method
   * returns the latest client entries, paginated according to the {@code page} and {@code size}
   * parameters. Otherwise, it performs an advanced search using {@link ClientAdvancedSearchService}.
   * <p>
   * The total number of results is included in the {@code x-total-count} HTTP header of the response.
   *
   * @param page the page number to retrieve (0-based index), defaults to 0 if not provided (query parameter)
   * @param size the number of results per page, defaults to 5 if not provided (query parameter)
   * @param criteria the advanced search criteria provided in the request body; may be null
   * @param serverResponse the server response used to set the {@code x-total-count} header
   * @return a {@link Flux} of {@link PredictiveSearchResultDto} representing the search results
   */
  @PostMapping
  public Flux<PredictiveSearchResultDto> findByAdvancedSearch(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "5") Integer size,
      @RequestBody ClientAdvancedSearchCriteriaDto criteria,
      ServerHttpResponse serverResponse) {

    PageRequest pageRequest = PageRequest.of(page, size);

    if (criteria == null || !criteria.hasValidParams()) {
      log.info("Receiving request to search the latest entries");
      return searchService
          .latestEntries(pageRequest)
          .collectList()
          .flatMapMany(list -> {
            if (list.isEmpty()) {
              serverResponse
                .getHeaders()
                .set(X_TOTAL_COUNT, "0");
              return Flux.empty();
            }
            serverResponse
                .getHeaders()
                .set(X_TOTAL_COUNT, list.get(0).getValue().toString());
            return Flux
                .fromIterable(list)
                .map(Pair::getKey);
          });
    }

    log.info("Receiving request to do an advanced search with criteria: {}", criteria);
    return advancedSearchService
        .advancedSearch(criteria, pageRequest)
        .switchOnFirst((signal, flux) -> {
          if (signal.hasValue()) {
            long total = signal.get() != null ? signal.get().getValue() : 0L;
            serverResponse
              .getHeaders()
              .set(X_TOTAL_COUNT, String.valueOf(total));
          } else {
            serverResponse
              .getHeaders()
              .set(X_TOTAL_COUNT, "0");
          }
          return flux.map(Pair::getKey);
        });
  }
  
}