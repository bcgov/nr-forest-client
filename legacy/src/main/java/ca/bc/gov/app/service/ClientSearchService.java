package ca.bc.gov.app.service;

import static org.springframework.data.relational.core.query.Criteria.where;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientDetailsDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.exception.NoValueFoundException;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import ca.bc.gov.app.repository.ForestClientLocationRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  public static final String CLIENT_NAME = "clientName";
  public static final String CLIENT_IDENTIFICATION = "clientIdentification";

  private final ForestClientRepository forestClientRepository;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;
  private final ForestClientRepository clientRepository;
  private final ForestClientContactRepository contactRepository;
  private final ForestClientLocationRepository locationRepository;

  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity>
      forestClientMapper;

  private final R2dbcEntityTemplate template;
  private final ForestClientConfiguration configuration;

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
  public Flux<ForestClientDto> findByRegistrationNumberOrCompanyName(String registrationNumber,
      String companyName) {

    if (StringUtils.isAllBlank(registrationNumber, companyName)) {
      log.error("Missing required parameter to search for registration number or company name");
      return Flux.error(new MissingRequiredParameterException("registrationNumber or companyName"));
    }

    log.info("Searching for registration number: {} or company name: {}", registrationNumber,
        companyName);

    return forestClientRepository.findClientByIncorporationOrName(registrationNumber, companyName)
        .doOnNext(
            dto -> log.info("Found client with registration name {} or company name {} as {} {}",
                registrationNumber, companyName, dto.getClientNumber(), dto.getClientName()))
        .switchIfEmpty(Flux.from(Mono.justOrEmpty(Optional.ofNullable(companyName)))
            .filter(StringUtils::isNotBlank).flatMap(
                name -> doingBusinessAsRepository.findByDoingBusinessAsName(name.toUpperCase(Locale.ROOT))
                    .doOnNext(dto -> log.info("Found client doing business as: {} {}",
                        dto.getClientNumber(), dto.getDoingBusinessAsName()))
                    .map(ClientDoingBusinessAsEntity::getClientNumber)
                    .flatMap(forestClientRepository::findByClientNumber)))
        .map(forestClientMapper::toDto).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber));
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
      String firstName, String lastName, LocalDate dob, String identification, boolean fuzzy) {

    if (StringUtils.isAnyBlank(firstName, lastName)) {
      log.error("Missing required parameter to search for individual");
      return Flux.error(
          new MissingRequiredParameterException("firstName, lastName, or dob"));
    }

    log.info(
        "Searching for individual: {} {} {} {}",
        firstName,
        lastName,
        dob,
        StringUtils.defaultString(identification));

    if (StringUtils.isBlank(identification) && fuzzy) {
      return clientRepository
          .findByIndividualFuzzy(
              String.format("%s %s", firstName, lastName),
              dob != null ? dob.atStartOfDay() : null
          )
          .map(forestClientMapper::toDto)
          .distinct(ForestClientDto::clientNumber)
          .sort(Comparator.comparing(ForestClientDto::clientNumber))
          .doOnNext(dto ->
              log.info(
                  "Found individual matching {} {} {} as {} {}",
                  firstName,
                  lastName,
                  dob,
                  dto.clientNumber(),
                  dto.clientName()));
    }

    Criteria queryCriteria = where("legalFirstName")
        .is(firstName)
        .ignoreCase(true)
        .and(CLIENT_NAME)
        .is(lastName)
        .ignoreCase(true)
        .and("clientTypeCode")
        .is("I")
        .ignoreCase(true);

    if( dob != null) {
      queryCriteria = queryCriteria
          .and("birthdate")
          .is(dob.atStartOfDay());
    }

    if (StringUtils.isNotBlank(identification)) {
      queryCriteria = queryCriteria
          .and(CLIENT_IDENTIFICATION)
          .is(identification)
          .ignoreCase(true);
    }

    return searchClientByQuery(queryCriteria, ForestClientEntity.class)
        .map(forestClientMapper::toDto)
        .doOnNext(dto ->
            log.info(
                "Found individual matching {} {} {} {} as {} {}",
                firstName,
                lastName,
                dob,
                StringUtils.defaultString(identification),
                dto.clientNumber(),
                dto.clientName()));
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

    return forestClientRepository.matchBy(companyName).map(forestClientMapper::toDto)
        .distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found match for {} as {} {}", companyName, dto.clientNumber(),
                dto.clientName()));
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

    Criteria queryCriteria = where(CLIENT_IDENTIFICATION).is(clientId).ignoreCase(true)
        .and(CLIENT_NAME).is(lastName).ignoreCase(true);

    return searchClientByQuery(queryCriteria, ForestClientEntity.class).map(
            forestClientMapper::toDto).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with clientId {} and lastName {} as  {} {}", clientId,
                lastName, dto.clientNumber(), dto.clientName()));
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
        .and(CLIENT_IDENTIFICATION).is(identification).ignoreCase(true).and("clientTypeCode")
        .is("I").ignoreCase(true);

    return searchClientByQuery(queryCriteria, ForestClientEntity.class).map(
            forestClientMapper::toDto).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with clientId {} {} as  {} {}", idType, identification,
                dto.clientNumber(), dto.clientName()));
  }

  /**
   * This method is used to find clients based on their email address. If the parameter is blank, it
   * returns a MissingRequiredParameterException. If the parameter is valid, it queries the
   * emailAddress field to match the provided email address. It does that for the location and
   * contact entities.
   *
   * @param email The email address of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> findByGeneralEmail(String email) {

    if (StringUtils.isBlank(email)) {
      return Flux.error(new MissingRequiredParameterException("email"));
    }

    Criteria queryCriteria = where("emailAddress").is(email).ignoreCase(true);

    Flux<ForestClientDto> locations = searchClientByQuery(queryCriteria,
        ForestClientLocationEntity.class).flatMap(entity -> searchClientByQuery(
        where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
        ForestClientEntity.class)).map(forestClientMapper::toDto);
    Flux<ForestClientDto> contacts = searchClientByQuery(queryCriteria,
        ForestClientContactEntity.class).flatMap(entity -> searchClientByQuery(
        where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
        ForestClientEntity.class)).map(forestClientMapper::toDto);

    return Flux.concat(locations, contacts).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with email {} as  {} {}", email, dto.clientNumber(),
                dto.clientName()));
  }

  /**
   * This method is used to find clients based on their phone number. If the parameter is blank, it
   * returns a MissingRequiredParameterException. If the parameter is valid, it queries the
   * businessPhone, cellPhone, faxNumber, and homePhone fields to match the provided phone number.
   * It does that for the location and contact entities.
   *
   * @param phoneNumber The phone number of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> findByGeneralPhoneNumber(String phoneNumber) {

    if (StringUtils.isBlank(phoneNumber)) {
      return Flux.error(new MissingRequiredParameterException("phoneNumber"));
    }

    Criteria queryCriteria = where("businessPhone").is(phoneNumber).or("cellPhone").is(phoneNumber)
        .or("faxNumber").is(phoneNumber);

    Flux<ForestClientDto> locations = searchClientByQuery(
        queryCriteria.or("homePhone").is(phoneNumber), ForestClientLocationEntity.class).flatMap(
          entity -> searchClientByQuery(
            where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
            ForestClientEntity.class)).map(forestClientMapper::toDto);

    Flux<ForestClientDto> contacts = searchClientByQuery(queryCriteria,
        ForestClientContactEntity.class).flatMap(entity -> searchClientByQuery(
        where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
        ForestClientEntity.class)).map(forestClientMapper::toDto);

    return Flux.concat(locations, contacts).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with phone number {} as  {} {}", phoneNumber,
                dto.clientNumber(), dto.clientName()));
  }

  /**
   * This method is used to find clients based on their address. If the parameter is blank, it
   * returns a MissingRequiredParameterException. If the parameter is valid, it queries the address,
   * city, province, postal code, and country fields to match the provided address. It does that for
   * the location entity.
   *
   * @param address The address of the client to be searched for.
   * @return A Flux stream of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> findByEntireAddress(AddressSearchDto address) {

    if (address == null || !address.isValid()) {
      return Flux.error(new MissingRequiredParameterException("address"));
    }

    return locationRepository.matchaddress(address.address(), address.postalCode(), address.city(),
            address.province(), address.country()).flatMap(entity -> searchClientByQuery(
            where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
            ForestClientEntity.class)).map(forestClientMapper::toDto)
        .distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with address {} as [{}] {}", address, dto.clientNumber(),
                dto.clientName()));
  }

  /**
   * Searches for clients based on a composite contact search criteria. It combines the names into a
   * single search string, and along with the email address and the phone number queries the
   * repository for matching client entities based on this name string, email, and phone number.
   * Each matching entity is then mapped to a {@link ForestClientDto}. The search results are made
   * distinct by client number.
   *
   * @param contact The {@link ContactSearchDto} containing search criteria such as name, email, and
   *                phone number.
   * @return A {@link Flux} of {@link ForestClientDto} stream of client DTOs that match the search
   *         criteria. If the contact parameter is null or not valid, a
   *         {@link MissingRequiredParameterException} is emitted.
   */
  public Flux<ForestClientDto> findByContact(ContactSearchDto contact) {

    if (contact == null || !contact.isValid()) {
      return Flux.error(new MissingRequiredParameterException("contact"));
    }

    String name = Stream.of(contact.firstName(), contact.middleName(), contact.lastName())
        .filter(StringUtils::isNotBlank).collect(Collectors.joining(" "));

    return contactRepository
        .matchByExpanded(
            name,
            contact.email(),
            contact.phone(),
            contact.phone2(),
            contact.fax()
        ).flatMap(entity -> searchClientByQuery(
            where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
            ForestClientEntity.class)).map(forestClientMapper::toDto)
        .distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with contact {} as [{}] {}", contact, dto.clientNumber(),
                dto.clientName()));
  }

  /**
   * Finds clients by their acronym. Logs the search process and results. If the acronym is blank,
   * returns a MissingRequiredParameterException.
   *
   * @param acronym the acronym to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  public Flux<ForestClientDto> findByAcronym(String acronym) {
    log.info("Searching for client with acronym {}", acronym);

    if (StringUtils.isBlank(acronym)) {
      return Flux.error(new MissingRequiredParameterException("acronym"));
    }

    Criteria queryCriteria = where("clientAcronym").is(acronym).ignoreCase(true);

    return searchClientByQuery(queryCriteria, ForestClientEntity.class).map(
            forestClientMapper::toDto).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with acronym {} as  {} {}", acronym, dto.clientNumber(),
                dto.clientName()));
  }

  /**
   * Finds clients by their doing business as (DBA) name. Logs the search process and results. If
   * the DBA name is blank, returns a MissingRequiredParameterException. If the isFuzzy parameter is
   * true, performs a fuzzy search.
   *
   * @param doingBusinessAs the DBA name to search for
   * @param isFuzzy         whether to perform a fuzzy search
   * @return a Flux containing the matching ForestClientDto objects
   */
  public Flux<ForestClientDto> findByDoingBusinessAs(String doingBusinessAs, boolean isFuzzy) {

    if (StringUtils.isBlank(doingBusinessAs)) {
      return Flux.error(new MissingRequiredParameterException("doingBusinessAs"));
    }

    return Mono.just(isFuzzy).filter(fuzzy -> fuzzy)
        .flatMapMany(fuzzy -> doingBusinessAsRepository.matchBy(doingBusinessAs)).switchIfEmpty(
            searchClientByQuery(where("doingBusinessAsName").is(doingBusinessAs).ignoreCase(true),
                ClientDoingBusinessAsEntity.class)).flatMap(entity -> searchClientByQuery(
            where(ApplicationConstants.CLIENT_NUMBER_LITERAL).is(entity.getClientNumber()),
            ForestClientEntity.class)).map(forestClientMapper::toDto)
        .distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with doing business as {} as [{}] {}", doingBusinessAs,
                dto.clientNumber(), dto.clientName()));
  }

  /**
   * Finds clients by their name. Logs the search process and results. If the client name is blank,
   * returns a MissingRequiredParameterException.
   *
   * @param clientName the name of the client to search for
   * @return a Flux containing the matching ForestClientDto objects
   */
  public Flux<ForestClientDto> findByClientName(String clientName) {
    log.info("Searching for client with name {}", clientName);

    if (StringUtils.isBlank(clientName)) {
      return Flux.error(new MissingRequiredParameterException(CLIENT_NAME));
    }

    Criteria queryCriteria = where(CLIENT_NAME).is(clientName).ignoreCase(true);

    return searchClientByQuery(queryCriteria, ForestClientEntity.class).map(
            forestClientMapper::toDto).distinct(ForestClientDto::clientNumber)
        .sort(Comparator.comparing(ForestClientDto::clientNumber)).doOnNext(
            dto -> log.info("Found client with name {} as  {} {}", clientName, dto.clientNumber(),
                dto.clientName()));
  }

  /**
   * Finds client details by their client number. Logs the search process and results. If the client
   * number is blank, returns a MissingRequiredParameterException. If no client is found, returns a
   * NoValueFoundException.
   *
   * @param clientNumber the client number to search for
   * @return a Mono containing the matching ForestClientDetailsDto object
   */
  public Mono<ForestClientDetailsDto> findByClientNumber(String clientNumber) {
    log.info("Searching for client with number {}", clientNumber);

    if (StringUtils.isBlank(clientNumber)) {
      return Mono.error(new MissingRequiredParameterException("clientNumber"));
    }

    return forestClientRepository
        .findDetailsByClientNumber(clientNumber)
        .map(informationDto -> new ForestClientDetailsDto(
            informationDto,
            List.of(),
            List.of(),
            null)
        )
        .flatMap(dto ->
            doingBusinessAsRepository
                .findLatestByClientNumber(dto.client().clientNumber())
                .map(dto::withDoingBusinessAs)
                .defaultIfEmpty(dto)
        )
        .flatMap(dto -> locationRepository
            .findLocationsByClientNumber(clientNumber)
            .collectList()
            .map(dto::withAddresses)
            .defaultIfEmpty(dto)
        )
        .flatMap(dto -> contactRepository
            .findContactsByClientNumber(clientNumber)
            .collectList()
            .map(dto::withContacts)
            .defaultIfEmpty(dto)
        )
        .switchIfEmpty(Mono.error(new NoValueFoundException("Client with number: " + clientNumber)))
        .doOnNext(dto -> log.info("Found client with client number {}", clientNumber));
  }

  /**
   * Performs a predictive search for clients using a "like" or similarity-based strategy.
   * If the search value is blank or null, a {@link MissingRequiredParameterException} is returned.
   *
   * <p>The method follows these steps:
   * <ul>
   *   <li>If the search value is blank, it returns a {@link MissingRequiredParameterException}.</li>
   *   <li>Attempts a "like"-based search to find matching clients and their count.</li>
   *   <li>If no matches are found, falls back to a similarity-based search.</li>
   *   <li>Logs the process and returns a {@link Flux} of pairs containing
   *       {@link PredictiveSearchResultDto} and the total match count.</li>
   * </ul>
   *
   * @param value the predictive search value (case-insensitive)
   * @param page  pagination parameters including page size and offset
   * @return a {@link Flux} of pairs with {@link PredictiveSearchResultDto} and match count
   */
  public Flux<Pair<PredictiveSearchResultDto, Long>> complexSearch(String value, Pageable page) {
    // This condition is for predictive search, and we will stop here if no query param is provided
    if (StringUtils.isBlank(value)) {
      return Flux.error(new MissingRequiredParameterException("value"));
    }

    return forestClientRepository.countByPredictiveSearchWithLike(value.toUpperCase(Locale.ROOT))
        .flatMapMany(count -> {
            if (count > 0) {
              return forestClientRepository
                  .findByPredictiveSearchWithLike(
                      value.toUpperCase(Locale.ROOT), page.getPageSize(), page.getOffset()
                  )
                  .doOnNext(dto -> log.info(
                      "Performed search with like for value {} as {} {} with score {}", 
                      value, dto.clientNumber(), dto.clientFullName(), dto.score())
                  )
                  .map(dto -> Pair.of(dto, count));
            } else {
              return forestClientRepository
                  .countByPredictiveSearchWithSimilarity(value.toUpperCase(Locale.ROOT))
                  .flatMapMany(similarityCount -> forestClientRepository
                      .findByPredictiveSearchWithSimilarity(
                          value.toUpperCase(Locale.ROOT), page.getPageSize(), page.getOffset()
                      )
                      .doOnNext(dto -> log.info(
                          "Performed search with similarity for value {} as {} {} with score {}",
                          value, dto.clientNumber(), dto.clientFullName(), dto.score()))
                      .map(dto -> Pair.of(dto, similarityCount)));
            }
        });
  }

  /**
   * Retrieves the latest entries of predictive search results. Logs the search process and
   * results.
   *
   * @param page the pagination information
   * @return a Flux containing pairs of PredictiveSearchResultDto objects and the total count of
   *         matching clients
   */
  public Flux<Pair<PredictiveSearchResultDto, Long>> latestEntries(Pageable page) {
    return forestClientRepository.countByEmptyFullSearch(
        LocalDateTime.now().minus(configuration.getData().getPredictiveCap())).flatMapMany(
        count -> forestClientRepository.findByEmptyFullSearch(page.getPageSize(), page.getOffset(),
            LocalDateTime.now().minus(configuration.getData().getPredictiveCap())).doOnNext(
            dto -> log.info("Found complex empty search as {} {} with score {}", dto.clientNumber(),
                dto.clientFullName(), dto.score())).map(dto -> Pair.of(dto, count)));
  }

  public Flux<ForestClientDto> searchByCorporationValues(
      String clientNumber,
      String companyType,
      String companyNumber
      ){
    return clientRepository
        .findByCompanyTypeOrNumber(clientNumber, companyType, companyNumber)
        .map(forestClientMapper::toDto);
  }

  public Flux<Pair<PredictiveSearchResultDto, Long>> searchByRelation(
      String clientNumber,
      String type,
      String value
  ) {
    if (StringUtils.isBlank(value)) {
      return Flux.error(new MissingRequiredParameterException("value"));
    }

    return forestClientRepository.countByRelatedClientWithLike(
            clientNumber,
            Optional.ofNullable(type).map(String::toUpperCase).orElse("NOVALUE"),
            value.toUpperCase(Locale.ROOT)
        )
        .flatMapMany(count -> {
          if (count > 0) {
            return forestClientRepository
                .findByRelatedClientWithLike(
                    clientNumber,
                    Optional.ofNullable(type).map(String::toUpperCase).orElse("NOVALUE"),
                    value.toUpperCase(Locale.ROOT),
                    //Why having pagination if hardcoded? We can add pagination if required
                    10, 0
                )
                .doOnNext(dto -> log.info(
                    "Performed related client search with like for value {} as {} {} with score {}",
                    value, dto.clientNumber(), dto.clientFullName(), dto.score())
                )
                .map(dto -> Pair.of(dto, count));
          } else {
            return forestClientRepository
                .countByRelatedClientWithSimilarity(
                    clientNumber,
                    Optional.ofNullable(type).map(String::toUpperCase).orElse("NOVALUE"),
                    value.toUpperCase(Locale.ROOT)
                )
                .flatMapMany(similarityCount -> forestClientRepository
                    .findByRelatedClientWithSimilarity(
                        clientNumber,
                        Optional.ofNullable(type).map(String::toUpperCase).orElse("NOVALUE"),
                        value.toUpperCase(Locale.ROOT),
                        //Why having pagination if hardcoded? We can add pagination if required
                        10, 0
                    )
                    .doOnNext(dto -> log.info(
                        "Performed related client search with similarity for value {} as {} {} with score {}",
                        value, dto.clientNumber(), dto.clientFullName(), dto.score()))
                    .map(dto -> Pair.of(dto, similarityCount)));
          }
        });
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
   * @param entityClass   The class of the entity to be retrieved.
   * @return A Flux stream of the entityClass objects.
   */
  private <T> Flux<T> searchClientByQuery(final Criteria queryCriteria,
      final Class<T> entityClass) {
    // Create a query based on the query criteria.
    Query searchQuery = Query.query(queryCriteria);

    log.info("Searching for clients with query {}", queryCriteria);

    return template.select(searchQuery.with(PageRequest.of(0, 1000))
            .sort(Sort.by(Sort.Order.asc(ApplicationConstants.CLIENT_NUMBER_LITERAL))), entityClass)
        .doOnNext(client -> log.info("Found client for query {}", queryCriteria));
  }

}
