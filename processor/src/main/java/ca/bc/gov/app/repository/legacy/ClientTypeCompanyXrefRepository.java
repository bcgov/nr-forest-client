package ca.bc.gov.app.repository.legacy;

import ca.bc.gov.app.entity.legacy.ClientTypeCompanyXrefEntity;
import ca.bc.gov.app.entity.legacy.ClientTypeCompanyXrefEntityKey;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ClientTypeCompanyXrefRepository
    extends ReactiveCrudRepository<ClientTypeCompanyXrefEntity, ClientTypeCompanyXrefEntityKey> {

  Flux<ClientTypeCompanyXrefEntity> findByClientTypeCode(String clientTypeCode);

  Flux<ClientTypeCompanyXrefEntity> findByClientTypeCodeAndRegistryCompanyTypeCode(
      String clientTypeCode, String registryCompanyTypeCode);

}
