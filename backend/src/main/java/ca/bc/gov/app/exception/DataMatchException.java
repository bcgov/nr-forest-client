package ca.bc.gov.app.exception;

import ca.bc.gov.app.dto.client.MatchResult;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class DataMatchException extends ResponseStatusException {

  private final transient List<MatchResult> matches;

  public DataMatchException(List<MatchResult> matches) {
    super(HttpStatus.CONFLICT, "Match found on existing data.");
    this.matches = matches;
  }

}
