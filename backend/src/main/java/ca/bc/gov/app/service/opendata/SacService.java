package ca.bc.gov.app.service.opendata;

import ca.bc.gov.app.dto.opendata.OpenData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SacService extends AbstractOpenDataService {

  public Mono<OpenData> getFirstNationData(String nationName) {
    return getFeature(nationName)
        .doOnNext(openData -> log.info("SAC: {}", openData));
  }

  private final WebClient webClient;

  public SacService(
      @Qualifier("openDataSacApi") WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  WebClient getWebClient() {
    return webClient;
  }

  @Override
  String getTypeName() {
    return "Donnees_Ouvertes-Open_Data_Premiere_Nation_First_Nation:Première_Nation___First_Nation";
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
    return "Donnees_Ouvertes-Open_Data_Premiere_Nation_First_Nation:Nom_de_bande___Band_Name";
  }
}
