package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ClientAlreadyExistException extends ResponseStatusException {
  public ClientAlreadyExistException(
      String clientNumber,
      String clientCode,
      String clientName
  ) {
    super(
        HttpStatus.CONFLICT,
        String.format(
            "Client %s with the Incorporation number %s already exists with client number %s",
            clientName,
            clientCode,
            clientNumber
        )
    );
  }
}
