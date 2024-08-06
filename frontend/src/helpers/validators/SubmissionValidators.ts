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
const revalidateBus = useEventBus<void>("revalidate-bus");

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

interface ErrorGroup {
  result: boolean;
  fields: Record<string, boolean>;
}

const errorGroups: Record<string, ErrorGroup> = {};

/**
 * Register a listener for submission errors on the error bus.
 * When an error is received, update the submission validators array.
 */
errorBus.on((errors, payload) => {
  submissionValidators = errors.map((error: ValidationMessageType) => {
    if (!payload.skipNotification) {
      notificationBus.emit(error);
    }
    const { groupId, warning } = payload;
    return { ...error, originalValue: "", groupId, warning };
  });
  revalidateBus.emit();
});

/**
 * Update the submission validators array with the provided fieldId and value.
 * If a validator with the same fieldId is found, update its originalValue property.
 * @param fieldId - The fieldId of the validator to update.
 * @param originalValue - The new value for the validator's originalValue property.
 */
const updateValidators = (
  fieldId: string,
  originalValue: string,
  groupId?: string,
  warning?: boolean,
): void => {
  submissionValidators = submissionValidators.map(
    (validator: ValidationMessageType) => {
      if (validator.fieldId === fieldId) {
        return { ...validator, originalValue, groupId, warning };
      }
      return validator;
    }
  );
};

const updateGroup = (groupId: string, fieldId: string, error: boolean) => {
  if (!errorGroups[groupId]) {
    errorGroups[groupId] = {
      fields: {},
      result: true,
    };
  }
  errorGroups[groupId].fields[fieldId] = error;
  const result = Object.values(errorGroups[groupId].fields).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
  );
  errorGroups[groupId].result = result;
  return result;
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
      (validator: ValidationMessageType) => validator.fieldId === fieldName,
    );
    if (foundError) {
      if (foundError.groupId) {
        const oldResult = errorGroups[foundError.groupId]
          ? errorGroups[foundError.groupId].result
          : undefined;
        const result = updateGroup(
          foundError.groupId,
          fieldName,
          foundError.originalValue === value || foundError.originalValue === "",
        );
        if (oldResult !== undefined && result !== oldResult) {
          revalidateBus.emit();
        }
      }
      if (
        (foundError.originalValue === value &&
          (!foundError.groupId || errorGroups[foundError.groupId].result)) ||
        foundError.originalValue === ""
      ) {
        updateValidators(fieldName, value, foundError.groupId, foundError.warning);
        if (foundError.warning) {
          return foundError;
        }
        return foundError.errorMsg;
      }
    }
    return "";
  };
};
