package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
