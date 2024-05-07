package ca.bc.gov.app.validator.client;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

public interface ReactiveValidator extends Validator {

  default Mono<Errors> validateReactive(Object target, Errors errors) {
    return Mono.just(errors);
  }

}
