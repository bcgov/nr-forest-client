package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoClientDataFound extends ResponseStatusException {
  public NoClientDataFound(String clientNumber) {
    super(HttpStatus.NOT_FOUND,
        String.format("No data found for client number %s", clientNumber));
  }
}
