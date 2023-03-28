package ca.bc.gov.app.exception;

import ca.bc.gov.app.dto.ValidationError;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends ResponseStatusException {
  private final List<ValidationError> errors;

  public ValidationException(List<ValidationError> errors) {
    super(HttpStatus.BAD_REQUEST, "Validation failed");
    this.errors = errors;
  }
}
