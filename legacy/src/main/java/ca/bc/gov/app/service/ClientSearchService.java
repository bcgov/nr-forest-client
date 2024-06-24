package ca.bc.gov.app.service;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
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

/**
 * The ClientSearchService class provides methods to search for clients based on various criteria.
 * It uses the ForestClientRepository and ClientDoingBusinessAsRepository to perform the searches.
 * The results are mapped to ForestClientDto objects using the AbstractForestClientMapper. The class
 * is annotated with @Service, indicating that it's a service component in the Spring framework.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientSearchService {

  private final ForestClientRepository forestClientRepository;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;
  private final ForestClientRepository clientRepository;
  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity> forestClientMapper;
  private final R2dbcEntityTemplate template;

  /**
   * This method is used to find clients based on their registration number or company name. It
   * first checks if both the registration number and company name are blank. If they are, it
   * returns MissingRequiredParameterException. If at least one parameter is valid, it searches for
   * company name or registration number. If no clients are found and the company name is not blank,
   * it searches doing business as table Each retrieved client entity is then mapped to a DTO (Data
   * Transfer Object) using the mapper.
   *
   * @param registrationNumber The registration number of the client to be searched for.
   * @param companyName        The company name of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
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
            .map(forestClientMapper::toDto);
  }

  /**
   * This method is used to find clients based on their first name, last name, date of birth, and
   * identification. If any of the parameters are blank or null, returns
   * MissingRequiredParameterException. If all parameters are valid, it queries the legalFirstName,
   * clientName and birthdate, and if present, the clientIdentification. For each client found, it
   * logs the client's number and name.
   *
   * @param firstName      The first name of the client to be searched for.
   * @param lastName       The last name of the client to be searched for.
   * @param dob            The date of birth of the client to be searched for.
   * @param identification The identification of the client to be searched for. Optional.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> findByIndividual(
      String firstName,
      String lastName,
      LocalDate dob,
      String identification,
      boolean fuzzy
  ) {

    if (StringUtils.isAnyBlank(firstName, lastName) || dob == null) {
      log.error("Missing required parameter to search for individual");
      return Flux.error(new MissingRequiredParameterException("firstName, lastName, or dob"));
    }

    log.info("Searching for individual: {} {} {} {}", firstName, lastName, dob,
        StringUtils.defaultString(identification));

    if (StringUtils.isBlank(identification) && fuzzy) {
      return clientRepository
          .findByIndividualFuzzy(
              String.format("%s %s", firstName, lastName),
              dob.atStartOfDay()
          )
          .map(forestClientMapper::toDto)
          .doOnNext(dto -> log.info("Found individual matching {} {} {} as {} {}",
              firstName, lastName, dob,
              dto.clientNumber(), dto.clientName())
          );
    }

    Criteria queryCriteria = where("legalFirstName").is(firstName).ignoreCase(true)
        .and("clientName").is(lastName).ignoreCase(true)
        .and("birthdate").is(dob.atStartOfDay())
        .and("clientTypeCode").is("I").ignoreCase(true);

    if (StringUtils.isNotBlank(identification)) {
      queryCriteria = queryCriteria
          .and("clientIdentification")
          .is(identification)
          .ignoreCase(true);
    }

    return searchClientByQuery(queryCriteria,ForestClientEntity.class)
        .map(forestClientMapper::toDto)
        .doOnNext(
            dto -> log.info("Found individual matching {} {} {} {} as {} {}",
                firstName, lastName, dob, StringUtils.defaultString(identification),
                dto.clientNumber(), dto.clientName()));
  }

  /**
   * This method is used to find clients based on their company name. If the parameter is blank, it
   * returns a MissingRequiredParameterException. If the parameter is valid, it calls the matchBy
   * that calls the JARO_WINKLER_SIMILARITY algorithm with the provided company name. Each client
   * found is then mapped to a DTO (Data Transfer Object) using the mapper.
   *
   * @param companyName The company name of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> matchBy(String companyName) {
    log.info("Searching for match: {}", companyName);

    if (StringUtils.isBlank(companyName)) {
      return Flux.error(new MissingRequiredParameterException("companyName"));
    }

    return
        forestClientRepository
            .matchBy(companyName)
            .map(forestClientMapper::toDto)
            .doOnNext(dto -> log.info("Found match for {} as {} {}",
                companyName,
                dto.clientNumber(), dto.clientName()));
  }

  /**
   * This method is used to find clients based on their identification and last name. If any
   * parameter is blank, it returns a MissingRequiredParameterException. If all parameters are
   * valid, it queries where the clientIdentification and clientName match the provided parameters,
   * ignoring case. For each client found, it logs the client's number and name.
   *
   * @param clientId The identification of the client to be searched for.
   * @param lastName The last name of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> findByIdAndLastName(String clientId, String lastName) {
    log.info("Searching for client: {} {}", clientId, lastName);

    if (StringUtils.isAnyBlank(clientId, lastName)) {
      return Flux.error(new MissingRequiredParameterException("clientId, lastName"));
    }

    Criteria queryCriteria = where("clientIdentification").is(clientId).ignoreCase(true)
        .and("clientName").is(lastName).ignoreCase(true);

    return searchClientByQuery(queryCriteria,ForestClientEntity.class)
        .map(forestClientMapper::toDto)
        .doOnNext(
            dto -> log.info("Found client with clientId {} and lastName {} as  {} {}",
                clientId, lastName,
                dto.clientNumber(), dto.clientName())
        );
  }

  /**
   * This method is used to find clients based on their identification type and identification
   * value. It checks if any of the parameters are blank. If any parameter is blank, it returns a
   * MissingRequiredParameterException. If all parameters are valid, it queries the clientIdTypeCode
   * and clientIdentification match the provided parameters, ignoring case. For each client found,
   * it logs the client's number and name.
   *
   * @param idType         The identification type of the client to be searched for.
   * @param identification The identification value of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> findByIdentification(String idType, String identification) {
    log.info("Searching for client with id {} value {}", idType, identification);

    if (StringUtils.isAnyBlank(idType, identification)) {
      return Flux.error(new MissingRequiredParameterException("idType, identification"));
    }

    Criteria queryCriteria = where("clientIdTypeCode").is(idType).ignoreCase(true)
        .and("clientIdentification").is(identification).ignoreCase(true)
        .and("clientTypeCode").is("I").ignoreCase(true);

    return searchClientByQuery(queryCriteria,ForestClientEntity.class)
        .map(forestClientMapper::toDto)
        .doOnNext(
            dto -> log.info("Found client with clientId {} {} as  {} {}",
                idType, identification,
                dto.clientNumber(), dto.clientName())
        );
  }

  public Flux<ForestClientDto> findByGeneralEmail(String email) {

    Criteria queryCriteria = where("emailAddress").is(email).ignoreCase(true);


    Flux<ForestClientDto> locations = searchClientByQuery(queryCriteria,ForestClientLocationEntity.class)
        .flatMap(entity -> searchClientByQuery(where("clientNumber").is(entity.getClientNumber()),ForestClientEntity.class))
        .map(forestClientMapper::toDto);
    Flux<ForestClientDto> contacts = searchClientByQuery(queryCriteria, ForestClientContactEntity.class)
        .flatMap(entity -> searchClientByQuery(where("clientNumber").is(entity.getClientNumber()),ForestClientEntity.class))
        .map(forestClientMapper::toDto);




    locations




        .switchIfEmpty(contacts)
        .doOnNext(
            dto -> log.info("Found client with email {} as  {} {}",
                email,
                dto.clientNumber(), dto.clientName())
        );






    return null;
  }

  public Flux<ForestClientDto> findByEntireAddress(
      String address,
      String city,
      String province,
      String postalCode,
      String country
  ){

    Criteria queryCriteria =
        where("city").is(city).ignoreCase(true)
            .and("province").is(province).ignoreCase(true)
            .and("postalCode").is(postalCode).ignoreCase(true)
            .and("country").is(country).ignoreCase(true)
            .and(
                where("addressOne").is(address).ignoreCase(true)
                    .or("addressTwo").is(address).ignoreCase(true)
                    .or("addressThree").is(address).ignoreCase(true)
            );

    return searchClientByQuery(queryCriteria,ForestClientLocationEntity.class)
        .flatMap(entity -> searchClientByQuery(where("clientNumber").is(entity.getClientNumber()),ForestClientEntity.class))
        .map(forestClientMapper::toDto)
        .doOnNext(
            dto -> log.info("Found client with address {} as  {} {}",
                address,
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
   * @param entityClass The class of the entity to be retrieved.
   * @return A Flux stream of the entityClass objects.
   */
  private <T> Flux<T> searchClientByQuery(
      final Criteria queryCriteria,
      final Class<T> entityClass
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
            entityClass
        )
        .doOnNext(client -> log.info("Found client for query {}",queryCriteria));
  }


}
