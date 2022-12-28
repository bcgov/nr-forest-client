package ca.bc.gov.app.handlers;

import ca.bc.gov.app.exception.InvalidRequestObjectException;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Slf4j
public class AbstractHandler<T, V extends Validator> {

  protected final Class<T> contentClass;
  private final V validator;


  protected <T> void validate(T target) {
    Errors errors = new BeanPropertyBindingResult(target, contentClass.getName());
    validator.validate(target, errors);
    if (errors.hasErrors()) {
      throw new InvalidRequestObjectException(getErrorMessages().apply(errors));
    }
  }

  private static Function<Errors, String> getErrorMessages() {
    return errors ->
        errors
            .getAllErrors()
            .stream()
            .map(FieldError.class::cast)
            .map(DefaultMessageSourceResolvable::getCode)
            .reduce(StringUtils.EMPTY,
                (message1, message2) -> String.join(",", message1, message2));
  }
}
