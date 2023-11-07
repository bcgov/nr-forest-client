package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.entity.client.SubmissionTypeCodeEnum;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionMailService {

  private final WebClient forestClientApi;

  @ServiceActivator(inputChannel = ApplicationConstant.SUBMISSION_MAIL_CHANNEL)
  public void sendMail(Message<EmailRequestDto> mailMessage) {

    if (
        Objects.equals(mailMessage.getHeaders().get(
            ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.class
        ), SubmissionTypeCodeEnum.RNC)
    ) {
      log.info("Receiving a review notification, mail not sent {} {} -> {}",
          mailMessage.getPayload().email(),
          mailMessage.getPayload().subject(),
          mailMessage.getPayload().variables()
      );
      return;
    }
    log.info("Sending email to {} {} -> {}",
        mailMessage.getPayload().email(),
        mailMessage.getPayload().subject(),
        mailMessage.getPayload().variables()
    );

    forestClientApi
        .post()
        .uri("/ches/email")
        .body(Mono.just(mailMessage.getPayload()), EmailRequestDto.class)
        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Void.class))
        .subscribe(source -> log.info("Email sent to {}", mailMessage.getPayload().email()));

  }

}
