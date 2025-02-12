package ca.bc.gov.app.service.partial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ClientPatchOperationService {

  String getPrefix();

  List<String> getRestrictedPaths();

  Mono<Void> applyPatch(String clientNumber, JsonPatch patch, ObjectMapper mapper);
}
