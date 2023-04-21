package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MissingReportFileException extends ResponseStatusException {
  public MissingReportFileException(String reportId) {
    super(HttpStatus.NOT_FOUND, "No report found for ID " + reportId);
  }
}
