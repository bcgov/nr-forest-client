package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.dto.ForestClientContactDetailsDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(9)
public class PatchOperationContactAddService implements ClientPatchOperation {

  private final R2dbcEntityOperations entityTemplate;

  @Override
  public String getPrefix() {
    return "contacts";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of();
  }

  @Override
  public Mono<Void> applyPatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {

    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "add",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    return
        Flux.fromStream(StreamSupport.stream(filteredNodeOps.spliterator(), false))
            .filter(node -> !node.get("path").asText().contains("locationCodes"))
            .map(node -> PatchUtils.loadAddValue(node, ForestClientContactDetailsDto.class, mapper))
            .flatMap(dto ->
                Flux.fromIterable(dto.locationCodes())
                    .map(locationCode ->
                        new ForestClientContactEntity(
                            null,
                            dto.clientNumber(),
                            locationCode,
                            dto.contactTypeCode(),
                            dto.contactName().toUpperCase(Locale.ROOT),
                            dto.businessPhone(),
                            dto.secondaryPhone(),
                            dto.faxNumber(),
                            dto.emailAddress(),
                            LocalDateTime.now(),
                            userId,
                            LocalDateTime.now(),
                            userId,
                            70L,
                            70L,
                            1L
                        )
                    )
                    .flatMap(entity ->
                        getNextContactId()
                            .map(entity::withClientContactId)
                    )
                    .flatMap(entity ->
                        entityTemplate
                            .insert(ForestClientContactEntity.class)
                            .using(entity)
                    )
            )
            .then();
  }

  private Mono<Long> getNextContactId() {
    return entityTemplate
        .getDatabaseClient()
        .sql("SELECT THE.client_contact_seq.NEXTVAL FROM dual")
        .fetch()
        .first()
        .map(row -> row.get("NEXTVAL"))
        .map(value -> Long.valueOf(value.toString()));
  }
}
