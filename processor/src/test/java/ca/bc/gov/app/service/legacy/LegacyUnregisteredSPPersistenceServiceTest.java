package ca.bc.gov.app.service.legacy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.integration.support.MessageBuilder;
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
  private final R2dbcEntityTemplate legacyR2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);
  private final CountryCodeRepository countryCodeRepository = mock(CountryCodeRepository.class);
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository = mock(
      ClientDoingBusinessAsRepository.class);

  private final LegacyUnregisteredSPPersistenceService service = new LegacyUnregisteredSPPersistenceService(
      submissionDetailRepository,
      submissionRepository,
      locationRepository,
      contactRepository,
      locationContactRepository,
      legacyR2dbcEntityTemplate,
      countryCodeRepository,
      doingBusinessAsRepository
  );

  @ParameterizedTest(name = "type: {0} expected: {1}")
  @MethodSource("byType")
  @DisplayName("filter by type")
  void shouldFilterByType(String type, boolean expected) {
    assertEquals(expected, service.filterByType(type));
  }

  @Test
  @DisplayName("get next channel")
  void shouldGetNextChannel() {
    assertEquals(ApplicationConstant.SUBMISSION_LEGACY_USP_CHANNEL, service.getNextChannel());
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
            MessageBuilder
                .withPayload("00000001")
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000001")
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.CLIENT_SUBMITTER_NAME, "Jhon Snow")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrProperty("payload");

          assertThat(message.getPayload())
              .isNotNull()
              .hasFieldOrPropertyWithValue("clientName", "BAXTER")
              .hasFieldOrPropertyWithValue("legalFirstName", "JAMES")
              .hasFieldOrPropertyWithValue("legalMiddleName", StringUtils.EMPTY)
              .hasFieldOrPropertyWithValue("clientComment",
                  "Jhon Snow submitted the sole proprietor with data acquired from Business BCeID")
              .hasFieldOrPropertyWithValue("clientTypeCode", "I")
              .hasFieldOrPropertyWithValue("clientNumber", "00000001");

          assertThat(message.getHeaders().get(ApplicationConstant.SUBMISSION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.getHeaders().get(ApplicationConstant.CREATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.UPDATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("JAMES BAXTER");

          assertThat(message.getHeaders().get(ApplicationConstant.INCORPORATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("not applicable");

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000001");

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