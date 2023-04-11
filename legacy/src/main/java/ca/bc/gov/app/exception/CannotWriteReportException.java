package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CannotWriteReportException extends ResponseStatusException {
  public CannotWriteReportException(String errorMessage) {
    super(HttpStatus.UNPROCESSABLE_ENTITY,errorMessage);
  }
}
