package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidAccessTokenException extends ResponseStatusException {

  private static final long serialVersionUID = -1534366921974396495L;

  public InvalidAccessTokenException() {
    super(HttpStatus.UNAUTHORIZED, "Provided access token is missing or invalid");
  }
  
}
