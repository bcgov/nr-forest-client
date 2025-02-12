package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.utils.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(5)
public class ClientPatchOperationDbaService implements ClientPatchOperationService {

  private final ClientDoingBusinessAsRepository dbaRepository;

  @Override
  public String getPrefix() {
    return "doingBusinessAs";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of(
        "/doingBusinessAsName"
    );
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonPatch patch, ObjectMapper mapper) {

    Mono<Void> addMono =
        Flux
            .fromIterable(addPatchedLocation(clientNumber, patch, mapper))
            .doOnNext(contact -> log.info("Adding Forest Client doing business as Location {}", contact))
            .flatMap(dbaRepository::save)
            .collectList()
            .then();

    Mono<Void> updateMono =
        updatePatchedLocation(patch, mapper)
            .doOnNext(contact -> log.info("Updating Forest Client doing business as Location {}", contact))
            .flatMap(dbaRepository::save)
            .collectList()
            .then();

    return addMono.then(updateMono);
  }


  private List<ClientDoingBusinessAsEntity> addPatchedLocation(
      String clientNumber,
      JsonPatch patch,
      ObjectMapper mapper
  ) {

    List<ClientDoingBusinessAsEntity> addedLocation = new ArrayList<>();

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "add",
        getPrefix(),
        mapper
    );

    filteredNode.forEach(node ->
        addedLocation.add(
            PatchUtils
                .loadAddValue(
                    node,
                    ClientDoingBusinessAsEntity.class,
                    mapper
                )
                .withClientNumber(clientNumber)
        )
    );

    return addedLocation;
  }

  private Flux<ClientDoingBusinessAsEntity> updatePatchedLocation(
      JsonPatch patch,
      ObjectMapper mapper) {

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    Stream<String> dbaIds = PatchUtils
        .loadIds(filteredNode)
        .stream()
        .distinct();

    return
        Flux
            .fromStream(dbaIds)
            .flatMap(dbaRepository::findById)
            .flatMap(dbaEntity ->
                Mono
                    .just(PatchUtils.patchClient(
                            PatchUtils.filterPatchOperations(
                                filteredNode,
                                dbaEntity.getId().toString(),
                                getRestrictedPaths(),
                                mapper
                            ),
                            dbaEntity,
                            ClientDoingBusinessAsEntity.class,
                            mapper
                        )
                    )
                    .filter(client -> !dbaEntity.equals(client))
                    .doOnNext(
                        client -> log.info("Detected Forest Client Doing business as changes {}", client))
            );
  }
}
