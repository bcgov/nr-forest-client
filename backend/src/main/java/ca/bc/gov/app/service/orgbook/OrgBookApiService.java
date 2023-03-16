package ca.bc.gov.app.service.orgbook;


import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.dto.orgbook.OrgBookResultListResponse;
import ca.bc.gov.app.dto.orgbook.OrgBookTopicListResponse;
import ca.bc.gov.app.util.CoreUtil;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrgBookApiService {

  private final WebClient orgBookApi;

  public OrgBookApiService(@Qualifier("orgBookApi") WebClient orgBookApi) {
    this.orgBookApi = orgBookApi;
  }

  public Flux<ClientNameCodeDto> findByClientName(String clientName) {
    log.info("Looking up on OrgBook for {}", clientName);
    return
        orgBookApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/v3/search/autocomplete")
                    .queryParam("q", CoreUtil.encodeString(clientName))
                    .queryParam("inactive", "false")
                    .queryParam("revoked", "false")
                    .build(new HashMap<>())
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono(
                clientResponse -> clientResponse.bodyToMono(OrgBookResultListResponse.class)
            )
            .flatMapMany(
                orgBookResultListResponse -> Flux.fromIterable(orgBookResultListResponse.results()))
            .filter(orgBookNameDto -> orgBookNameDto.subType().equalsIgnoreCase("entity_name"))
            .map(orgBookNameDto -> new ClientNameCodeDto(orgBookNameDto.topicSourceId(),
                orgBookNameDto.value()))
            .doOnNext(content -> log.info("OrgBook Name Lookup {} -> {}", clientName, content));

  }

  public Mono<OrgBookTopicListResponse> findByIncorporationNumber(
      String incorporationNumber) {
    return
        orgBookApi
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/search/topic")
                .queryParam("format", "json")
                .queryParam("inactive", "any")
                .queryParam("ordering", "-score")
                .queryParam("q", incorporationNumber)
                .build(new HashMap<>())
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono(
                clientResponse -> clientResponse.bodyToMono(OrgBookTopicListResponse.class))
            .doOnNext(
                content -> log.info("OrgBook Incorporation Lookup {} -> {}", incorporationNumber,
                    content));

  }

}
