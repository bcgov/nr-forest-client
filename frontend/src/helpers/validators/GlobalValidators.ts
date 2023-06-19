//Defines the email regular expression
const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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
export const isNotEmpty = (value: string) => {
  if (value && value.trim().length > 0) return "";
  return "This field is required and cannot be empty";
}

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
export const isEmail = (value: string) => {
  if (isNotEmpty(value) === "" && emailRegex.test(value)) return "";
  return "This field must be a valid email";
}

export const isPhoneNumber = (value: string) => {
  if (isNotEmpty(value) === "" && value.match(/^[0-9]{10}$/)) return "";
  return "This field must be a valid phone number";
}


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
 * isCanadianPostalCode('a1a1a1') // false
 * isCanadianPostalCode('A1A1A1') // true
 * isCanadianPostalCode('A1A 1A1') // true
 * isCanadianPostalCode('A1A-1A1') // true
 **/
export const isCanadianPostalCode = (value: string) => {
  if (isNotEmpty(value) === "" && value.match(/^[A-Z]\d[A-Z][ -]?\d[A-Z]\d$/)) return "";
  return "This field must be a valid Canadian postal code";
}

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
export const isUsZipCode = (value: string) => {
  if (isNotEmpty(value) === "" && value.match(/^\d{5}(?:[-\s]\d{4})?$/)) return "";
  return "This field must be a valid US zip code";
}

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
export const isMaxSize = (maxSize: number) => {
  return (value: string) => {
    if (isNotEmpty(value) === "" && value.length <= maxSize) return "";
    return `This field must be at most ${maxSize} characters long`;
  }
}

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
export const isMinSize = (minSize: number) => {
  return (value: string) => {
    if (isNotEmpty(value) === "" && value.length >= minSize) return "";
    return `This field must be at least ${minSize} characters long`;
  }
}

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
export const isOnlyNumbers = (value: string) => {
  if (isNotEmpty(value) === "" && value.match(/^[0-9]*$/)) return "";
  return "This field must be composed of only numbers";
}

/**
 * Checks if the value is unique in an array of values
 * @param values - The array of values
 * @param name - The name of the field
 * @returns A function that returns an empty string if the value is unique in the array of values, error message otherwise
 * @example
 * isUnique(['a', 'b', 'c'])('') // true
 * isUnique(['a', 'b', 'c'])('a') // false
 * isUnique(['a', 'b', 'c'])('b') // false
 * isUnique(['a', 'b', 'c'])('c') // false
 * isUnique(['a', 'b', 'c'])('d') // true 
 */
export const isUniqueDescriptive = (name: string, values: string[]) => {
  return (value: string) => {
    if (value && !values.includes(value)) return "";
    return `${value} is alredy being used in another ${name}`;
  }
}