package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.repository.ClientUpdateReasonRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Observed
@Order(4)
public class PatchOperationStatusService extends PatchOperationReasonService {


  public PatchOperationStatusService(
      @Autowired ForestClientRepository clientRepository,
      @Autowired ClientUpdateReasonRepository clientUpdateReasonRepository
  ) {
    super(clientRepository, clientUpdateReasonRepository);
  }

  /**
   * Returns the prefix associated with this patch operation.
   * <p>
   * Patches with this prefix are processed by this service. In this case, only patches related to
   * "client" fields are handled.
   * </p>
   *
   * @return The string prefix "client".
   */
  @Override
  public String getPrefix() {
    return "client";
  }

  /**
   * Returns a list of paths that are restricted from modification.
   * <p>
   * These fields are protected and cannot be updated through JSON Patch operations. Attempts to
   * modify them will be filtered out before applying the patch.
   * </p>
   *
   * @return A list of restricted JSON Patch paths.
   */
  @Override
  public List<String> getRestrictedPaths() {
    return List.of("/clientStatusCode");
  }

  @Override
  String getFieldName() {
    return "/client/clientStatusCode";
  }

  @Override
  String getReasonId() {
    return "STATUS";
  }

  @Override
  String getReasonType() {
    return null;
  }

}
