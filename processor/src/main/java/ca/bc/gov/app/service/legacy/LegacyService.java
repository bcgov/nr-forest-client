package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import ca.bc.gov.app.service.processor.ProcessorMatcher;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyService {

  private final List<ProcessorMatcher> matchers;

  @ServiceActivator(
      inputChannel = "matchCheckingChannel",
      outputChannel = "goodStanceChannel",
      async = "true"
  )
  public Mono<Message<SubmissionInformationDto>> matchCheck(
      Message<SubmissionInformationDto> eventMono) {

    return
        Flux
            .fromIterable(matchers)
            .doOnNext(matcher -> log.info("Running {}", matcher.name()))
            .map(ProcessorMatcher::matches)
            .doOnError(x -> log.error("Oops we have a winner", x))
            .reduce(Function::andThen)
            .doOnError(x -> log.error("Reduced Winners are tight", x))
            .flatMap(allMatchers -> allMatchers.apply(Mono.just(eventMono.getPayload())))
            .doOnError(x -> log.error("This is awkward", x))
            .onErrorContinue((t, o) -> log.error("This is O {} and this is BAM", o, t))
            .map(event -> eventMono);
  }

  @ServiceActivator(inputChannel = "goodStanceChannel")
  public Message<SubmissionInformationDto> goodStanding(Message<SubmissionInformationDto> eventMono) {

    String replyChannelName = new Random().nextBoolean() ?
        "reviewChannel" :
        "autoApproveChannel";


    return /*Mono.just(eventMono)
        .doOnNext(x -> log.info("Checking good standing {}", x))
        .map(event ->*/
        MessageBuilder
            .fromMessage(eventMono)
            .setReplyChannelName(replyChannelName)
            .setHeader("output-channel", replyChannelName)
            .setHeader(MessageHeaders.REPLY_CHANNEL, replyChannelName)
            .build()
        /*)*/;
  }
}
