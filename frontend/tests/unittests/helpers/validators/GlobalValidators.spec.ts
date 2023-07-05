import { describe, it, expect } from 'vitest'
import {
  isNotEmpty,
  isEmail,
  isPhoneNumber,
  isCanadianPostalCode,
  isUsZipCode,
  isMaxSize,
  isMinSize,
  isOnlyNumbers,
  isUniqueDescriptive,
  isNoSpecialCharacters
} from '@/helpers/validators/GlobalValidators'

describe('GlobalValidators', () => {
  it('should return empty when isNotEmpty is called with a non-empty string', () => {
    expect(isNotEmpty('a')).toBe('')
  })
  it('should return empty when isNotEmpty is called on a string with leading whitespaces', () => {
    expect(isNotEmpty(' a')).toBe('')
  })
  it('should return empty when isNotEmpty is called on a string with trailing whitespaces', () => {
    expect(isNotEmpty('a ')).toBe('')
  })
  it('should return empty when isNotEmpty is called on a string with leading and trailing whitespaces', () => {
    expect(isNotEmpty(' a ')).toBe('')
  })
  it('should return an error message when isNotEmpty is called on an empty string', () => {
    expect(isNotEmpty('')).toBe('This field is required and cannot be empty')
  })

  it('should return empty when isEmail is called with a valid email', () => {
    expect(isEmail('mail@mail.ca')).toBe('')
  })
  it('should return an error message when isEmail is called with an invalid email', () => {
    expect(isEmail('mail')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with an empty string', () => {
    expect(isEmail('')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with a string with leading whitespaces', () => {
    expect(isEmail('  mail@mail.ca')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with a string with trailing whitespaces', () => {
    expect(isEmail('mail@mail.ca  ')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with a string with leading and trailing whitespaces', () => {
    expect(isEmail('  mail@mail.ca  ')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with an email with whitespace in the middle of it', () => {
    expect(isEmail('mail @mail.ca')).toBe('This field must be a valid email')
  })

  it('should return empty when isPhoneNumber is called with a valid phone number', () => {
    expect(isPhoneNumber('+14165555555')).toBe('')
  })
  it('should return empty when isPhoneNumber is called with a phone number without country code', () => {
    expect(isPhoneNumber('4165555555')).toBe('')
  })
  it('should return an error message when isPhoneNumber is called with an empty string', () => {
    expect(isPhoneNumber('')).toBe('This field must be a valid phone number')
  })
  it('should return an error message when isPhoneNumber is called with a string with leading whitespaces', () => {
    expect(isPhoneNumber('  +14165555555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a string with trailing whitespaces', () => {
    expect(isPhoneNumber('+14165555555  ')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a string with leading and trailing whitespaces', () => {
    expect(isPhoneNumber('  +14165555555  ')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with whitespace in the middle of it', () => {
    expect(isPhoneNumber('+1416 555 5555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a letter in it', () => {
    expect(isPhoneNumber('+1416555a555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a special character in it', () => {
    expect(isPhoneNumber('+1416555!555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a space in it', () => {
    expect(isPhoneNumber('+1416555 555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a dash in it', () => {
    expect(isPhoneNumber('+1416555-555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a dot in it', () => {
    expect(isPhoneNumber('+1416555.555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return empty when isCanadianPostalCode is called with a valid postal code', () => {
    expect(isCanadianPostalCode('A1A1A1')).toBe('')
  })
  it('should return an error message when isCanadianPostalCode is called with an empty string', () => {
    expect(isCanadianPostalCode('')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a string with leading whitespaces', () => {
    expect(isCanadianPostalCode('  A1A1A1')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a string with trailing whitespaces', () => {
    expect(isCanadianPostalCode('A1A1A1  ')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a string with leading and trailing whitespaces', () => {
    expect(isCanadianPostalCode('  A1A1A1  ')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a postal code with whitespace in the middle of it', () => {
    expect(isCanadianPostalCode('A1A 1A1')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return empty when isCanadianPostalCode is called with a postal code with lowercase letters', () => {
    expect(isCanadianPostalCode('a1a1a1')).toBe('')
  })
  it('should return empty when isCanadianPostalCode is called with a postal code with uppercase and lowercase letters', () => {
    expect(isCanadianPostalCode('A1a1A1')).toBe('')
  })
  it('should return an error message when isCanadianPostalCode is called with a postal code with a number in the first position', () => {
    expect(isCanadianPostalCode('1A1A1B')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a postal code with a dash', () => {
    expect(isCanadianPostalCode('A1-A1A1')).toBe(
      'This field must be a valid Canadian postal code'
    )
  })
  it('should return empty when isUsZipCode is called with a valid zip code', () => {
    expect(isUsZipCode('12345')).toBe('')
  })
  it('should return an error message when isUsZipCode is called with an empty string', () => {
    expect(isUsZipCode('')).toBe('This field must be a valid US zip code')
  })
  it('should return an error message when isUsZipCode is called with a string with leading whitespaces', () => {
    expect(isUsZipCode('  12345')).toBe(
      'This field must be a valid US zip code'
    )
  })
  it('should return an error message when isUsZipCode is called with a string with trailing whitespaces', () => {
    expect(isUsZipCode('12345  ')).toBe(
      'This field must be a valid US zip code'
    )
  })
  it('should return an error message when isUsZipCode is called with a string with leading and trailing whitespaces', () => {
    expect(isUsZipCode('  12345  ')).toBe(
      'This field must be a valid US zip code'
    )
  })
  it('should return an error message when isUsZipCode is called with a zip code with whitespace in the middle of it', () => {
    expect(isUsZipCode('123 45')).toBe('This field must be a valid US zip code')
  })
  it('should return an error message when isUsZipCode is called with a zip code with letters', () => {
    expect(isUsZipCode('1234a')).toBe('This field must be a valid US zip code')
  })
  it('should return empty when isMaxSize is called on a string with a size less than the max size', () => {
    expect(isMaxSize(5)('123')).toBe('')
  })
  it('should return empty when isMaxSize is called on a string with a size equal to the max size', () => {
    expect(isMaxSize(5)('12345')).toBe('')
  })
  it('should return an error message when isMaxSize is called on a string with a size greater than the max size', () => {
    expect(isMaxSize(5)('123456')).toBe(
      'This field must be at most 5 characters long'
    )
  })
  it('should return empty when isMinSize is called on a string with a size greater than the min size', () => {
    expect(isMinSize(5)('123456')).toBe('')
  })
  it('should return empty when isMinSize is called on a string with a size equal to the min size', () => {
    expect(isMinSize(5)('12345')).toBe('')
  })
  it('should return an error message when isMinSize is called on a string with a size less than the min size', () => {
    expect(isMinSize(5)('1234')).toBe(
      'This field must be at least 5 characters long'
    )
  })
  it('should return an error message when isMinSize is called on an empty string', () => {
    expect(isMinSize(5)('')).toBe(
      'This field must be at least 5 characters long'
    )
  })
  it('should return empty when isOnlyNumbers is called on a string with only numbers', () => {
    expect(isOnlyNumbers('123')).toBe('')
  })
  it('should return an error message when isOnlyNumbers is called on a string with letters', () => {
    expect(isOnlyNumbers('123a')).toBe(
      'This field must be composed of only numbers'
    )
  })
  it('should return an error message when isOnlyNumbers is called on an empty string', () => {
    expect(isOnlyNumbers('')).toBe(
      'This field must be composed of only numbers'
    )
  })
  it('should return empty when isUniqueDescriptive is called on two different sets of strings', () => {
    const isUnique = isUniqueDescriptive()
    expect(isUnique('key1', 'field1')('value')).toBe('')
    expect(isUnique('key2', 'field1')('value')).toBe('')
  })
  it('should return an error message when isUniqueDescriptive is called on two fields but for the same set of strings', () => {
    const isUnique = isUniqueDescriptive()
    expect(isUnique('key1', 'field1')('value')).toBe('')
    expect(isUnique('key1', 'field2')('value')).toBe(
      'This value is already in use'
    )
  })
  it('should return empty when isNoSpecialCharacters is called on a string with no special characters', () => {
    expect(isNoSpecialCharacters('abc123')).toBe('')
  })
  it('should return an error message when isNoSpecialCharacters is called on a string with special characters', () => {
    expect(isNoSpecialCharacters('abc123!')).toBe(
      'No special characters allowed'
    )
  })
  it('should return an error message when isNoSpecialCharacters is called on an empty string', () => {
    expect(isNoSpecialCharacters('')).toBe('No special characters allowed')
  })
})
