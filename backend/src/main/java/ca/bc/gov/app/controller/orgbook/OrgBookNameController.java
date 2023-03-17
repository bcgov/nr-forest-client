package ca.bc.gov.app.controller.orgbook;

import ca.bc.gov.app.dto.client.ClientNameCodeDto;
import ca.bc.gov.app.dto.orgbook.OrgBookTopicListResponse;
import ca.bc.gov.app.service.orgbook.OrgBookApiService;
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
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(
    name = "OrgBook API",
    description = "A route for BC OrgBook data access and consumption"
)
@RequestMapping(value = "/api/orgbook", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class OrgBookNameController {

  private final OrgBookApiService service;

  @GetMapping(value = "/name/{name}")
  @Operation(
      summary = "Search the OrgBook based on the name",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - Data was found based on the provided name",
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
  public Flux<ClientNameCodeDto> findByClientName(
      @Parameter(
          description = "The name to lookup",
          example = "Power Corp"
      )
      @PathVariable String name) {
    return service
        .findByClientName(name);
  }

  @GetMapping(value = "/incorporation/{incorporationId}")
  @Operation(
      summary = "Search the OrgBook based on the incorporation id",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK - Data was found based on the provided incorporation",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(
                      name = "TopicListResponse",
                      implementation = OrgBookTopicListResponse.class
                  )
              )
          )
      }
  )
  public Mono<OrgBookTopicListResponse> findByIncorporationNumber(
      @Parameter(
          description = "The incorporation ID to lookup",
          example = "BC0772006"
      )
      @PathVariable String incorporationId) {
    return service
        .findByIncorporationNumber(incorporationId);
  }
}
