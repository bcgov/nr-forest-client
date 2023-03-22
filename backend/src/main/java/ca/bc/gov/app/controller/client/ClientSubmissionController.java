package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.service.client.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(
    name = "FSA Clients",
    description = "The FSA Client endpoint, responsible for handling client data"
)
@RequestMapping(value = "/api/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientSubmissionController {

  private final ClientService clientService;

  @PostMapping("/submissions")
  @Operation(
      summary = "Submit client data",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "New client submission posted",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = String.class),
                  examples = {@ExampleObject(value = "Created")}
              ),
              headers = {
                  @Header(
                      name = "Location",
                      schema = @Schema(
                          implementation = String.class,
                          example = "/api/clients/submissions/000123"
                      )
                  ),
                  @Header(
                      name = "x-sub-id",
                      description = "ID of the submission that was created",
                      schema = @Schema(
                          implementation = String.class,
                          example = "000123"
                      )
                  )
              }
          )
      }
  )
  public Mono<Void> submit(
      @RequestBody ClientSubmissionDto request,
      ServerHttpResponse serverResponse) {
    return Mono.just(request)
        .switchIfEmpty(
            Mono.error(new InvalidRequestObjectException("no request body was provided"))
        )
        .flatMap(clientService::submit)
        .doOnNext(submissionId -> {
          serverResponse
              .setStatusCode(HttpStatus.CREATED);

          HttpHeaders headers = serverResponse.getHeaders();
          headers
              .add(
                  "Location",
                  String.format("/api/clients/submissions/%d", submissionId));
          headers.add(
              "x-sub-id", String.valueOf(submissionId));
        })
        .then();
  }
}
