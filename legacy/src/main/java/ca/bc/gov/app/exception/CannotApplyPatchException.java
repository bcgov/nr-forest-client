package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;


@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CannotApplyPatchException extends ResponseStatusException {
  public CannotApplyPatchException(String errorMessage, Throwable e) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage,e);
  }
}