package ca.bc.gov.app.controller.client;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.bc.gov.app.validator.SubmissionValidatorService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/submission-limit", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientSubmissionLimitController {
  
  private final SubmissionValidatorService submissionValidatorService;
  
  @GetMapping
  public Mono<Void> validateSubmissionLimit(
      JwtAuthenticationToken principal
  ) {
    return submissionValidatorService.validateSubmissionLimit(principal);
  }
}
