package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.util.ClientMapper.getLocationIdByName;
import static ca.bc.gov.app.util.ClientMapper.mapAllToSubmissionLocationEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionDetailEntity;
import static org.springframework.data.relational.core.query.Query.query;

import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.models.client.SubmissionTypeCodeEnum;
import ca.bc.gov.app.predicates.QueryPredicates;
import ca.bc.gov.app.predicates.SubmissionDetailPredicates;
import ca.bc.gov.app.predicates.SubmissionPredicates;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.service.ches.ChesCommonServicesService;
import ca.bc.gov.app.util.ClientMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionLocationRepository submissionLocationRepository;
  private final SubmissionContactRepository submissionContactRepository;
  private final SubmissionLocationContactRepository submissionLocationContactRepository;
  private final ChesCommonServicesService chesService;
  private final R2dbcEntityTemplate template;

  public Flux<ClientListSubmissionDto> listSubmissions(
      int page,
      int size,
      String[] requestType,
      SubmissionStatusEnum[] requestStatus,
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
                    QueryPredicates
                    .orEqualTo(requestType, "submissionType")
                    .and(SubmissionPredicates.orStatus(requestStatus))
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
                                    QueryPredicates
                                        .orEqualTo(clientType, "clientTypeCode")
                                        .and(SubmissionDetailPredicates.orName(name))
                                )
                        ),
                        SubmissionDetailEntity.class
                    )
                    .map(submissionDetail ->
                        new ClientListSubmissionDto(
                            submission.getSubmissionId(),
                            submission.getSubmissionType().getDescription(),
                            submissionDetail.getOrganizationName(),
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

  /**
   * Submits a new client submission and returns a Mono of the submission ID.
   *
   * @param clientSubmissionDto the DTO representing the client submission
   * @return a Mono of the submission ID
   */
  public Mono<Integer> submit(
      ClientSubmissionDto clientSubmissionDto,
      String userId,
      String userEmail
  ) {

    return
        Mono
            .just(
                SubmissionEntity
                    .builder()
                    .submissionStatus(SubmissionStatusEnum.N)
                    .submissionType(SubmissionTypeCodeEnum.SPP)
                    .submissionDate(LocalDateTime.now())
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build()
            )
            //Save submission to begin with
            .flatMap(submissionRepository::save)
            //Save the submission detail
            .map(submission -> mapToSubmissionDetailEntity(submission.getSubmissionId(),
                clientSubmissionDto.businessInformation())
            )
            .flatMap(submissionDetailRepository::save)
            //Save the locations and contacts and do the association
            .flatMap(submission ->
                //Save all locations
                saveAddresses(clientSubmissionDto, submission)
                    //For each contact, save it,
                    // then find the associated location and save the association
                    .flatMapMany(locations ->
                        //Best way to handle lists reactively is by using Flux
                        //Convert the list into a flux and process each entry individually
                        Flux.fromIterable(
                                clientSubmissionDto
                                    .location()
                                    .contacts()
                            )
                            .flatMap(contact -> saveAndAssociateContact(locations, contact,
                                submission.getSubmissionId()))
                    )
                    //Then grab all back as a list, to make all reactive flows complete
                    .collectList()
                    //Return what we need only
                    .thenReturn(submission.getSubmissionId())
            )
            .flatMap(submissionId -> sendEmail(submissionId, clientSubmissionDto, userEmail));
  }

  private Mono<SubmissionLocationContactEntity> saveAndAssociateContact(
      List<SubmissionLocationEntity> locations,
      ClientContactDto contact,
      Integer submissionId
  ) {
    return submissionContactRepository
        .save(ClientMapper.mapToSubmissionContactEntity(contact).withSubmissionId(submissionId))
        .map(contactEntity ->
            SubmissionLocationContactEntity
                .builder()
                .submissionLocationId(getLocationIdByName(locations, contact))
                .submissionContactId(contactEntity.getSubmissionContactId())
                .build()
        )
        .flatMap(submissionLocationContactRepository::save);
  }

  private Mono<List<SubmissionLocationEntity>> saveAddresses(
      ClientSubmissionDto clientSubmissionDto,
      SubmissionDetailEntity submission) {
    return submissionLocationRepository
        .saveAll(
            mapAllToSubmissionLocationEntity(
                submission.getSubmissionId(),
                clientSubmissionDto.location().addresses())
        )
        .collectList();
  }

  private Mono<Integer> sendEmail(
      Integer submissionId,
      ClientSubmissionDto clientSubmissionDto,
      String email
  ) {
    return
        chesService
            .buildTemplate(
                "registration",
                clientSubmissionDto.description()
            )
            .flatMap(body ->
                chesService
                    .sendEmail(
                        new ChesRequest(
                            List.of(email),
                            body
                        )
                    )
            )
            .doOnNext(mailId -> log.info("Mail sent, transaction ID is {}", mailId))
            .thenReturn(submissionId);
  }

}
