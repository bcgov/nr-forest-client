package ca.bc.gov.app.service.legacy;


import static ca.bc.gov.app.TestConstants.CLIENT_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionContactEntity;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.entity.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.SubmissionLocationEntity;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
            new MessagingWrapper<>(
                2,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000",
                    ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES",
                    ApplicationConstant.INCORPORATION_NUMBER, "FM0159297",
                    ApplicationConstant.CLIENT_TYPE_CODE, "C"
                )
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.parameters().get(ApplicationConstant.SUBMISSION_ID))
              .as("submission id")
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.parameters().get(ApplicationConstant.CREATED_BY))
              .as("created by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.UPDATED_BY))
              .as("updated by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME))
              .as("forest client name")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("STAR DOT STAR VENTURES");

          assertThat(message.parameters().get(ApplicationConstant.INCORPORATION_NUMBER))
              .as("incorporation number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM0159297");

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000000");

          assertThat(message.parameters().get(ApplicationConstant.LOCATION_CODE))
              .as("location code")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00");

          assertThat(message.parameters().get(ApplicationConstant.LOCATION_ID))
              .as("location id")
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(1);


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

    Map<String, Object> params = Map.of(
        ApplicationConstant.SUBMISSION_ID, 2,
        ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
        ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
        ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000",
        ApplicationConstant.FOREST_CLIENT_NAME, "STAR DOT STAR VENTURES",
        ApplicationConstant.INCORPORATION_NUMBER, "FM0159297",
        ApplicationConstant.LOCATION_CODE, "00",
        ApplicationConstant.LOCATION_ID, 1,
        ApplicationConstant.CLIENT_TYPE_CODE, "C"
    );

    service.createContact(
            new MessagingWrapper<>(
                2,
                params
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.parameters().get(ApplicationConstant.SUBMISSION_ID))
              .as("submission id")
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.parameters().get(ApplicationConstant.CREATED_BY))
              .as("created by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.UPDATED_BY))
              .as("updated by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME))
              .as("forest client name")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("STAR DOT STAR VENTURES");

          assertThat(message.parameters().get(ApplicationConstant.INCORPORATION_NUMBER))
              .as("incorporation number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("FM0159297");

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000000");

          assertThat(message.parameters().get(ApplicationConstant.LOCATION_CODE))
              .as("location code")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00");

          assertThat(message.parameters().get(ApplicationConstant.LOCATION_ID))
              .as("location id")
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(1);

        })
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
            new MessagingWrapper<>(
                2,
                Map.of(
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000",
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME
                )
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull();

          assertThat(message.parameters().get(ApplicationConstant.SUBMISSION_ID))
              .as("submission id")
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.parameters().get(ApplicationConstant.CREATED_BY))
              .as("created by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.UPDATED_BY))
              .as("updated by")
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.CLIENT_TYPE_CODE))
              .as("client type code")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo(clientTypeCode);

          assertThat(message.parameters().get(ApplicationConstant.CLIENT_SUBMITTER_NAME))
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
            new MessagingWrapper<>(
                CLIENT_ENTITY,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.CREATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.UPDATED_BY, ApplicationConstant.PROCESSOR_USER_NAME,
                    ApplicationConstant.CLIENT_TYPE_CODE, "C",
                    ApplicationConstant.FOREST_CLIENT_NUMBER, "00000000",
                    ApplicationConstant.FOREST_CLIENT_NAME, "CHAMPAGNE SUPERNOVA"
                )
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .as("message")
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.parameters().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .as("forest client number")
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("00000000");
        })
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
