package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.service.ClientSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@Tag(
    name = "Search Client",
    description = "Search clients based on some criterias"
)
@RequestMapping(value = "/api/search", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientSearchController {

  private final ClientSearchService service;

  @GetMapping("/incorporationOrName")
  @Operation(
      summary = "List forest client entries by incorporation number or name",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns a client based on it's number",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "ForestClient",
                          implementation = ForestClientDto.class
                      )
                  )
              )
          ),
          @ApiResponse(
              responseCode = "412",
              description = "Missing value for parameter incorporationNumber or companyName",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = String.class),
                  examples = {
                      @ExampleObject(
                          value =
                              "Missing value for parameter incorporationNumber or companyName")
                  }
              )
          )
      }
  )
  public Flux<ForestClientDto> findByIncorporationOrName(
      @Parameter(description = "The incorporation ID to lookup", example = "BC0772006")
      @RequestParam(required = false) String incorporationNumber,

      @Parameter(description = "The name to lookup", example = "Power Corp")
      @RequestParam(required = false) String companyName
  ) {
    return service
        .findByIncorporationOrName(incorporationNumber, companyName);
  }


}
