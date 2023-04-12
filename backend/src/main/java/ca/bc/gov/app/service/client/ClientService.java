package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionDetailEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationContactEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmitterEntity;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.client.SubmitterRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
  private final SubmissionRepository submissionRepository;

  private final SubmitterRepository submitterRepository;
  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionLocationRepository submissionLocationRepository;
  private final SubmissionLocationContactRepository submissionLocationContactRepository;
  private final BcRegistryService bcRegistryService;

  /**
   * <p><b>Find Active Client Type Codes</b></p>
   * <p>List client type code based on it's effective and expiration date.
   * The rule used for it is the expiration date must not be set or should be bigger than
   * provided date and the effective date bust be before or equal to the provided date.</p>
   * <p>The order is by description.</p>
   *
   * @param targetDate The date to be used as reference.
   * @return A list of {@link ClientNameCodeDto}
   */
  public Flux<ClientNameCodeDto> findActiveClientTypeCodes(LocalDate targetDate) {

    return
        clientTypeCodeRepository
            .findActiveAt(targetDate)
            .map(entity -> new ClientNameCodeDto(
                    entity.getCode(),
                    entity.getDescription()
                )
            );
  }

  /**
   * <p><b>List countries</b></p>
   * <p>List countries by page with a defined size.
   * The list will be sorted by order and country name.</p>
   * List countries by page with a defined size. The list will be sorted by order and country name.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link ClientNameCodeDto} entries.
   */
  public Flux<ClientNameCodeDto> listCountries(int page, int size) {
    return countryCodeRepository
        .findBy(PageRequest.of(page, size, Sort.by("order", "description")))
        .map(entity -> new ClientNameCodeDto(entity.getCountryCode(), entity.getDescription()));
  }

  /**
   * <p><b>List Provinces</b></p>
   * <p>List provinces by country (which include states) by page with a defined size.
   * The list will be sorted by province name.</p>
   *
   * @param countryCode The code of the country to list provinces from.
   * @param page        The page number, it is a 0-index base.
   * @param size        The amount of entries per page.
   * @return A list of {@link ClientNameCodeDto} entries.
   */
  public Flux<ClientNameCodeDto> listProvinces(String countryCode, int page, int size) {
    return provinceCodeRepository
        .findByCountryCode(countryCode, PageRequest.of(page, size, Sort.by("description")))
        .map(entity -> new ClientNameCodeDto(entity.getProvinceCode(), entity.getDescription()));
  }

  /**
   * <p><b>List contact types</b></p>
   * List contact type codes by page with a defined size.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link ClientNameCodeDto} entries.
   */
  public Flux<ClientNameCodeDto> listClientContactTypeCodes(int page, int size) {
    return contactTypeCodeRepository
        .findBy(PageRequest.of(page, size))
        .map(entity -> new ClientNameCodeDto(
            entity.getContactTypeCode(),
            entity.getDescription()));
  }


  /**
   * Submits a new client submission and returns a Mono of the submission ID.
   *
   * @param clientSubmissionDto the DTO representing the client submission
   * @return a Mono of the submission ID
   */
  public Mono<Integer> submit(ClientSubmissionDto clientSubmissionDto) {
    SubmissionEntity submissionEntity =
        SubmissionEntity
            .builder()
            .submitterUserId(UUID.randomUUID().toString()) //TODO: set the correct user
            .submissionStatus(SubmissionStatusEnum.S)
            .submissionDate(LocalDateTime.now())
            .createdBy(UUID.randomUUID().toString()) //TODO: receive user id
            .build();

    return submissionRepository.save(submissionEntity)
        .map(submission ->
            mapToSubmitterEntity(
                submission.getSubmissionId(),
                clientSubmissionDto.submitterInformation()))
        .flatMap(submitterRepository::save)
        .map(submitter ->
            mapToSubmissionDetailEntity(
                submitter.getSubmissionId(),
                clientSubmissionDto)
        )
        .flatMap(submissionDetailRepository::save)
        .flatMap(submissionDetail ->
            submitLocations(
                clientSubmissionDto.location(),
                submissionDetail.getSubmissionId())
                .thenReturn(submissionDetail.getSubmissionId())
        );
  }

  /**
   * Retrieves the client details for a given client number by making calls to BC Registry service.
   * The details include the company standing and addresses.
   *
   * @param clientNumber the client number for which to retrieve details
   * @return a Mono that emits a ClientDetailsDto object representing the details of the client
   */
  public Mono<ClientDetailsDto> getClientDetails(String clientNumber) {
    log.info("Loading details for {}", clientNumber);
    return
        bcRegistryService
            .requestDocumentData(clientNumber)
            .next()
            .flatMap(buildDetails());
  }

  /**
   * Searches the BC Registry API for {@link BcRegistryFacetSearchResultEntryDto} instances
   * matching the given value and maps them to {@link ClientLookUpDto} instances.
   *
   * @param value the value to search for
   * @return a {@link Flux} of {@link ClientLookUpDto} instances representing
   * matching BC Registry entries
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
        ));
  }

  private Function<BcRegistryDocumentDto, Mono<ClientDetailsDto>> buildDetails() {
    return document ->
        buildAddress(document,
            new ClientDetailsDto(
                document.business().legalName(),
                document.business().identifier(),
                document.business().goodStanding(),
                List.of()
            )
        );
  }

  private Mono<ClientDetailsDto> buildAddress(
      BcRegistryDocumentDto document, ClientDetailsDto clientDetails) {
    AtomicInteger index = new AtomicInteger(0);
    return
        Flux.fromIterable(
                document
                    .matchOfficesParties()
                    .entrySet()
            )
            .flatMap(mapToClientAddress(index))
            .flatMap(address -> loadCountry(address.country().text()).map(address::withCountry))
            .flatMap(address ->
                loadProvince(address.country().value(), address.province().value())
                    .map(address::withProvince)
            )
            .collectList()
            .doOnNext(addresses -> log.info("Address list built {}", addresses))
            .map(clientDetails::withAddresses);
  }

  private Function<Map.Entry<BcRegistryAddressDto, Set<BcRegistryPartyDto>>, Mono<ClientAddressDto>>
  mapToClientAddress(AtomicInteger index) {
    return entry ->
        loadContacts(entry.getValue())
            .map(contacts ->
                new ClientAddressDto(
                    entry.getKey().streetAddress(),
                    new ClientValueTextDto("", entry.getKey().addressCountry()),
                    new ClientValueTextDto(entry.getKey().addressRegion(), ""),
                    entry.getKey().addressCity(),
                    entry.getKey().postalCode(),
                    index.getAndIncrement(),
                    contacts
                )
            )
            .doOnNext(address -> log.info("Building address {}", address));
  }

  private Mono<List<ClientContactDto>> loadContacts(Set<BcRegistryPartyDto> parties) {
    AtomicInteger index = new AtomicInteger(0);
    return
        Flux
            .fromIterable(parties)
            .filter(BcRegistryPartyDto::isValid)
            .map(party ->
                new ClientContactDto(
                    new ClientValueTextDto(party.officer().partyType(), ""),
                    party.officer().firstName(),
                    party.officer().lastName(),
                    "",
                    party.officer().email(),
                    index.getAndIncrement()
                )
            )
            .doOnNext(contact -> log.info("Converting client contact {}", contact))
            .flatMap(contact -> loadContactType(contact.contactType().value())
                .map(contact::withContactType)
            )
            .collectList()
            .defaultIfEmpty(new ArrayList<>());
  }

  private Mono<ClientValueTextDto> loadContactType(String contactCode) {
    return
        contactTypeCodeRepository
            .findById(contactCode)
            .map(
                entity -> new ClientValueTextDto(
                    entity.getContactTypeCode(),
                    entity.getDescription()
                )
            ).defaultIfEmpty(new ClientValueTextDto(contactCode, contactCode));
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

  private Mono<Void> submitLocations(ClientLocationDto clientLocationDto, Integer submissionId) {
    return Flux.fromIterable(clientLocationDto
            .addresses())
        .flatMap(addressDto ->
            submissionLocationRepository
                .save(mapToSubmissionLocationEntity(submissionId, addressDto))
                .flatMap(location ->
                    submitLocationContacts(addressDto, location.getSubmissionLocationId())))
        .then();
  }

  private Mono<Void> submitLocationContacts(ClientAddressDto addressDto,
                                            Integer submissionLocationId) {
    return Flux
        .fromIterable(addressDto.contacts())
        .flatMap(contactDto ->
            submissionLocationContactRepository.save(
                mapToSubmissionLocationContactEntity(
                    submissionLocationId,
                    contactDto)))
        .then();
  }


}
