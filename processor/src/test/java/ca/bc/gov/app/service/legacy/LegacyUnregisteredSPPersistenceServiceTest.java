package ca.bc.gov.app.service.legacy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
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

@DisplayName("Unit Test | Legacy Unregistered Sole Proprietor Persistence Service")
class LegacyUnregisteredSPPersistenceServiceTest {


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

  private final LegacyUnregisteredSPPersistenceService service = new LegacyUnregisteredSPPersistenceService(
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
        .organizationName("Baxter, James")
        .businessTypeCode("U")
        .clientTypeCode("USP")
        .goodStandingInd("Y")
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
              .hasFieldOrPropertyWithValue("legalMiddleName", StringUtils.EMPTY)
              .hasFieldOrPropertyWithValue("clientComment",
                  "Jhon Snow submitted the sole proprietor with data acquired from Business BCeID")
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
              .isEqualTo("JAMES BAXTER");

          assertThat(message.parameters().get(ApplicationConstant.REGISTRATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("not applicable");

        })
        .verifyComplete();
  }

  private static Stream<Arguments> byType() {
    return Stream.of(
        Arguments.of("I", false),
        Arguments.of("C", false),
        Arguments.of("USP", true),
        Arguments.of("RSP", false)
    );
  }

}
