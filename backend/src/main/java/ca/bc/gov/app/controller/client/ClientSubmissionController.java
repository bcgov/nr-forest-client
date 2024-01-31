package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.controller.AbstractController;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.submissions.SubmissionApproveRejectDto;
import ca.bc.gov.app.dto.submissions.SubmissionDetailsDto;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import ca.bc.gov.app.validator.client.ClientSubmitRequestValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/clients/submissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Observed
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
      @RequestParam(required = false)
      String[] requestType,
      @RequestParam(required = false)
      SubmissionStatusEnum[] requestStatus,
      @RequestParam(required = false)
      String[] clientType,
      @RequestParam(required = false)
      String[] name,
      @RequestParam(required = false)
      String[] updatedAt,
      ServerWebExchange exchange
  ) {
    log.info(
        "Listing submissions: page={}, size={}, requestType={}, requestStatus={}, clientType={}, name={}, updatedAt={}",
        page, size, requestType, requestStatus, clientType, name, updatedAt);
    
    return clientService
        .listSubmissions(
            page,
            size,
            requestType,
            requestStatus,
            clientType,
            name,
            updatedAt
        )
        .doOnNext(dto -> exchange.getResponse()
            .getHeaders()
            .putIfAbsent(
                ApplicationConstant.X_TOTAL_COUNT,
                List.of(dto.count().toString())
            )
        )
        .doFinally(signalType -> {
            HttpHeaders headers = new HttpHeaders();
            headers.put(
                    ApplicationConstant.X_TOTAL_COUNT,
                    List.of(String.valueOf(0))
            );
            exchange.getResponse().getHeaders().addAll(headers);
          }
        );
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> submit(
      @RequestBody ClientSubmissionDto request,
      @RequestHeader(ApplicationConstant.USERID_HEADER) String userId,
      @RequestHeader(name = ApplicationConstant.BUSINESSID_HEADER, defaultValue = StringUtils.EMPTY) String businessId,
      @RequestHeader(ApplicationConstant.USERMAIL_HEADER) String userEmail,
      @RequestHeader(ApplicationConstant.USERNAME_HEADER) String userName,
      ServerHttpResponse serverResponse) {
    return Mono.just(request)
        .switchIfEmpty(
            Mono.error(new InvalidRequestObjectException("no request body was provided"))
        )
        .doOnNext(sub -> log.info("Submitting request: {}", sub))
        .doOnNext(this::validate)
        .doOnNext(sub -> log.info("Request is valid: {}", sub))
        .doOnError(e -> log.error("Request is invalid: {}", e.getMessage()))
        .flatMap(submissionDto -> clientService.submit(
            submissionDto,
            userId,
            userEmail,
            userName,
            businessId))
        .doOnNext(submissionId -> log.info("Submission persisted: {}", submissionId))
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
  public Mono<SubmissionDetailsDto> getSubmissionDetail(
      @PathVariable Long id,
      ServerHttpResponse serverResponse
  ) {
    log.info("Requesting submission detail for id: {}", id);
    return clientService
        .getSubmissionDetail(id)
        .switchIfEmpty(Mono.error(new NoClientDataFound(String.valueOf(id))));
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
    log.info("Approving or rejecting submission with id: {} {}", id, request.approved());
    return clientService.approveOrReject(id, userId, userEmail, userName, request);
  }

}
