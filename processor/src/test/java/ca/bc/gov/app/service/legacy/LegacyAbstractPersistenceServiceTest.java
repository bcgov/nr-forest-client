package ca.bc.gov.app.service.legacy;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.entity.client.CountryCodeEntity;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.legacy.ForestClientContactEntity;
import ca.bc.gov.app.entity.legacy.ForestClientLocationEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
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

  @BeforeEach
  void beforeEach() {
    when(countryCodeRepository.findAll())
        .thenReturn(Flux.just(new CountryCodeEntity("CA", "Canada")));
  }

  @Test
  @DisplayName("create locations")
  void shouldCreateLocations() {
    ReactiveInsert<ForestClientLocationEntity> reactiveInsert = mock(ReactiveInsert.class);

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
    when(legacyR2dbcEntityTemplate.insert(eq(ForestClientLocationEntity.class)))
        .thenReturn(reactiveInsert);
    when(reactiveInsert.using(any()))
        .thenReturn(Mono.just(ForestClientLocationEntity.builder()
                .clientLocnCode("00")
                .build()
            )
        );
    when(legacyR2dbcEntityTemplate.selectOne(any(), any()))
        .thenReturn(Mono.empty());

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
  @DisplayName("create contacts")
  void shouldCreateContacts() {
    ReactiveInsert<ForestClientContactEntity> insert = mock(ReactiveInsert.class);

    when(locationContactRepository.findBySubmissionLocationId(eq(1)))
        .thenReturn(Flux.just(
                SubmissionLocationContactEntity
                    .builder()
                    .submissionLocationId(1)
                    .submissionContactId(1)
                    .build()
            )
        );
    when(contactRepository.findById(eq(1)))
        .thenReturn(Mono.just(
                SubmissionContactEntity
                    .builder()
                    .submissionContactId(1)
                    .submissionId(2)
                    .contactTypeCode("BL")
                    .firstName("JOHN")
                    .lastName("DOE")
                    .businessPhoneNumber("2505555555")
                    .emailAddress("mail@mail.ca")
                    .build()
            )
        );
    when(legacyR2dbcEntityTemplate.insert(ForestClientContactEntity.class))
        .thenReturn(insert);
    when(insert.using(any()))
        .thenReturn(Mono.just(ForestClientContactEntity.builder().contactName("Text").build()));
    when(legacyR2dbcEntityTemplate.selectOne(any(), any()))
        .thenReturn(Mono.empty());

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

}
