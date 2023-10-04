package ca.bc.gov.app.service.legacy;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.legacy.ForestClientContactEntity;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.entity.legacy.ForestClientLocationEntity;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ForestClientContactRepository;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation.ReactiveInsert;
import org.springframework.integration.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legacy Persistence Service")
class LegacyPersistenceServiceTest {

  private final SubmissionDetailRepository submissionDetailRepository = mock(
      SubmissionDetailRepository.class);
  private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
  private final SubmissionLocationRepository locationRepository = mock(
      SubmissionLocationRepository.class);
  private final ForestClientRepository forestClientRepository = mock(ForestClientRepository.class);
  private final ForestClientContactRepository forestClientContactRepository = mock(
      ForestClientContactRepository.class);
  private final SubmissionContactRepository contactRepository = mock(
      SubmissionContactRepository.class);
  private final SubmissionLocationContactRepository locationContactRepository = mock(
      SubmissionLocationContactRepository.class);
  private final R2dbcEntityTemplate legacyR2dbcEntityTemplate = mock(R2dbcEntityTemplate.class);

  private final LegacyPersistenceService service = new LegacyPersistenceService(
      submissionDetailRepository,
      submissionRepository,
      locationRepository,
      forestClientRepository,
      forestClientContactRepository,
      contactRepository,
      locationContactRepository,
      legacyR2dbcEntityTemplate
  );

  @Test
  @DisplayName("create forest client")
  void shouldCreateForestClient() {
    SubmissionDetailEntity entity = SubmissionDetailEntity.builder()
        .submissionId(2)
        .submissionDetailId(2)
        .organizationName("STAR DOT STAR VENTURES")
        .incorporationNumber("FM0159297")
        .businessTypeCode("R")
        .clientTypeCode("P")
        .goodStandingInd("Y")
        .build();

    when(submissionDetailRepository.findBySubmissionId(eq(2)))
        .thenReturn(Mono.just(entity));
    when(forestClientRepository.save(any(ForestClientEntity.class)))
        .thenReturn(Mono.just(ForestClientEntity.builder()
                .clientNumber("00000000")
                .build()
            )
        );
    when(submissionDetailRepository.save(any(SubmissionDetailEntity.class)))
        .thenReturn(Mono.just(entity));

    service
        .createForestClient(
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

        })
        .verifyComplete();
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
    when(forestClientContactRepository.save(any(ForestClientContactEntity.class)))
        .thenReturn(Mono.just(ForestClientContactEntity.builder().build()));

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

    when(contactRepository.findFirstBySubmissionId(eq(2)))
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
                .userId(ApplicationConstant.PROCESSOR_USER_NAME)
                .build()
        ));

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
                .isInstanceOf(EmailRequestDto.class)
                .hasFieldOrPropertyWithValue("incorporation", "FM0159297")
                .hasFieldOrPropertyWithValue("name", "STAR DOT STAR VENTURES")
                .hasFieldOrPropertyWithValue("userId", ApplicationConstant.PROCESSOR_USER_NAME)
                .hasFieldOrPropertyWithValue("userName", "JOHN")
                .hasFieldOrPropertyWithValue("email", "mail@mail.ca")
                .hasFieldOrPropertyWithValue("templateName", "approval")
                .hasFieldOrPropertyWithValue("subject", "Success")
                .hasFieldOrPropertyWithValue("variables", Map.of(
                        "userName", "JOHN",
                        "business", Map.of(
                            "name", "STAR DOT STAR VENTURES",
                            "clientNumber", "00000000"
                        )
                    )
                )
        )
        .verifyComplete();
  }

}