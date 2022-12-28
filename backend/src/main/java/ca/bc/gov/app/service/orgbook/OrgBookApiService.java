package ca.bc.gov.app.service.orgbook;


import ca.bc.gov.app.configuration.OrgBookConfiguration;
import ca.bc.gov.app.dto.orgbook.OrgBookResultListResponse;
import ca.bc.gov.app.dto.orgbook.OrgBookTopicListResponse;
import ca.bc.gov.app.util.CoreUtil;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrgBookApiService {

  private final OrgBookConfiguration configuration;

  private WebClient orgBookApi;

  @PostConstruct
  public void setUp() {
    this.orgBookApi = WebClient
        .builder()
        .baseUrl(configuration.getUri().toString())
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  public Mono<OrgBookResultListResponse> findByClientName(String clientName) {

    return
        orgBookApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/v3/search/autocomplete")
                    .queryParam("q", CoreUtil.encodeString(clientName))
                    .build(new HashMap<>())
            )
            .exchangeToMono(
                clientResponse -> clientResponse.bodyToMono(OrgBookResultListResponse.class))
            .doOnNext(content -> log.info("OrgBook Name Lookup {} -> {}", clientName, content));

  }


  public Mono<OrgBookTopicListResponse> findByIncorporationNumber(
      String incorporationNumber) {

    return
        orgBookApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/v4/search/topic")
                    .queryParam("format", "json")
                    .queryParam("inactive", "any")
                    .queryParam("ordering", "-score")
                    .queryParam("q", incorporationNumber)
                    .build(new HashMap<>())
            )
            .exchangeToMono(
                clientResponse -> clientResponse.bodyToMono(OrgBookTopicListResponse.class))
            .doOnNext(
                content -> log.info("OrgBook Incorporation Lookup {} -> {}", incorporationNumber,
                    content));

  }

}
