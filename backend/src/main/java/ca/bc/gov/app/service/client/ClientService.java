package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionDetailEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationContactEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmitterEntity;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.entity.client.SubmissionEntity;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
   * The list will be sorted by order and country contactFirstName.</p>
   * List countries by page with a defined size. The list will be sorted by order and country contactFirstName.
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
   * The list will be sorted by province contactFirstName.</p>
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
