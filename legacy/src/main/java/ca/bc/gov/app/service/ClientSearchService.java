package ca.bc.gov.app.service;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientSearchService {

  private final ForestClientRepository forestClientRepository;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;
  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity> mapper;
  private final R2dbcEntityTemplate template;

  public Flux<ForestClientDto> findByRegistrationNumberOrCompanyName(
      String registrationNumber,
      String companyName
  ) {

    if (StringUtils.isAllBlank(registrationNumber, companyName)) {
      log.error("Missing required parameter to search for registration number or company name");
      return Flux.error(new MissingRequiredParameterException("registrationNumber or companyName"));
    }

    log.info("Searching for registration number: {} or company name: {}", registrationNumber,
        companyName);

    return
        forestClientRepository
            .findClientByIncorporationOrName(registrationNumber, companyName)
            .doOnNext(
                dto -> log.info(
                    "Found client with registration name {} or company name {} as {} {}",
                    registrationNumber, companyName,
                    dto.getClientNumber(), dto.getClientName()))
            .switchIfEmpty(
                Flux
                    .from(Mono.justOrEmpty(Optional.ofNullable(companyName)))
                    .filter(StringUtils::isNotBlank)
                    .flatMap(name ->
                        doingBusinessAsRepository
                            .findByDoingBusinessAsName(name.toUpperCase())
                            .doOnNext(dto -> log.info("Found client doing business as: {} {}",
                                dto.getClientNumber(), dto.getDoingBusinessAsName()))
                            .map(ClientDoingBusinessAsEntity::getClientNumber)
                            .flatMap(forestClientRepository::findByClientNumber)
                    )
            )
            .map(mapper::toDto);
  }

  public Flux<ForestClientDto> findByIndividual(
      String firstName,
      String lastName,
      LocalDate dob,
      String identification
  ) {

    if (StringUtils.isAnyBlank(firstName, lastName) || dob == null) {
      log.error("Missing required parameter to search for individual");
      return Flux.error(new MissingRequiredParameterException("firstName, lastName, or dob"));
    }

    log.info("Searching for individual: {} {} {} {}", firstName, lastName, dob,
        StringUtils.defaultString(identification));

    Criteria queryCriteria = where("legalFirstName").is(firstName).ignoreCase(true)
        .and("clientName").is(lastName).ignoreCase(true)
        .and("birthdate").is(dob.atStartOfDay())
        .and("clientTypeCode").is("I");

    if (StringUtils.isNotBlank(identification)) {
      queryCriteria = queryCriteria
          .and("clientIdentification")
          .is(identification)
          .ignoreCase(true);
    }

    return searchClientByQuery(queryCriteria)
        .doOnNext(
            dto -> log.info("Found individual matching {} {} {} {} as {} {}",
                firstName, lastName, dob, StringUtils.defaultString(identification),
                dto.clientNumber(), dto.clientName()));
  }

  public Flux<ForestClientDto> matchBy(String companyName) {
    log.info("Searching for match: {}", companyName);

    if (StringUtils.isBlank(companyName)) {
      return Flux.error(new MissingRequiredParameterException("companyName"));
    }

    return
        forestClientRepository
            .matchBy(companyName)
            .map(mapper::toDto)
            .doOnNext(dto -> log.info("Found match for {} as {} {}",
                companyName,
                dto.clientNumber(), dto.clientName()));
  }

  public Flux<ForestClientDto> findByIdAndLastName(String clientId, String lastName) {
    log.info("Searching for client: {} {}", clientId, lastName);

    if (StringUtils.isAnyBlank(clientId, lastName)) {
      return Flux.error(new MissingRequiredParameterException("clientId, lastName"));
    }

    Criteria queryCriteria = where("clientIdentification").is(clientId).ignoreCase(true)
        .and("clientName").is(lastName).ignoreCase(true);

    return searchClientByQuery(queryCriteria)
        .doOnNext(
            dto -> log.info("Found client with clientId {} and lastName {} as  {} {}",
                clientId, lastName,
                dto.clientNumber(), dto.clientName())
        );
  }

  public Flux<ForestClientDto> findByIdentification(String idType, String identification) {
    log.info("Searching for client with id {} value {}", idType, identification);

    if (StringUtils.isAnyBlank(idType, identification)) {
      return Flux.error(new MissingRequiredParameterException("idType, identification"));
    }

    Criteria queryCriteria = where("clientIdTypeCode").is(idType).ignoreCase(true)
        .and("clientIdentification").is(identification).ignoreCase(true);

    return searchClientByQuery(queryCriteria)
        .doOnNext(
            dto -> log.info("Found client with clientId {} {} as  {} {}",
                idType, identification,
                dto.clientNumber(), dto.clientName())
        );
  }

  /**
   * This method is used to search for clients based on a given query criteria, page number, and
   * page size. It first creates a query based on the provided query criteria. Then, it counts the
   * total number of clients that match the search query. It retrieves the specific page of clients
   * based on the page number and size. The clients are sorted in ascending order by client number
   * and then by client name. Each retrieved client entity is then mapped to a DTO (Data Transfer
   * Object). The count of total matching clients is also set in each client DTO. Finally, it logs
   * the client number of each retrieved client.
   *
   * @param queryCriteria The criteria used to search for clients.
   * @return A Flux stream of ForestClientDto objects.
   */
  private Flux<ForestClientDto> searchClientByQuery(
      final Criteria queryCriteria
  ) {
    // Create a query based on the query criteria.
    Query searchQuery = Query.query(queryCriteria);

    log.info("Searching for clients with query {}", queryCriteria);

    return template
        .select(
            searchQuery
                .with(PageRequest.of(0, 1000))
                .sort(
                    Sort
                        .by(Sort.Order.asc("clientNumber"))
                        .and(Sort.by(Sort.Order.asc("clientName")))
                ),
            ForestClientEntity.class
        )
        // Map each client entity to a DTO and set the count of total matching clients.
        .map(mapper::toDto)
        .doOnNext(client -> log.info(
                "Found client for query {} as [{}] - {}",
                queryCriteria,
                client.clientNumber(),
                client.clientName()
            )
        );
  }

}
