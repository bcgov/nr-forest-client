package ca.bc.gov.app.controller.ches;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.service.client.ClientService;
import reactor.core.publisher.Mono;

public class ChesController {
  
  private ClientService clientService;
  
  @PostMapping("/email")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
    return clientService.sendEmail(emailRequestDto);
  }
  
}
