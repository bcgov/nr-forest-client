package ca.bc.gov.app.service.openmaps;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.openmaps.OpenMapsResponseDto;
import ca.bc.gov.app.dto.openmaps.PropertyDto;
import ca.bc.gov.app.exception.NoFirstNationException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OpenMapsService {

  private final WebClient openMapsApi;

  public OpenMapsService(@Qualifier("openMapsApi") WebClient openMapsApi) {
    this.openMapsApi = openMapsApi;
  }

  public Mono<PropertyDto> getFirstNation(String firstNationId) {

    return
        openMapsApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .queryParam("service", "WFS")
                    .queryParam("version", "2.0.0")
                    .queryParam("request", "GetFeature")
                    .queryParam("typeName",
                        "WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP")
                    .queryParam("count", "10000")
                    .queryParam("CQL_FILTER",
                        "FIRST_NATION_FEDERAL_ID=" + firstNationId)
                    .queryParam("outputFormat", "json")
                    .build(new HashMap<>())
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono(clientResponse -> clientResponse.bodyToMono(OpenMapsResponseDto.class))
            .filter(not(OpenMapsResponseDto::empty))
            .map(OpenMapsResponseDto::features)
            .map(feature -> feature.get(0).properties())
            .switchIfEmpty(Mono.error(new NoFirstNationException(firstNationId)));

  }

}
