package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.service.processor.ProcessorMatcher;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
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
      inputChannel = ApplicationConstant.MATCH_CHECKING_CHANNEL,
      outputChannel = ApplicationConstant.FORWARD_CHANNEL,
      async = "true"
  )
  public Mono<Message<List<MatcherResult>>> matchCheck(
      Message<SubmissionInformationDto> eventMono
  ) {

    Function<Boolean, String> replier =
        value -> BooleanUtils.toString(
            value,
            ApplicationConstant.AUTO_APPROVE_CHANNEL,
            ApplicationConstant.REVIEW_CHANNEL
        );

    return
        validateSubmission(eventMono.getPayload())
            .map(matchList ->
                MessageBuilder
                    .withPayload(matchList)
                    .setReplyChannelName(replier.apply(matchList.isEmpty()))
                    .setHeader("output-channel", replier.apply(matchList.isEmpty()))
                    .setHeader(MessageHeaders.REPLY_CHANNEL, replier.apply(matchList.isEmpty()))
                    .setHeader("submission-id",eventMono.getHeaders().get("submission-id"))
                    .build()
            );
  }

  public Mono<List<MatcherResult>> validateSubmission(SubmissionInformationDto message) {
    return Flux
        .fromIterable(matchers)
        .doOnNext(matcher -> log.info("Running {}", matcher.name()))
        //If matcher returns empty, all good, if not, it is a problem
        .flatMap(matcher -> matcher.matches(message))
        .doOnNext(results -> log.info("Matched a result {}", results))
        .collectList();
  }

  @ServiceActivator(inputChannel = ApplicationConstant.FORWARD_CHANNEL)
  public Message<List<MatcherResult>> approved(Message<List<MatcherResult>> eventMono) {
    return eventMono;
  }

}
