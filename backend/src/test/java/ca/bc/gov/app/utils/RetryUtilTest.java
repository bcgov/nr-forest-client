package ca.bc.gov.app.utils;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.exception.SubmissionNotCompletedException;
import ca.bc.gov.app.util.RetryUtil;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.SynchronousSink;

@DisplayName("Unit Tests | RetryUtil")
class RetryUtilTest {

  @Test
  @DisplayName("handleRetry passes client number downstream when client number is not blank")
  void handleRetryPassesClientNumberWhenNotBlank() {
    Integer submissionId = 1;
    SubmissionDetailEntity submissionDetail = mock(SubmissionDetailEntity.class);
    when(submissionDetail.getClientNumber()).thenReturn("12345");

    SynchronousSink<Object> sink = mock(SynchronousSink.class);

    BiConsumer<SubmissionDetailEntity, SynchronousSink<Object>> retryHandler = RetryUtil.handleRetry(
        submissionId);

    retryHandler.accept(submissionDetail, sink);

    verify(sink).next("12345");
    verifyNoMoreInteractions(sink);
  }

  @Test
  @DisplayName("handleRetry emits SubmissionNotCompletedException when client number is blank")
  void handleRetryEmitsExceptionWhenClientNumberIsBlank() {
    Integer submissionId = 2;
    SubmissionDetailEntity submissionDetail = mock(SubmissionDetailEntity.class);
    when(submissionDetail.getClientNumber()).thenReturn("");

    SynchronousSink<Object> sink = mock(SynchronousSink.class);

    BiConsumer<SubmissionDetailEntity, SynchronousSink<Object>> retryHandler = RetryUtil.handleRetry(
        submissionId);

    retryHandler.accept(submissionDetail, sink);

    verify(sink).error(any(SubmissionNotCompletedException.class));
    verifyNoMoreInteractions(sink);
  }

  @Test
  @DisplayName("handleRetry emits SubmissionNotCompletedException when client number is null")
  void handleRetryEmitsExceptionWhenClientNumberIsNull() {
    Integer submissionId = 3;
    SubmissionDetailEntity submissionDetail = mock(SubmissionDetailEntity.class);
    when(submissionDetail.getClientNumber()).thenReturn(null);

    SynchronousSink<Object> sink = mock(SynchronousSink.class);

    BiConsumer<SubmissionDetailEntity, SynchronousSink<Object>> retryHandler = RetryUtil.handleRetry(
        submissionId);

    retryHandler.accept(submissionDetail, sink);

    verify(sink).error(any(SubmissionNotCompletedException.class));
    verifyNoMoreInteractions(sink);
  }
}