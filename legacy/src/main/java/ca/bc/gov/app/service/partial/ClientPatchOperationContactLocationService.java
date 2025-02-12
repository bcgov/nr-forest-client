package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import ca.bc.gov.app.service.ClientContactService;
import ca.bc.gov.app.utils.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(4)
public class ClientPatchOperationContactLocationService implements ClientPatchOperationService {

  private final ForestClientContactRepository contactRepository;
  private final ClientContactService clientContactService;
  private final AbstractForestClientMapper<ForestClientContactDto, ForestClientContactEntity> contactMapper;

  @Override
  public String getPrefix() {
    return "contacts";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of("/locationCode");
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonPatch contactPatch, ObjectMapper mapper) {

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        contactPatch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    Map<Long, List<String>> idToLocationAssociation = StreamSupport
        .stream(filteredNode.spliterator(), false)
        .map(node -> {
          List<String> locationIds = new ArrayList<>();
          node.get("value").forEach(locationId -> locationIds.add(locationId.asText()));
          return Pair.of(Long.valueOf(PatchUtils.loadId(node)), locationIds);
        })
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    return
        Flux
            .fromIterable(PatchUtils.loadIds(filteredNode))
            .map(Long::valueOf)
            .flatMap(contactRepository::findById)
            .flatMap(contact ->
                extractChangePair(
                    contact.getClientNumber(),
                    contact.getContactName(),
                    idToLocationAssociation.get(contact.getClientContactId())
                )
                    .flatMap(pair ->
                        processRemovals(
                            contact.getClientNumber(),
                            contact.getContactName(),
                            pair.getRight()
                        )
                            .then(addPatchedContact(contact, pair.getLeft()))
                    )
            )
            .collectList()
            .then();
  }


  private Mono<Void> addPatchedContact(
      ForestClientContactEntity contact,
      Set<String> locations
  ) {

    return
        Flux
            .fromIterable(locations)
            .map(location -> contactMapper.toDto(contact).withClientLocnCode(location))
            .flatMap(clientContactService::saveAndGetIndex)
            .collectList()
            .then();
  }

  private Mono<Void> processRemovals(
      String clientNumber,
      String contactName,
      Set<String> removedLocations
  ) {
    return
        Mono
            .just(removedLocations)
            .filter(removed -> !removed.isEmpty())
            .flatMap(locations ->
                contactRepository
                    .deleteByClientNumberAndAndContactNameAndClientLocnCodeIn(
                        clientNumber,
                        contactName,
                        locations
                    )
            );
  }

  private Pair<Set<String>, Set<String>> extractAddRemoveLocations(List<String> original,
      List<String> changed) {

    Set<String> originalSet = new HashSet<>(original);
    Set<String> changedSet = new HashSet<>(changed);
    // Find entries to be added
    Set<String> toBeAdded = new HashSet<>(changedSet);
    toBeAdded.removeAll(originalSet);
    // Find entries to be removed
    Set<String> toBeRemoved = new HashSet<>(originalSet);
    toBeRemoved.removeAll(changedSet);

    return Pair.of(toBeAdded, toBeRemoved);

  }

  private Mono<Pair<Set<String>, Set<String>>> extractChangePair(
      String clientNumber,
      String contactName,
      List<String> changedLocations
  ) {
    return
        contactRepository
            .findAllByClientNumberAndContactName(clientNumber, contactName)
            .map(ForestClientContactEntity::getClientLocnCode)
            .distinct()
            .collectList()
            .map(locationCodes -> extractAddRemoveLocations(locationCodes, changedLocations));
  }
}
