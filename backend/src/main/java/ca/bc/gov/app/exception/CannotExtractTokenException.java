package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class CannotExtractTokenException extends ResponseStatusException {

  private static final long serialVersionUID = 4552220932718117887L;

  public CannotExtractTokenException() {
    super(HttpStatus.PRECONDITION_FAILED, "Cannot retrieve a token");
  }
  
}
