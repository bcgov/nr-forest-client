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
import ca.bc.gov.app.entity.RelatedClientEntity;
import ca.bc.gov.app.repository.RelatedClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDeleteOperation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | PatchOperationsRelatedClientService")
class PatchOperationsRelatedClientServiceTest {

  private R2dbcEntityOperations entityTemplate;
  private RelatedClientRepository relatedClientRepository;
  private DatabaseClient databaseClient;
  private DatabaseClient.GenericExecuteSpec executeSpec;
  private FetchSpec<java.util.Map<String, Object>> fetchSpec;
  private ReactiveDeleteOperation.ReactiveDelete reactiveDelete;
  private ReactiveDeleteOperation.TerminatingDelete terminatingDelete;
  private ObjectMapper mapper;

  private PatchOperationsRelatedClientService patchService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    entityTemplate = mock(R2dbcEntityOperations.class);
    relatedClientRepository = mock(RelatedClientRepository.class);
    databaseClient = mock(DatabaseClient.class);
    executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
    fetchSpec = mock(FetchSpec.class);
    reactiveDelete = mock(ReactiveDeleteOperation.ReactiveDelete.class);
    terminatingDelete = mock(ReactiveDeleteOperation.TerminatingDelete.class);
    mapper = new JsonMapper();

    when(entityTemplate.delete(RelatedClientEntity.class)).thenReturn(reactiveDelete);
    when(reactiveDelete.matching(any())).thenReturn(terminatingDelete);
    when(terminatingDelete.all()).thenReturn(Mono.just(1L));

    RelatedClientEntity existingEntity = RelatedClientEntity.builder()
        .clientNumber("00000158")
        .clientLocationCode("00")
        .relationshipType("JV")
        .relatedClientNumber("00000159")
        .relatedClientLocationCode("00")
        .revision(1L)
        .build();

    when(relatedClientRepository.findToUpdate(any(), any(), any(), any(), any()))
        .thenReturn(Mono.just(existingEntity));

    when(entityTemplate.getDatabaseClient()).thenReturn(databaseClient);
    when(databaseClient.sql(any(String.class))).thenReturn(executeSpec);
    when(executeSpec.bind(any(String.class), any())).thenReturn(executeSpec);
    when(executeSpec.fetch()).thenReturn(fetchSpec);
    when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

    patchService = new PatchOperationsRelatedClientService(
        entityTemplate,
        relatedClientRepository
    );
  }

  @Test
  @DisplayName("Check prefix")
  void shouldReturnPrefix() {
    assertEquals("relatedClients", patchService.getPrefix());
  }

  @Test
  @DisplayName("Check restricted paths")
  void shouldReturnEmptyRestrictedPaths() {
    assertTrue(patchService.getRestrictedPaths().isEmpty());
  }

  @Test
  @DisplayName("Ignore patches that do not target relatedClients")
  void shouldIgnoreIrrelevantPatch() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/client/clientComment\",\"value\":\"test\"}]"
    );

    StepVerifier.create(patchService.applyPatch("00000158", patch, mapper, "user1"))
        .verifyComplete();

    verify(entityTemplate, never()).delete(RelatedClientEntity.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[{\"op\":\"remove\",\"path\":\"/relatedClients/0000015800JV0000015900\"}]",
      "[{\"op\":\"replace\",\"path\":\"/relatedClients/0000015800JV0000015900\",\"value\":\"\"}]",
      "[{\"op\":\"replace\",\"path\":\"/relatedClients/0000015800JV0000015900\",\"value\":\"   \"}]",
      "[{\"op\":\"replace\",\"path\":\"/relatedClients/0000015800JV0000015900\",\"value\":null}]"
  })
  @DisplayName("Delete related client when patch indicates removal or blank value on identifier")
  void shouldDeleteRelatedClientOnRemoval(String patchString) {
    JsonNode patch = mapper.readTree(patchString);

    StepVerifier.create(patchService.applyPatch("00000158", patch, mapper, "user1"))
        .verifyComplete();

    verify(entityTemplate).delete(RelatedClientEntity.class);
    verify(executeSpec).bind(eq("userId"), eq("user1"));
  }

  @Test
  @DisplayName("Fallback to default userId when userId is null or blank on delete")
  void shouldFallbackToDefaultUserIdWhenBlank() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"remove\",\"path\":\"/relatedClients/0000015800JV0000015900\"}]"
    );

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(executeSpec.bind(eq("userId"), captor.capture())).thenReturn(executeSpec);

    StepVerifier.create(patchService.applyPatch("00000158", patch, mapper, null))
        .verifyComplete();

    assertEquals(ApplicationConstants.DEFAULT_USER_ID, captor.getValue());
  }
}
