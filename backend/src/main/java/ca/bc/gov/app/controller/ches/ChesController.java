package ca.bc.gov.app.controller.ches;

import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.service.client.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller for handling email sending via CHES (Common Hosted Email Service).
 * This controller provides endpoints for sending emails using the CHES service.
 */
@RestController
@Tag(
    name = "CHES",
    description = "The CHES endpoint is responsible for handling the sending of emails"
)
@RequestMapping(value = "/api/ches", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ChesController {
  
  private ClientService clientService;
  
  /**
   * Endpoint for sending an email using CHES.
   *
   * @param emailRequestDto The DTO containing the email details.
   * @return A Mono representing the result of the email sending process.
   */
  @PostMapping("/email")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
    return clientService.sendEmail(emailRequestDto);
  }
  
}
