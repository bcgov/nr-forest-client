package ca.bc.gov.app.service.opendata;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.opendata.OpenData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class AbstractOpenDataService {

  abstract WebClient getWebClient();
  abstract String getTypeName();
  abstract String getVersion();
  abstract String getOutputFormat();
  abstract String getSearchField();


  public Mono<OpenData> getFeature(String nationName) {

    if (StringUtils.isBlank(nationName))
      return Mono.empty();

    return getWebClient()
        .get()
        .uri(pathBuilder ->
            pathBuilder
                .queryParam("service", "WFS")
                .queryParam("request", "GetFeature")
                .queryParam("typeName",getTypeName())
                .queryParam("version", getVersion())
                .queryParam("outputFormat", getOutputFormat())
                .queryParam("filter",
                    String.format(ApplicationConstant.OPENDATA_FILTER,
                        getSearchField(),
                        nationName,

                        getSearchField(),
                        nationName.toLowerCase(),

                        getSearchField(),
                        nationName.toUpperCase(),

                        getSearchField(),
                        StringUtils.capitalize(nationName)
                    )
                )
                .build()
        )
        .exchangeToMono(response -> response.bodyToMono(OpenData.class))
        .filter(openData -> !openData.features().isEmpty())
        .doOnNext(openData -> log.info("OpenData: {}", openData));
  }

}
