package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.service.ClientService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@Tag(
    name = "Forest Client",
    description = "Aggregation and other reports for forest clients"
)
@RequestMapping(value = "/api/client", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ForestClientController {

  private final ClientService clientService;

  public Flux<ForestClientDto> listClientData(
      @RequestParam(required = false) String clientNumber,
      @Parameter(description = "The one index page number, defaults to 0", example = "0")
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,

      @Parameter(description = "The amount of data to be returned per page, defaults to 10",
          example = "10")
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size
  ) {
    return clientService.listClientData(clientNumber, page, size);
  }

}
