package ca.bc.gov.app.service.legacy;

import static ca.bc.gov.app.TestConstants.SUBMISSION_CONTACT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.processor.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legacy Registered Sole Proprietor Persistence Service")
class LegacyRegisteredSPPersistenceServiceTest {

  private final SubmissionDetailRepository submissionDetailRepository = mock(
      SubmissionDetailRepository.class);
  private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
  private final SubmissionLocationRepository locationRepository = mock(
      SubmissionLocationRepository.class);
  private final SubmissionContactRepository contactRepository = mock(
      SubmissionContactRepository.class);

  private final LegacyService legacyService = mock(LegacyService.class);
  private final BcRegistryService bcRegistryService = mock(BcRegistryService.class);

  private final LegacyRegisteredSPPersistenceService service = new LegacyRegisteredSPPersistenceService(
      submissionDetailRepository,
      submissionRepository,
      locationRepository,
      contactRepository,
      mock(SubmissionLocationContactRepository.class),
      legacyService,
      bcRegistryService
  );

  @ParameterizedTest(name = "type: {0} expected: {1}")
  @MethodSource("byType")
  @DisplayName("filter by type")
  void shouldFilterByType(String type, boolean expected) {
    assertEquals(expected, service.filterByType(type));
  }

  @Test
  @DisplayName("create forest client")
  void shouldCreateForestClient() {

    SubmissionDetailEntity entity = SubmissionDetailEntity.builder()
        .submissionId(2)
        .submissionDetailId(2)
        .organizationName("Baxter Corp")
        .registrationNumber("FM00184546")
        .businessTypeCode("R")
        .clientTypeCode("RSP")
        .goodStandingInd("Y")
        .build();

    when(bcRegistryService.requestDocumentData(any(String.class)))
        .thenReturn(Flux.just(TestConstants.BCREG_DOC_DATA));

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(entity));

    when(contactRepository.findBySubmissionId(any()))
        .thenReturn(Flux.empty());

    service
        .generateForestClient(
            new MessagingWrapper<>(
                "00000001",
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000001",
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow",
                    ApplicationConstant.MATCHING_INFO, Map.of("businessName", "Night Watch")
                )
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrProperty("payload");

          assertThat(message.payload())
              .isNotNull()
              .hasFieldOrPropertyWithValue("clientName", "BAXTER")
              .hasFieldOrPropertyWithValue("legalFirstName", "JAMES")
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "BCRE")
              .hasFieldOrPropertyWithValue("clientIdentification", "FM00184546")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "00184546")
              .hasFieldOrPropertyWithValue("clientComment",
                  String.join(" ",
                      "Jhon Snow of BCeID Business Night Watch submitted the",
                      "sole proprietor registered on BC Registry with number",
                      "FM00184546",
                      "and company name",
                      "BAXTER CORP"
                  )
              )
              .hasFieldOrPropertyWithValue("clientTypeCode", "I");

          assertThat(message.parameters().get(ApplicationConstant.SUBMISSION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.parameters().get(ApplicationConstant.CREATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.UPDATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("BAXTER CORP");

          assertThat(message.parameters().get(ApplicationConstant.REGISTRATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM00184546");
        })
        .verifyComplete();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("generateForestClient")
  @DisplayName("create forest client")
  void shouldCreateForestClientFromParams(
      SubmissionDetailEntity entity,
      MessagingWrapper<String> wrapper,
      ForestClientDto dto
  ) {

    when(bcRegistryService.requestDocumentData(any(String.class)))
        .thenReturn(Flux.just(TestConstants.BCREG_DOC_DATA));

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(entity));

    when(contactRepository.findBySubmissionId(any()))
        .thenReturn(Flux.empty());

    service
        .generateForestClient(wrapper)
        .as(StepVerifier::create)
        .expectNext(new MessagingWrapper<>(dto, wrapper.parameters()))
        .verifyComplete();
  }

  @Test
  @DisplayName("create forest client")
  void shouldCreateForestClientWhenNoBcRegData() {

    SubmissionDetailEntity entity = SubmissionDetailEntity.builder()
        .submissionId(2)
        .submissionDetailId(2)
        .organizationName("Baxter Corp")
        .registrationNumber("FM00184546")
        .businessTypeCode("R")
        .clientTypeCode("RSP")
        .goodStandingInd("Y")
        .build();

    when(bcRegistryService.requestDocumentData(any(String.class)))
        .thenReturn(Flux.empty());

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(entity));

    when(contactRepository.findBySubmissionId(any()))
        .thenReturn(Flux.just(SUBMISSION_CONTACT.withLastName("BAXTER")));

    service
        .generateForestClient(
            new MessagingWrapper<>(
                "00000001",
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000001",
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow",
                    ApplicationConstant.MATCHING_INFO, Map.of("businessName", "Night Watch")
                )
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrProperty("payload");

          assertThat(message.payload())
              .isNotNull()
              .hasFieldOrPropertyWithValue("clientName", "BAXTER")
              .hasFieldOrPropertyWithValue("legalFirstName", "JAMES")
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "BCRE")
              .hasFieldOrPropertyWithValue("clientIdentification", "FM00184546")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "00184546")
              .hasFieldOrPropertyWithValue("clientComment",
                  String.join(" ",
                      "Jhon Snow of BCeID Business Night Watch submitted the",
                      "sole proprietor registered on BC Registry with number",
                      "FM00184546",
                      "and company name",
                      "BAXTER CORP"
                  )
              )
              .hasFieldOrPropertyWithValue("clientTypeCode", "I");

          assertThat(message.parameters().get(ApplicationConstant.SUBMISSION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.parameters().get(ApplicationConstant.CREATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.UPDATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("BAXTER CORP");

          assertThat(message.parameters().get(ApplicationConstant.REGISTRATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM00184546");


        })
        .verifyComplete();
  }

  @Test
  @DisplayName("create client with doing business")
  void shouldCreateClient() {

    SubmissionDetailEntity detailEntity = SubmissionDetailEntity
        .builder()
        .submissionId(2)
        .registrationNumber("XX0000000")
        .organizationName("Sample test")
        .clientTypeCode("RSP")
        .clientNumber("00000000")
        .build();

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(detailEntity));
    when(submissionDetailRepository.save(any()))
        .thenReturn(Mono.just(detailEntity));
    when(legacyService.createDoingBusinessAs(any(), any(), any(), any()))
        .thenReturn(Mono.just("00000000"));
    when(legacyService.createClient(any()))
        .thenReturn(Mono.just("00000000"));

    service
        .createForestClient(
            new MessagingWrapper<>(
                TestConstants
                    .CLIENT_ENTITY
                    .withClientTypeCode("I")
                    .withClientIdTypeCode("BCRE")
                ,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.CLIENT_TYPE_CODE, "RSP",
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000",
                    ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA",
                    ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow"
                )
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          Assertions.assertThat(message)
              .as("message")
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          Assertions.assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000000");
        })
        .verifyComplete();

  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("generateDoingBusinessAs")
  @DisplayName("create all client with doing business")
  void shouldCreateDBA(
      MessagingWrapper<ForestClientDto> wrapper
  ) {

    SubmissionDetailEntity detailEntity = SubmissionDetailEntity
        .builder()
        .submissionId(2)
        .registrationNumber("XX0000000")
        .organizationName("Sample test")
        .clientTypeCode("RSP")
        .clientNumber(wrapper.payload().clientNumber())
        .build();

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(detailEntity));
    when(submissionDetailRepository.save(any()))
        .thenReturn(Mono.just(detailEntity));
    when(legacyService.createDoingBusinessAs(any(), any(), any(), any()))
        .thenReturn(Mono.just(wrapper.payload().clientNumber()));
    when(legacyService.createClient(any()))
        .thenReturn(Mono.just(wrapper.payload().clientNumber()));

    service
        .createForestClient(wrapper)
        .as(StepVerifier::create)
        .assertNext(message -> {
          Assertions.assertThat(message)
              .as("message")
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 9999);

          Assertions.assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo(wrapper.payload().clientNumber());
        })
        .verifyComplete();

  }

  private static Stream<Arguments> byType() {
    return Stream.of(
        Arguments.of("I", false),
        Arguments.of("C", false),
        Arguments.of("USP", false),
        Arguments.of("RSP", true)
    );
  }

  private static Stream<Arguments> generateForestClient() {
    Integer submissionId = 9999;
    String clientNumber = null;

    return Stream.of(
        Arguments.of(
            named("Staff-submitted registered sole proprietor",
                SubmissionDetailEntity.builder()
                    .submissionId(submissionId)
                    .submissionDetailId(submissionId)
                    .organizationName("Baxter Corp")
                    .doingBusinessAs("Baxter Corp")
                    .firstName("James")
                    .lastName("Baxter")
                    .registrationNumber("FM00184546")
                    .businessTypeCode("R")
                    .clientTypeCode("RSP")
                    .goodStandingInd("Y")
                    .birthdate(LocalDate.of(1962, 3, 12))
                    .build()
            ),
            new MessagingWrapper<>(
                clientNumber,
                Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
            )
                .withParameter(ApplicationConstant.SUBMISSION_STARTER,
                    ValidationSourceEnum.STAFF)
                .withParameter(ApplicationConstant.MATCHING_INFO,
                    Map.of(
                        "name", "John Wick",
                        "email", "jhon.wick@gov.bc.ca",
                        "userId", "IDIR\\JWICK",
                        "businessId", StringUtils.EMPTY,
                        "businessName", StringUtils.EMPTY
                    )
                )
                .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A)
                .withParameter(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.SUBMISSION_NAME,
                    "Baxter Corp")
                .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "RSP")
                .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                    "James Baxter")
                .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                    "BAXTER CORP")
                .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "FM00184546")
                .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, true)
                .withParameter(ApplicationConstant.DOING_BUSINESS_AS, "BAXTER CORP"),
            new ForestClientDto(
                clientNumber,
                "BAXTER",
                "JAMES",
                StringUtils.EMPTY,
                "ACT",
                "I",
                LocalDate.of(1962, 3, 12),
                "BCRE",
                "FM00184546",
                "FM",
                "00184546",
                null, //clientComment
                "IDIR\\JWICK",
                "IDIR\\JWICK",
                ApplicationConstant.ORG_UNIT,
                null, //acronym
                null, //wsbn,
                StringUtils.EMPTY
            )
        ),
        Arguments.of(
            named("Staff-submitted RSP with acronym and work safe bc",
                SubmissionDetailEntity.builder()
                    .submissionId(submissionId)
                    .submissionDetailId(submissionId)
                    .organizationName("Baxter Corp")
                    .doingBusinessAs("Baxter Corp")
                    .firstName("James")
                    .lastName("Baxter")
                    .registrationNumber("FM00184546")
                    .businessTypeCode("R")
                    .clientTypeCode("RSP")
                    .goodStandingInd("Y")
                    .birthdate(LocalDate.of(1962, 3, 12))
                    .clientAcronym("BAXC")
                    .workSafeBCNumber("123456")
                    .build()
            ),
            new MessagingWrapper<>(
                clientNumber,
                Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
            )
                .withParameter(ApplicationConstant.SUBMISSION_STARTER,
                    ValidationSourceEnum.STAFF)
                .withParameter(ApplicationConstant.MATCHING_INFO,
                    Map.of(
                        "name", "John Wick",
                        "email", "jhon.wick@gov.bc.ca",
                        "userId", "IDIR\\JWICK",
                        "businessId", StringUtils.EMPTY,
                        "businessName", StringUtils.EMPTY
                    )
                )
                .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A)
                .withParameter(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.SUBMISSION_NAME,
                    "Baxter Corp")
                .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "RSP")
                .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                    "James Baxter")
                .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                    "BAXTER CORP")
                .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "FM00184546")
                .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, true)
                .withParameter(ApplicationConstant.DOING_BUSINESS_AS, "BAXTER CORP"),
            new ForestClientDto(
                clientNumber,
                "BAXTER",
                "JAMES",
                StringUtils.EMPTY,
                "ACT",
                "I",
                LocalDate.of(1962, 3, 12),
                "BCRE",
                "FM00184546",
                "FM",
                "00184546",
                null, //clientComment
                "IDIR\\JWICK",
                "IDIR\\JWICK",
                ApplicationConstant.ORG_UNIT,
                "BAXC", //acronym
                "123456", //wsbn,
                StringUtils.EMPTY
            )
        ),
        Arguments.of(
            named("External-submitted RSP",
                SubmissionDetailEntity.builder()
                    .submissionId(submissionId)
                    .submissionDetailId(submissionId)
                    .organizationName("Baxter Corp")
                    .registrationNumber("FM00184546")
                    .businessTypeCode("R")
                    .clientTypeCode("RSP")
                    .goodStandingInd("Y")
                    .birthdate(LocalDate.of(1962, 3, 12))
                    .build()
            ),
            new MessagingWrapper<>(
                clientNumber,
                Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
            )
                .withParameter(ApplicationConstant.SUBMISSION_STARTER,
                    ValidationSourceEnum.EXTERNAL)
                .withParameter(ApplicationConstant.MATCHING_INFO, null)
                .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A)
                .withParameter(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.SUBMISSION_NAME,
                    "Baxter Corp")
                .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "RSP")
                .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\ottomated")
                .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                    "James Baxter")
                .withParameter(ApplicationConstant.CREATED_BY, "BCEIDBUSINESS\\JWICK")
                .withParameter(ApplicationConstant.UPDATED_BY, "BCEIDBUSINESS\\JWICK")
                .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                    "BAXTER CORP")
                .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "FM00184546")
                .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, true)
                .withParameter(ApplicationConstant.DOING_BUSINESS_AS, "BAXTER CORP"),
            new ForestClientDto(
                clientNumber,
                "BAXTER",
                "JAMES",
                StringUtils.EMPTY,
                "ACT",
                "I",
                LocalDate.of(1962, 3, 12),
                "BCRE",
                "FM00184546",
                "FM",
                "00184546",
                String.join(" ",
                    "James Baxter of BCeID Business null submitted the",
                    "sole proprietor registered on BC Registry with number",
                    "FM00184546",
                    "and company name",
                    "BAXTER CORP"
                ),
                "BCEIDBUSINESS\\JWICK",
                "BCEIDBUSINESS\\JWICK",
                ApplicationConstant.ORG_UNIT,
                null,
                null,
                StringUtils.EMPTY
            )
        )
    );
  }

  private static Stream<Arguments> generateDoingBusinessAs() {
    Integer submissionId = 9999;
    String clientNumber = "00100000";

    return Stream.of(
        Arguments.of(
            named("Staff-submitted registered sole proprietor",
                new MessagingWrapper<>(
                    new ForestClientDto(
                        clientNumber,
                        "BAXTER",
                        "JAMES",
                        StringUtils.EMPTY,
                        "ACT",
                        "I",
                        LocalDate.of(1962, 3, 12),
                        "BCRE",
                        "FM00184546",
                        "FM",
                        "00184546",
                        null, //clientComment
                        "IDIR\\JWICK",
                        "IDIR\\JWICK",
                        ApplicationConstant.ORG_UNIT,
                        null, //acronym
                        null, //wsbn
                        null
                    ),
                    Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
                )
                    .withParameter(ApplicationConstant.SUBMISSION_STARTER,
                        ValidationSourceEnum.STAFF)
                    .withParameter(ApplicationConstant.MATCHING_INFO,
                        Map.of(
                            "name", "John Wick",
                            "email", "jhon.wick@gov.bc.ca",
                            "userId", "IDIR\\JWICK",
                            "businessId", StringUtils.EMPTY,
                            "businessName", StringUtils.EMPTY
                        )
                    )
                    .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A)
                    .withParameter(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.SUBMISSION_NAME,
                        "Baxter Corp")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "RSP")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                        "James Baxter")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                        "BAXTER CORP")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "FM00184546")
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, true)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, "BAXTER CORP")
            )
        ),
        Arguments.of(
            named("External-submitted RSP",
                new MessagingWrapper<>(
                    new ForestClientDto(
                        clientNumber,
                        "BAXTER",
                        "JAMES",
                        StringUtils.EMPTY,
                        "ACT",
                        "I",
                        LocalDate.of(1962, 3, 12),
                        "BCRE",
                        "FM00184546",
                        "FM",
                        "00184546",
                        String.join(" ",
                            "James Baxter of BCeID Business null submitted the",
                            "sole proprietor registered on BC Registry with number",
                            "FM00184546",
                            "and company name",
                            "BAXTER CORP"
                        ),
                        "BCEIDBUSINESS\\JWICK",
                        "BCEIDBUSINESS\\JWICK",
                        ApplicationConstant.ORG_UNIT,
                        null,
                        null,
                        StringUtils.EMPTY
                    ),
                    Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
                )
                    .withParameter(ApplicationConstant.SUBMISSION_STARTER,
                        ValidationSourceEnum.EXTERNAL)
                    .withParameter(ApplicationConstant.MATCHING_INFO, null)
                    .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A)
                    .withParameter(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.SUBMISSION_NAME,
                        "Baxter Corp")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "RSP")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\ottomated")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                        "James Baxter")
                    .withParameter(ApplicationConstant.CREATED_BY, "BCEIDBUSINESS\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "BCEIDBUSINESS\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                        "BAXTER CORP")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "FM00184546")
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, true)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, "BAXTER CORP")
            )
        )
    );
  }
}
