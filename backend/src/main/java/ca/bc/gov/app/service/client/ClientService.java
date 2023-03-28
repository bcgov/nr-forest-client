package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionDetailEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationContactEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmitterEntity;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.ClientAddressDataDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;


@Service
@RequiredArgsConstructor
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
    return
        bcRegistryService
            .getCompanyStanding(clientNumber)
            .flatMap(details ->
                bcRegistryService
                    .getAddresses(clientNumber)
                    .onErrorReturn(NoClientDataFound.class,
                        new BcRegistryBusinessAdressesDto(null, null)
                    )
                    .flatMapIterable(expandAddresses())
                    .index()
                    .flatMap(buildAddress())
                    .collectList()
                    .map(buildDetails(details))
            );
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
        .searchByFacces(value)
        .map(entry -> new ClientLookUpDto(
            entry.identifier(),
            entry.name(),
            entry.status(),
            entry.legalType()
        ));
  }

  private Function<List<ClientAddressDataDto>, ClientDetailsDto> buildDetails(
      BcRegistryBusinessDto details) {
    return addresses -> new ClientDetailsDto(
        details.legalName(),
        details.identifier(),
        details.goodStanding(),
        addresses
    );
  }

  private Function<Tuple2<Long, BcRegistryAddressDto>, Mono<ClientAddressDataDto>> buildAddress() {
    return addressTuple ->
        Mono
            .just(
                new ClientAddressDataDto(
                    addressTuple.getT2().streetAddress(),
                    null,
                    null,
                    addressTuple.getT2().addressCity(),
                    addressTuple.getT2().postalCode(),
                    addressTuple.getT1().intValue()
                )
            )
            .flatMap(address ->
                loadCountry(addressTuple.getT2().addressCountry())
                    .map(address::withCountry)
            )
            .flatMap(address ->
                loadProvince(
                    addressTuple.getT2().addressCountry(),
                    addressTuple.getT2().addressRegion()
                )
                    .map(address::withProvince)
            );
  }

  private Mono<ClientValueTextDto> loadCountry(String countryCode) {
    return
        countryCodeRepository
            .findByCountryCode(countryCode)
            .map(
                entity -> new ClientValueTextDto(entity.getCountryCode(), entity.getDescription())
            );
  }

  private Mono<ClientValueTextDto> loadProvince(String countryCode, String province) {
    return
        provinceCodeRepository
            .findByCountryCodeAndProvinceCode(countryCode, province)
            .map(
                entity -> new ClientValueTextDto(entity.getProvinceCode(), entity.getDescription())
            );
  }

  private Function<BcRegistryBusinessAdressesDto, List<BcRegistryAddressDto>> expandAddresses() {
    return addresses ->
        Stream
            .of(addresses.mailingAddress(), addresses.deliveryAddress())
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(BcRegistryAddressDto::id))
            .toList();
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
