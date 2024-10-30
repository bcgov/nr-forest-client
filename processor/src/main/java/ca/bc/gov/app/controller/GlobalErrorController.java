package ca.bc.gov.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@ControllerAdvice
@Slf4j
@Component
@Order(-2)
public class GlobalErrorController extends AbstractErrorWebExceptionHandler {

  public GlobalErrorController(
      ErrorAttributes errorAttributes,
      WebProperties webProperties,
      ApplicationContext applicationContext,
      ServerCodecConfigurer configurer) {
    super(errorAttributes, webProperties.getResources(), applicationContext);
    this.setMessageWriters(configurer.getWriters());
  }

  /**
   * This method is responsible for routing all requests to the error response rendering method. It
   * overrides the getRoutingFunction method from the AbstractErrorWebExceptionHandler class. The
   * method takes ErrorAttributes as a parameter, which are used to get the error associated with a
   * request. It returns a RouterFunction that routes all requests to the renderErrorResponse
   * method.
   *
   * @param errorAttributes The ErrorAttributes associated with the request.
   * @return A RouterFunction that routes all requests to the renderErrorResponse method.
   */
  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(
        RequestPredicates.all(), request -> renderErrorResponse(request, errorAttributes));
  }

  private Mono<ServerResponse> renderErrorResponse(
      ServerRequest request, ErrorAttributes errorAttributes) {

    // Get the error associated with the request and fill in its stack trace
    Throwable exception = errorAttributes.getError(request).fillInStackTrace();

    // Log the error
    log.error(
        "An error was generated during request for {} {}",
        request.method(),
        request.requestPath(),
        exception);

    // Get the error message
    String errorMessage = exception.getMessage();
    // Set the default error status to INTERNAL_SERVER_ERROR
    HttpStatusCode errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    // If the error is a ResponseStatusException, get the reason and status code from the exception
    if (exception instanceof ResponseStatusException responseStatusException) {
      errorMessage = responseStatusException.getReason();
      errorStatus = responseStatusException.getStatusCode();
    }

    // If the error message is blank, set it to an empty string
    errorMessage =
        BooleanUtils.toString(StringUtils.isBlank(errorMessage), StringUtils.EMPTY, errorMessage);

    // Log the error status and message
    log.error("{} - {}", errorStatus, errorMessage);

    // Return a response with the status code and error message
    return ServerResponse.status(errorStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(errorMessage));
  }

}
