package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class MissingRequiredParameterException extends ResponseStatusException {
  public MissingRequiredParameterException(String parameterName) {
    super(HttpStatus.EXPECTATION_FAILED,
        String.format("Missing value for parameter %s", parameterName));
  }
}
