package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.exception.ValidationException;
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
  
  /**
   * Validates the submission limit for the authenticated user.
   * <p>
   * This endpoint checks whether the authenticated user has exceeded their allowed number of submissions
   * within a specified time frame. The time frame and submission limits are configurable parameters.
   * 
   * @param principal the authentication token containing the details of the currently authenticated user.
   *        This token is used to extract the user ID and determine the submission limits applicable to them.
   * @return a {@link Mono<Void>} that completes when the validation process is finished. If the user has
   *         exceeded their submission limit, an error will be emitted; otherwise, the Mono will complete
   *         successfully.
   * 
   * @throws ValidationException if the user exceeds the maximum number of submissions allowed within
   *         the specified time frame.
   */
  @GetMapping
  public Mono<Void> validateSubmissionLimit(
      JwtAuthenticationToken principal
  ) {
    return submissionValidatorService.validateSubmissionLimit(principal);
  }
}
