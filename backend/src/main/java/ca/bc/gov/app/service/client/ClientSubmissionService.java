package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionDetailEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationContactEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionLocationEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmitterEntity;
import static org.springframework.data.relational.core.query.Query.query;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.predicates.AbstractQueryPredicates;
import ca.bc.gov.app.predicates.SubmissionDetailPredicates;
import ca.bc.gov.app.predicates.SubmissionPredicates;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.client.SubmitterRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionService {

  private final SubmissionRepository submissionRepository;
  private final SubmitterRepository submitterRepository;
  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionLocationRepository submissionLocationRepository;
  private final SubmissionLocationContactRepository submissionLocationContactRepository;
  private final R2dbcEntityTemplate template;


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

  public Flux<ClientListSubmissionDto> listSubmissions(
      int page,
      int size,
      String[] requestType,
      String[] requestStatus,
      String[] clientType,
      String[] name,
      String[] updatedAt
  ) {

    log.info("Searching for Page {} Size {} Type {} Status {} Client {} Name {} Updated {}",
        page,
        size,
        requestType,
        requestStatus,
        clientType,
        name,
        updatedAt
    );

    return
        template
            .select(
                query(
                    //Commenting it out as we don't have the requestType field
                    /*AbstractQueryPredicates*/
                        /*.orEqualTo(requestType, "requestType")*/
                        /*.and(*/SubmissionPredicates.orStatus(requestStatus)/*)*/
                        .and(SubmissionPredicates.orUpdatedAt(updatedAt))
                )
                    .with(PageRequest.of(page, size)),
                SubmissionEntity.class
            )
            .flatMap(submission ->
                template
                    .selectOne(
                        query(
                            Criteria
                                .where("submissionId")
                                .is(submission.getSubmissionId())
                                .and(
                                    AbstractQueryPredicates
                                        .orEqualTo(clientType, "clientTypeCode")
                                        .and(SubmissionDetailPredicates.orName(name))
                                )
                        ),
                        SubmissionDetailEntity.class
                    )
                    .map(submissionDetail ->
                        new ClientListSubmissionDto(
                            submission.getSubmissionId(),
                            "", //TODO: Must include and process
                            submissionDetail.getDisplayName(),
                            submissionDetail.getClientTypeCode(),
                            String.format("%s | %s",
                                StringUtils.defaultString(submission.getUpdatedBy()),
                                Optional
                                    .ofNullable(submission.getUpdatedAt())
                                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    .orElse(StringUtils.EMPTY)
                            ),
                            submission
                                .getSubmissionStatus()
                                .getDescription()
                        )
                    )
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
