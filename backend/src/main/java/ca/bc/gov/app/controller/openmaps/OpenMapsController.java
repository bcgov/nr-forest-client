package ca.bc.gov.app.controller.openmaps;

import ca.bc.gov.app.dto.openmaps.PropertyDto;
import ca.bc.gov.app.service.openmaps.OpenMapsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(
    name = "OpenMaps API",
    description = "Exposes OpenMaps consumption for First nation data extraction"
)
@RequestMapping(value = "/api/maps", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class OpenMapsController {

  private final OpenMapsService openMapsService;

  @GetMapping(value = "/firstNation/{id}")
  @Operation(
      summary = "Get First Nation data by ID",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - Data was found based on the provided id",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(
                      name = "FirstNationResponse",
                      implementation = PropertyDto.class
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "No first nation found with provided federal id",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = String.class),
                  examples = {@ExampleObject(value = "No first nation found with federal id 99999")}
              )
          )
      }
  )
  public Mono<PropertyDto> findFirstNation(
      @Parameter(
          description = "The first nation federal ID to lookup",
          example = "656"
      )
      @PathVariable String id) {
    return openMapsService
        .getFirstNation(id);
  }
}
