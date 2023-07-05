// Defines the used regular expressions
const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const specialCharacters: RegExp = /^[a-zA-Z0-9\sÀ-ÖØ-öø-ÿ]+$/
const e164Regex: RegExp = /^((\+?[1-9]\d{1,14})|(\(\d{3}\) \d{3}-\d{4}))$/
const canadianPostalCodeRegex: RegExp = /^[A-Z]\d[A-Z][ -]?\d[A-Z]\d$/
const usZipCodeRegex: RegExp = /^\d{5}(?:[-\s]\d{4})?$/

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
  if (value && value.trim().length > 0) return ''
  return 'This field is required and cannot be empty'
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
  if (isNotEmpty(value) === '' && emailRegex.test(value)) return ''
  return 'This field must be a valid email'
}

export const isPhoneNumber = (value: string) => {
  if (isNotEmpty(value) === '' && e164Regex.test(value)) return ''
  return 'This field must be a valid phone number'
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
  if (isNotEmpty(value) === '' && canadianPostalCodeRegex.test(value)) return ''
  return 'This field must be a valid Canadian postal code'
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
  if (isNotEmpty(value) === '' && usZipCodeRegex.test(value)) {
    return ''
  }
  return 'This field must be a valid US zip code'
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
    if (isNotEmpty(value) === '' && value.length <= maxSize) return ''
    return `This field must be at most ${maxSize} characters long`
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
    if (isNotEmpty(value) === '' && value.length >= minSize) return ''
    return `This field must be at least ${minSize} characters long`
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
  if (isNotEmpty(value) === '' && value.match(/^[0-9]*$/)) return ''
  return 'This field must be composed of only numbers'
}

/**
 * Check if value is unique in the group of entries
 * @param key - The key of the group of entries
 * @param field - The field of the group of entries, this is kind of a unique reference per field
 * @returns A function that returns an empty string if the value is unique in the group of entries, error message otherwise
 * @example
 * const isUnique = isUniqueDescriptive();
 * isUnique('key', 'field')('value'); // true
 * isUnique('key', 'field')('value'); // false
 **/
export const isUniqueDescriptive = (): ((
  key: string,
  field: string
) => (value: string) => string) => {
  const record: Record<string, Record<string, string>> = {}

  return (key: string, fieldId: string) => (value: string) => {
    // if the record contains the key and the fieldId is not the same as mine, check all the values, except the one if my fieldId to see if it includes my value
    const fieldsToCheck = Object.keys(record[key] || {}).filter(
      (field) => field !== fieldId
    )

    // Get all the values of the fields to check, except the one of my fieldId
    const values = fieldsToCheck.map((field: string) => record[key][field])
    record[key] = { ...record[key], [fieldId]: value }

    if (
      values.some(
        (entry: string) => entry.toLowerCase() === value.toLowerCase()
      )
    ) {
      return 'This value is already in use'
    }

    return ''
  }
}

export const isNoSpecialCharacters = (value: string) => {
  if (isNotEmpty(value) === '' && specialCharacters.test(value)) return ''
  return 'No special characters allowed'
}
