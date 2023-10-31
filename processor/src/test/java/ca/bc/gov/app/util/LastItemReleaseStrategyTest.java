package ca.bc.gov.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@DisplayName("Unit Test | Last Item Release Strategy Test Suite")
class LastItemReleaseStrategyTest {

  private final LastItemReleaseStrategy strategy = new LastItemReleaseStrategy();


  @ParameterizedTest(name = "canRelease() should return {3} when messageCount is {0} and totalMessages is {1} with header {2}")
  @MethodSource("canRelease")
  @DisplayName("can release strategy")
  void shouldReleaseWhenDone(int messageCount, Object totalMessages, boolean includeHeader,
      boolean expected) {
    assertEquals(
        expected,
        strategy.canRelease(createMessageGroup(messageCount, totalMessages, includeHeader))
    );
  }

  private static Stream<Arguments> canRelease() {
    return Stream.of(
        Arguments.of(1, 2, true, false),
        Arguments.of(1, 1, false, false),
        Arguments.of(1, "not-an-integer", true, false),
        Arguments.of(1, null, true, false),
        Arguments.of(1, -1, true, false),
        Arguments.of(1, 0, true, false),
        Arguments.of(5, 1, true, false),
        Arguments.of(5, 5, true, true)
    );
  }


  private MessageGroup createMessageGroup(int totalMessages, Object totalHeader,
      boolean includeTotalHeader) {

    return
        new TestMessageGroup(
            IntStream
                .range(1, totalMessages + 1)
                .mapToObj(value ->
                    MessageBuilder
                        .withPayload(value)
                        .setHeader(includeTotalHeader ? "total" : "not-total", totalHeader)
                        .build()
                )
                .toList()
        );
  }

  private static class TestMessageGroup implements MessageGroup {

    private final Collection<Message<?>> messages = new ArrayList<>();

    public TestMessageGroup(List<Message<Integer>> messages) {
      if (messages != null) {
        this.messages.addAll(messages);
      }
    }

    @Override
    public String getGroupId() {
      return "test-group";
    }

    @Override
    public int getLastReleasedMessageSequenceNumber() {
      return 0;
    }

    @Override
    public void setLastReleasedMessageSequenceNumber(int sequenceNumber) {

    }

    @Override
    public boolean isComplete() {
      return false;
    }

    @Override
    public void complete() {

    }

    @Override
    public int getSequenceSize() {
      return 0;
    }

    @Override
    public int size() {
      return messages.size();
    }

    @Override
    public Message<?> getOne() {
      return messages.isEmpty() ? null : messages.stream().findFirst().orElse(null);
    }

    @Override
    public long getTimestamp() {
      return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Override
    public long getLastModified() {
      return 0;
    }

    @Override
    public void setLastModified(long lastModified) {

    }

    @Override
    public void setCondition(String condition) {

    }

    @Override
    public String getCondition() {
      return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean canAdd(Message<?> message) {
      return false;
    }

    @Override
    public void add(Message<?> messageToAdd) {

    }

    @Override
    public boolean remove(Message<?> messageToRemove) {
      return false;
    }

    @Override
    public Collection<Message<?>> getMessages() {
      return messages;
    }


  }


}