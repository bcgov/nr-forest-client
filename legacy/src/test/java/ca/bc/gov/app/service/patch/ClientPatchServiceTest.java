package ca.bc.gov.app.service.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | ClientPatchService")
class ClientPatchServiceTest {

  private ObjectMapper mapper;
  private ClientPatchOperation operation1;
  private ClientPatchOperation operation2;
  private ReactiveTransactionManager transactionManager;
  private ReactiveTransaction reactiveTransaction;

  private ClientPatchService clientPatchService;

  @BeforeEach
  void setUp() {
    mapper = new JsonMapper();
    operation1 = mock(ClientPatchOperation.class);
    operation2 = mock(ClientPatchOperation.class);
    transactionManager = mock(ReactiveTransactionManager.class);
    reactiveTransaction = mock(ReactiveTransaction.class);

    when(transactionManager.getReactiveTransaction(any(TransactionDefinition.class)))
        .thenReturn(Mono.just(reactiveTransaction));
    when(transactionManager.commit(reactiveTransaction))
        .thenReturn(Mono.empty());
    when(transactionManager.rollback(reactiveTransaction))
        .thenReturn(Mono.empty());

    when(operation1.applyPatch(any(), any(), any(), any()))
        .thenReturn(Mono.empty());
    when(operation2.applyPatch(any(), any(), any(), any()))
        .thenReturn(Mono.empty());

    clientPatchService = new ClientPatchService(
        mapper,
        List.of(operation1, operation2),
        transactionManager
    );
  }

  @Test
  @DisplayName("Pass provided userId to downstream patch services")
  void shouldPassUserIdWhenProvided() {
    JsonNode patch = mapper.createArrayNode();

    StepVerifier.create(clientPatchService.patchClient("00000001", patch, "idir\\johnsmith"))
        .verifyComplete();

    verify(operation1).applyPatch("00000001", patch, mapper, "idir\\johnsmith");
    verify(operation2).applyPatch("00000001", patch, mapper, "idir\\johnsmith");
    verify(transactionManager).commit(reactiveTransaction);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  @DisplayName("Fallback to ApplicationConstants.DEFAULT_USER_ID when userId is null, empty, or blank")
  void shouldFallbackToDefaultUserIdWhenBlank(String userId) {
    JsonNode patch = mapper.createArrayNode();

    StepVerifier.create(clientPatchService.patchClient("00000001", patch, userId))
        .verifyComplete();

    ArgumentCaptor<String> userCaptor1 = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> userCaptor2 = ArgumentCaptor.forClass(String.class);

    verify(operation1).applyPatch(eq("00000001"), eq(patch), eq(mapper), userCaptor1.capture());
    verify(operation2).applyPatch(eq("00000001"), eq(patch), eq(mapper), userCaptor2.capture());

    assertEquals(ApplicationConstants.DEFAULT_USER_ID, userCaptor1.getValue());
    assertEquals(ApplicationConstants.DEFAULT_USER_ID, userCaptor2.getValue());
    verify(transactionManager).commit(reactiveTransaction);
  }
}
