import type { Ref } from "vue";
import { useEventBus } from "@vueuse/core";
import type { ValidationMessageType } from "@/dto/CommonTypesDto";

// Defines the used regular expressions
// @sonar-ignore-next-line
const emailRegex: RegExp = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
const specialCharacters: RegExp = /^[a-zA-Z0-9\sÀ-ÖØ-öø-ÿ]+$/;
const e164Regex: RegExp = /^((\+?[1-9]\d{1,14})|(\(\d{3}\) \d{3}-\d{4}))$/;
const canadianPostalCodeRegex: RegExp = /^(([A-Z]\d){3})$/i;
const usZipCodeRegex: RegExp = /^\d{5}(?:[-\s]\d{4})?$/;

const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);

const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);

/**
 * Checks if the value is not empty
 * @param value - The value to check
 * @returns An empty string if the value is not empty, error message otherwise
 * @example
 * isNotEmpty('') // false
 * isNotEmpty(' ') // false
 * isNotEmpty('a') // true
 * isNotEmpty(' a ') // true
 **/
export const isNotEmpty =
  (message: string = "This field is required") =>
  (value: string): string => {
    if (value && value.trim().length > 0) return "";
    return message;
  };

/**
 * Checks if the value is an email
 * @param value - The value to check
 * @returns An empty string if the value is an email, error message otherwise
 * @example
 * isEmail('') // false
 * isEmail(' ') // false
 * isEmail('a') // false
 * isEmail('a@') // false
 * isEmail('a@b') // false
 * isEmail(' a@b ') // false
 * isEmail('mail@mail.com') // true
 **/
export const isEmail =
  (message: string = "This field must be a valid email") =>
  (value: string): string => {
    if (isNotEmpty(message)(value) === "" && emailRegex.test(value)) return "";
    return message;
  };

export const isPhoneNumber =
  (message: string = "This field must be a valid phone number") =>
  (value: string): string => {
    if (isNotEmpty(message)(value) === "" && e164Regex.test(value)) return "";
    return message;
  };

/**
 * Checks if the value is a canadian postal code
 * @param value - The value to check
 * @returns An empty string if the value is a canadian postal code, error message otherwise
 * @example
 * isCanadianPostalCode('') // false
 * isCanadianPostalCode(' ') // false
 * isCanadianPostalCode('a') // false
 * isCanadianPostalCode('a1') // false
 * isCanadianPostalCode('a1a') // false
 * isCanadianPostalCode('a1a1') // false
 * isCanadianPostalCode('a1a1a') // false
 * isCanadianPostalCode(' a1a1a ') // false
 * isCanadianPostalCode('a1a1a1') // true
 * isCanadianPostalCode('a1A1a1') // true
 * isCanadianPostalCode('A1A1A1') // true
 * isCanadianPostalCode('A1A 1A1') // false
 * isCanadianPostalCode('A1A-1A1') // false
 **/
export const isCanadianPostalCode = (
  value: string,
  message: string = "This field must be a valid Canadian postal code without spaces or dashes"
): string => {
  if (isNotEmpty(message)(value) === "" && canadianPostalCodeRegex.test(value))
    return "";
  return message;
};

/**
 * Checks if the value is a US zip code
 * @param value - The value to check
 * @returns True if the value is a US zip code, error message otherwise
 * @example
 * isUsZipCode('') // false
 * isUsZipCode(' ') // false
 * isUsZipCode('a') // false
 * isUsZipCode('a1') // false
 * isUsZipCode('a1a') // false
 * isUsZipCode('a1a1') // false
 * isUsZipCode('a1a1a') // false
 * isUsZipCode(' a1a1a ') // false
 * isUsZipCode('a1a1a1') // false
 * isUsZipCode("12345") // true
 * isUsZipCode("12345-6789") // true
 * isUsZipCode("123456") // false
 * isUsZipCode("ABCDE") // false
 **/
export const isUsZipCode = (
  value: string,
  message: string = "This field must be a valid US zip code"
): string => {
  if (isNotEmpty(message)(value) === "" && usZipCodeRegex.test(value)) {
    return "";
  }
  return message;
};

/**
 * Checks if the value is not empty and has a maximum size
 * @param maxSize - The maximum size
 * @returns A function that returns true if the value is not empty and has a maximum size, error message otherwise
 * @example
 * isMaxSize(5)('') // false
 * isMaxSize(5)(' ') // false
 * isMaxSize(5)('a') // true
 * isMaxSize(5)(' a ') // true
 * isMaxSize(5)('abcde') // true
 * isMaxSize(5)('abcdef') // false
 **/
export const isMaxSize =
  (message: string = "This field must be smaller") =>
  (maxSize: number) => {
    return (value: string): string => {
      if (isNotEmpty(message)(value) === "" && value.length <= maxSize)
        return "";
      return message;
    };
  };

/**
 * Checks if the value is not empty and has a minimum size
 * @param minSize - The minimum size
 * @returns A function that returns an empty string if the value is not empty and has a minimum size, error message otherwise
 * @example
 * isMinSize(5)('') // false
 * isMinSize(5)(' ') // false
 * isMinSize(5)('a') // false
 * isMinSize(5)(' a ') // false
 * isMinSize(5)('abcde') // true
 * isMinSize(5)('abcdef') // true
 **/
export const isMinSize =
  (message: string = "This field must bigger") =>
  (minSize: number) => {
    return (value: string): string => {
      if (isNotEmpty(message)(value) === "" && value.length >= minSize)
        return "";
      return message;
    };
  };

/**
 * Checks if the value is not empty and is composed of only numbers
 * @param value - The value to check
 * @returns An empty string if the value is not empty and is composed of only numbers, error message otherwise
 * @example
 * isOnlyNumbers('') // false
 * isOnlyNumbers(' ') // false
 * isOnlyNumbers('a') // false
 * isOnlyNumbers(' a ') // false
 * isOnlyNumbers('123') // true
 * isOnlyNumbers('123a') // false
 **/
export const isOnlyNumbers =
  (message: string = "This field must be composed of only numbers") =>
  (value: string): string => {
    if (isNotEmpty(message)(value) === "" && /^\d*$/.exec(value)) return "";
    return message;
  };

/**
 * Check if value is unique in the group of entries
 * @param key - The key of the group of entries
 * @param field - The field of the group of entries, this is kind of a unique reference per field
 * @returns An object with two functions, one that returns an empty string if the value is unique in the group of entries, error message otherwise, other that removes the entry from the group of entries
 * @example
 * const isUnique = isUniqueDescriptive();
 * isUnique.add('key', 'field')('value'); // true
 * isUnique.add('key', 'field')('value'); // false
 * isUnique.remove('key', 'field'); // true
 **/
export const isUniqueDescriptive = (): {
  add: (
    key: string,
    fieldId: string
  ) => (value: string, message: string) => string;
  remove: (key: string, fieldId: string) => boolean;
} => {
  const record: Record<string, Record<string, string>> = {};

  const add =
    (key: string, fieldId: string) =>
    (
      value: string,
      message: string = "This value is already in use"
    ): string => {
      // if the record contains the key and the fieldId is not the same as mine, check all the values, except the one if my fieldId to see if it includes my value
      const fieldsToCheck = Object.keys(record[key] || {}).filter(
        (field) => field !== fieldId
      );
      // Get all the values of the fields to check, except the one of my fieldId
      const values = fieldsToCheck.map((field: string) => record[key][field]);
      record[key] = { ...record[key], [fieldId]: value };

      if (
        values.some(
          (entry: string) => entry.toLowerCase() === value.toLowerCase()
        )
      ) {
        return message;
      }

      return "";
    };

  const remove = (key: string, fieldId: string): boolean =>
    delete record[key][fieldId];

  return {
    add,
    remove,
  };
};

export const isNoSpecialCharacters =
  (message: string = "No special characters allowed") =>
  (value: string): string => {
    if (specialCharacters.test(value)) return "";
    return message;
  };

export const isContainedIn =
  (values: Ref<string[]>, message = "No value selected") =>
  (value: string): string => {
    if (values.value.includes(value)) return "";
    return message;
  };

export const isNot =
  (referenceValue: string, message = "Value is not allowed") =>
  (value: string): string => {
    if (value !== referenceValue) return "";
    return message;
  };

// This function will extract the field value from a DTO object
export const getFieldValue = (path: string, value: any): string | string[] => {
  // First we set is in a temporary variable
  let temporaryValue: any = value;
  // We split the path by dots and iterate over it
  path.split(".").forEach((key: string) => {
    const fieldKey = key.includes("(")
      ? key.replace(")", "").split("(")[0]
      : key;
    // If the temporary value is not undefined we dig in
    if (temporaryValue[fieldKey] !== undefined) {
      // If the temporary value is an array we iterate over it
      if (Array.isArray(temporaryValue[fieldKey])) {
        // Array fields are split by star to indicate that we want to iterate over the array
        // Due to that, we split the path by the star and get the second element containing the rest of the path
        temporaryValue = temporaryValue[fieldKey].map((item: any) =>
          getFieldValue(path.split(".*.").slice(1).join(".*."), item)
        );
        // if is not an array we just set the value
      } else {
        temporaryValue = temporaryValue[fieldKey];
      }
    }
  });

  // At the end we return the value, keep in mind that the value can be an array if the field is an array
  return temporaryValue;
};

// We declare here a collection of all validations for every field in the form
export const formFieldValidations: Record<
  string,
  ((value: string) => string)[]
> = {};

// This function will return all validators for the field
export const getValidations = (key: string): ((value: string) => string)[] =>
  formFieldValidations[key] || [];

// This function will run the validators and return the errors
export const runValidation = (
  key: string,
  target: any,
  validation: (value: string) => string,
  notify: boolean = false,
  exhaustive = false
): boolean => {
  // We split the field key and the condition if it has one
  const [fieldKey, fieldCondition] = key.includes("(")
    ? key.replace(")", "").split("(")
    : [key, "true"];
  // We then load the field value
  const fieldValue = getFieldValue(fieldKey, target);

  // We define a function to evaluate the condition safely
  const evaluateCondition = (condition: string, item: any): boolean => {
    if (condition === "true") {
      return true;
    }

    try {
      return Function('"use strict";return (' + condition + ')').call(target, item);
    } catch (error) {
      console.error("Error evaluating condition:", error);
      return false;
    }
  };

  const executeValidation = (
    item: any,
    condition: string,
    fieldId: string = fieldKey
  ): string => {
    try {
      if (evaluateCondition(condition, item)) {
        const validationResponse = validation(item);
        if (notify) {
          // Note: also notifies when valid - errorMsg will be empty.
          notificationBus.emit({ fieldId, errorMsg: validationResponse }, item);
        }
        return validationResponse;
      } else {
        return "";
      }
    } catch (error) {
      console.error("Error executing validation:", error);
      return "";
    }
  };

  // If the field value is an array, we run the validation for every item in the array
  if (Array.isArray(fieldValue)) {
    let hasInvalidItem = false;
    for (let index = 0; index < fieldValue.length; index++) {
      const item = fieldValue[index];
      // And sometimes we can end up with another array inside, that's life
      if (Array.isArray(item)) {
        if (item.length === 0) item.push("");
        let hasInvalidSubItem = false;
        for (const subItem of item) {
          const valid =
            executeValidation(
              subItem,
              fieldCondition.replace(".*.", `[${index}].`),
              fieldKey.replace(".*.", `[${index}].`)
            ) === "";
          if (!valid) {
            hasInvalidSubItem = true;
            if (!exhaustive) {
              break;
            }
          }
        }
        return !hasInvalidSubItem;
      }
      // If it is not an array here, just validate it
      const valid =
        executeValidation(
          item,
          fieldCondition.replace(".*.", `[${index}].`),
          fieldKey.replace(".*.", `[${index}].`)
        ) === "";
      if (!valid) {
        hasInvalidItem = true;
        if (!exhaustive) {
          break;
        }
      }
    }
    return !hasInvalidItem;
  }
  // If the field value is not an array, we run the validation for the field
  return executeValidation(fieldValue, fieldCondition) === "";
};


// This function will run the validators and return the errors
export const validate = (
  keys: string[],
  target: any,
  notify: boolean = false
): boolean => {
  // For every received key we get the validations and run them
  return keys.every((key) => {
    // First we get all validators for that field
    const validations: ((value: string) => string)[] = getValidations(key);
    // We split the field key and the condition if it has one
    const [fieldKey, fieldCondition] = key.includes("(")
      ? key.replace(")", "").split("(")
      : [key, "true"];
    // For every validator we run it and check if the result is empty
    return validations.every((validation: (value: string) => string) => {
      // We then load the field value
      const fieldValue = getFieldValue(fieldKey, target);
      // We define a function that will run the validation if the condition is true
      const buildEval = (condition: string) =>
        condition === "true" ? "true" : `target.${condition}`;
      const runValidation = (
        item: any,
        condition: string,
        fieldId: string = fieldKey
      ): string => {
        // eslint-disable-next-line no-eval
        if (eval(condition)) {
          const validationResponse = validation(item);
          if (notify && validationResponse) {
            errorBus.emit([{ fieldId, errorMsg: validationResponse }]);
          }
          return validationResponse;
        } else {
          return "";
        }
      };
      // If the field value is an array we run the validation for every item in the array
      if (Array.isArray(fieldValue)) {
        return fieldValue.every((item: any, index: number) => {
          // And sometimes we can end up with another array inside, that's life
          if (Array.isArray(item)) {
            if (item.length === 0) item.push("");
            return item.every(
              (subItem: any) =>
                runValidation(
                  subItem,
                  buildEval(fieldCondition.replace(".*.", `[${index}].`)),
                  fieldKey.replace(".*.", `[${index}].`)
                ) === ""
            );
          }
          // If it is not an array here, just validate it
          return (
            runValidation(
              item,
              buildEval(fieldCondition.replace(".*.", `[${index}].`)),
              fieldKey.replace(".*.", `[${index}].`)
            ) === ""
          );
        });
      }
      // If the field value is not an array we run the validation for the field
      return runValidation(fieldValue, fieldCondition) === "";
    });
  });
};
