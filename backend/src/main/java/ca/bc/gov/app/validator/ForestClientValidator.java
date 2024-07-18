package ca.bc.gov.app.validator;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import reactor.core.publisher.Mono;

public interface ForestClientValidator<T> {

  /**
   * Returns if this validation can be used for the given source.
   *
   * @param source The source to check.
   * @return True if this validation can be used for the given source, false otherwise.
   */
  boolean supports(ValidationSourceEnum source);

  /**
   * Validates the given target.
   *
   * @param target The target to validate.
   * @param index The index of the target, in case of a list of objects.
   * @return A Mono that emits a ValidationError if the target is invalid, or completes if the target is valid.
   */
  Mono<ValidationError> validate(T target,Integer index);

}
