package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoValueFoundException extends ResponseStatusException {
  public NoValueFoundException(String errorMessage) {
    super(HttpStatus.NOT_FOUND, "No value found for " + errorMessage);
  }
}
