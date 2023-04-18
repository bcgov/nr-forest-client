package ca.bc.gov.app.controller;

import ca.bc.gov.app.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Slf4j
public class AbstractController<T, V extends Validator> {

  protected final Class<T> contentClass;
  private final V validator;

  protected <S> void validate(S target) {
    ValidationUtil.validate(target,contentClass,validator);
  }


}
