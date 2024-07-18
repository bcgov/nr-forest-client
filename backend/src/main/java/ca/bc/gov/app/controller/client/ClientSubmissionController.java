package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientListSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.dto.submissions.SubmissionApproveRejectDto;
import ca.bc.gov.app.dto.submissions.SubmissionDetailsDto;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import ca.bc.gov.app.validator.SubmissionValidatorService;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/clients/submissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Observed
@RequiredArgsConstructor
public class ClientSubmissionController {

  private final ClientSubmissionService clientService;
  private final SubmissionValidatorService validator;


  @GetMapping
  public Flux<ClientListSubmissionDto> listSubmissions(

      @RequestParam(required = false, defaultValue = "0")
      int page,
      @RequestParam(required = false, defaultValue = "10")
      int size,
      @RequestParam(required = false)
      SubmissionStatusEnum[] requestStatus,
      @RequestParam(required = false)
      String[] clientType,
      @RequestParam(required = false)
      String[] district,
      @RequestParam(required = false)
      String[] name,
      @RequestParam(required = false)
      String[] submittedAt,
      ServerHttpResponse serverResponse
  ) {
    log.info(
        "Listing submissions: page={}, size={}, requestType={}, requestStatus={}, clientType={}, name={}, submittedAt={}",
        page, size, requestStatus, clientType, district, name, submittedAt);

    return clientService
        .listSubmissions(
            page,
            size,
            requestStatus,
            clientType,
            district,
            name,
            submittedAt
        )
        .doOnNext(dto -> serverResponse
            .getHeaders()
            .putIfAbsent(
                ApplicationConstant.X_TOTAL_COUNT,
                List.of(dto.count().toString())
            )
        )
        .doFinally(signalType -> serverResponse
            .getHeaders()
            .putIfAbsent(
                ApplicationConstant.X_TOTAL_COUNT,
                List.of("0")
            )
        );
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> submit(
      @RequestBody ClientSubmissionDto request,
      ServerHttpResponse serverResponse,
      JwtAuthenticationToken principal
  ) {

    return Mono.just(
            new ClientSubmissionDto(
                request.businessInformation(),
                request.location(),
                JwtPrincipalUtil.getUserId(principal).replaceFirst("^BCSC\\\\", ""),
                JwtPrincipalUtil.getLastName(principal)
            )
        )
        .switchIfEmpty(
            Mono.error(new InvalidRequestObjectException("no request body was provided"))
        )
        .doOnNext(sub -> log.info("Submitting request: {}",
            Optional
                .ofNullable(
                    sub.businessInformation()
                )
                .map(ClientBusinessInformationDto::businessName)
                .orElse("No Business Name")
        ))
        .flatMap(sub -> validator.validate(sub, ValidationSourceEnum.EXTERNAL))
        .doOnNext(sub -> log.info("External submission is valid: {}",
            sub.businessInformation().businessName()))
        .doOnError(e -> log.error("External submission is invalid: {}", e.getMessage()))
        .flatMap(submissionDto -> clientService.submit(submissionDto, principal))
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

  @PostMapping("/staff")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Void> submitStaff(
      @RequestBody ClientSubmissionDto request,
      ServerHttpResponse serverResponse,
      //TODO: uncomment here
      //JwtAuthenticationToken principal
      AbstractAuthenticationToken user
  ) {

    //TODO: remove here
    JwtAuthenticationToken principal = new JwtAuthenticationToken(
        new Jwt(
            "token",
            LocalDateTime.now().toInstant(ZoneOffset.UTC),
            LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.UTC),
            Map.of(
                "preferred_username", "staff@bcsc",
                "email", "staff@bcsc",
                "name", "Staff BCSC",
                "sub", "BCSC\\staff"
            ),
            Map.of(
                "sub", "561b8922-0269-4f6b-a0ba-23bc25b1fd60",
                "cognito:groups", List.of("CLIENT_EDITOR"),
                "custom:idp_name", "idir",
                "custom:idp_user_id", "C4F7EAD6855E42858A5AB9EF3529A20D",
                "custom:idp_username", "PCRUZ",
                "custom:idp_display_name", "Cruz, Paulo WLRS:EX",
                "email", "paulo.cruz@gov.bc.ca"
            )
        )
    );

    log.info("Staff {} submitting request for: {}",
        JwtPrincipalUtil.getUserId(principal),
        request.businessInformation().businessName()
    );

    return
        validator.validate(request, ValidationSourceEnum.STAFF)
            .doOnNext(sub -> log.info("Staff submission is valid: {}",
                sub.businessInformation().businessName()))
            .doOnError(e -> log.error("Staff submission is invalid: {}", e.getMessage()))
            .flatMap(submission -> clientService.staffSubmit(submission, principal))
            .doOnNext(clientId ->
                serverResponse
                    .getHeaders()
                    .addAll(
                        CollectionUtils
                            .toMultiValueMap(
                                Map.of(
                                    "Location",
                                    List.of(String.format("/api/clients/details/%s", clientId)),
                                    "x-client-id", List.of(String.valueOf(clientId))
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
      JwtAuthenticationToken principal,
      @RequestBody SubmissionApproveRejectDto request
  ) {
    log.info("Approving or rejecting submission with id: {} {}", id, request.approved());
    return clientService
        .approveOrReject(
            id,
            JwtPrincipalUtil.getUserId(principal),
            JwtPrincipalUtil.getEmail(principal),
            JwtPrincipalUtil.getName(principal),
            request
        );
  }

}
