package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.exception.SubmissionNotCompletedException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.retry.Retry;

@DisplayName("Integrated Test | FSA Staff Client Submission Controller")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class ClientStaffSubmissionControllerIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  private SubmissionDetailRepository repository;
  
  @Autowired
  private ObjectMapper mapper;
  
  @RegisterExtension
  static WireMockExtension chesStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10010)
              //.notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @RegisterExtension
  static WireMockExtension processorStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10070)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @RegisterExtension
  static WireMockExtension bcRegistryStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10040)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void init() {
    chesStub
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    chesStub
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    chesStub
        .stubFor(
            post("/token/uri")
                .willReturn(
                    ok(TestConstants.CHES_TOKEN_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    processorStub
        .stubFor(
            get(urlMatching("/api/processor/\\d"))
                .willReturn(created())
        );

    client = client.mutate()
        .responseTimeout(Duration.ofMinutes(3))
        .build();

    loadInitialDbEntries();
  }

  // This is a global list of submission IDs that have been processed.
  // This list is used to control whether a submission has been saved to the database already or not.
  private final AtomicReference<List<Integer>> globalIdList = new AtomicReference<>(List.of());

  @Test
  @DisplayName("Failed due to validation")
  @Order(1)
  void shouldFailSubmissionDueToValidation() throws JsonProcessingException {

    ClientSubmissionDto dto = mapper.readValue(
        TestConstants.STAFF_SUBMITTED_INDIVIDUAL_JSON,
        ClientSubmissionDto.class
    );
    dto = dto.withBusinessInformation(
        dto.businessInformation().withLegalType("J")
    );

    client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
        )
        .post()
        .uri("/api/clients/submissions/staff")
        .body(Mono.just(dto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBodyList(ValidationError.class)
        .hasSize(1)
        .contains(
            new ValidationError(
                "businessInformation.legalType",
                "Legal type has an invalid value [J]"
            )
        );

  }

  @Test
  @DisplayName("Successfully created staff submission of individual")
  @Order(2)
  void shouldSubmitIndividualClientSubmission() throws JsonProcessingException {

    ClientSubmissionDto dto = mapper.readValue(
        TestConstants.STAFF_SUBMITTED_INDIVIDUAL_JSON,
        ClientSubmissionDto.class
    );

    markSubmissionWithClientNumber("00123456");

    client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
        )
        .post()
        .uri("/api/clients/submissions/staff")
        .body(Mono.just(dto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().valueEquals("Location", "/api/clients/details/00123456")
        .expectHeader().valueEquals("x-client-id", "00123456")
        .expectBody().isEmpty();

  }

  @Test
  @DisplayName("Sole proprietorship not owner by person is not allowed")
  @Order(3)
  void shouldNotAllowSubmissionFromNonPersonProprietor() throws JsonProcessingException {

    bcRegistryStub
        .stubFor(
            post("/registry-search/api/v1/businesses/FM00004455/documents/requests")
                .willReturn(
                    status(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestConstants.BCREG_DOC_REQ_RES)
                )
        );

    bcRegistryStub
        .stubFor(
            get("/registry-search/api/v1/businesses/FM00004455/documents/aa0a00a0a")
                .willReturn(
                    status(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestConstants.BCREG_DOC_DATA_SPORG)
                )
        );

    ClientSubmissionDto dto = mapper.readValue(
        TestConstants.STAFF_SUBMITTED_SPORG_JSON,
        ClientSubmissionDto.class
    );

    client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
        )
        .post()
        .uri("/api/clients/submissions/staff")
        .body(Mono.just(dto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isBadRequest();

  }

  /**
   * Marks a submission with a specific client number if it's not already marked. This method
   * performs several operations in a reactive pipeline:
   * <ol>
   *   <li>Retrieves all submission details from the repository.</li>
   * <li>Maps each submission detail to its ID and collects these IDs into a list.</li>
   * <li>Uses the collected list of submission IDs to find any IDs not already in the global ID list.</li>
   * <li>If there are missing IDs (indicating new submissions), it emits the first missing ID found.</li>
   * <li>If all IDs are present in the global list, it emits an error indicating no new submissions need processing.</li>
   * <li>Retries the operation on encountering a {@link SubmissionNotCompletedException}, with a backoff strategy.</li>
   * <li>Finds the submission detail by ID for the missing submission ID emitted earlier.</li>
   * <li>Updates the found submission detail with the provided client number.</li>
   * <li>Saves the updated submission detail back to the repository.</li>
   * <li>Adds the ID of the processed submission detail to the global list of processed IDs.</li>
   * </ol>
   * <p>
   * This is to simulate the processor service updating the data on postgres. The code here will keep a global list of submissions,
   * and it will check against the current list to see if the submission has been saved.
   * When saved, it will then send the id of the submission forward to have the client number added to it and saved.
   *
   * @param clientNumber The client number to mark the submission with.
   */
  private void markSubmissionWithClientNumber(String clientNumber) {
    repository
        .findAll()
        .map(SubmissionDetailEntity::getSubmissionDetailId)
        .collectList()
        .handle(
            (submissionIdList, sink) -> {
              if (!globalIdList.get().containsAll(submissionIdList)) {
                Integer missingSubmissionIds = submissionIdList
                    .stream()
                    .filter(submissionId -> !globalIdList.get().contains(submissionId))
                    .findFirst()
                    .orElse(0);
                sink.next(missingSubmissionIds);
              } else {
                sink.error(new SubmissionNotCompletedException(1));
              }
            }
        )
        .cast(Integer.class)
        .retryWhen(
            Retry
                .backoff(100, Duration.ofSeconds(1))
                .jitter(0.05)
                .filter(SubmissionNotCompletedException.class::isInstance)
        )
        .flatMap(repository::findById)
        .map(submissionDetail -> submissionDetail.withClientNumber(clientNumber))
        .flatMap(repository::save)
        .doOnNext(submissionDetail -> addToGlobalList(submissionDetail.getSubmissionDetailId()))
        .subscribe();
  }

  /**
   * Adds a given submission ID to the global list of submission IDs. This method retrieves the
   * current list of submission IDs from the {@code globalIdList} atomic reference, creates a new
   * list from it, adds the given submission ID to the new list, and then updates the atomic
   * reference with this new list. This ensures that the list of submission IDs is always up to date
   * and thread-safe.
   *
   * @param id The submission ID to be added to the global list.
   */
  private void addToGlobalList(Integer id) {
    Set<Integer> list = new HashSet<>(globalIdList.get());
    log.info("Global list of IDs updated. Was {} now added {}", list, id);
    list.add(id);
    globalIdList.set(new ArrayList<>(list));
  }

  /**
   * Initializes the database with existing submission details. This method performs the following
   * steps:
   * <ol>
   * <li>Retrieves all submission detail entities from the repository.</li>
   * <li>Extracts the submission detail ID from each entity.</li>
   * <li>Adds each submission detail ID to the global list of processed IDs using {@code addToGlobalList}.</li>
   * <li>Collects all IDs into a list and verifies the operation completes successfully.</li>
   * </ol>
   * <p>
   * This is intended to run before tests to ensure the global list of processed IDs is populated
   * with IDs from pre-existing submissions in the database, allowing for accurate simulation of
   * the application's behavior in a test environment.
   */
  private void loadInitialDbEntries() {

    repository
        .findAll()
        .map(SubmissionDetailEntity::getSubmissionDetailId)
        .doOnNext(this::addToGlobalList)
        .collectList()
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }

}
