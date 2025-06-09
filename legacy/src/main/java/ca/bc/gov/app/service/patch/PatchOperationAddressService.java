package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.dto.FieldReasonDto;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(5)
public class PatchOperationAddressService implements ClientPatchOperation {

  public static final String LOCATION_REASON_UPDATE = """
      UPDATE CLI_LOCN_AUDIT
      SET
      	CLIENT_UPDATE_REASON_CODE = :reasonCode
      WHERE
      	CLIENT_UPDATE_REASON_CODE = 'UND'
      	AND CLIENT_NUMBER = :clientNumber
      	AND CLIENT_LOCN_CODE = :locationCode
      	AND CLIENT_AUDIT_CODE = 'UPD'""";

  private final R2dbcEntityOperations entityTemplate;

  @Override
  public String getPrefix() {
    return "addresses";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of();
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonNode patch, ObjectMapper mapper,
      String userId) {
    if (!PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      return Mono.empty();
    }
    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "add",
        "reasons",
        mapper
    );

    return
        Flux
            .fromIterable(filteredNode)
            .map(op -> PatchUtils.loadAddValue(op, FieldReasonDto.class, mapper))
            .doOnNext(
                op -> log.info("Client {} updated field {} due to {}", clientNumber, op.field(),
                    op.reason()))
            .flatMap(opValue -> updateReasonCode(clientNumber, opValue))
            .collectList()
            .then();
  }

  private Mono<Void> updateReasonCode(
      String clientNumber,
      FieldReasonDto reasonCode
  ) {

    String locationCode = reasonCode.field().replace("/addresses/", StringUtils.EMPTY);

    return
        entityTemplate
            .getDatabaseClient()
            .sql(LOCATION_REASON_UPDATE)
            .bind("reasonCode", reasonCode.reason())
            .bind("clientNumber", clientNumber)
            .bind("locationCode", locationCode)
            .fetch()
            .one()
            .doOnNext(
                reason -> log.info("Reason code applied to {} {} as {}",
                    clientNumber, locationCode, reasonCode)
            )
            .then();
  }

}
