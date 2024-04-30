package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.validator.client.ReactiveValidator;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {

  public static <S, T, V extends Validator> void validate(
      S target,
      Class<T> contentClass,
      V validator
  ) {
    Errors errors = new BeanPropertyBindingResult(target, contentClass.getName());
    validator.validate(target, errors);
    if (errors.hasErrors()) {
      throw new ValidationException(getValidationErrors(errors));
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <S, T, V extends ReactiveValidator> Mono<S> validateReactive(
      S target,
      Class<T> contentClass,
      V validator
  ) {
    return
        validator
            .validateReactive(target, new BeanPropertyBindingResult(target, contentClass.getName()))
            .filter(Errors::hasErrors)
            .flatMap(result -> Mono.error(new ValidationException(getValidationErrors(result))))
            .defaultIfEmpty(target)
            .map(s -> (S) s);
  }

  private static List<ValidationError> getValidationErrors(Errors errors) {
    return errors
        .getFieldErrors()
        .stream()
        .map(fieldError ->
            new ValidationError(fieldError.getField(), fieldError.getCode())
        )
        .toList();
  }

}
