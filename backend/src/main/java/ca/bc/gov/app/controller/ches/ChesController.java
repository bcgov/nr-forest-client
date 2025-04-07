package ca.bc.gov.app.controller.ches;

import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.service.client.ClientService;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller for handling email sending via CHES (Common Hosted Email Service). This controller
 * provides endpoints for sending emails using the CHES service.
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/ches", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ChesController {

  private final ClientService clientService;

  /**
   * Endpoint for sending an email using CHES.
   *
   * @param emailRequestDto The DTO containing the email details.
   * @return A Mono representing the result of the email sending process.
   */
  @PostMapping("/email")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<String> sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
    log.info("Sending email using CHES {}", emailRequestDto);
    return clientService.sendEmail(emailRequestDto);
  }

  /**
   * Handles HTTP POST requests to send an email notification about a duplicated client.
   *
   * <p>This endpoint accepts an {@link EmailRequestDto} in the request body and sends an email
   * to the specified recipients. The request is authenticated using a 
   * {@link JwtAuthenticationToken}, from which the user ID and business ID are extracted.
   *
   * @param emailRequestDto the email request data, including recipient information
   * @param principal the authentication token containing user and business details
   * @return a {@link Mono} that completes when the email trigger process is initiated
   */
  @PostMapping("/duplicate")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> sendEmail(
      @RequestBody EmailRequestDto emailRequestDto,
      JwtAuthenticationToken principal
  ) {
    log.info("Sending email to {} from the client service.", emailRequestDto.emailsCsv());
    return clientService
        .triggerEmailDuplicatedClient(
            emailRequestDto,
            JwtPrincipalUtil.getUserId(principal),
            JwtPrincipalUtil.getBusinessId(principal)
        );
  }

}
