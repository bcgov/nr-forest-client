package ca.bc.gov.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
public class SubmissionNotCompletedException extends ResponseStatusException {

  private final Integer submissionId;

  public SubmissionNotCompletedException(Integer submissionId) {
    super(
        HttpStatus.REQUEST_TIMEOUT,
        String.format("Submission %d is still being processed", submissionId)
    );
    this.submissionId = submissionId;
  }
}
