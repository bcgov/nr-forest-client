package ca.bc.gov.app.service.patch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientPatchService {

  private final ObjectMapper mapper;
  private final List<ClientPatchOperation> partialServices;

  public Mono<Void> patchClient(String clientNumber, JsonPatch forestClient) {
    log.info("Patching client with client number {} if any changes are detected", clientNumber);

    return
        partialServices
            .stream()
            .map(service -> service.applyPatch(clientNumber, forestClient, mapper))
            .reduce(Mono.empty(), Mono::then);
  }
}
