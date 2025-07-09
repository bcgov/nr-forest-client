package ca.bc.gov.app.service;

import ca.bc.gov.app.entity.ClientRelatedProjection;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
@Slf4j
@Observed
public class ClientRelatedService {

  private final ForestClientRepository clientRepository;

  public Flux<ClientRelatedProjection> getRelatedClientList(String clientNumber) {
    log.info("Fetching related clients for client number: {}", clientNumber);

    return clientRepository
        .findByClientRelatedList(clientNumber)
        .doOnNext(relatedClientNumber ->
            log.info("Found related client number: {}", relatedClientNumber)
        );
  }

}
