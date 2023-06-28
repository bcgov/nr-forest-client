package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.service.client.ClientAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Slf4j
@Tag(
    name = "FSA Clients",
    description = "The FSA Client endpoint, responsible for handling client data"
)
@RequestMapping(value = "/api/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientAddressController {

  private final ClientAddressService clientAddressService;

  @GetMapping("/addresses")
  @Operation(
      summary = "Autocomplete addresses",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns a list of possible addresses",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "NameCode",
                          implementation = ClientNameCodeDto.class
                      )
                  )
              )
          )
      }
  )
  public Flux<ClientNameCodeDto> findPossibleAddresses(
      @Parameter(description =
          "The name or ISO 2 or 3 character code for the country to search in, defaults to CA",
          example = "UK")
      @RequestParam(value = "country", required = false, defaultValue = "CA")
      String country,

      @Parameter(description =
          "The maximum number of autocomplete suggestions to return, defaults to 7",
          example = "7")
      @RequestParam(value = "maxSuggestions", required = false, defaultValue = "7")
      Integer maxSuggestions,

      @Parameter(description = "The search term to find", example = "2701 ri")
      @RequestParam(value = "searchTerm", required = true)
      String searchTerm) {
    return clientAddressService
        .findPossibleAddresses(country, maxSuggestions, searchTerm);
  }

  @GetMapping("/addresses/{addressId}")
  @Operation(
      summary = "Retrieve address",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns an addresses",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "Address",
                          implementation = ClientAddressDto.class
                      )
                  )
              )
          )
      }
  )
  public Mono<ClientAddressDto> getAddress(
      @Parameter(
          description = """
              The id of the address to retrieve the details for.
               The id can be obtained through <b>/api/client/addresses<b> endpoint""",
          example = "CA|CP|B|0000001"
      )
      @PathVariable String addressId) {
    return clientAddressService
        .getAddress(addressId);
  }
}
