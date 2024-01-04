package ca.bc.gov.app.service.legacy;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.matchers.ProcessorMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legacy Service")
class LegacyLoadingServiceTest {

  private final LegacyLoadingService service = new LegacyLoadingService(List.of(new TestProcessorMatcher()));

  @Test
  @DisplayName("Forward it")
  void shouldJustForwardMessage() {
    List<MatcherResult> matchers = new ArrayList<>();
    Message<List<MatcherResult>> message = MessageBuilder
        .withPayload(matchers)
        .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
        .build();

    assertEquals(message, service.approved(message));
  }

  @ParameterizedTest
  @MethodSource("matchCheck")
  @DisplayName("Check and Mat(ch)e")
  void shouldCheckAndMatch(SubmissionInformationDto input, String responseChannel) {
    service
        .matchCheck(
            MessageBuilder
                .withPayload(input)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message ->
            assertThat(message)
                .isNotNull()
                .isInstanceOf(Message.class)
                .hasFieldOrProperty("payload")
                .hasFieldOrProperty("headers")
                .extracting(Message::getHeaders, as(InstanceOfAssertFactories.MAP))
                .isNotNull()
                .isNotEmpty()
                .containsKey("id")
                .containsKey("timestamp")
                .containsEntry(MessageHeaders.REPLY_CHANNEL, responseChannel)
                .containsEntry(ApplicationConstant.SUBMISSION_ID, 1)
        )
        .verifyComplete();
  }

  private static Stream<Arguments> matchCheck() {
    return
        Stream.of(
            Arguments.of(
                TestConstants.SUBMISSION_INFORMATION,
                ApplicationConstant.AUTO_APPROVE_CHANNEL
            ),
            Arguments.of(
                TestConstants.SUBMISSION_INFORMATION.withGoodStanding("N"),
                ApplicationConstant.REVIEW_CHANNEL
            )
        );
  }

  private static class TestProcessorMatcher implements ProcessorMatcher{

    @Override
    public boolean enabled(SubmissionInformationDto submission) {
      return true;
    }

    @Override
    public String name() {
      return "Test matcher";
    }

    @Override
    public Mono<MatcherResult> matches(SubmissionInformationDto submission) {
      return Mono
          .just(submission.goodStanding())
          .filter(value -> value.equalsIgnoreCase("N"))
          .map(value -> new MatcherResult("test",value));
    }
  }


}