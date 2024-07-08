package ca.bc.gov.app.controller.opendata;


import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.service.opendata.OpenDataService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequestMapping(value = "/api/opendata", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class OpenDataController {

  private final OpenDataService openDataService;

  @GetMapping("/{nationName}")
  public Flux<ClientDetailsDto> getFirstNationData(
      @PathVariable String nationName) {
    log.info("Requesting first nation data for nationName: {}", nationName);
    return openDataService
        .getFirstNationData(nationName);
  }

}
