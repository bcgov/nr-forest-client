package ca.bc.gov.app.util;

import java.util.Optional;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;

public class LastItemReleaseStrategy implements ReleaseStrategy {

  @Override
  public boolean canRelease(MessageGroup messageGroup) {
    int messageCount = messageGroup.size();
    int totalMessages = messageGroup
        .getMessages()
        .stream()
        .map(Message::getHeaders)
        .filter(headers -> headers.containsKey("total"))
        .map(headers ->
            Optional
                .ofNullable(headers.get("total"))
                .map(Object::toString)
                .map(Integer::parseInt)
                .orElse(0)
        )
        .findFirst()
        .orElse(0);

    return messageCount == totalMessages; // Release the group if all messages have been received
  }
}

