package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.legacy.ForestClientDto;
import io.micrometer.observation.annotation.Observed;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@Observed
public class ClientLegacyService {

  private final WebClient legacyApi;

  public ClientLegacyService(@Qualifier("legacyApi") WebClient legacyApi) {
    this.legacyApi = legacyApi;
  }

  /**
   * Searches for a list of {@link ForestClientDto} in the legacy API based on
   * the given incorporation number and company name.
   *
   * @param incorporationNumber the incorporation number to search for
   * @param companyName         the company name to search for
   * @return a Flux of ForestClientDto objects matching the search criteria
   */
  public Flux<ForestClientDto> searchLegacy(String incorporationNumber, String companyName) {
    return
        legacyApi
            .get()
            .uri(builder ->
                builder
                    .path("/search/incorporationOrName")
                    .queryParamIfPresent("incorporationNumber", Optional.ofNullable(incorporationNumber))
                    .queryParamIfPresent("companyName", Optional.ofNullable(companyName))
                    .build(Map.of())
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .doOnNext(
                dto -> log.info("Loading Legacy data for {} {} from legacy with client number {}",
                    incorporationNumber, companyName, dto.clientNumber()));
  }
}
