package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {


  public static <S, T, V extends Validator> Mono<S> validateReactive(
      S target,
      Class<T> contentClass,
      V validator
  ) {
    Errors errors = new BeanPropertyBindingResult(target, contentClass.getName());
    validator.validate(target, errors);
    if (errors.hasErrors()) {
      return Mono.error(new ValidationException(getValidationErrors(errors)));
    }
    return Mono.just(target);
  }

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

  private static List<ValidationError> getValidationErrors(Errors errors) {
    return errors
        .getFieldErrors()
        .stream()
        .map(fieldError ->
            new ValidationError()
                .withFieldId(fieldError.getField())
                .withErrorMsg(fieldError.getCode())
        )
        .collect(Collectors.toList());
  }
}
