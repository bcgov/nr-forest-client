package ca.bc.gov.app.service.legacy;

import static ca.bc.gov.app.TestConstants.SUBMISSION_CONTACT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.util.Map;
import java.util.stream.Stream;
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
        .incorporationNumber("FM00184546")
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
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "BCRE")
              .hasFieldOrPropertyWithValue("clientIdentification", "FM00184546")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "00184546")
              .hasFieldOrPropertyWithValue("clientComment",
                  String.join(" ",
                      "Jhon Snow submitted the",
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

          assertThat(message.parameters().get(ApplicationConstant.INCORPORATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM00184546");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("create forest client")
  void shouldCreateForestClientWhenNoBcRegData() {

    SubmissionDetailEntity entity = SubmissionDetailEntity.builder()
        .submissionId(2)
        .submissionDetailId(2)
        .organizationName("Baxter Corp")
        .incorporationNumber("FM00184546")
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
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "BCRE")
              .hasFieldOrPropertyWithValue("clientIdentification", "FM00184546")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "00184546")
              .hasFieldOrPropertyWithValue("clientComment",
                  String.join(" ",
                      "Jhon Snow submitted the",
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

          assertThat(message.parameters().get(ApplicationConstant.INCORPORATION_NUMBER))
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
        .incorporationNumber("XX0000000")
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

  private static Stream<Arguments> byType() {
    return Stream.of(
        Arguments.of("I", false),
        Arguments.of("C", false),
        Arguments.of("USP", false),
        Arguments.of("RSP", true)
    );
  }

}
