package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.service.client.ClientService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/districts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class DistrictController {

  private final ClientService clientService;


  @GetMapping
  public Flux<CodeNameDto> getActiveDistrictCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of districts from the client service.");
    return clientService.getActiveDistrictCodes(page, size);
  }

  @GetMapping("/{districtCode}")
  public Mono<DistrictDto> getDistrictByCode(@PathVariable String districtCode) {
    log.info("Requesting a district by code {} from the client service.", districtCode);
    return clientService.getDistrictByCode(districtCode);
  }

}
