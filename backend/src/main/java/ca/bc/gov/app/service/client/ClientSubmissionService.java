package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.util.ClientMapper.getLocationIdByName;
import static ca.bc.gov.app.util.ClientMapper.mapAllToSubmissionLocationEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionContactEntity;
import static ca.bc.gov.app.util.ClientMapper.mapToSubmissionDetailEntity;
import static org.springframework.data.relational.core.query.Query.query;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.submissions.SubmissionAddressDto;
import ca.bc.gov.app.dto.submissions.SubmissionApproveRejectDto;
import ca.bc.gov.app.dto.submissions.SubmissionBusinessDto;
import ca.bc.gov.app.dto.submissions.SubmissionContactDto;
import ca.bc.gov.app.dto.submissions.SubmissionDetailsDto;
import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
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
import ca.bc.gov.app.repository.client.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.service.ches.ChesService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.r2dbc.core.DatabaseClient;
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
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository;
  private final ChesService chesService;
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
        getClientTypes()
            .flatMapMany(clientTypes ->
                loadSubmissions(page, size, requestType, requestStatus, updatedAt)
                    .flatMap(submission ->
                        loadSubmissionDetail(clientType, name, submission)
                            .map(submissionDetail ->
                                new ClientListSubmissionDto(
                                    submission.getSubmissionId(),
                                    submission.getSubmissionType().getDescription(),
                                    submissionDetail.getOrganizationName(),
                                    clientTypes.getOrDefault(submissionDetail.getClientTypeCode(),
                                        submissionDetail.getClientTypeCode()),
                                    Optional
                                        .ofNullable(submission.getUpdatedAt())
                                        .map(date -> date.format(
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                        .orElse(StringUtils.EMPTY),
                                    StringUtils.defaultString(submission.getUpdatedBy()),
                                    submission
                                        .getSubmissionStatus()
                                        .getDescription()
                                )
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
      String userEmail,
      String userName
  ) {

    return
        Mono
            .just(
                SubmissionEntity
                    .builder()
                    .submissionStatus(SubmissionStatusEnum.N)
                    .submissionType(SubmissionTypeCodeEnum.SPP)
                    .submissionDate(LocalDateTime.now())
                    .createdBy(userName)
                    .updatedBy(userName)
                    .build()
            )
            //Save submission to begin with
            .flatMap(submissionRepository::save)
            //Save the submission detail
            .map(submission -> mapToSubmissionDetailEntity(submission.getSubmissionId(),
                clientSubmissionDto.businessInformation())
            )
            .flatMap(submissionDetailRepository::save)
            //Save the locationNames and contacts and do the association
            .flatMap(submission ->
                //Save all locationNames
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
                            .flatMap(contact ->
                                saveAndAssociateContact(
                                    locations,
                                    contact,
                                    submission.getSubmissionId(),
                                    userId
                                )
                            )
                    )
                    //Then grab all back as a list, to make all reactive flows complete
                    .collectList()
                    //Return what we need only
                    .thenReturn(submission.getSubmissionId())
            )
            .flatMap(submissionId -> sendEmail(submissionId,
                clientSubmissionDto,
                userEmail,
                userName));
  }

  public Mono<SubmissionDetailsDto> getSubmissionDetail(Long id) {

    DatabaseClient client = template.getDatabaseClient();

    Mono<SubmissionDetailsDto> detailsBusiness =
        client
            .sql(ApplicationConstant.SUBMISSION_DETAILS_QUERY)
            .bind(ApplicationConstant.SUBMISSION_ID, id)
            .map((row, metadata) ->
                new SubmissionDetailsDto(
                    row.get("submission_id", Long.class),
                    row.get("status", String.class),
                    row.get("submission_type", String.class),
                    row.get("submission_date", LocalDateTime.class),
                    row.get("update_timestamp", LocalDateTime.class),
                    null,
                    row.get("update_user", String.class),
                    new SubmissionBusinessDto(
                        row.get("business_type", String.class),
                        row.get("incorporation_number", String.class),
                        row.get("client_number", String.class),
                        row.get("organization_name", String.class),
                        row.get("client_type", String.class),
                        row.get("good_standing", String.class),
                        row.get("birthdate", LocalDate.class)
                    ),
                    List.of(),
                    List.of(),
                    Map.of()
                )
            )
            .one();

    Flux<SubmissionContactDto> contacts =
        client
            .sql(ApplicationConstant.SUBMISSION_CONTACTS_QUERY
            )
            .bind(ApplicationConstant.SUBMISSION_ID, id)
            .map((row, metadata) -> new SubmissionContactDto(
                Objects.requireNonNull(row.get("index", Integer.class)) - 1,
                row.get("contact_desc", String.class),
                row.get("first_name", String.class),
                row.get("last_name", String.class),
                row.get("business_phone_number", String.class),
                row.get("email_address", String.class),
                List.of(StringUtils.defaultString(row.get("locations", String.class)).split(", ")),
                row.get("idp_user_id", String.class)
            ))
            .all();

    Flux<SubmissionAddressDto> addresses =
        client
            .sql(ApplicationConstant.SUBMISSION_LOCATION_QUERY
            )
            .bind(ApplicationConstant.SUBMISSION_ID, id)
            .map((row, metadata) -> new SubmissionAddressDto(
                Objects.requireNonNull(row.get("index", Integer.class)) - 1,
                row.get("street_address", String.class),
                row.get("country_desc", String.class),
                row.get("province_desc", String.class),
                row.get("city_name", String.class),
                row.get("postal_code", String.class),
                row.get("location_name", String.class)
            ))
            .all();

    return detailsBusiness
        .flatMap(submissionDetailsDto ->
            contacts
                .collectList()
                .map(submissionDetailsDto::withContact)
        )
        .flatMap(submissionDetailsDto ->
            addresses
                .collectList()
                .map(submissionDetailsDto::withAddress)
        )
        .flatMap(submissionDetailsDto ->
            submissionMatchDetailRepository
                .findBySubmissionId(id.intValue())
                .map(matched ->
                    submissionDetailsDto
                        .withApprovedTimestamp(matched.getUpdatedAt())
                        .withMatchers(matched.getMatchers())
                )
                .defaultIfEmpty(
                    submissionDetailsDto
                        .withMatchers(Map.of())
                )
        );

  }

  @SuppressWarnings("java:S1172")
  public Mono<Void> approveOrReject(
      Long id,
      String userId,
      String userEmail,
      String userName,
      SubmissionApproveRejectDto request
  ) {
    return
        submissionRepository
            .findById(id.intValue())
            .map(submission -> {
              submission.setUpdatedBy(userName);
              submission.setUpdatedAt(LocalDateTime.now());
              submission.setSubmissionStatus(
                  request.approved() ? SubmissionStatusEnum.A : SubmissionStatusEnum.R
              );
              return submission;
            })
            .flatMap(submissionRepository::save)
            .flatMap(submission ->
                submissionMatchDetailRepository
                    .findBySubmissionId(id.intValue())
                    .map(matched ->
                        matched
                            .withStatus(BooleanUtils.toString(request.approved(), "Y", "N"))
                            .withUpdatedAt(LocalDateTime.now())
                            .withMatchingMessage(processRejectionReason(request))
                    )
                    .flatMap(submissionMatchDetailRepository::save)
            )
            .then();
  }

  private Mono<SubmissionLocationContactEntity> saveAndAssociateContact(
      List<SubmissionLocationEntity> locations,
      ClientContactDto contact,
      Integer submissionId,
      String userId
  ) {

    return
        Mono
            .just(mapToSubmissionContactEntity(contact))
            .map(contactEntity -> contactEntity.withSubmissionId(submissionId))
            .map(contactEntity -> contactEntity.withUserId(contact.index() == 0 ? userId : null))
            .flatMap(submissionContactRepository::save)
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
      String email,
      String userName
  ) {
    return chesService.sendEmail("registration",
            email,
            "Client number application received",
            clientSubmissionDto.description(userName))
        .thenReturn(submissionId);
  }


  private Mono<Map<String, String>> getClientTypes() {
    return template
        .select(
            query(
                QueryPredicates.isBefore(LocalDateTime.now(), "effectiveAt")
                    .and(
                        QueryPredicates.isAfter(LocalDateTime.now(), "expiredAt")
                            .or(QueryPredicates.isNull("expiredAt"))
                    )
            ),
            ClientTypeCodeEntity.class
        )
        .collectList()
        //Convert the list into a map using code as the key and description as value
        .map(clientTypeCodeEntities ->
            clientTypeCodeEntities
                .stream()
                .collect(Collectors.toMap(ClientTypeCodeEntity::getCode,
                    ClientTypeCodeEntity::getDescription))
        );
  }


  private Mono<SubmissionDetailEntity> loadSubmissionDetail(String[] clientType,
      String[] name, SubmissionEntity submission) {
    return template
        .selectOne(
            query(
                Criteria
                    .where(ApplicationConstant.SUBMISSION_ID)
                    .is(submission.getSubmissionId())
                    .and(
                        QueryPredicates
                            .orEqualTo(clientType, "clientTypeCode")
                            .and(SubmissionDetailPredicates.orName(name))
                    )
            ),
            SubmissionDetailEntity.class
        );
  }

  private Flux<SubmissionEntity> loadSubmissions(int page, int size, String[] requestType,
      SubmissionStatusEnum[] requestStatus, String[] updatedAt) {

    Criteria userQuery = SubmissionPredicates
        .orUpdatedAt(updatedAt)
        .and(SubmissionPredicates.orStatus(requestStatus))
        .and(QueryPredicates.orEqualTo(requestType, ApplicationConstant.SUBMISSION_TYPE));

    //If no user provided query found, then use the default one
    if (userQuery.isEmpty()) {
      //List all submission of type SPP, or submissions of type AAC and RNC that were updated in the last 24 hours
      userQuery = QueryPredicates
          .orEqualTo(new String[]{"SPP"}, ApplicationConstant.SUBMISSION_TYPE)
          .or(
              QueryPredicates
                  .isAfter(LocalDateTime.now().minusDays(1L), "submissionDate")
                  .and(
                      QueryPredicates
                          //When I said AAC and RNC, I meant AAC or RNC for query purpose
                          .orEqualTo(new String[]{"AAC", "RNC"},
                              ApplicationConstant.SUBMISSION_TYPE)
                  )
          );
    }


    return template
        .select(
            query(userQuery)
                .with(PageRequest.of(page, size))
                .sort(Sort.by("updatedAt").descending()),
            SubmissionEntity.class
        );
  }

  private String processRejectionReason(SubmissionApproveRejectDto request) {

    StringBuilder stringBuilder = new StringBuilder();

    request
        .reasons()
        .forEach(reason -> {
          if (reason.equalsIgnoreCase("duplicated")) {
            stringBuilder.append(
                    "A client that matches your submission already exists with number: ")
                .append(request.message())
                .append("< /br>");
          }
          if (reason.equalsIgnoreCase("goodstanding")) {
            stringBuilder.append("Client is not in good standing with BC Registries")
                .append("< /br>");
          }

        });

    return stringBuilder.toString();
  }

}
