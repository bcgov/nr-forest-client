package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.controller.AbstractController;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.submissions.SubmissionApproveRejectDto;
import ca.bc.gov.app.dto.submissions.SubmissionDetailsDto;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import ca.bc.gov.app.validator.client.ClientSubmitRequestValidator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "RNC,AAC,SPP") String[] requestType,
      @RequestParam(required = false) SubmissionStatusEnum[] requestStatus,
      @RequestParam(required = false) String[] clientType,
      @RequestParam(required = false) String[] name,
      @RequestParam(required = false) String[] updatedAt
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
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> submit(
      @RequestBody ClientSubmissionDto request,
      @RequestHeader(ApplicationConstant.USERID_HEADER) String userId,
      @RequestHeader(ApplicationConstant.USERMAIL_HEADER) String userEmail,
      @RequestHeader(ApplicationConstant.USERNAME_HEADER) String userName,
      ServerHttpResponse serverResponse
  ) {
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

  @GetMapping("/{id}")
  public Mono<SubmissionDetailsDto> getSubmissionDetail(@PathVariable Long id) {
    return clientService.getSubmissionDetail(id);
  }

  @PostMapping("/{id}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> approveOrReject(
      @PathVariable Long id,
      @RequestHeader(ApplicationConstant.USERID_HEADER) String userId,
      @RequestHeader(ApplicationConstant.USERMAIL_HEADER) String userEmail,
      @RequestHeader(ApplicationConstant.USERNAME_HEADER) String userName,
      @RequestBody SubmissionApproveRejectDto request
  ) {
    return clientService.approveOrReject(id, userId, userEmail, userName, request);
  }

}
