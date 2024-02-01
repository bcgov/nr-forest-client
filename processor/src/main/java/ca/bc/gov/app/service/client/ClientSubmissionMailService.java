package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.EmailRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionMailService {

  private final WebClient forestClientApi;

  public Mono<String> sendMail(EmailRequestDto mailMessage) {

    log.info("Sending email to {} {} -> {}",
        mailMessage.email(),
        mailMessage.subject(),
        mailMessage.variables()
    );

    return
        forestClientApi
            .post()
            .uri("/ches/email")
            .body(Mono.just(mailMessage), EmailRequestDto.class)
            .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
            .doOnNext(source -> log.info("Email sent to {} {}", mailMessage.email(),source))
            .doOnError(throwable -> log.error("Error sending email to {}", mailMessage.email(),
                throwable));

  }

}
