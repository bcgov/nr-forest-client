package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.dto.FirstNationBandVidationDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.service.ForestClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

  private final ForestClientService service;

  @GetMapping("/bands")
  @Operation(
      summary = "List First nation band validation information",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns a client based on it's number",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "FirstNationBand",
                          implementation = FirstNationBandVidationDto.class
                      )
                  )
              )
          )
      }
  )
  public Flux<FirstNationBandVidationDto> getFirstNationBandInfo() {
    return service.getFirstNationBandInfo();
  }


  @GetMapping("/business")
  @Operation(
      summary = "List all clients we are doing business with",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns a client based on it's number",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "ClientInformation",
                          implementation = ClientPublicViewDto.class
                      )
                  )
              )
          )
      }
  )
  public Flux<ClientPublicViewDto> getClientDoingBusiness() {
    return service.getClientDoingBusiness();
  }

  @GetMapping("/unregistered")
  @Operation(
      summary = "List all clients that are unregistered",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns a client based on it's number",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "ClientInformation",
                          implementation = ClientPublicViewDto.class
                      )
                  )
              )
          )
      }
  )
  public Flux<ClientPublicViewDto> getUnregisteredCompanies() {
    return service.getUnregisteredCompanies();
  }
}
