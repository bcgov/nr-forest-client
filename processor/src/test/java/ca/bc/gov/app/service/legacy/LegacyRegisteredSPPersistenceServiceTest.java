package ca.bc.gov.app.service.legacy;

import static ca.bc.gov.app.TestConstants.SUBMISSION_CONTACT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.legacy.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation.ReactiveInsert;
import org.springframework.integration.support.MessageBuilder;
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
  private final SubmissionLocationContactRepository locationContactRepository = mock(
      SubmissionLocationContactRepository.class);
  private final R2dbcEntityTemplate legacyR2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);
  private final CountryCodeRepository countryCodeRepository = mock(CountryCodeRepository.class);
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository = mock(
      ClientDoingBusinessAsRepository.class);
  private final BcRegistryService bcRegistryService = mock(BcRegistryService.class);

  private final LegacyRegisteredSPPersistenceService service = new LegacyRegisteredSPPersistenceService(
      submissionDetailRepository,
      submissionRepository,
      locationRepository,
      contactRepository,
      locationContactRepository,
      legacyR2dbcEntityTemplate,
      countryCodeRepository,
      doingBusinessAsRepository,
      bcRegistryService
  );

  @ParameterizedTest(name = "type: {0} expected: {1}")
  @MethodSource("byType")
  @DisplayName("filter by type")
  void shouldFilterByType(String type, boolean expected){
    assertEquals(expected, service.filterByType(type));
  }

  @Test
  @DisplayName("get next channel")
  void shouldGetNextChannel(){
    assertEquals(ApplicationConstant.SUBMISSION_LEGACY_RSP_CHANNEL, service.getNextChannel());
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
              .hasFieldOrPropertyWithValue("clientName", "BAXTER")
              .hasFieldOrPropertyWithValue("legalFirstName", "JAMES")
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "OTHR")
              .hasFieldOrPropertyWithValue("clientIdentification", "FM00184546")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "00184546")
              .hasFieldOrPropertyWithValue("clientComment",
                  String.join(" ",
                      "Sole proprietorship registered on BC Registry with number",
                      "FM00184546",
                      "and company name",
                      "BAXTER CORP"
                  )
              )
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
              .isEqualTo("BAXTER CORP");

          assertThat(message.getHeaders().get(ApplicationConstant.INCORPORATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM00184546");

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000001");

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
              .hasFieldOrPropertyWithValue("clientName", "BAXTER")
              .hasFieldOrPropertyWithValue("legalFirstName", "JAMES")
              .hasFieldOrPropertyWithValue("clientIdTypeCode", "OTHR")
              .hasFieldOrPropertyWithValue("clientIdentification", "FM00184546")
              .hasFieldOrPropertyWithValue("registryCompanyTypeCode", "FM")
              .hasFieldOrPropertyWithValue("corpRegnNmbr", "00184546")
              .hasFieldOrPropertyWithValue("clientComment",
                  String.join(" ",
                      "Sole proprietorship registered on BC Registry with number",
                      "FM00184546",
                      "and company name",
                      "BAXTER CORP"
                  )
              )
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
              .isEqualTo("BAXTER CORP");

          assertThat(message.getHeaders().get(ApplicationConstant.INCORPORATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM00184546");

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000001");

        })
        .verifyComplete();
  }

  @Test
  @DisplayName("create client with doing business")
  void shouldCreateClient() {
    ReactiveInsert<ClientDoingBusinessAsEntity> doingBusinessInsert = mock(ReactiveInsert.class);
    ReactiveInsert<ForestClientEntity> clientInsert = mock(ReactiveInsert.class);

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
    when(doingBusinessAsRepository.existsByClientNumber(any()))
        .thenReturn(Mono.just(false));
    when(legacyR2dbcEntityTemplate.selectOne(any(),any()))
        .thenReturn(Mono.just(ClientDoingBusinessAsEntity.builder().id(1).build()));
    when(legacyR2dbcEntityTemplate.insert(ForestClientEntity.class))
        .thenReturn(clientInsert);
    when(legacyR2dbcEntityTemplate.insert(ClientDoingBusinessAsEntity.class))
        .thenReturn(doingBusinessInsert);
    when(doingBusinessInsert.using(any()))
        .thenReturn(Mono.just(ClientDoingBusinessAsEntity.builder().clientNumber("00000000")
                .build()
            )
        );
    when(clientInsert.using(any())).thenReturn(Mono.just(TestConstants.CLIENT_ENTITY));

    service
        .createForestClient(
            MessageBuilder
                .withPayload(
                    TestConstants
                        .CLIENT_ENTITY
                        .withClientTypeCode("I")
                        .withClientIdTypeCode("OTHR")
                )
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, "RSP")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          Assertions.assertThat(message)
              .as("message")
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload",2);


          Assertions.assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000000");
        })
        .verifyComplete();

  }

  private static Stream<Arguments> byType(){
    return Stream.of(
        Arguments.of("I", false),
        Arguments.of("C", false),
        Arguments.of("USP", false),
        Arguments.of("RSP", true)
    );
  }

}