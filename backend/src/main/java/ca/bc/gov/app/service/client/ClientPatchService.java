package ca.bc.gov.app.service.client;

import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientPatchService {

  private final ClientLegacyService legacyService;

  public Mono<Void> patchClient(String clientNumber, JsonPatch forestClient) {
    log.info("Sending request to the legacy system to patch client {}", clientNumber);
    return legacyService.patchClient(clientNumber, forestClient);
  }
}
