package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.service.client.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
public class ClientController {

  private final ClientService clientService;

  @GetMapping("/{clientNumber}")
  @Operation(
      summary = "Get the details of a client",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Returns a client data based on it's number",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(
                      name = "ClientDetails",
                      implementation = ClientDetailsDto.class
                  )
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "No client found based on the provided ID",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = String.class),
                  examples = {
                      @ExampleObject(
                          value =
                              "No data found for client number 00000002")
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Provided access token is missing or invalid",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = String.class),
                  examples = {
                      @ExampleObject(
                          value =
                              "Provided access token is missing or invalid")
                  }
              )
          )
      }
  )
  public Mono<ClientDetailsDto> getClientDetails(
      @Parameter(
          description = "The client number to look for",
          example = "00000002"
      )
      @PathVariable String clientNumber
  ) {
    return clientService.getClientDetails(clientNumber);
  }

  @GetMapping("/activeCountryCodes")
  @Operation(
      summary = "List countries",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - List a page of countries with name and code",
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
  public Flux<ClientNameCodeDto> listCountries(
      @Parameter(description = "The one index page number, defaults to 0", example = "0")
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,

      @Parameter(description = "The amount of data to be returned per page, defaults to 10",
          example = "10")
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size,
      ServerHttpResponse serverResponse) {
    return clientService
        .listCountries(page, size);
  }

  @GetMapping("/activeCountryCodes/{countryCode}")
  @Operation(
      summary = "List provinces",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - List a page of provinces with name and code",
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
  public Flux<ClientNameCodeDto> listProvinces(
      @Parameter(
          description = """
              The code of the country.
               The code can be obtained through <b>/api/client/country<b> endpoint""",
          example = "CA"
      )
      @PathVariable String countryCode,

      @Parameter(description = "The one index page number, defaults to 0", example = "0")
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,

      @Parameter(description = "The amount of data to be returned per page, defaults to 10",
          example = "10")
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    return clientService
        .listProvinces(countryCode, page, size);
  }

  @GetMapping("/activeClientTypeCodes")
  @Operation(
      summary = "List active clients with their type codes",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - List a page of active clients with name and code",
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
  public Flux<ClientNameCodeDto> findActiveClientTypeCodes() {
    return clientService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  @GetMapping("/activeContactTypeCodes")
  @Operation(
      summary = "List contact type codes",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - List a page of contact type codes",
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
  public Flux<ClientNameCodeDto> listClientContactTypeCodes(
      @Parameter(description = "The one index page number, defaults to 0", example = "0")
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,

      @Parameter(description = "The amount of data to be returned per page, defaults to 10",
          example = "10")
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size
  ) {
    return clientService
        .listClientContactTypeCodes(page, size);
  }

  @GetMapping(value = "/name/{name}")
  @Operation(
      summary = "Search the Bc Registry based on the name",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - Data was found based on the provided name",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(
                      schema = @Schema(
                          name = "ClientLookup",
                          implementation = ClientLookUpDto.class
                      )
                  )
              )
          )
      }
  )
  public Flux<ClientLookUpDto> findByClientName(
      @Parameter(
          description = "The name to lookup",
          example = "Power Corp"
      )
      @PathVariable String name) {
    return clientService
        .findByClientNameOrIncorporation(name);
  }

  @GetMapping(value = "/incorporation/{incorporationId}")
  @Operation(
      summary = "Search the BcRegistry based on the incorporation id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - Data was found based on the provided incorporation",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(
                      name = "ClientLookup",
                      implementation = ClientLookUpDto.class
                  )
              )
          )
      }
  )
  public Mono<ClientLookUpDto> findByIncorporationNumber(
      @Parameter(
          description = "The incorporation ID to lookup",
          example = "BC0772006"
      )
      @PathVariable String incorporationId) {
    return clientService
        .findByClientNameOrIncorporation(incorporationId)
        .next()
        .switchIfEmpty(Mono.error(new NoClientDataFound(incorporationId)));
  }
}
