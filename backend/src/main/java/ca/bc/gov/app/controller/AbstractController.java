package ca.bc.gov.app.controller;

import ca.bc.gov.app.util.ValidationUtil;
import ca.bc.gov.app.validator.client.ReactiveValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AbstractController<T, V extends ReactiveValidator> {

  protected final Class<T> contentClass;
  private final V validator;

  protected <S> void validate(S target) {
    ValidationUtil.validate(target, contentClass, validator);
  }
  
  protected <S> Mono<S> validateReactive(S target) {
    return ValidationUtil.validateReactive(target, contentClass, validator);
  }

}
