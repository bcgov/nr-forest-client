package ca.bc.gov.app.service.legacy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation.ReactiveInsert;
import org.springframework.integration.support.MessageBuilder;
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
  private final R2dbcEntityTemplate legacyR2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);
  private final CountryCodeRepository countryCodeRepository = mock(CountryCodeRepository.class);
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository = mock(
      ClientDoingBusinessAsRepository.class);

  private final LegacyClientPersistenceService service = new LegacyClientPersistenceService(
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
  void shouldFilterByType(String type, boolean expected){
    assertEquals(expected, service.filterByType(type));
  }


  @Test
  @DisplayName("create forest client")
  void shouldCreateForestClient() {
    ReactiveInsert<ForestClientEntity> reactiveInsert = mock(ReactiveInsert.class);

    SubmissionDetailEntity entity = SubmissionDetailEntity.builder()
        .submissionId(2)
        .submissionDetailId(2)
        .organizationName("STAR DOT STAR VENTURES")
        .incorporationNumber("FM0159297")
        .businessTypeCode("R")
        .clientTypeCode("C")
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
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrProperty("payload");

          assertThat(message.getPayload())
              .isNotNull()
              .hasFieldOrPropertyWithValue("clientName", "STAR DOT STAR VENTURES")
              .hasFieldOrPropertyWithValue("clientTypeCode", "C")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "0159297")
              .hasFieldOrPropertyWithValue("clientNumber", "00000001")
              .hasFieldOrPropertyWithValue("clientComment", "Client details acquired from BC Registry FM0159297");

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
              .isEqualTo("STAR DOT STAR VENTURES");

          assertThat(message.getHeaders().get(ApplicationConstant.INCORPORATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM0159297");

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000001");

        })
        .verifyComplete();
  }

  private static Stream<Arguments> byType(){
    return Stream.of(
        Arguments.of("I", false),
        Arguments.of("C", true),
        Arguments.of("USP", false),
        Arguments.of("RSP", false)
    );
  }

}