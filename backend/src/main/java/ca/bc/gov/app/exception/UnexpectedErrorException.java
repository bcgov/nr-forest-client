package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedErrorException extends ResponseStatusException {
  public UnexpectedErrorException(int statusCode, String message) {
    super(HttpStatus.valueOf(statusCode), message);
  }
}
