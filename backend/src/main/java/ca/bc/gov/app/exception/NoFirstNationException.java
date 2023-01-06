package ca.bc.gov.app.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoFirstNationException extends ResponseStatusException {
  public NoFirstNationException(String firstNationId) {
    super(HttpStatus.NOT_FOUND,
        String.format("No first nation found with federal id %s", firstNationId));
  }
}
