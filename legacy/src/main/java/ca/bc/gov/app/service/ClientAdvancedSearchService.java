package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ClientAdvancedSearchCriteriaDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    ClientAdvancedSearchCriteriaDto sanitizedCriteria = criteria.sanitized();
    log.info("Performing advanced search with criteria: {}", sanitizedCriteria);

    return forestClientRepository
        .countByAdvancedSearch(
            sanitizedCriteria.clientName(), 
            sanitizedCriteria.firstName(),
            sanitizedCriteria.middleName(), 
            sanitizedCriteria.clientStatus(),
            sanitizedCriteria.clientType(), 
            sanitizedCriteria.clientIdType(),
            sanitizedCriteria.clientIdentification(), 
            sanitizedCriteria.emailAddress(),
            sanitizedCriteria.contactName()
        )
        .defaultIfEmpty(0L)
        .flatMapMany(count -> {
          if (count == 0) {
            return Flux.empty();
          }
          return forestClientRepository
              .findByAdvancedSearch(
                  sanitizedCriteria.clientName(), 
                  sanitizedCriteria.firstName(), 
                  sanitizedCriteria.middleName(),
                  sanitizedCriteria.clientStatus(), 
                  sanitizedCriteria.clientType(), 
                  sanitizedCriteria.clientIdType(),
                  sanitizedCriteria.clientIdentification(), 
                  sanitizedCriteria.emailAddress(),
                  sanitizedCriteria.contactName(),
                  page.getPageSize(), page.getOffset()
              )
              .doOnNext(dto -> log.info(
                  "Advanced search matched client {} {}",
                  dto.clientNumber(), dto.clientFullName())
              )
              .map(dto -> Pair.of(dto, count));
        });
  }
}
