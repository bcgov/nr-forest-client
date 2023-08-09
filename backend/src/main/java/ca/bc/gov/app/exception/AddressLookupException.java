package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AddressLookupException extends ResponseStatusException {

  private static final long serialVersionUID = -2992276338244727156L;

  public AddressLookupException(String error) {
    super(HttpStatus.BAD_REQUEST, error);
  }
  
}
