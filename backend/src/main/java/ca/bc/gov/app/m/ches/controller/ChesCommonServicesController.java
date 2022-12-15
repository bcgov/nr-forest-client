package ca.bc.gov.app.m.ches.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ca.bc.gov.app.m.ches.service.ChesCommonServicesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Ches - Common Services")
@CrossOrigin(origins = "${frontend.url}")
@RestController
@RequestMapping("/app/m/ches")
@RequiredArgsConstructor
public class ChesCommonServicesController {

  private final ChesCommonServicesService chesEmailService;

  @PostMapping(value = "/sendEmail",
      produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> sendEmail(@RequestParam("emailTo") String emailTo,
                                          @RequestParam("emailBody") String emailBody) {
    return chesEmailService.sendEmail(emailTo, emailBody);
  }

}
