import type { Ref } from "vue";
import { useEventBus } from "@vueuse/core";
import subYears from "date-fns/subYears";
import startOfToday from "date-fns/startOfToday";
import isBefore from "date-fns/isBefore";
import parseISO from "date-fns/parseISO";
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
 */
export const isNotEmpty =
  (message: string = "This field is required") =>
  (value: string): string => {
    if (value && value.trim().length > 0) return "";
    return message;
  };

/**
 * Checks if the array is not empty
 * @param array - The array to check
 * @returns An empty string if the array is not empty, error message otherwise
 * @example
 * isNotEmptyArray([]) // false
 * isNotEmptyArray('a') // true
 * isNotEmptyArray([' ']) // true
 * isNotEmptyArray([undefined]) // true
 */
export const isNotEmptyArray =
  (message: string = "This field is required") =>
  (array: any[]): string => {
    if (array && array.length > 0) return "";
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
 */
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
 */
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
 */
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
 */
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
 */
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
 */
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
 */
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
        ) && value.trim() !== ""
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

export const isWithinRange =
  (minValue: number, maxValue: number, message = "Value is out of range") =>
  (value: number | string): string => {
    if (value >= minValue && value <= maxValue) return "";
    return message;
  };

/**
 * Checks if the value is a possibly valid day for the specified month.
 * Note: February 29 will always be considered valid, since this validation does not consider the year.
 *
 * @param validMonth a valid month
 * @param message the error message to be returned if the validation fails.
 */
export const isValidDayOfMonth =
  (
    validMonth: string,
    message = "Value is not a valid day in the selected month"
  ) =>
  (value: string): string => {
    const arbitraryLeapYear = 2000;
    const dateString = `${arbitraryLeapYear}-${validMonth}-${value}`;
    const date = parseISO(dateString);
    if (isNaN(date.getTime())) return message;
    const isoStringDate = date.toISOString().substring(0, 10);
    if (isoStringDate !== dateString) return message;
    return "";
  };

/**
 * Checks if the value is a valid day for the specified year and month.
 * i.e. it tells if the date formed by the provided year, month and day exists.
 *
 * @param validYear a valid year
 * @param validMonth a valid month
 * @param message the error message to be returned if the validation fails.
 */
export const isValidDayOfMonthYear =
  (
    validYear: string,
    validMonth: string,
    message = "Value is not a valid day in the selected month and year"
  ) =>
  (value: string): string => {
    const dateString = `${validYear}-${validMonth}-${value}`;
    const date = parseISO(dateString);
    if (isNaN(date.getTime())) return message;
    const isoStringDate = date.toISOString().substring(0, 10);
    if (isoStringDate !== dateString) return message;
    return "";
  };

export const isMinimumYearsAgo =
  (
    years: number,
    message: string | ((years: number) => string) = (years) =>
      `Value must be at least ${years} years ago`
  ) =>
  (value: string): string => {
    const maximumDate = subYears(startOfToday(), years);
    const valueDate = parseISO(value);
    if (valueDate > maximumDate) {
      if (typeof message === "function") {
        return message(years);
      }
      return message;
    }
    return "";
  };

export const isGreaterThan =
  (
    compareTo: number,
    message: string = `Value must be greater than ${compareTo}`
  ) =>
  (value: string): string => {
    if (Number(value) > compareTo) {
      return "";
    }
    return message;
  };

export const isLessThan =
  (
    compareTo: number,
    message: string = `Value must be less than ${compareTo}`
  ) =>
  (value: string): string => {
    if (Number(value) < compareTo) {
      return "";
    }
    return message;
  };

export const isDateInThePast = (message: string) => (value: string) => {
  const dateValue = parseISO(value);
  if (!isBefore(dateValue, startOfToday())) {
    return message;
  }
  return "";
};

/**
 * Retrieves the value of a field in an object or array based on a given path.
 * If the field is an array, it returns an array of values.
 *
 * @param path - The path to the field, using dot notation. Array fields are split by "*".
 * @param value - The object or array from which to retrieve the field value.
 * @returns The value of the field, or an array of values if the field is an array.
 */
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


/**
 * Parses the aggregator condition and returns an object containing the parsed values.
 * @param conditional - The aggregator condition to parse.
 * @returns An object with the parsed values: value1, operator, and value2.
 * @throws {Error} If the condition format is invalid.
 */
const parseAggregatorCondition = (conditinal: string) : { value1: string, operator: string, value2: string } =>{

  if (conditinal.includes("&&") || conditinal.includes("||")) {        
      const regex = /(.+?)\s*(&&|\|\|)\s*(.+?)$/;
      const match = conditinal.replace("(","").replace(")","").match(regex);
      if (match) {
          return {
              value1: match[1],
              operator: match[2],
              value2: match[3]
          };
      } else {
          throw new Error("Invalid condition format it should be just string or string && string or string || string -> " + conditinal);
      }
  }        

  return { value1: conditinal.replace("(","").replace(")",""), operator: "&&", value2: "true" };
}

/**
* Parses an equality condition and returns an object with the parsed values.
* @param condition - The condition to parse.
* @returns An object with the parsed values: value1, operator, and value2.
* @throws {Error} If the condition format is invalid.
*/
const parseEqualityCondition = (condition: string): { value1: string, operator: string, value2: string } => {
  if(condition.includes("===") || condition.includes("!==")) {
      const regex = /(.+?)\s*(===|!==)\s*(?:"(.*?)"|\$(\..+?))$/;
      const match = condition.match(regex);
      if (match) {
          return {
              value1: match[1].replace("$.",""),
              operator: match[2],
              value2: match[3]
          };
      } else {
          throw new Error("Invalid condition format, it should be just a string or string === string or string !== string");
      }
  }
  return { value1: condition, operator: "===", value2: "true" };
}

/**
* Evaluates a logical condition based on the provided values and operator.
* @param value1 - The first value to be evaluated.
* @param operator - The logical operator to be used ('&&' for AND, '||' for OR).
* @param value2 - The second value to be evaluated.
* @returns The result of the logical condition evaluation.
* @throws {Error} If an invalid operator is provided.
*/
const evaluateLogicalCondition = ({ value1, operator, value2 }: { value1: string, operator: string, value2: string }): boolean => {
  const boolValue1 = value1 === 'true';
  const boolValue2 = value2 === 'true';

  switch (operator) {
      case '&&':
          return boolValue1 && boolValue2;
      case '||':
          return boolValue1 || boolValue2;
      default:
          throw new Error('Invalid operator');
  }
}

/**
* Evaluates an entry against an item based on the provided values and operator.
* @param item - The item to evaluate against.
* @param entry - The entry containing the values and operator to evaluate.
* @returns A boolean indicating whether the evaluation is true or false.
*/
const evaluateEntry = (item: any, entry: { value1: string, operator: string, value2: string }): boolean => {

  if(entry.value1 === 'true' && entry.value2 === 'true')
  return true;

  const value1Result = getFieldValue(entry.value1, item);

  const compareValues = (val1: any, val2: string, operator: string): boolean => {
      switch (operator) {
          case '===':
              return val1 === val2;
          case '!==':
              return val1 !== val2;
          default:
              throw new Error(`Unsupported operator: ${operator}`);
      }
  };

  if (Array.isArray(value1Result)) {
      return value1Result.some(individualValue => compareValues(individualValue, entry.value2, entry.operator));
  } else {
      return compareValues(value1Result, entry.value2, entry.operator);
  }
}

/**
* Evaluates a condition for a given item.
* @param item - The item to evaluate the condition against.
* @param condition - The condition to evaluate.
* @returns A boolean indicating whether the condition is true or false.
*/
const evaluateCondition = (item: any, condition: string): boolean => {

  if (condition === 'true') {
      return true;
  }

  if(condition === 'location.addresses[0].country.value === "US"'){
  console.log("condition: ", condition)
  console.log("item: ", item)
  }

  const conditionParsed = parseAggregatorCondition(condition);
  const condition1 = evaluateEntry(item, parseEqualityCondition(conditionParsed.value1));
  const condition2 = evaluateEntry(item, parseEqualityCondition(conditionParsed.value2));

  if(condition === 'location.addresses[0].country.value === "US"')
 { 
  console.log("conditionParsed: ", conditionParsed)
  console.log("condition1: ", condition1)
  console.log("condition2: ", condition2)
  console.log("evaluateLogicalCondition: ", evaluateLogicalCondition({ value1: `${condition1}`, operator: conditionParsed.operator, value2: `${condition2}`}))
}



  return evaluateLogicalCondition({ value1: `${condition1}`, operator: conditionParsed.operator, value2: `${condition2}`});
}

// We declare here a collection of all validations for every field in the form
export const formFieldValidations: Record<
  string,
  ((value: string) => string)[]
> = {};

// This function will return all validators for the field
export const getValidations = (key: string): ((value: string) => string)[] =>
  formFieldValidations[key] || [];

const arrayIndexGlobalRegex = /\.\*\./g;
const targetGlobalRegex = /\$\./g;

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
    
    const fieldValue = getFieldValue(fieldKey, target);
    const fieldEvaluation = evaluateCondition(target, fieldCondition.replace(targetGlobalRegex,""));
    
    // We skip if we evaluate and the result is false
    // Meaning we should not validate this field using this set of validations
    if(!fieldEvaluation){
      return true;
    }
    
    // For every validator we run it and check if the result is empty
    return validations.every((validation: (value: string) => string) => {
      // If the field value is an array we run the validation for every item in the array
      if (Array.isArray(fieldValue)) {
        return fieldValue.every((item: any) => {
          // And sometimes we can end up with another array inside, that's life
          if (Array.isArray(item)) {
            if (item.length === 0) item.push("");
            return item.every((subItem: any) => validation(subItem) === "");
          }
          // If it is not an array here, just validate it
          return validation(item) === "";
        });
      }else{
        return validation(fieldValue) === "";
      }
    });
  });
};

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
  const fieldEvaluation = evaluateCondition(target, fieldCondition.replace(targetGlobalRegex,""));

  // We skip if we evaluate and the result is false
  // Meaning we should not validate this field using this set of validations
  if(!fieldEvaluation){
    return true;
  }

  const validateValue = () : string | (string | string[])[] => {
    if (Array.isArray(fieldValue)) {
      return fieldValue.map((item: any) => {
        // And sometimes we can end up with another array inside, that's life
        if (Array.isArray(item)) {
          if (item.length === 0) item.push("");
          return item.map(validation);
        }
        // If it is not an array here, just validate it
        return validation(item);
      });
    }else{
      return validation(fieldValue);
    }
  }

  const validationResponse = validateValue();

  
  if (notify) {
    // Note: also notifies when valid - errorMsg will be empty.
    const validationResponseMessages : string[] = [];    
    (Array.isArray(validationResponse) ? validationResponse.flat() : [validationResponse])
      .forEach((validationResponseMessage) => validationResponseMessages.push(validationResponseMessage))

      getFirstErrorMessage(validationResponseMessages,exhaustive).forEach((validationResponseMessage) => {      
      notificationBus.emit({ fieldName: key, fieldId: fieldKey, errorMsg: validationResponseMessage }, fieldValue);
    })

    
  }

  // If the validation response is not empty we return false
  return isValidResponse(validationResponse);
};

const isValidResponse = (validationResponse: string | (string | string[])[]): boolean => {
  if (typeof validationResponse === 'string') {
      // Case 1: If it's a string and empty, return true
      return validationResponse.trim() === '';
  } else if (Array.isArray(validationResponse)) {
      // Case 2: If it's an empty array, return true
      if (validationResponse.length === 0) {
          return true;
      }

      // Check if it's an array of empty arrays
      if (validationResponse.every(val => Array.isArray(val) && val.length === 0)) {
          return true;
      }

      // Check if it's an array of empty strings
      if (validationResponse.every(val => typeof val === 'string' && val.trim() === '')) {
          return true;
      }

      // Otherwise, it's not one of the valid cases, so return false
      return false;
  } else {
      // If it's neither a string nor an array, it's an invalid response
      return false;
  }
}

const getFirstErrorMessage = (errors: string[],includeAll: boolean): string[] => {

  if(includeAll)
  return errors;

  const result: string[] = [];
  for (const error of errors) {
      result.push(error);
      if (error.trim() !== '') {
          break;
      }
  }
  return result;
}

export const isNullOrUndefinedOrBlank = (
  input: string | null | undefined
): boolean => input === null || input === undefined || input.trim() === "";
