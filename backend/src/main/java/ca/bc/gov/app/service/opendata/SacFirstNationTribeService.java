package ca.bc.gov.app.service.opendata;

import ca.bc.gov.app.dto.opendata.OpenData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SacFirstNationTribeService extends AbstractOpenDataService {

  public Mono<OpenData> getFirstNationData(String nationName) {
    return getFeature(nationName)
        .doOnNext(openData -> log.info("SAC: {}", openData));
  }

  private final WebClient webClient;

  public SacFirstNationTribeService(
      @Qualifier("openDataSacTribeApi") WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  WebClient getWebClient() {
    return webClient;
  }

  @Override
  String getTypeName() {
    return "Donnees_Ouvertes-Open_Data_Conseil_Tribal_Tribal_Council:Conseil_tribal___Tribal_Council";
  }

  @Override
  String getVersion() {
    return "2.0.0";
  }

  @Override
  String getOutputFormat() {
    return "GEOJSON";
  }

  @Override
  String getSearchField() {
    return "Donnees_Ouvertes-Open_Data_Conseil_Tribal_Tribal_Council:Nom_du_conseil_tribal___Tribal_Council_Name";
  }
}
