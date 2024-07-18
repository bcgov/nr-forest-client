package ca.bc.gov.app.util;

import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.exception.SubmissionNotCompletedException;
import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.SynchronousSink;

/**
 * Utility class providing static methods to handle retry logic in reactive streams.
 * This class is designed to assist with retrying operations based on specific conditions
 * encountered during stream processing, particularly in the context of handling submissions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RetryUtil {

  /**
   * Provides a retry handler for processing submission details. This takes a SubmissionDetailEntity
   * and in case of a blank client number, emits a SubmissionNotCompletedException that forces a retry.
   * In case of a non-blank client number, it passes the client number downstream.
   *
   * @param submissionId The ID of the submission being processed. This is used in the exception
   *                     message if the submission cannot be completed.
   * @return A BiConsumer that acts as a retry handler, either passing the client number downstream
   * or emitting an error.
   */
  public static BiConsumer<SubmissionDetailEntity, SynchronousSink<Object>> handleRetry(
      Integer submissionId
  ) {
    return (submissionDetail, sink) -> {
      if (StringUtils.isNotBlank(submissionDetail.getClientNumber())) {
        sink.next(submissionDetail.getClientNumber());
      } else {
        sink.error(new SubmissionNotCompletedException(submissionId));
      }
    };
  }

}
