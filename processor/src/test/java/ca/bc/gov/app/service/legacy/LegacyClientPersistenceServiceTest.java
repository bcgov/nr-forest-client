package ca.bc.gov.app.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
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
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legacy Client Persistence Service")
class LegacyClientPersistenceServiceTest {

  private final SubmissionDetailRepository submissionDetailRepository = mock(
      SubmissionDetailRepository.class);
  private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
  private final SubmissionLocationRepository locationRepository = mock(
      SubmissionLocationRepository.class);
  private final SubmissionContactRepository contactRepository = mock(
      SubmissionContactRepository.class);
  private final SubmissionLocationContactRepository locationContactRepository = mock(
      SubmissionLocationContactRepository.class);
  private final LegacyService legacyService = mock(LegacyService.class);

  private final LegacyClientPersistenceService service = new LegacyClientPersistenceService(
      submissionDetailRepository,
      submissionRepository,
      locationRepository,
      contactRepository,
      locationContactRepository,
      legacyService
  );

  @ParameterizedTest(name = "type: {0} expected: {1}")
  @MethodSource("byType")
  @DisplayName("filter by type")
  void shouldFilterByType(String type, boolean expected) {
    assertEquals(expected, service.filterByType(type));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("generateForestClient")
  @DisplayName("create forest client")
  void shouldCreateForestClient(
      SubmissionDetailEntity entity,
      MessagingWrapper<String> wrapper,
      ForestClientDto dto
  ) {

    when(
        submissionDetailRepository
            .findBySubmissionId(
                wrapper.getParameter(ApplicationConstant.SUBMISSION_ID, Integer.class)
            )
    )
        .thenReturn(Mono.just(entity));

    service
        .generateForestClient(wrapper)
        .as(StepVerifier::create)
        .expectNext(new MessagingWrapper<>(dto, wrapper.parameters()))
        .verifyComplete();

  }

  private static Stream<Arguments> byType() {
    return Stream.of(
        Arguments.of("I", false),
        Arguments.of("C", true),
        Arguments.of("USP", false),
        Arguments.of("RSP", false)
    );
  }

  private static Stream<Arguments> generateForestClient() {
    Integer submissionId = 9999;
    String clientNumber = null;

    return
        Stream.of(
            Arguments.of(
                named("External Corporation",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("STAR DOT STAR VENTURES")
                        .registrationNumber("C0159297")
                        .businessTypeCode("R")
                        .clientTypeCode("C")
                        .goodStandingInd("Y")
                        .build()
                ),
                new MessagingWrapper<>(
                    clientNumber,
                    Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
                )
                    .withParameter(ApplicationConstant.SUBMISSION_STARTER,
                        ValidationSourceEnum.EXTERNAL)
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "STAR DOT STAR VENTURES")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "C")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "C0159297")
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "STAR DOT STAR VENTURES",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "C",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C",
                    "0159297",
                    "Jhon Snow submitted the client details acquired from BC Registry C0159297",
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    null,
                    null,
                    StringUtils.EMPTY
                )
            ),
            Arguments.of(
                named("Staff Corporation",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("STAR DOT STAR VENTURES")
                        .registrationNumber("C0159297")
                        .businessTypeCode("R")
                        .clientTypeCode("C")
                        .goodStandingInd("Y")
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "STAR DOT STAR VENTURES")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "C")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "C0159297")
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "STAR DOT STAR VENTURES",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "C",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C",
                    "0159297",
                    null,
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    null,
                    null,
                    StringUtils.EMPTY
                )
            ),
            Arguments.of(
                named("Staff Government",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("Government of British Columbia")
                        .businessTypeCode("U")
                        .clientTypeCode("G")
                        .workSafeBCNumber("123456")
                        .goodStandingInd("Y")
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "Government of British Columbia")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "G")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "GOVERNMENT OF BRITISH COLUMBIA")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "GOVERNMENT OF BRITISH COLUMBIA",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "G",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    null,
                    "123456",
                    StringUtils.EMPTY
                )
            ),
            Arguments.of(
                named("Staff Ministry of Forest",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("Timber Pricing Division")
                        .businessTypeCode("U")
                        .clientTypeCode("F")
                        .clientAcronym("TPD")
                        .goodStandingInd("Y")
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "Timber Pricing Division")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "F")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "TIMBER PRICING DIVISION")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "TIMBER PRICING DIVISION",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "F",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    "TPD",
                    null,
                    StringUtils.EMPTY
                )
            ),
            Arguments.of(
                named("Staff Unregistered Company",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("SuperNova Company")
                        .businessTypeCode("U")
                        .clientTypeCode("U")
                        .clientAcronym("TPD")
                        .workSafeBCNumber("123456")
                        .notes("Don't want to register with BC Registries")
                        .goodStandingInd("Y")
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "SuperNova Company")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "U")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "SUPERNOVA COMPANY")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "SUPERNOVA COMPANY",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "U",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "Don't want to register with BC Registries",
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    "TPD",
                    "123456",
                    StringUtils.EMPTY
                )
            ),
            Arguments.of(
                named("Staff First Nations Band",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("Unexistent Band")
                        .registrationNumber("DINA1")
                        .businessTypeCode("U")
                        .clientTypeCode("B")
                        .goodStandingInd("Y")
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "Unexistent Band")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "B")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "UNEXISTENT BAND")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "DINA1")
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "UNEXISTENT BAND",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "B",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "DINA",
                    "1",
                    null,
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    null,
                    null,
                    StringUtils.EMPTY
                )
            ),
            Arguments.of(
                named("Staff First Nations Tribe",
                    SubmissionDetailEntity.builder()
                        .submissionId(submissionId)
                        .submissionDetailId(submissionId)
                        .organizationName("Unexistent Band")
                        .registrationNumber("DINA12345")
                        .businessTypeCode("U")
                        .clientTypeCode("T")
                        .goodStandingInd("Y")
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
                    .withParameter(ApplicationConstant.SUBMISSION_NAME, "Unexistent Band")
                    .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "T")
                    .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                    .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "UNEXISTENT BAND")
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "DINA12345")
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS, false)
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS, StringUtils.EMPTY),
                new ForestClientDto(
                    clientNumber,
                    "UNEXISTENT BAND",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "ACT",
                    "T",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "DINA",
                    "12345",
                    null,
                    "IDIR\\JWICK",
                    "IDIR\\JWICK",
                    ApplicationConstant.ORG_UNIT,
                    null,
                    null,
                    StringUtils.EMPTY
                )
            )
        );
  }

}
