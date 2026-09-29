package ca.bc.gov.app.service.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import java.time.LocalDateTime;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | PatchOperationLocationService")
class PatchOperationLocationServiceTest {

  private R2dbcEntityOperations entityTemplate;
  private ObjectMapper mapper;

  private PatchOperationLocationService service;

  @BeforeEach
  void setup() {
    entityTemplate = mock(R2dbcEntityOperations.class);
    mapper = new JsonMapper();

    ForestClientLocationEntity location = ForestClientLocationEntity.builder()
        .clientNumber("00000159")
        .clientLocnCode("00")
        .clientLocnName("OFFICE")
        .addressOne("123 TEST ST")
        .emailAddress("test@test.com")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .createdBy("user1")
        .updatedBy("user1")
        .createdByUnit(70L)
        .updatedByUnit(70L)
        .revision(1L)
        .build();

    when(entityTemplate.selectOne(any(Query.class), eq(ForestClientLocationEntity.class)))
        .thenReturn(Mono.just(location));

    when(entityTemplate.update(any(Query.class), any(Update.class), eq(ForestClientLocationEntity.class)))
        .thenReturn(Mono.just(1L));

    service = new PatchOperationLocationService(entityTemplate);
  }

  @Test
  @DisplayName("Check prefix")
  void shouldReturnPrefix() {
    assertEquals("addresses", service.getPrefix());
  }

  @Test
  @DisplayName("Check restricted paths")
  void shouldReturnRestrictedPaths() {
    assertTrue(service.getRestrictedPaths().contains("/emailAddress"));
    assertTrue(service.getRestrictedPaths().contains("/addressOne"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[{\"op\":\"remove\",\"path\":\"/addresses/00/emailAddress\"}]",
      "[{\"op\":\"replace\",\"path\":\"/addresses/00/emailAddress\",\"value\":null}]"
  })
  @DisplayName("Clear location email when op is remove or replace with null")
  void shouldClearLocationEmailOnRemoveOrNull(String patchString) {
    JsonNode patch = mapper.readTree(patchString);

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientLocationEntity.class));

    Update update = updateCaptor.getValue();
    assertTrue(update.getAssignments().keySet().stream()
        .anyMatch(k -> k.getReference().equalsIgnoreCase("email_address")));
  }

  @Test
  @DisplayName("Fallback to default userId when blank")
  void shouldFallbackToDefaultUserIdWhenBlank() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/addresses/00/emailAddress\",\"value\":\"new@example.com\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "  "))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientLocationEntity.class));

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
        "[{\"op\":\"remove\",\"path\":\"/addresses/00/emailAddress\"},"
            + "{\"op\":\"replace\",\"path\":\"/addresses/00/emailAddress\",\"value\":\"final@example.com\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientLocationEntity.class));

    Update update = updateCaptor.getValue();
    assertEquals("final@example.com",
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
        "[{\"op\":\"replace\",\"path\":\"/addresses/00/emailAddress\",\"value\":\"temp@example.com\"},"
            + "{\"op\":\"remove\",\"path\":\"/addresses/00/emailAddress\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
    verify(entityTemplate).update(any(Query.class), updateCaptor.capture(), eq(ForestClientLocationEntity.class));

    Update update = updateCaptor.getValue();
    Map.Entry<?, Object> emailEntry = update.getAssignments().entrySet().stream()
        .filter(e -> e.getKey().getReference().equalsIgnoreCase("email_address"))
        .findFirst()
        .orElse(null);
    org.junit.jupiter.api.Assertions.assertNotNull(emailEntry);
    org.junit.jupiter.api.Assertions.assertNull(emailEntry.getValue());
  }
}
