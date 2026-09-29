package ca.bc.gov.app.service.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | PatchOperationContactEditService")
class PatchOperationContactEditServiceTest {

  private R2dbcEntityOperations entityTemplate;
  private DatabaseClient databaseClient;
  private DatabaseClient.GenericExecuteSpec executeSpec;
  private FetchSpec<Map<String, Object>> fetchSpec;
  private ObjectMapper mapper;

  private PatchOperationContactEditService service;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    entityTemplate = mock(R2dbcEntityOperations.class);
    databaseClient = mock(DatabaseClient.class);
    executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
    fetchSpec = mock(FetchSpec.class);
    mapper = new JsonMapper();

    ForestClientContactEntity contact = ForestClientContactEntity.builder()
        .clientContactId(26L)
        .clientNumber("00000159")
        .clientLocnCode("00")
        .contactCode("BL")
        .contactName("TEST CONTACT")
        .emailAddress("old@example.com")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user1")
        .updatedBy("user1")
        .createdByUnit(70L)
        .updatedByUnit(70L)
        .revision(1L)
        .build();

    when(entityTemplate.selectOne(any(Query.class), eq(ForestClientContactEntity.class)))
        .thenReturn(Mono.just(contact));

    when(entityTemplate.getDatabaseClient()).thenReturn(databaseClient);
    when(databaseClient.sql(any(String.class))).thenReturn(executeSpec);
    when(executeSpec.bind(any(String.class), any())).thenReturn(executeSpec);
    when(executeSpec.fetch()).thenReturn(fetchSpec);
    when(fetchSpec.all()).thenReturn(Flux.just(Map.of("CLIENT_CONTACT_ID", "26")));

    when(entityTemplate.update(any(Query.class), any(Update.class), eq(ForestClientContactEntity.class)))
        .thenReturn(Mono.just(1L));

    service = new PatchOperationContactEditService(entityTemplate);
  }

  @Test
  @DisplayName("Check prefix")
  void shouldReturnPrefix() {
    assertEquals("contacts", service.getPrefix());
  }

  @Test
  @DisplayName("Check restricted paths")
  void shouldReturnRestrictedPaths() {
    assertTrue(service.getRestrictedPaths().contains("/emailAddress"));
    assertTrue(service.getRestrictedPaths().contains("/contactName"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[{\"op\":\"remove\",\"path\":\"/contacts/26/emailAddress\"}]",
      "[{\"op\":\"replace\",\"path\":\"/contacts/26/emailAddress\",\"value\":null}]"
  })
  @DisplayName("Clear contact email when op is remove or replace with null")
  void shouldClearContactEmailOnRemoveOrNull(String patchString) {
    JsonNode patch = mapper.readTree(patchString);

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientContactEntity.class));

    Update update = updateCaptor.getValue();
    assertTrue(update.getAssignments().keySet().stream().anyMatch(k -> k.getReference().equalsIgnoreCase("email_address")));
  }

  @Test
  @DisplayName("Fallback to default userId when blank")
  void shouldFallbackToDefaultUserIdWhenBlank() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/contacts/26/emailAddress\",\"value\":\"new@test.com\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "   "))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientContactEntity.class));

    Update update = updateCaptor.getValue();
    assertEquals(ApplicationConstants.DEFAULT_USER_ID,
        update.getAssignments().entrySet().stream()
            .filter(e -> e.getKey().getReference().equalsIgnoreCase("update_userid"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null)
    );
  }

  @Test
  @DisplayName("Preserve operation order when remove is followed by replace")
  void shouldPreserveOrderWhenRemoveFollowedByReplace() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"remove\",\"path\":\"/contacts/26/emailAddress\"},"
            + "{\"op\":\"replace\",\"path\":\"/contacts/26/emailAddress\",\"value\":\"final@test.com\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientContactEntity.class));

    Update update = updateCaptor.getValue();
    assertEquals("final@test.com",
        update.getAssignments().entrySet().stream()
            .filter(e -> e.getKey().getReference().equalsIgnoreCase("email_address"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null)
    );
  }

  @Test
  @DisplayName("Preserve operation order when replace is followed by remove")
  void shouldPreserveOrderWhenReplaceFollowedByRemove() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/contacts/26/emailAddress\",\"value\":\"temp@test.com\"},"
            + "{\"op\":\"remove\",\"path\":\"/contacts/26/emailAddress\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientContactEntity.class));

    Update update = updateCaptor.getValue();
    Map.Entry<?, Object> emailEntry = update.getAssignments().entrySet().stream()
        .filter(e -> e.getKey().getReference().equalsIgnoreCase("email_address"))
        .findFirst()
        .orElse(null);
    org.junit.jupiter.api.Assertions.assertNotNull(emailEntry);
    org.junit.jupiter.api.Assertions.assertNull(emailEntry.getValue());
  }

  @Test
  @DisplayName("Ignore operations with overflow contact IDs without throwing NumberFormatException")
  void shouldIgnoreOverflowContactIdsWithoutError() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/contacts/99999999999999999999999999/emailAddress\",\"value\":\"new@test.com\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    verify(entityTemplate, never()).update(any(Query.class), any(Update.class), eq(ForestClientContactEntity.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[{\"op\":\"remove\",\"path\":\"/contacts/26/contactName\"}]",
      "[{\"op\":\"replace\",\"path\":\"/contacts/26/contactName\",\"value\":null}]",
      "[{\"op\":\"replace\",\"path\":\"/contacts/26/contactName\",\"value\":\"\"}]",
      "[{\"op\":\"replace\",\"path\":\"/contacts/26/contactName\",\"value\":\"   \"}]",
      "[{\"op\":\"remove\",\"path\":\"/contacts/26/contactTypeCode\"}]",
      "[{\"op\":\"replace\",\"path\":\"/contacts/26/contactTypeCode\",\"value\":null}]",
      "[{\"op\":\"replace\",\"path\":\"/contacts/26/contactTypeCode\",\"value\":\"\"}]"
  })
  @DisplayName("Reject removing or clearing mandatory contact fields with 400 Bad Request")
  void shouldFailWhenClearingMandatoryContactField(String patchJson) {
    JsonNode patch = mapper.readTree(patchJson);

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .expectErrorMatches(throwable ->
            throwable instanceof org.springframework.web.server.ResponseStatusException rse
                && rse.getStatusCode().value() == 400
        )
        .verify();

    verify(entityTemplate, never()).update(any(Query.class), any(Update.class), eq(ForestClientContactEntity.class));
  }
}
