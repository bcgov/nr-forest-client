package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.dto.client.IdentificationTypeDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.ClientAlreadyExistException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.exception.UnsuportedClientTypeException;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.DistrictCodeRepository;
import ca.bc.gov.app.repository.client.IdentificationTypeCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import ca.bc.gov.app.service.ches.ChesService;
import ca.bc.gov.app.util.ClientValidationUtils;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientService {

  private final ClientTypeCodeRepository clientTypeCodeRepository;
  private final DistrictCodeRepository districtCodeRepository;
  private final CountryCodeRepository countryCodeRepository;
  private final ProvinceCodeRepository provinceCodeRepository;
  private final ContactTypeCodeRepository contactTypeCodeRepository;
  private final IdentificationTypeCodeRepository identificationTypeCodeRepository;
  private final BcRegistryService bcRegistryService;
  private final ChesService chesService;
  private final ClientLegacyService legacyService;
  private final Predicate<BcRegistryAddressDto> isMultiAddressEnabled;

  LocalDate currentDate = LocalDate.now();

  /**
   * <p><b>Find Active Client Type Codes</b></p>
   * <p>List client type code based on it's effective and expiration date.
   * The rule used for it is the expiration date must not be set or should be bigger than provided
   * date and the effective date bust be before or equal to the provided date.</p>
   * <p>The order is by description.</p>
   *
   * @param targetDate The date to be used as reference.
   * @return A list of {@link CodeNameDto}
   */
  public Flux<CodeNameDto> findActiveClientTypeCodes(LocalDate targetDate) {
    log.info("Loading active client type codes for {}", targetDate);
    return
        clientTypeCodeRepository
            .findActiveAt(targetDate)
            .map(entity -> new CodeNameDto(
                    entity.getCode(),
                    entity.getDescription()
                )
            );
  }

  /**
   * <p><b>List natural resource districts</b></p>
   * <p>List natural resource districts by page with a defined size.</p>
   * List natural resource districts by page with a defined size. The list will be sorted by
   * district name.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> getActiveDistrictCodes(int page, int size) {
    log.info("Loading natural resource districts for page {} with size {}", page, size);
    return districtCodeRepository
        .findAllBy(PageRequest.of(page, size, Sort.by("description")))
        .filter(entity -> (currentDate.isBefore(entity.getExpiredAt())
                           || currentDate.isEqual(entity.getExpiredAt()))
                          &&
                          (currentDate.isAfter(entity.getEffectiveAt())
                           || currentDate.isEqual(entity.getEffectiveAt())))
        .map(entity -> new CodeNameDto(entity.getCode(), entity.getDescription()));
  }

  /**
   * <p><b>List countries</b></p>
   * <p>List countries by page with a defined size.
   * The list will be sorted by order and country name.</p> List countries by page with a defined
   * size. The list will be sorted by order and country name.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listCountries(int page, int size) {
    log.info("Loading countries for page {} with size {}", page, size);
    return countryCodeRepository
        .findAllBy(PageRequest.of(page, size, Sort.by("order", "description")))
        .filter(entity -> (currentDate.isBefore(entity.getExpiredAt())
                           || currentDate.isEqual(entity.getExpiredAt()))
                          &&
                          (currentDate.isAfter(entity.getEffectiveAt())
                           || currentDate.isEqual(entity.getEffectiveAt())))
        .map(entity -> new CodeNameDto(entity.getCountryCode(), entity.getDescription()));
  }

  /**
   * Retrieves country information by its country code. This method queries the
   * {@code countryCodeRepository} to find a country entity with the specified country code. If a
   * matching entity is found, it is mapped to a {@code CodeNameDto} object, which encapsulates the
   * country code and description. The resulting data is wrapped in a Mono, which represents the
   * asynchronous result of the operation.
   *
   * @param countryCode The code of the country to retrieve information for.
   * @return A Mono that emits the {@code CodeNameDto} object if a matching country is found, or an
   * empty result if no match is found.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getCountryByCode(String countryCode) {
    log.info("Loading country for {}", countryCode);
    return countryCodeRepository
        .findByCountryCode(countryCode)
        .map(entity -> new CodeNameDto(entity.getCountryCode(),
            entity.getDescription()));
  }

  /**
   * Retrieves a client type by its unique code. This method queries the clientTypeCodeRepository to
   * find a client type entity with the specified code. If a matching entity is found, it is
   * converted to a {@code CodeNameDto} object containing the code and description, and wrapped in a
   * Mono. If no matching entity is found, the Mono will complete without emitting any items.
   *
   * @param code The unique code of the client type to retrieve.
   * @return A Mono emitting a {@code CodeNameDto} if a matching client type is found, or an empty
   * result if no match is found.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getClientTypeByCode(String code) {
    log.info("Loading client type for {}", code);
    return clientTypeCodeRepository
        .findByCode(code)
        .map(entity -> new CodeNameDto(entity.getCode(),
            entity.getDescription()));
  }

  /**
   * <p><b>List Provinces</b></p>
   * <p>List provinces by country (which include states) by page with a defined size.
   * The list will be sorted by province name.</p>
   *
   * @param countryCode The code of the country to list provinces from.
   * @param page        The page number, it is a 0-index base.
   * @param size        The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listProvinces(String countryCode, int page, int size) {
    log.info("Loading provinces for {} with page {} and size {}", countryCode, page, size);
    return provinceCodeRepository
        .findByCountryCode(countryCode, PageRequest.of(page, size, Sort.by("description")))
        .map(entity -> new CodeNameDto(entity.getProvinceCode(), entity.getDescription()));
  }

  /**
   * <p><b>List contact types</b></p>
   * List contact type codes by page with a defined size.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listClientContactTypeCodes(LocalDate activeDate, int page, int size) {
    log.info("Loading contact types for page {} with size {}", page, size);
    return contactTypeCodeRepository
        .findActiveAt(activeDate, PageRequest.of(page, size))
        .map(entity -> new CodeNameDto(
            entity.getContactTypeCode(),
            entity.getDescription()));
  }

  /**
   * Retrieves the client details for a given client number by making calls to BC Registry service.
   * The details include the company standing and addresses.
   *
   * @param clientNumber the client number for which to retrieve details
   * @return a Mono that emits a ClientDetailsDto object representing the details of the client
   */
  public Mono<ClientDetailsDto> getClientDetails(
      String clientNumber,
      String userId,
      String businessId,
      String provider
  ) {
    log.info("Loading details for {}", clientNumber);
    return
        bcRegistryService
            .requestDocumentData(clientNumber)
            .next()
            .doOnNext(document ->
                log.info("Searching on Oracle legacy db for {} {}",
                    document.business().identifier(),
                    document.business().legalName()
                )
            )
            .flatMap(document ->
                legacyService
                    .searchLegacy(
                        document.business().identifier(),
                        document.business().legalName(),
                        userId,
                        businessId
                    )
                    .next()
                    .filter(isMatchWith(document))
                    .doOnNext(legacy ->
                        log.info("Found legacy entry for {} {}",
                            document.business().identifier(),
                            document.business().legalName()
                        )
                    )
                    .flatMap(legacy -> Mono
                        .error(
                            new ClientAlreadyExistException(
                                legacy.clientNumber(),
                                document.business().identifier(),
                                document.business().legalName())
                        )
                    )
                    .defaultIfEmpty(document)
                    .doOnNext(value ->
                        log.info("No entry found on legacy for {} {}",
                            document.business().identifier(), document.business().legalName()
                        )
                    )
            )
            .map(BcRegistryDocumentDto.class::cast)

            .flatMap(client -> {
              // FSADT1-1388: Allow IDIR users to search for any client type
              if(provider.equalsIgnoreCase("idir"))
                return Mono.just(client);

              if (ApplicationConstant.AVAILABLE_CLIENT_TYPES.contains(
                  ClientValidationUtils.getClientType(
                          LegalTypeEnum.valueOf(client.business().legalType())
                      )
                      .toString()
              )
              ) {
                return Mono.just(client);
              }
              return Mono.error(
                  new UnsuportedClientTypeException(ClientValidationUtils.getClientType(
                          LegalTypeEnum.valueOf(client.business().legalType())
                      )
                      .toString()
                  ));
            })

            //if document type is SP and party contains only one entry that is not a person, fail
            .filter(document ->
                // FSADT1-1388: Allow IDIR users to search for any client type
                provider.equalsIgnoreCase("idir") ||
                !("SP".equalsIgnoreCase(document.business().legalType())
                  && document.parties().size() == 1
                  && !document.parties().get(0).isPerson()
                )
            )
            .flatMap(buildDetails())
            .switchIfEmpty(Mono.error(new UnableToProcessRequestException(
                "Unable to process request. This sole proprietor is not owner by a person"
            )));
  }

  /**
   * Searches the BC Registry API for {@link BcRegistryFacetSearchResultEntryDto} instances matching
   * the given value and maps them to {@link ClientLookUpDto} instances.
   *
   * @param value the value to search for
   * @return a {@link Flux} of {@link ClientLookUpDto} instances representing matching BC Registry
   * entries
   * @throws NoClientDataFound           if no matching data is found
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Flux<ClientLookUpDto> findByClientNameOrIncorporation(String value) {
    log.info("Searching for {}", value);
    return bcRegistryService
        .searchByFacets(value)
        .map(entry -> new ClientLookUpDto(
                entry.identifier(),
                entry.name(),
                entry.status(),
                entry.legalType()
            )
        );
  }

  /**
   * <p><b>Send Email</b></p>
   * <p>Send email to a client.</p>
   *
   * @param emailRequestDto The request data containing client details.
   * @return A {@link Mono} of {@link String}.
   */
  public Mono<String> sendEmail(EmailRequestDto emailRequestDto) {
    return triggerEmail(emailRequestDto);
  }

  /**
   * <p><b>Send Email</b></p>
   * <p>Send email to the client when entry already exists.</p>
   *
   * @param emailRequestDto The request data containing user and client details.
   * @return A {@link Mono} of {@link Void}.
   */
  public Mono<Void> triggerEmailDuplicatedClient(
      EmailRequestDto emailRequestDto,
      String userId,
      String businessId
  ) {
    return
        legacyService
            .searchLegacy(
                emailRequestDto.registrationNumber(),
                emailRequestDto.name(),
                userId,
                businessId
            )
            .next()
            .flatMap(
                triggerEmailDuplicatedClient(emailRequestDto.email(), emailRequestDto.userName()))
            .then();
  }

  public Mono<Void> findByIndividual(String userId, String lastName) {
    return legacyService
        .searchIdAndLastName(userId, lastName)
        .doOnNext(legacy -> log.info("Found legacy entry for {} {}", userId, lastName))
        //If we have result, we return a Mono.error with the exception, otherwise return a Mono.empty
        .next()
        .flatMap(legacy -> Mono
            .error(new ClientAlreadyExistException(legacy.clientNumber()))
        );
  }
  
  public Mono<String> findByUserIdAndLastName(String userId, String lastName) {
    return legacyService
        .searchIdAndLastName(userId, lastName)
        .doOnNext(legacy -> log.info("Found legacy entry for {} {}", userId, lastName))
        .next()
        .map(ForestClientDto::clientNumber);
  }
  
  private Function<BcRegistryDocumentDto, Mono<ClientDetailsDto>> buildDetails() {
    return document ->
        buildAddress(
            document,
            buildSimpleClientDetails(document.business())
        );
  }

  private ClientDetailsDto buildSimpleClientDetails(
      BcRegistryBusinessDto businessDto
  ) {

    if (businessDto == null) {
      return new ClientDetailsDto(
          "",
          "",
          false,
          List.of(),
          List.of()
      );
    }
    log.info("Building simple client details for {} with standing {}", businessDto.identifier(),
        businessDto.goodStanding());
    return
        new ClientDetailsDto(
            businessDto.legalName(),
            businessDto.identifier(),
            businessDto.goodStanding(),
            List.of(),
            List.of()
        );
  }

  private Mono<ClientDetailsDto> buildAddress(
      BcRegistryDocumentDto document,
      ClientDetailsDto clientDetails
  ) {
    AtomicInteger index = new AtomicInteger(0);

    return
        Flux.fromIterable(
                document
                    .offices()
                    .addresses()
            )
            .filter(BcRegistryAddressDto::isValid)
            .filter(isMultiAddressEnabled)
            .map(addressDto ->
                new ClientAddressDto(
                    addressDto.streetAddress(),
                    null,
                    null,
                    new ClientValueTextDto("", addressDto.addressCountry()),
                    new ClientValueTextDto(addressDto.addressRegion(), ""),
                    addressDto.addressCity(),
                    Optional
                        .ofNullable(addressDto.postalCode())
                        .map(String::trim)
                        .map(value -> value.replaceAll("\\s+", ""))
                        .orElse(StringUtils.EMPTY),
                    null,
                    null,
                    null,
                    null,
                    null,
                    index.getAndIncrement(),
                    (addressDto.addressType() != null ? addressDto.addressType() : "").concat(
                        " address").toUpperCase()
                )
            )
            .flatMap(address -> loadCountry(address.country().text()).map(address::withCountry))
            .flatMap(address ->
                loadProvince(address.country().value(), address.province().value())
                    .map(address::withProvince)
            )
            .sort(Comparator.comparing(ClientAddressDto::locationName).reversed())
            .collectList()
            .defaultIfEmpty(new ArrayList<>())
            .flatMap(addresses ->
                Flux.fromIterable(document.parties())
                    .filter(party ->
                        !"SP".equalsIgnoreCase(document.business().legalType()) || party.isPerson())
                    .map(party ->
                        new ClientContactDto(
                            null,
                            party.officer().firstName(),
                            party.officer().lastName(),
                            "",
                            "",
                            "",
                            party.officer().email(),
                            index.getAndIncrement(),
                            matchAddress(addresses, party)
                        )
                    )
                    .collectList()
                    .defaultIfEmpty(new ArrayList<>())
                    .map(contacts -> clientDetails
                        .withAddresses(addresses)
                        .withContacts(contacts)
                    )
            );
  }

  private static List<ClientValueTextDto> matchAddress(List<ClientAddressDto> addresses,
      BcRegistryPartyDto party) {
    return addresses
        .stream()
        .filter(address ->
            party.isMatch(
                address.city(),
                address.country().text(),
                address.province().value(),
                address.postalCode(),
                address.streetAddress()
            )
        )
        .map(address -> new ClientValueTextDto(
                String.valueOf(address.index()),
                address.locationName()
            )
        )
        .toList();
  }

  private Mono<ClientValueTextDto> loadCountry(String countryCode) {
    return
        countryCodeRepository
            .findByDescription(countryCode)
            .map(
                entity -> new ClientValueTextDto(entity.getCountryCode(), entity.getDescription())
            )
            .defaultIfEmpty(new ClientValueTextDto(StringUtils.EMPTY, countryCode));
  }

  private Mono<ClientValueTextDto> loadProvince(String countryCode, String province) {
    return
        provinceCodeRepository
            .findByCountryCodeAndProvinceCode(countryCode, province)
            .map(
                entity -> new ClientValueTextDto(entity.getProvinceCode(), entity.getDescription())
            )
            .defaultIfEmpty(new ClientValueTextDto(province, province));
  }

  private Predicate<ForestClientDto> isMatchWith(BcRegistryDocumentDto document) {
    return legacy ->
        StringUtils.equals(
            StringUtils.defaultString(legacy.registryCompanyTypeCode()) +
            StringUtils.defaultString(legacy.corpRegnNmbr()),
            document.business().identifier()
        ) &&
        StringUtils.equals(
            document.business().legalName(),
            legacy.legalName()
        );
  }

  private Function<ForestClientDto, Mono<ForestClientDto>> triggerEmailDuplicatedClient(
      String email, String userName) {

    return legacy -> chesService.sendEmail(
            "matched",
            email,
            "Client number application can’t go ahead",
            legacy.description(userName),
            null)
        .thenReturn(legacy);
  }

  private Mono<String> triggerEmail(EmailRequestDto emailRequestDto) {
    return chesService.sendEmail(
        emailRequestDto.templateName(),
        emailRequestDto.email(),
        emailRequestDto.subject(),
        emailRequestDto.variables(),
        null);
  }

  /**
   * Retrieves natural resource district information by its district code. This method queries the
   * {@code districtCodeRepository} to find a district entity with the specified district code. If a
   * matching entity is found, it is mapped to a {@code DistrictDto} object, which encapsulates the
   * district code, description and email. The resulting data is wrapped in a Mono, which represents
   * the asynchronous result of the operation.
   *
   * @param districtCode The code of the district to retrieve information for.
   * @return A Mono that emits the {@code DistrictDto} object if a matching district is found, or an
   * empty result if no match is found.
   * @see DistrictDto
   */
  public Mono<DistrictDto> getDistrictByCode(String districtCode) {
    log.info("Loading district for {}", districtCode);
    return districtCodeRepository
        .findByCode(districtCode)
        .map(entity -> new DistrictDto(
                entity.getCode(),
                entity.getDescription(),
                entity.getEmailAddress()
            )
        );
  }

  /**
   * Retrieves all active identification types as of the specified target date.
   *
   * @param targetDate the date to check for active identification types.
   * @return a Flux stream of IdentificationTypeDto containing the code, description, and country code of each active identification type.
   */
  public Flux<IdentificationTypeDto> getAllActiveIdentificationTypes(LocalDate targetDate) {
    log.info("Loading active identification type codes by {}", targetDate);
    return identificationTypeCodeRepository
        .findActiveAt(targetDate)
        .map(entity -> new IdentificationTypeDto(
                             entity.getCode(), 
                             entity.getDescription(),
                             entity.getCountryCode()));
  }

  /**
   * Retrieves an identification type by its code.
   *
   * @param idCode the code of the identification type to retrieve.
   * @return a Mono containing a CodeNameDto with the code and description of the identification type, or an empty Mono if not found.
   */
  public Mono<CodeNameDto> getIdentificationTypeByCode(String idCode) {
    log.info("Loading identification type by {}", idCode);
    return identificationTypeCodeRepository
        .findByCode(idCode)
        .map(entity -> new CodeNameDto(entity.getCode(),
                                       entity.getDescription()));
  }

}
