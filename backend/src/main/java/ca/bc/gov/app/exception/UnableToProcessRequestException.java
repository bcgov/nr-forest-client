package ca.bc.gov.app.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnableToProcessRequestException extends ResponseStatusException {

  public UnableToProcessRequestException(String message) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message);
  }

  public UnableToProcessRequestException(List<String> messages) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, String.join(",", messages));
  }
  
}
