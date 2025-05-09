/**
 * Helper functions for submission validation.
 * These functions are used to validate a submission based on the submission validators array.
 * The submission validators array is updated when an error is received on the error bus.
 *
 *
 * For example, when a submission happens, in case of an error being received, it will emit an event to the error bus,
 * which will update the submission validators array.
 *
 * Then, when a field is updated, the submission validation function will check if the field has an error in the submission validators array.
 * If it does, it will return the error message.
 *
 * It will use an initial value as empty then will update it with the value of the field as initial value, so that the error message will be displayed only once.
 * @packageDocumentation
 * @module SubmissionValidators
 * @category Helper
 * @preferred
 *
 * @see {@link SubmissionValidators.spec.ts}
 */
import { useEventBus } from "@vueuse/core";
import type { ValidationMessageType } from "@/dto/CommonTypesDto";

/**
 * Event bus for submission error notifications.
 */
const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);

/**
 * Event bus for revalidating the submission.
 */
const revalidateBus = useEventBus<string[]|undefined>("revalidate-bus");

/**
 * Event bus for error notifications bar.
 */
const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);

/**
 * Array of submission validators.
 */
let submissionValidators: ValidationMessageType[] = [];

/**
 * Register a listener for submission errors on the error bus.
 * When an error is received, update the submission validators array.
 */
errorBus.on((errors, payload = {}) => {
  if (errors) {
    errors.forEach((error: ValidationMessageType) => {
      if (!payload.skipNotification) {
        notificationBus.emit(error);
      }
      const { warning } = payload;
      submissionValidators.push({ ...error, originalValue: "", warning });
    });
  }
  revalidateBus.emit();
});

/**
 * Update the submission validators array with the provided fieldId and value.
 * If a validator with the same fieldId is found, update its originalValue property.
 * @param fieldId - The fieldId of the validator to update.
 * @param value - The new value for the validator's originalValue property.
 */
const updateValidators = (fieldId: string, originalValue: string, warning?: boolean): void => {
  submissionValidators = submissionValidators.map((validator: ValidationMessageType) => {
    if (validator.fieldId === fieldId) {
      return { ...validator, originalValue, warning };
    }
    return validator;
  });
};

/**
 * Create a submission validation function for the specified fieldName.
 * This function validates a value based on the submission validators array.
 * @param fieldName - The name of the field to validate.
 * @returns A function that takes a value and returns an error (object or string) if applicable.
 */
export const submissionValidation = (
  fieldName: string,
): ((value: string) => string | ValidationMessageType) => {
  return (value: string) => {
    const foundError = submissionValidators.find(
      (validator: ValidationMessageType) => validator.fieldId === fieldName
    );
    if (foundError) {
      if (foundError && (foundError.originalValue === value || foundError.originalValue === "")) {
        updateValidators(fieldName, value, foundError.warning);
        if (foundError.warning || foundError.custom) {
          return foundError;
        }
        return foundError.errorMsg;
      }
    }
    return "";
  };
};

export const resetSubmissionValidators = (): void => {
  submissionValidators = [];
};
