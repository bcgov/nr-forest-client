package ca.bc.gov.app.service.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | PatchOperationContactRemoveService")
class PatchOperationContactRemoveServiceTest {

  private R2dbcEntityOperations entityTemplate;
  private ReactiveTransactionManager transactionManager;
  private DatabaseClient databaseClient;
  private DatabaseClient.GenericExecuteSpec executeSpec;
  private FetchSpec<Map<String, Object>> fetchSpec;
  private ObjectMapper mapper;

  private PatchOperationContactRemoveService service;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    entityTemplate = mock(R2dbcEntityOperations.class);
    transactionManager = mock(ReactiveTransactionManager.class);
    databaseClient = mock(DatabaseClient.class);
    executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
    fetchSpec = mock(FetchSpec.class);
    mapper = new JsonMapper();

    ReactiveTransaction transaction = mock(ReactiveTransaction.class);
    when(transactionManager.getReactiveTransaction(any())).thenReturn(Mono.just(transaction));
    when(transactionManager.commit(any())).thenReturn(Mono.empty());
    when(transactionManager.rollback(any())).thenReturn(Mono.empty());

    when(entityTemplate.getDatabaseClient()).thenReturn(databaseClient);
    when(databaseClient.sql(any(String.class))).thenReturn(executeSpec);
    when(executeSpec.bind(any(String.class), any())).thenReturn(executeSpec);
    when(executeSpec.fetch()).thenReturn(fetchSpec);
    when(fetchSpec.all()).thenReturn(Flux.empty());
    when(fetchSpec.one()).thenReturn(Mono.just(Map.of("IN_USE_COUNT", 0L, "CLIENT_CONTACT_ID", 26L)));
    when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

    service = new PatchOperationContactRemoveService(entityTemplate, transactionManager);
  }

  @Test
  @DisplayName("Check prefix")
  void shouldReturnPrefix() {
    assertEquals("contacts", service.getPrefix());
  }

  @Test
  @DisplayName("Check restricted paths")
  void shouldReturnEmptyRestrictedPaths() {
    assertTrue(service.getRestrictedPaths().isEmpty());
  }

  @Test
  @DisplayName("Ignore remove operations on field subpaths such as emailAddress")
  void shouldIgnoreSubpathRemovalsWithoutDeletingContact() {
    // When a caller sends op: "remove" on a contact attribute, ContactRemoveService should NOT
    // attempt to parse the subpath as a Long and delete the whole contact.
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"remove\",\"path\":\"/contacts/26/emailAddress\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    verify(databaseClient, never()).sql(any(String.class));
  }

  @Test
  @DisplayName("Delete contact when remove operation targets contact root ID")
  void shouldDeleteContactOnRootRemove() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"remove\",\"path\":\"/contacts/26\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    verify(databaseClient, org.mockito.Mockito.atLeastOnce()).sql(any(String.class));
  }

  @Test
  @DisplayName("Ignore remove operations with overflow or non-long numeric paths without throwing NumberFormatException")
  void shouldIgnoreOverflowContactIdsWithoutError() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"remove\",\"path\":\"/contacts/99999999999999999999999999\"}]"
    );

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    verify(databaseClient, never()).sql(any(String.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[{\"op\":\"remove\",\"path\":\"/contacts/26/0\"}]",
      "[{\"op\":\"remove\",\"path\":\"/contacts/26/0/1\"}]",
      "[{\"op\":\"remove\",\"path\":\"/contacts/12/34\"}]",
      "[{\"op\":\"remove\",\"path\":\"/contacts/26/locationCodes/0\"}]"
  })
  @DisplayName("Ignore remove operations on numeric subpaths without deleting contact")
  void shouldIgnoreNumericSubpathRemovalsWithoutDeletingContact(String patchString) {
    JsonNode patch = mapper.readTree(patchString);

    StepVerifier.create(service.applyPatch("00000159", patch, mapper, "user1"))
        .verifyComplete();

    verify(databaseClient, never()).sql(any(String.class));
  }
}
