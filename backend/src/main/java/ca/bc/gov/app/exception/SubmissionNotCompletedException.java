package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
public class SubmissionNotCompletedException extends ResponseStatusException {

  public SubmissionNotCompletedException(Integer submissionId) {
    super(
        HttpStatus.REQUEST_TIMEOUT,
        String.format("Submission %d is still being processed", submissionId)
    );
  }
}
