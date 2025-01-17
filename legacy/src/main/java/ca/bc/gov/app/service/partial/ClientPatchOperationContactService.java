package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import ca.bc.gov.app.utils.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(2)
public class ClientPatchOperationContactService implements ClientPatchOperationService {

  private final ForestClientContactRepository contactRepository;

  @Override
  public String getPrefix() {
    return "contacts";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of(
        "/contactCode",
        "/contactName",
        "/businessPhone",
        "/secondaryPhone",
        "/faxNumber",
        "/emailAddress"
    );
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonPatch contactPatch, ObjectMapper mapper) {

    Mono<Void> removeMono =
        Flux
            .fromIterable(removePatchedContacts(contactPatch, mapper))
            .map(Long::valueOf)
            .filterWhen(contactRepository::existsById)
            .doOnNext(contactId -> log.info("Removing Forest Client Contact with id {}", contactId))
            .flatMap(contactRepository::deleteById)
            .collectList()
            .then();

    Mono<Void> addMono =
        Flux
            .fromIterable(addPatchedContact(contactPatch, clientNumber, mapper))
            .doOnNext(contact -> log.info("Adding Forest Client Contact {}", contact))
            .flatMap(contactRepository::save)
            .collectList()
            .then();

    Mono<Void> updateMono =
        updatePatchedContact(contactPatch, mapper)
            .doOnNext(contact -> log.info("Updating Forest Client Contact {}", contact))
            .flatMap(contactRepository::save)
            .collectList()
            .then();

    return removeMono.then(addMono).then(updateMono);

  }

  private List<ForestClientContactEntity> addPatchedContact(JsonPatch patch, String clientNumber,
      ObjectMapper mapper) {

    List<ForestClientContactEntity> addedVehicle = new ArrayList<>();

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(patch, "add", "contacts", mapper);

    filteredNode.forEach(node ->
        addedVehicle.add(
            PatchUtils
                .loadAddValue(
                    node,
                    ForestClientContactEntity.class,
                    mapper
                )
                .withClientNumber(clientNumber)
        )
    );

    return addedVehicle;
  }

  private Flux<ForestClientContactEntity> updatePatchedContact(JsonPatch patch,
      ObjectMapper mapper) {

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    Set<Long> contactIds = PatchUtils
        .loadIds(filteredNode)
        .stream()
        .map(Long::valueOf)
        .collect(Collectors.toSet());

    return
        contactRepository
            .findAllById(contactIds)
            .flatMap(contact ->
                Mono
                    .just(PatchUtils.patchClient(
                        PatchUtils.filterPatchOperations(
                            filteredNode,
                            contact.getClientContactId().toString(),
                            getRestrictedPaths(),
                            mapper
                        ),
                        contact,
                        ForestClientContactEntity.class,
                        mapper
                    )
                    )
                    .filter(client -> !contact.equals(client))
                    .doOnNext(client -> log.info("Detected Forest Client Contact changes {}", client))
            );
  }

  private List<String> removePatchedContacts(JsonPatch patch, ObjectMapper mapper) {

    List<String> removedContacts = new ArrayList<>();

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(patch, "remove", "contacts", mapper);

    filteredNode.forEach(node ->
        removedContacts.add(
            node.get("path").asText().replace("/", StringUtils.EMPTY))
    );

    return removedContacts;
  }

}
