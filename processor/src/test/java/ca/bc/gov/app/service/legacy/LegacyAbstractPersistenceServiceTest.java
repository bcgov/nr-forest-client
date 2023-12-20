package ca.bc.gov.app.service.legacy;


import static ca.bc.gov.app.TestConstants.CLIENT_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation.ReactiveInsert;
import org.springframework.integration.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legacy Persistence Service")
class LegacyAbstractPersistenceServiceTest {

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


  @ParameterizedTest
  @MethodSource("contactExist")
  @DisplayName("create locations")
  void shouldCreateLocations(boolean locationExisted) {

    when(locationRepository.findBySubmissionId(eq(2)))
        .thenReturn(Flux.just(
                SubmissionLocationEntity
                    .builder()
                    .submissionLocationId(1)
                    .submissionId(2)
                    .streetAddress("123 MAIN ST")
                    .countryCode("CA")
                    .provinceCode("BC")
                    .cityName("VICTORIA")
                    .postalCode("V8V 3V3")
                    .name("Mailing address")
                    .build()
            )
        );
    when(legacyService.createLocation(any(), anyString(), any(), anyString()))
        .thenReturn(Mono.just("00123456"));

    service
        .createLocations(
            MessageBuilder
                .withPayload(2)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES")
                .setHeader(ApplicationConstant.INCORPORATION_NUMBER, "FM0159297")
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, "C")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

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
              .isEqualTo("00000000");

          assertThat(message.getHeaders().get(ApplicationConstant.LOCATION_CODE))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00");

          assertThat(message.getHeaders().get(ApplicationConstant.LOCATION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(1);

          assertThat(message.getHeaders().get(ApplicationConstant.TOTAL))
              .isNotNull()
              .isInstanceOf(Long.class)
              .isEqualTo(1L);

          assertThat(message.getHeaders().get(ApplicationConstant.INDEX))
              .isNotNull()
              .isInstanceOf(Long.class)
              .isEqualTo(0L);

        })
        .verifyComplete();

  }

  @ParameterizedTest
  @MethodSource("contactExist")
  @DisplayName("create contacts")
  void shouldCreateContacts(boolean contactExisted) {

    SubmissionContactEntity contact = SubmissionContactEntity
        .builder()
        .submissionContactId(1)
        .submissionId(2)
        .contactTypeCode("BL")
        .firstName("JOHN")
        .lastName("DOE")
        .businessPhoneNumber("2505555555")
        .emailAddress("mail@mail.ca")
        .build();
    when(locationContactRepository.findBySubmissionLocationId(any()))
        .thenReturn(Flux.just(
                SubmissionLocationContactEntity
                    .builder()
                    .submissionLocationId(1)
                    .submissionContactId(1)
                    .build()
            )
        );
    when(contactRepository.findById(any(Integer.class)))
        .thenReturn(Mono.just(contact));
    when(legacyService.createContact(any()))
        .thenReturn(Mono.just("00000000"));

    service.createContact(
            MessageBuilder
                .withPayload(2)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES")
                .setHeader(ApplicationConstant.INCORPORATION_NUMBER, "FM0159297")
                .setHeader(ApplicationConstant.LOCATION_CODE, "00")
                .setHeader(ApplicationConstant.LOCATION_ID, 1)
                .setHeader(ApplicationConstant.TOTAL, 1L)
                .setHeader(ApplicationConstant.INDEX, 0L)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, "C")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

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
              .isEqualTo("00000000");

          assertThat(message.getHeaders().get(ApplicationConstant.LOCATION_CODE))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00");

          assertThat(message.getHeaders().get(ApplicationConstant.LOCATION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(1);

          assertThat(message.getHeaders().get(ApplicationConstant.TOTAL))
              .isNotNull()
              .isInstanceOf(Long.class)
              .isEqualTo(1L);

          assertThat(message.getHeaders().get(ApplicationConstant.INDEX))
              .isNotNull()
              .isInstanceOf(Long.class)
              .isEqualTo(0L);

        })
        .verifyComplete();
  }

  @Test
  @DisplayName("send email")
  void shouldSendEmail() {

    service
        .sendNotification(
            MessageBuilder
                .withPayload(2)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES")
                .setHeader(ApplicationConstant.INCORPORATION_NUMBER, "FM0159297")
                .setHeader(ApplicationConstant.LOCATION_CODE, "00")
                .setHeader(ApplicationConstant.LOCATION_ID, 1)
                .setHeader(ApplicationConstant.TOTAL, 1L)
                .setHeader(ApplicationConstant.INDEX, 0L)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, "C")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(mailMessage ->
            assertThat(mailMessage.getPayload())
                .isNotNull()
                .isInstanceOf(Integer.class)
                .isEqualTo(2L)
        )
        .verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("clientData")
  @DisplayName("check client data")
  void shouldCheckClientData(
      String clientTypeCode,
      String clientNumber
  ) {

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(
                SubmissionDetailEntity
                    .builder()
                    .submissionId(2)
                    .incorporationNumber("XX0000000")
                    .organizationName("Sample test")
                    .clientTypeCode(clientTypeCode)
                    .clientNumber(clientNumber)
                    .build()
            )
        );

    when(contactRepository.findFirstBySubmissionId(any()))
        .thenReturn(Mono.just(
                SubmissionContactEntity
                    .builder()
                    .firstName("Jhon")
                    .lastName("Snow")
                    .build()
            )
        );

    service
        .checkClientData(
            MessageBuilder
                .withPayload(2)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.getHeaders().get(ApplicationConstant.SUBMISSION_ID))
              .as("submission id")
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.getHeaders().get(ApplicationConstant.CREATED_BY))
              .as("created by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.UPDATED_BY))
              .as("updated by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE))
              .as("client type code")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo(clientTypeCode);

          assertThat(message.getHeaders().get(ApplicationConstant.CLIENT_SUBMITTER_NAME))
              .as("client submitter name")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("Jhon Snow");
        })
        .verifyComplete();
  }


  @Test
  @DisplayName("create client that is not individual")
  void shouldCreateClient() {

    SubmissionDetailEntity detailEntity = SubmissionDetailEntity
        .builder()
        .submissionId(2)
        .incorporationNumber("XX0000000")
        .organizationName("Sample test")
        .clientTypeCode("C")
        .clientNumber("00000000")
        .build();

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(detailEntity));
    when(submissionDetailRepository.save(any()))
        .thenReturn(Mono.just(detailEntity));
    when(legacyService.createClient(any()))
        .thenReturn(Mono.just("00000000"));


    service
        .createForestClient(
            MessageBuilder
                .withPayload(CLIENT_ENTITY)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, "C")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .as("message")
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000000");
        })
        .verifyComplete();

  }

  @ParameterizedTest
  @MethodSource("data")
  @DisplayName("create client that is individual")
  void shouldTryToCreateClient(String type) {

    service
        .createForestClient(
            MessageBuilder
                .withPayload(CLIENT_ENTITY)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, type)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA")
                .build()
        )
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("data")
  @DisplayName("create contact that is individual")
  void shouldTryToCreateContact(String type) {
    service
        .createContact(
            MessageBuilder
                .withPayload(1)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, type)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA")
                .build()
        )
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("data")
  @DisplayName("create location that is individual")
  void shouldTryToCreateLocation(String type) {
    service
        .createLocations(
            MessageBuilder
                .withPayload(1)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .setHeader(ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME)
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, type)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA")
                .build()
        )
        .as(StepVerifier::create)
        .verifyComplete();
  }

  private static Stream<Arguments> clientData() {
    return Stream.of(
        Arguments.of("C", "00000000"),
        Arguments.of("C", null)
    );
  }

  private static Stream<String> data() {
    return Stream.of("I", "RSP", "USP");
  }

  private static Stream<Boolean> contactExist() {
    return Stream.of(true, false);
  }

}
