package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class NoAssociationException extends ResponseStatusException {

  public NoAssociationException(String from, String to, String value) {
    super(HttpStatus.PRECONDITION_FAILED,
        String.format("No association between %s and %s found for value %s", from, to, value));
  }
}
