package ca.bc.gov.app.controller.ches;

import ca.bc.gov.app.controller.AbstractController;
import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.service.ches.ChesCommonServicesService;
import ca.bc.gov.app.validator.ches.ChesRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
    name = "Ches API",
    description = "A route for common services consumption"
)
@RequestMapping(value = "/api/mail", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChesController extends AbstractController<ChesRequest, ChesRequestValidator> {

  private final ChesCommonServicesService service;

  public ChesController(ChesCommonServicesService service, ChesRequestValidator validator) {
    super(ChesRequest.class, validator);
    this.service = service;
  }

  @PostMapping
  @Operation(
      summary = "Send an contactEmail to one or more contactEmail addresses",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "Mail was sent",
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
                          example = "/api/mail/00000000-0000-0000-0000-000000000000"
                      )
                  )
              }
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Something went wrong, a required parameter wasn't being sent",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = String.class),
                  examples = {@ExampleObject(value = "Destination contactEmail is required")}
              )
          )
      }
  )
  public Mono<Void> sendMail(
      @RequestBody ChesRequest request,
      ServerHttpResponse serverResponse) {
    return Mono.just(request)
        .switchIfEmpty(
            Mono.error(new InvalidRequestObjectException("no request body was provided"))
        )
        .doOnNext(this::validate)
        .doOnNext(
            requestBody -> log.info("Requesting an contactEmail to be sent {}", requestBody))
        .flatMap(service::sendEmail)
        .doOnNext(companyId -> {
              serverResponse
                  .setStatusCode(HttpStatus.CREATED);
              serverResponse
                  .getHeaders()
                  .add("Location", String.format("/api/mail/%s", companyId));
            }
        )
        .then();
  }
}
