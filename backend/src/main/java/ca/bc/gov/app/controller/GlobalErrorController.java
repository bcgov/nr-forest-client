package ca.bc.gov.app.controller;

import ca.bc.gov.app.exception.ValidationException;
import java.util.Arrays;
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
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The GlobalErrorController class extends the AbstractErrorWebExceptionHandler class.
 * It is annotated with @RestControllerAdvice, @ControllerAdvice, @Slf4j, @Component, and @Order.
 *
 * @RestControllerAdvice is a convenience annotation that is itself annotated with @ControllerAdvice and @ResponseBody.
 * This annotation is used to assist with exception handling in a @Controller class.
 *
 * @ControllerAdvice is an annotation provided by Spring allowing you to handle exceptions across the whole application, not just an individual controller.
 * You can think of it as an interceptor of exceptions thrown by methods annotated with @RequestMapping and similar.
 *
 * @Slf4j is a simple facade for logging systems allowing the end-user to plug in the desired logging system at deployment time.
 *
 * @Component is an annotation that allows Spring to automatically detect our custom beans.
 *
 * @Order is used to sort the components that Spring should load in the ApplicationContext.
 *
 * The GlobalErrorController class is responsible for handling and routing all the exceptions that occur within the application.
 */
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
 * This method is responsible for routing all requests to the error response rendering method.
 * It overrides the getRoutingFunction method from the AbstractErrorWebExceptionHandler class.
 * The method takes ErrorAttributes as a parameter, which are used to get the error associated with a request.
 * It returns a RouterFunction that routes all requests to the renderErrorResponse method.
 *
 * @param errorAttributes The ErrorAttributes associated with the request.
 * @return A RouterFunction that routes all requests to the renderErrorResponse method.
 */
@Override
protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
  return RouterFunctions.route(
      RequestPredicates.all(), request -> renderErrorResponse(request, errorAttributes));
}

  /**
 * This method is responsible for handling and rendering error responses.
 * It takes a ServerRequest and ErrorAttributes as parameters.
 * The method first retrieves the error associated with the request and logs it.
 * If the error is an instance of ValidationException, it logs the validation errors and returns a
 * response with the status code, reason, and errors from the exception.
 * If the error is not a ValidationException, it checks if it's a ResponseStatusException.
 * If it is, it retrieves the reason and status code from the exception.
 * If the error message is blank, it sets it to an empty string.
 * Finally, it logs the error status and message, and returns a response with the status code
 * and error message.
 *
 * @param request The ServerRequest that caused the error.
 * @param errorAttributes The ErrorAttributes associated with the request.
 * @return A Mono<ServerResponse> that represents the error response.
 */
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

  // If the error is a ValidationException, log the validation errors and return a response with the status code, reason, and errors from the exception
  if (exception instanceof ValidationException validationException) {
    log.error("Failed Validations: {}",
        Arrays.toString(validationException.getErrors().toArray()));
    return ServerResponse.status(validationException.getStatusCode())
        .header("Reason", validationException.getReason())
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(validationException.getErrors()));
  }

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

  /**
 * This method is used to handle CSRF (Cross-Site Request Forgery) tokens in the application.
 * It is annotated with @ModelAttribute, which means its executed before any request mapping methods.
 * The method returns a Mono<Void> as the cookie will be set asynchronously behind the scene and this
 * is here only to subscribe the requester to receive the cookie.
 *
 * Another important thing here is that this method will always set the csrf token on the stream.
 *
 * @param exchange The ServerWebExchange interface provides access to information about the request and response.
 * @return Mono<Void> If the CSRF token is not present in the ServerWebExchange, it returns an empty Mono.
 *                   If the CSRF token is present, it adds the token to the ServerWebExchange attributes and then returns a Mono<Void> indicating completion.
 */
@ModelAttribute
Mono<Void> csrfCookie(ServerWebExchange exchange) {
  // Retrieve the CSRF token from the ServerWebExchange
  Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());

  // If the CSRF token is not present, return an empty Mono
  if (csrfToken == null) {
    return Mono.empty();
  }

  // If the CSRF token is present, add it to the ServerWebExchange attributes
  // The token is added under the key CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME
  // After adding the token, a Mono<Void> indicating completion is returned
  return csrfToken
      .doOnSuccess(token -> exchange
          .getAttributes()
          .put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token))
      .then();
}
}
