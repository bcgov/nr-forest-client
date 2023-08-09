package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.controller.AbstractController;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import ca.bc.gov.app.validator.client.ClientSubmitRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(
    name = "FSA Clients",
    description = "The FSA Client endpoint, responsible for handling client data"
)
@RestController
@RequestMapping(value = "/api/clients/submissions", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientSubmissionController extends
    AbstractController<ClientSubmissionDto, ClientSubmitRequestValidator> {

  private final ClientSubmissionService clientService;

  public ClientSubmissionController(
      ClientSubmissionService clientService, ClientSubmitRequestValidator validator) {
    super(ClientSubmissionDto.class, validator);
    this.clientService = clientService;
  }

  @GetMapping
  public Flux<ClientListSubmissionDto> listSubmissions(

      @RequestParam(required = false, defaultValue = "0")
      int page,
      @RequestParam(required = false, defaultValue = "10")
      int size,
      @RequestParam(required = false,defaultValue = "RNC,AAC")
      String[] requestType,
      @RequestParam(required = false)
      SubmissionStatusEnum[] requestStatus,
      @RequestParam(required = false)
      String[] clientType,
      @RequestParam(required = false)
      String[] name,
      @RequestParam(required = false)
      String[] updatedAt
  ) {
    return clientService
        .listSubmissions(
            page,
            size,
            requestType,
            requestStatus,
            clientType,
            name,
            updatedAt
        );
  }

  @PostMapping
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
                  ),
                  @Header(
                      name = ApplicationConstant.USERID_HEADER,
                      description = "The ID of the submitter who is making the submission",
                      schema = @Schema(
                          implementation = String.class,
                          example = "1234"
                      )
                  ),
                  @Header(
                      name = ApplicationConstant.USERMAIL_HEADER,
                      description = "The email address of the submitter who is making the submission",
                      schema = @Schema(
                          implementation = String.class,
                          example = "joe.doe@gov.bc.ca"
                      )
                  ),
                  @Header(
                      name = ApplicationConstant.USERNAME_HEADER,
                      description = "The name of the submitter who is making the submission",
                      schema = @Schema(
                          implementation = String.class,
                          example = "Joe Doe"
                      )
                  )
              }
          )
      }
  )
  
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> submit(
      @RequestBody ClientSubmissionDto request,
      @RequestHeader(ApplicationConstant.USERID_HEADER) String userId,
      @RequestHeader(ApplicationConstant.USERMAIL_HEADER) String userEmail,
      @RequestHeader(ApplicationConstant.USERNAME_HEADER) String userName,
      ServerHttpResponse serverResponse) {
    return Mono.just(request)
        .switchIfEmpty(
            Mono.error(new InvalidRequestObjectException("no request body was provided"))
        )
        .doOnNext(this::validate)
        .flatMap(submissionDto -> clientService.submit(submissionDto, userId, userEmail, userName))
        .doOnNext(submissionId ->
            serverResponse
                .getHeaders()
                .addAll(
                    CollectionUtils
                        .toMultiValueMap(
                            Map.of(
                                "Location",
                                List.of(String.format("/api/clients/submissions/%d", submissionId)),
                                "x-sub-id", List.of(String.valueOf(submissionId))
                            )
                        )
                )
        )
        .then();
  }
  
}
