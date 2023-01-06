package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidRoleException extends ResponseStatusException {
  public InvalidRoleException() {
    super(HttpStatus.FORBIDDEN, "You don't have the required role to perform this action");
  }
}
