package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.repository.ForestClientLocationRepository;
import ca.bc.gov.app.utils.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
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
@Order(3)
public class ClientPatchOperationLocationService implements ClientPatchOperationService {

  private final ForestClientLocationRepository locationRepository;

  @Override
  public String getPrefix() {
    return "addresses";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of(
        "/locationName",
        "/locationTypeCode",
        "/addressOne",
        "/addressTwo",
        "/addressThree",
        "/city",
        "/province",
        "/postalCode",
        "/country",
        "/businessPhone",
        "/homePhone",
        "/cellPhone",
        "/faxNumber",
        "/emailAddress",
        "/cliLocnComment",
        "/locnExpiredInd"
    );
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonPatch patch, ObjectMapper mapper) {

    Mono<Void> addMono =
        Flux
            .fromIterable(addPatchedLocation(clientNumber, patch, mapper))
            .doOnNext(contact -> log.info("Adding Forest Client Location {}", contact))
            .flatMap(locationRepository::save)
            .collectList()
            .then();

    Mono<Void> updateMono =
        updatePatchedLocation(clientNumber, patch, mapper)
            .doOnNext(contact -> log.info("Updating Forest Client Location {}", contact))
            .flatMap(locationRepository::save)
            .collectList()
            .then();

    return addMono.then(updateMono);
  }

  private List<ForestClientLocationEntity> addPatchedLocation(
      String clientNumber,
      JsonPatch patch,
      ObjectMapper mapper
  ) {

    List<ForestClientLocationEntity> addedLocation = new ArrayList<>();

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(patch, "add", getPrefix(), mapper);

    filteredNode.forEach(node ->
        addedLocation.add(
            PatchUtils
                .loadAddValue(
                    node,
                    ForestClientLocationEntity.class,
                    mapper
                )
                .withClientNumber(clientNumber)
                .withCreatedAt(LocalDateTime.now())
                .withUpdatedAt(LocalDateTime.now())
                .withUpdatedByUnit(70L)
                .withCreatedByUnit(70L)
                .withRevision(1L)
        )
    );

    return addedLocation;
  }

  private Flux<ForestClientLocationEntity> updatePatchedLocation(
      String clientNumber,
      JsonPatch patch,
      ObjectMapper mapper) {

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    Stream<String> locationIds = PatchUtils
        .loadIds(filteredNode)
        .stream()
        .distinct();

    return
        Flux
            .fromStream(locationIds)
            .flatMap(locationCode ->
                locationRepository
                    .findByClientNumberAndClientLocnCode(clientNumber, locationCode)
            )
            .flatMap(location ->
                Mono
                    .just(PatchUtils.patchClient(
                            PatchUtils.filterPatchOperations(
                                filteredNode,
                                location.getClientLocnCode(),
                                getRestrictedPaths(),
                                mapper
                            ),
                            location,
                            ForestClientLocationEntity.class,
                            mapper
                        )
                    )
                    .filter(client -> !location.equals(client))
                    .doOnNext(
                        client -> log.info("Detected Forest Client Location changes {}", client))
            );
  }
}
