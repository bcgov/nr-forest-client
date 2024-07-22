package ca.bc.gov.app.service.opendata;

import ca.bc.gov.app.dto.opendata.OpenData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class BcMapsService extends AbstractOpenDataService {

  private final WebClient webClient;

  public BcMapsService(
      @Qualifier("openDataBcMapsApi") WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  WebClient getWebClient() {
    return webClient;
  }

  @Override
  String getTypeName() {
    return "WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP";
  }

  @Override
  String getVersion() {
    return "1.0.0";
  }

  @Override
  String getOutputFormat() {
    return "JSON";
  }

  @Override
  String getSearchField() {
    return "pub:FIRST_NATION_BC_NAME";
  }

  public Mono<OpenData> getFirstNationData(String nationName) {
    return getFeature(nationName)
        .doOnNext(openData -> log.info("BC Maps: {}", openData));
  }
}
