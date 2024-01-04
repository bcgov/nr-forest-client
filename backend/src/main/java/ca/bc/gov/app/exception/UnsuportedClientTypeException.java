package ca.bc.gov.app.exception;

import ca.bc.gov.app.dto.legacy.ClientTypeCodeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class UnsuportedClientTypeException extends ResponseStatusException {

  public UnsuportedClientTypeException(String clientType) {
    super(HttpStatus.NOT_ACCEPTABLE,
        String.format("Client type %s is not supported at the moment",
            ClientTypeCodeEnum.as(clientType)));
  }

}