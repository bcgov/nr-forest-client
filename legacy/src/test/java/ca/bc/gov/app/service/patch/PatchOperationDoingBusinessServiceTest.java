package ca.bc.gov.app.service.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.service.ClientDoingBusinessAsService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | PatchOperationDoingBusinessService")
class PatchOperationDoingBusinessServiceTest {

  private ClientDoingBusinessAsRepository dbaRepository;
  private ClientDoingBusinessAsService service;
  private ReactiveTransactionManager transactionManager;
  private ObjectMapper mapper;

  private PatchOperationDoingBusinessService patchService;

  @BeforeEach
  void setup() {
    dbaRepository = mock(ClientDoingBusinessAsRepository.class);
    service = mock(ClientDoingBusinessAsService.class);
    transactionManager = mock(ReactiveTransactionManager.class);
    mapper = new JsonMapper();

    ReactiveTransaction transaction = mock(ReactiveTransaction.class);
    when(transactionManager.getReactiveTransaction(any())).thenReturn(Mono.just(transaction));
    when(transactionManager.commit(any())).thenReturn(Mono.empty());
    when(transactionManager.rollback(any())).thenReturn(Mono.empty());

    patchService = new PatchOperationDoingBusinessService(
        dbaRepository,
        service,
        transactionManager
    );
  }

  @Test
  @DisplayName("Check prefix")
  void shouldReturnPrefix() {
    assertEquals("doingBusinessAs", patchService.getPrefix());
  }

  @Test
  @DisplayName("Check restricted paths")
  void shouldReturnEmptyRestrictedPaths() {
    assertTrue(patchService.getRestrictedPaths().isEmpty());
  }

  @Test
  @DisplayName("Ignore patches that do not target doingBusinessAs")
  void shouldIgnoreIrrelevantPatch() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/client/clientComment\",\"value\":\"test\"}]"
    );

    StepVerifier.create(patchService.applyPatch("00175721", patch, mapper, "user1"))
        .verifyComplete();

    verify(dbaRepository, never()).findByClientNumber(anyString());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "[{\"op\":\"remove\",\"path\":\"/doingBusinessAs\"}]",
      "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":\"\"}]",
      "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":\"   \"}]",
      "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":null}]"
  })
  @DisplayName("Delete existing DBA when patch indicates removal or blank value")
  void shouldDeleteExistingDbaOnRemoval(String patchString) {
    ClientDoingBusinessAsEntity existingDba = ClientDoingBusinessAsEntity.builder()
        .id(42)
        .clientNumber("00175721")
        .doingBusinessAsName("BABINE FOREST PRODUCTS")
        .revision(1L)
        .createdAt(LocalDateTime.now())
        .createdBy("user1")
        .updatedAt(LocalDateTime.now())
        .updatedBy("user1")
        .createdByUnit(70L)
        .updatedByUnit(70L)
        .build();

    when(dbaRepository.findByClientNumber("00175721")).thenReturn(Flux.just(existingDba));
    when(dbaRepository.delete(existingDba)).thenReturn(Mono.empty());

    JsonNode patch = mapper.readTree(patchString);

    StepVerifier.create(patchService.applyPatch("00175721", patch, mapper, "user1"))
        .verifyComplete();

    verify(dbaRepository).delete(existingDba);
    verify(transactionManager).getReactiveTransaction(any());
  }

  @Test
  @DisplayName("Complete safely when removing DBA but client has no DBA")
  void shouldCompleteSafelyWhenNoDbaToRemove() {
    when(dbaRepository.findByClientNumber("00175721")).thenReturn(Flux.empty());

    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":\"\"}]"
    );

    StepVerifier.create(patchService.applyPatch("00175721", patch, mapper, "user1"))
        .verifyComplete();

    verify(dbaRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Update existing DBA when valid name is provided")
  void shouldUpdateExistingDbaWhenValidNameProvided() {
    ClientDoingBusinessAsEntity existingDba = ClientDoingBusinessAsEntity.builder()
        .id(42)
        .clientNumber("00175721")
        .doingBusinessAsName("OLD NAME")
        .revision(1L)
        .createdAt(LocalDateTime.now())
        .createdBy("user1")
        .updatedAt(LocalDateTime.now())
        .updatedBy("user1")
        .createdByUnit(70L)
        .updatedByUnit(70L)
        .build();

    when(dbaRepository.findByClientNumber("00175721")).thenReturn(Flux.just(existingDba));
    when(dbaRepository.save(any())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0))
    );

    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":\"New Name\"}]"
    );

    StepVerifier.create(patchService.applyPatch("00175721", patch, mapper, "user2"))
        .verifyComplete();

    verify(dbaRepository).save(any(ClientDoingBusinessAsEntity.class));
    verify(service, never()).saveAndGetIndex(any());
  }

  @Test
  @DisplayName("Create new DBA when none exists and valid name is provided")
  void shouldCreateNewDbaWhenNoneExistsAndValidNameProvided() {
    when(dbaRepository.findByClientNumber("00175721")).thenReturn(Flux.empty());
    when(service.saveAndGetIndex(any())).thenReturn(Mono.just("00175721"));

    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":\"New Name\"}]"
    );

    StepVerifier.create(patchService.applyPatch("00175721", patch, mapper, "user2"))
        .verifyComplete();

    verify(dbaRepository, never()).save(any());
    verify(service).saveAndGetIndex(any(ClientDoingBusinessAsDto.class));
  }

  @Test
  @DisplayName("Fallback to default userId (idir\\ottomated) when userId is null or blank")
  void shouldFallbackToDefaultUserIdWhenBlank() {
    ClientDoingBusinessAsEntity existingDba = ClientDoingBusinessAsEntity.builder()
        .id(42)
        .clientNumber("00175721")
        .doingBusinessAsName("OLD NAME")
        .revision(1L)
        .createdAt(LocalDateTime.now())
        .createdBy("user1")
        .updatedAt(LocalDateTime.now())
        .updatedBy("user1")
        .createdByUnit(70L)
        .updatedByUnit(70L)
        .build();

    when(dbaRepository.findByClientNumber("00175721")).thenReturn(Flux.just(existingDba));
    ArgumentCaptor<ClientDoingBusinessAsEntity> captor =
        ArgumentCaptor.forClass(ClientDoingBusinessAsEntity.class);
    when(dbaRepository.save(captor.capture())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0))
    );

    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\",\"value\":\"New Name\"}]"
    );

    StepVerifier.create(patchService.applyPatch("00175721", patch, mapper, null))
        .verifyComplete();

    assertEquals("idir\\ottomated", captor.getValue().getUpdatedBy());
  }

  @Test
  @DisplayName("Throw exception when patch format has no value for non-delete op")
  void shouldThrowWhenNoValueProvided() {
    JsonNode patch = mapper.readTree(
        "[{\"op\":\"replace\",\"path\":\"/doingBusinessAs\"}]"
    );

    assertThrows(IllegalArgumentException.class, () ->
        patchService.applyPatch("00175721", patch, mapper, "user1")
    );

    verify(dbaRepository, never()).findByClientNumber(anyString());
  }

}
