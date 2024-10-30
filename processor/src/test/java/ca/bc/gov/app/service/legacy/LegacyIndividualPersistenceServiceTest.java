package ca.bc.gov.app.service.legacy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.ArgumentMatchers.eq;
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
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legacy Individual Persistence Service")
class LegacyIndividualPersistenceServiceTest {


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

  private final LegacyIndividualPersistenceService service = new LegacyIndividualPersistenceService(
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

  @Test
  @DisplayName("create forest client")
  void shouldCreateForestClient() {

    SubmissionDetailEntity entity = SubmissionDetailEntity.builder()
        .submissionId(2)
        .submissionDetailId(2)
        .organizationName("James Baxter")
        .firstName("James")
        .lastName("Baxter")
        .businessTypeCode("U")
        .clientTypeCode("I")
        .goodStandingInd("Y")
        .identificationTypeCode("OTHR")
        .clientIdentification("ABC:123")
        .build();

    when(submissionDetailRepository.findBySubmissionId(eq(2)))
        .thenReturn(Mono.just(entity));

    service
        .generateForestClient(
            new MessagingWrapper<>(
                "00000001",
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000001",
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow"
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
              .hasFieldOrPropertyWithValue("clientComment",
                  "Jhon Snow submitted the individual with data acquired from BC Services Card")
              .hasFieldOrPropertyWithValue("clientTypeCode", "I")
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "OTHR")
              .hasFieldOrPropertyWithValue("clientIdentification", "ABC:123");

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
              .isEqualTo("JAMES BAXTER");

          assertThat(message.parameters().get(ApplicationConstant.REGISTRATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("not applicable");

        })
        .verifyComplete();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("generateForestClient")
  @DisplayName("All kinds of forest clients")
  void shouldCreateIndividualFromStaff(
      MessagingWrapper<String> wrapper,
      SubmissionDetailEntity entity,
      ForestClientDto dto
  ) {
    when(
        submissionDetailRepository
            .findBySubmissionId(
                wrapper
                    .getParameter(
                        ApplicationConstant.SUBMISSION_ID, Integer.class
                    )
            )
    )
        .thenReturn(Mono.just(entity));

    service
        .generateForestClient(wrapper)
        .as(StepVerifier::create)
        .expectNext(new MessagingWrapper<>(
                dto,
                wrapper.parameters()
            )
        )
        .verifyComplete();

  }

  private static Stream<Arguments> byType() {
    return Stream.of(
        Arguments.of("I", true),
        Arguments.of("C", false),
        Arguments.of("USP", false),
        Arguments.of("RSP", false)
    );
  }

  private static Stream<Arguments> generateForestClient() {
    Integer submissionId = 9999;
    String clientNumber = null;

    return Stream.of(
        Arguments.of(
            named("Staff-submitted Individual",
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
                .withParameter(ApplicationConstant.SUBMISSION_NAME, "Johnathan Valelono Wick")
                .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "I")
                .withParameter(ApplicationConstant.MATCHED_USER, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Johnathan Valelono Wick")
                .withParameter(ApplicationConstant.CREATED_BY, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.UPDATED_BY, "IDIR\\JWICK")
                .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "JOHNATHAN WICK")
                .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "not applicable")
            ),
            SubmissionDetailEntity.builder()
                .submissionId(submissionId)
                .submissionDetailId(submissionId)
                .organizationName("Johnathan Valelono Wick")
                .firstName("Johnathan")
                .middleName("Valelono")
                .lastName("Wick")
                .businessTypeCode("U")
                .clientTypeCode("I")
                .goodStandingInd("Y")
                .birthdate(LocalDate.of(1962, 3, 12))
                .identificationTypeCode("CDDL")
                .countryCode("CA")
                .provinceCode("BC")
                .clientIdentification("12345678")
                .build(),
            new ForestClientDto(
                clientNumber,
                "WICK",
                "JOHNATHAN",
                "VALELONO",
                "ACT",
                "I",
                LocalDate.of(1962, 3, 12),
                "BCDL",
                "12345678",
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                null,
                "IDIR\\JWICK",
                "IDIR\\JWICK",
                ApplicationConstant.ORG_UNIT,
                null,
                StringUtils.EMPTY,
                StringUtils.EMPTY
            )
        ),
        Arguments.of(
            named("BCSC-submitted Individual",
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
                .withParameter(ApplicationConstant.SUBMISSION_NAME, "Johnathan Wick")
                .withParameter(ApplicationConstant.MATCHING_REASON, StringUtils.EMPTY)
                .withParameter(ApplicationConstant.CLIENT_TYPE_CODE, "I")
                .withParameter(ApplicationConstant.MATCHED_USER, "idir\\ottomated")
                .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Johnathan Wick")
                .withParameter(ApplicationConstant.CREATED_BY,
                    "BCSC\\83JB4SM0MGI9KXYFFHBT2Y2F76AIYBYQ")
                .withParameter(ApplicationConstant.UPDATED_BY,
                    "idir\\ottomated")
                .withParameter(ApplicationConstant.FOREST_CLIENT_NAME, "JOHNATHAN WICK")
                .withParameter(ApplicationConstant.REGISTRATION_NUMBER, "not applicable")
            ),
            SubmissionDetailEntity.builder()
                .submissionId(submissionId)
                .submissionDetailId(submissionId)
                .organizationName("Johnathan Wick")
                .firstName(null)
                .middleName(null)
                .lastName(null)
                .businessTypeCode("U")
                .clientTypeCode("I")
                .goodStandingInd("Y")
                .birthdate(LocalDate.of(1962, 3, 12))
                .identificationTypeCode(null)
                .clientIdentification(null)
                .build(),
            new ForestClientDto(
                clientNumber,
                "WICK",
                "JOHNATHAN",
                StringUtils.EMPTY,
                "ACT",
                "I",
                LocalDate.of(1962, 3, 12),
                "BCSC",
                "83JB4SM0MGI9KXYFFHBT2Y2F76AIYB",
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                "Johnathan Wick submitted the individual with data acquired from BC Services Card",
                "BCSC\\83JB4SM0MGI9KXYFFHBT2Y2F7",
                "idir\\ottomated",
                ApplicationConstant.ORG_UNIT,
                null,
                StringUtils.EMPTY,
                StringUtils.EMPTY
            )
        )
    );
  }

}
