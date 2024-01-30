package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class RequestAlreadyProcessedException extends ResponseStatusException {

  public RequestAlreadyProcessedException() {
    super(HttpStatus.CONFLICT,"This submission was already processed");
  }
  public RequestAlreadyProcessedException(String message) {
    super(HttpStatus.CONFLICT,
        String.format("This submission was already processed as %s", message));
  }

}
