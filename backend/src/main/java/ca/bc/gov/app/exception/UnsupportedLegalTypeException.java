package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class UnsupportedLegalTypeException extends ResponseStatusException  {
  public UnsupportedLegalTypeException(String legalType) {
    super(HttpStatus.NOT_ACCEPTABLE, "Unsupported Legal Type: " + legalType);
  }
}

