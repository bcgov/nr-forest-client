package ca.bc.gov.app.service.patch;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ClientPatchOperation {

  String getPrefix();

  List<String> getRestrictedPaths();

  Mono<Void> applyPatch(String clientNumber, Object patch, ObjectMapper mapper);
}
