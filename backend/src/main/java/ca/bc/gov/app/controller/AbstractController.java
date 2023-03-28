package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Slf4j
public class AbstractController<T, V extends Validator> {

  protected final Class<T> contentClass;
  private final V validator;


  protected <S> void validate(S target) {
    Errors errors = new BeanPropertyBindingResult(target, contentClass.getName());
    validator.validate(target, errors);
    if (errors.hasErrors()) {
      throw new ValidationException(getValidationErrors(errors));
    }
  }

  private List<ValidationError> getValidationErrors(Errors errors) {
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
