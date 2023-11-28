package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.ClientAlreadyExistException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import ca.bc.gov.app.service.ches.ChesService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
public class ClientService {

  private final ClientTypeCodeRepository clientTypeCodeRepository;
  private final CountryCodeRepository countryCodeRepository;
  private final ProvinceCodeRepository provinceCodeRepository;
  private final ContactTypeCodeRepository contactTypeCodeRepository;
  private final BcRegistryService bcRegistryService;
  private final ChesService chesService;
  private final ClientLegacyService legacyService;

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
    return countryCodeRepository
        .findBy(PageRequest.of(page, size, Sort.by("order", "description")))
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
    return countryCodeRepository
        .findByCountryCode(countryCode)
        .map(entity -> new CodeNameDto(entity.getCountryCode(),
                                       entity.getDescription()));
  }

  /**
   * Retrieves a client type by its unique code.
   * This method queries the clientTypeCodeRepository to find a client type entity
   * with the specified code. If a matching entity is found, it is converted to a
   * {@code CodeNameDto} object containing the code and description, and wrapped
   * in a Mono. If no matching entity is found, the Mono will complete without emitting
   * any items.
   *
   * @param code The unique code of the client type to retrieve.
   * @return A Mono emitting a {@code CodeNameDto} if a matching client type is found, or an
   *         empty result if no match is found.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getClientTypeByCode(String code) {
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
  public Flux<CodeNameDto> listClientContactTypeCodes(int page, int size) {
    return contactTypeCodeRepository
        .findBy(PageRequest.of(page, size))
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
      String clientNumber
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
                    .searchLegacy(document.business().identifier(), document.business().legalName())
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
            .flatMap(buildDetails());
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
   * @return A {@link Mono} of {@link Void}.
   */
  public Mono<Void> sendEmail(EmailRequestDto emailRequestDto) {
    return triggerEmail(emailRequestDto).then();
  }

  /**
   * <p><b>Send Email</b></p>
   * <p>Send email to the client when entry already exists.</p>
   *
   * @param emailRequestDto The request data containing user and client details.
   * @return A {@link Mono} of {@link Void}.
   */
  public Mono<Void> triggerEmailDuplicatedClient(EmailRequestDto emailRequestDto) {
    return
        legacyService
            .searchLegacy(emailRequestDto.incorporation(), emailRequestDto.name())
            .next()
            .flatMap(
                triggerEmailDuplicatedClient(emailRequestDto.email(), emailRequestDto.userName()))
            .then();
  }

  private Function<BcRegistryDocumentDto, Mono<ClientDetailsDto>> buildDetails() {
    return document ->
        buildAddress(document,
            new ClientDetailsDto(
                document.business().legalName(),
                document.business().identifier(),
                document.business().goodStanding(),
                List.of(),
                List.of()
            )
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
            .map(addressDto ->
                new ClientAddressDto(
                    addressDto.streetAddress(),
                    new ClientValueTextDto("", addressDto.addressCountry()),
                    new ClientValueTextDto(addressDto.addressRegion(), ""),
                    addressDto.addressCity(),
                    addressDto.postalCode().trim().replaceAll("\\s+", ""),
                    index.getAndIncrement(),
                    (addressDto.addressType() != null ? addressDto.addressType() : "").concat(" address").toUpperCase()
                )
            )
            .flatMap(address -> loadCountry(address.country().text()).map(address::withCountry))
            .flatMap(address ->
                loadProvince(address.country().value(), address.province().value())
                    .map(address::withProvince)
            )
            .collectList()
            .defaultIfEmpty(new ArrayList<>())
            .flatMap(addresses ->
                Flux.fromIterable(document.parties())
                    .map(party ->
                        new ClientContactDto(
                            null,
                            party.officer().firstName(),
                            party.officer().lastName(),
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

    return legacy -> chesService.sendEmail("matched",
            email,
            "Client number application can’t go ahead",
            legacy.description(userName))
        .thenReturn(legacy);
  }

  private Mono<String> triggerEmail(EmailRequestDto emailRequestDto) {
    return chesService.sendEmail(emailRequestDto.templateName(),
        emailRequestDto.email(),
        emailRequestDto.subject(),
        emailRequestDto.variables());
  }

}
