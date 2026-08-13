package ca.bc.gov.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when an attempt is made to delete a client contact that is still
 * referenced by another system (e.g. EMS, GAS2, LEXIS, or SCS).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ContactInUseException extends ResponseStatusException {

  /**
   * Creates the exception with an HTTP 409 (Conflict) status and a message explaining that the
   * contact must be removed from the other system before it can be deleted here.
   */
  public ContactInUseException() {
    super(
        HttpStatus.CONFLICT,
        "You can't delete this contact yet because it's being used by EMS, GAS2, LEXIS, or SCS. "
            + "Remove it from the other system first, then try again."
    );
  }

}
