package ca.bc.gov.app.util;

import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HandlerUtils {


  public static Consumer<ResponseStatusException> handleStatusResponse() {
    return t -> ServerResponse.status(t.getStatusCode())
        .body(BodyInserters.fromValue(t.getReason()));
  }

  public static Consumer<Throwable> handleError() {
    return throwable ->
        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BodyInserters.fromValue(throwable.getMessage()));
  }
}
