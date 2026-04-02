package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ClientAdvancedSearchCriteriaDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientAdvancedSearchService {

  private final ForestClientRepository forestClientRepository;

  /**
   * Performs an advanced search based on the criteria DTO.
   * Each non-blank field acts as a filter; blank/null fields are ignored by the SQL query.
   * Multiple fields are combined with AND logic.
   *
   * <p>The query joins forest_client, client_location, and client_contact tables,
   * allowing searches across all of them.
   *
   * <p>Example usage:
   * <pre>
   *   /api/clients/advanced-search?clientStatus=ACT&amp;firstName=Joe
   *   /api/clients/advanced-search?contactEmail=john@example.com&amp;clientName=MyCorp
   * </pre>
   *
   * @param criteria the search criteria DTO with optional fields
   * @param page     pagination parameters including page size and offset
   * @return a {@link Flux} of pairs with {@link PredictiveSearchResultDto} and total count
   */
  public Flux<Pair<PredictiveSearchResultDto, Long>> advancedSearch(
      ClientAdvancedSearchCriteriaDto criteria, Pageable page) {

    if (criteria == null || !criteria.hasValidParams()) {
      return Flux.error(new MissingRequiredParameterException("searchParams"));
    }

    log.info("Performing advanced search with criteria: {}", criteria);

    return forestClientRepository
        .countByAdvancedSearch(
            blankToNull(criteria.clientName()),
            blankToNull(criteria.firstName()),
            blankToNull(criteria.middleName()),
            blankToNull(criteria.clientStatus()),
            blankToNull(criteria.clientType()),
            blankToNull(criteria.clientIdType()),
            blankToNull(criteria.clientIdentification()),
            blankToNull(criteria.locationEmail()),
            blankToNull(criteria.contactName()),
            blankToNull(criteria.contactEmail())
        )
        .defaultIfEmpty(0L)
        .flatMapMany(count -> {
          if (count == 0) {
            return Flux.empty();
          }
          return forestClientRepository
              .findByAdvancedSearch(
                  blankToNull(criteria.clientName()),
                  blankToNull(criteria.firstName()),
                  blankToNull(criteria.middleName()),
                  blankToNull(criteria.clientStatus()),
                  blankToNull(criteria.clientType()),
                  blankToNull(criteria.clientIdType()),
                  blankToNull(criteria.clientIdentification()),
                  blankToNull(criteria.locationEmail()),
                  blankToNull(criteria.contactName()),
                  blankToNull(criteria.contactEmail()),
                  page.getPageSize(), page.getOffset()
              )
              .doOnNext(dto -> log.info(
                  "Advanced search matched client {} {}",
                  dto.clientNumber(), dto.clientFullName())
              )
              .map(dto -> Pair.of(dto, count));
        });
  }

  private static String blankToNull(String value) {
    return StringUtils.isBlank(value) ? null : value;
  }
}
