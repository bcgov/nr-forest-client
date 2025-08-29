import { describe, it, expect } from 'vitest'
import { ref } from 'vue'
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
  isNoSpecialCharacters,
  isContainedIn,
  isNot,
  hasOnlyNamingCharacters,
  isAscii,
  isAsciiLineBreak,
  isIdCharacters,
  isRegex,
  validateSelection,
  isGreaterThanOrEqualTo,
  isLessThanOrEqualTo,
} from '@/helpers/validators/GlobalValidators'

describe('GlobalValidators', () => {
  it('should return empty when isNotEmpty is called with a non-empty string', () => {
    expect(isNotEmpty()('a')).toBe('')
  })
  it('should return empty when isNotEmpty is called on a string with leading whitespaces', () => {
    expect(isNotEmpty()(' a')).toBe('')
  })
  it('should return empty when isNotEmpty is called on a string with trailing whitespaces', () => {
    expect(isNotEmpty()('a ')).toBe('')
  })
  it('should return empty when isNotEmpty is called on a string with leading and trailing whitespaces', () => {
    expect(isNotEmpty()(' a ')).toBe('')
  })
  it('should return an error message when isNotEmpty is called on an empty string', () => {
    expect(isNotEmpty()('')).toBe('This field is required')
  })

  it('should return empty when isEmail is called with a valid email', () => {
    expect(isEmail()('mail@mail.ca')).toBe('')
  })
  it('should return an error message when isEmail is called with an invalid email', () => {
    expect(isEmail()('mail')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with an empty string', () => {
    expect(isEmail()('')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with a string with leading whitespaces', () => {
    expect(isEmail()('  mail@mail.ca')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with a string with trailing whitespaces', () => {
    expect(isEmail()('mail@mail.ca  ')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with a string with leading and trailing whitespaces', () => {
    expect(isEmail()('  mail@mail.ca  ')).toBe('This field must be a valid email')
  })
  it('should return an error message when isEmail is called with an email with whitespace in the middle of it', () => {
    expect(isEmail()('mail @mail.ca')).toBe('This field must be a valid email')
  })

  it('should return empty when isPhoneNumber is called with a valid phone number', () => {
    expect(isPhoneNumber()('+14165555555')).toBe('')
  })
  it('should return empty when isPhoneNumber is called with a phone number without country code', () => {
    expect(isPhoneNumber()('4165555555')).toBe('')
  })
  it('should return an error message when isPhoneNumber is called with an empty string', () => {
    expect(isPhoneNumber()('')).toBe('This field must be a valid phone number')
  })
  it('should return an error message when isPhoneNumber is called with a string with leading whitespaces', () => {
    expect(isPhoneNumber()('  +14165555555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a string with trailing whitespaces', () => {
    expect(isPhoneNumber()('+14165555555  ')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a string with leading and trailing whitespaces', () => {
    expect(isPhoneNumber()('  +14165555555  ')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with whitespace in the middle of it', () => {
    expect(isPhoneNumber()('+1416 555 5555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a letter in it', () => {
    expect(isPhoneNumber()('+1416555a555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a special character in it', () => {
    expect(isPhoneNumber()('+1416555!555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a space in it', () => {
    expect(isPhoneNumber()('+1416555 555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a dash in it', () => {
    expect(isPhoneNumber()('+1416555-555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return an error message when isPhoneNumber is called with a phone number with a dot in it', () => {
    expect(isPhoneNumber()('+1416555.555')).toBe(
      'This field must be a valid phone number'
    )
  })
  it('should return empty when isCanadianPostalCode is called with a valid postal code', () => {
    expect(isCanadianPostalCode('A1A1A1')).toBe('')
  })
  it('should return an error message when isCanadianPostalCode is called with an empty string', () => {
    expect(isCanadianPostalCode('')).toBe(
      'This field must be a valid Canadian postal code without spaces or dashes'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a string with leading whitespaces', () => {
    expect(isCanadianPostalCode('  A1A1A1')).toBe(
      'This field must be a valid Canadian postal code without spaces or dashes'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a string with trailing whitespaces', () => {
    expect(isCanadianPostalCode('A1A1A1  ')).toBe(
      'This field must be a valid Canadian postal code without spaces or dashes'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a string with leading and trailing whitespaces', () => {
    expect(isCanadianPostalCode('  A1A1A1  ')).toBe(
      'This field must be a valid Canadian postal code without spaces or dashes'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a postal code with whitespace in the middle of it', () => {
    expect(isCanadianPostalCode('A1A 1A1')).toBe(
      'This field must be a valid Canadian postal code without spaces or dashes'
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
      'This field must be a valid Canadian postal code without spaces or dashes'
    )
  })
  it('should return an error message when isCanadianPostalCode is called with a postal code with a dash', () => {
    expect(isCanadianPostalCode('A1-A1A1')).toBe(
      'This field must be a valid Canadian postal code without spaces or dashes'
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
    expect(isMaxSize()(5)('123')).toBe('')
  })
  it('should return empty when isMaxSize is called on a string with a size equal to the max size', () => {
    expect(isMaxSize()(5)('12345')).toBe('')
  })
  it('should return an error message when isMaxSize is called on a string with a size greater than the max size', () => {
    expect(isMaxSize('This field must be at most 5 characters long')(5)('123456')).toBe(
      'This field must be at most 5 characters long'
    )
  })
  it('should return empty when isMaxSize is called on an empty string', () => {
    expect(isMaxSize('This field must be at most 5 characters long')(5)('')).toBe('')
  })
  it.each([[null], [undefined]])('should return empty when isMaxSize is called on %s', (value) => {
    expect(isMaxSize('This field must be at most 5 characters long')(5)(value)).toBe('')
  })
  it('should return empty when isMinSize is called on a string with a size greater than the min size', () => {
    expect(isMinSize()(5)('123456')).toBe('')
  })
  it('should return empty when isMinSize is called on a string with a size equal to the min size', () => {
    expect(isMinSize()(5)('12345')).toBe('')
  })
  it('should return an error message when isMinSize is called on a string with a size less than the min size', () => {
    expect(isMinSize('This field must be at least 5 characters long')(5)('1234')).toBe(
      'This field must be at least 5 characters long'
    )
  })
  it('should return an error message when isMinSize is called on an empty string', () => {
    expect(isMinSize('This field must be at least 5 characters long')(5)('')).toBe(
      'This field must be at least 5 characters long'
    )
  })
  it('should return empty when isOnlyNumbers is called on a string with only numbers', () => {
    expect(isOnlyNumbers()('123')).toBe('')
  })
  it('should return an error message when isOnlyNumbers is called on a string with letters', () => {
    expect(isOnlyNumbers()('123a')).toBe(
      'This field must be composed of only numbers'
    )
  })
  it('should return an error message when isOnlyNumbers is called on an empty string', () => {
    expect(isOnlyNumbers()('')).toBe(
      'This field must be composed of only numbers'
    )
  })
  it('should return empty when isUniqueDescriptive is called on two different sets of strings', () => {
    const isUnique = isUniqueDescriptive()
    expect(isUnique.add('key1', 'field1')('value')).toBe('')
    expect(isUnique.add('key2', 'field1')('value')).toBe('')
  })
  it('should return an error message when isUniqueDescriptive is called on two fields but for the same set of strings', () => {
    const isUnique = isUniqueDescriptive()
    expect(isUnique.add('key1', 'field1')('value')).toBe('')
    expect(isUnique.add('key1', 'field2')('value')).toBe(
      'This value is already in use'
    )
  })
  it('should return empty when isNoSpecialCharacters is called on a string with no special characters', () => {
    expect(isNoSpecialCharacters()('abc123')).toBe('')
  })
  it('should return an error message when isNoSpecialCharacters is called on a string with special characters', () => {
    expect(isNoSpecialCharacters()('abc123!')).toBe(
      'No special characters allowed'
    )
  })
  it('should return empty when isNoSpecialCharacters is called on an empty string', () => {
    expect(isNoSpecialCharacters()('')).toBe('')
  })
  it('should return empty when content is contained in the list', () => {
    expect(isContainedIn(ref(['a', 'b']))('a')).toBe('')
  })
  it('should return an error message when content is not contained in the list', () => {
    expect(isContainedIn(ref(['a', 'b']))('c')).toBe('No value selected')
  })
  it('should return empty when value is different than the other value', () => {
    expect(isNot('a')('b')).toBe('')
  })
  it('should return an error message when value is equal to the other value', () => {
    expect(isNot('a')('a')).toBe('Value is not allowed')
  })
  it("should return an error when value has any non-\"naming-like\" characters", () => {
    const result = hasOnlyNamingCharacters()("T!m");
    expect(result).toEqual(expect.any(String));
    expect(result).not.toBe("");
  });
  it("should return an error when value has any accented characters", () => {
    const result = hasOnlyNamingCharacters()("Tóm");
    expect(result).toEqual(expect.any(String));
    expect(result).not.toBe("");
  });
  it("should return empty when value has only \"naming-like\" characters", () => {
    const result = hasOnlyNamingCharacters()("Tom-d'John Paul");
    expect(result).toBe("");
  });

  const generateTestCase = (fn: Function) => ({
    fn,
    name: fn.name,
  })
  describe.each([generateTestCase(isAscii), generateTestCase(isAsciiLineBreak)])("$name", ({ fn }) => {
    it("should return an error when value has any non-ASCII characters", () => {
      const result = fn()("Aço");
      expect(result).toEqual(expect.any(String));
      expect(result).not.toBe("");
    });
    it("should return empty when value has only ASCII characters", () => {
      const result = fn()("AZaz09 '!@#$%_-+()/\\");
      expect(result).toBe("");
    });
    it("should return empty when value is an empty string", () => {
      const result = fn()("");
      expect(result).toBe("");
    });
  });

  it("should return an error when value has a line break", () => {
    const result = isAscii()("First\nSecond");
    expect(result).not.toBe("");
  });

  it("should return empty when value has a line break", () => {
    const result = isAsciiLineBreak()("First\nSecond");
    expect(result).toBe("");
  });

  describe("isIdCharacters", () => {
    it.each([
      ["is an empty string", ""],
      ["contains only A-Z and 0-9", "A1R4"],
    ])("should return empty string when value %s", (_, value) => {
      const result = isIdCharacters()(value);
      expect(result).toBe("");
    });

    it.each([
      ["contains a lower-case letter", "A1t2"],
      ["contains a space", "A 12"],
      ["contains a punctuation symbol", "A!12"],
      ["contains an accented letter", "Á1F2"],
      ["contains a hyphen", "A1-R4"],
    ])("should return an error when value %s", (_, value) => {
      const result = isIdCharacters()(value);
      expect(result).not.toBe("");
    });
  });
  describe("isRegex", () => {
    const sampleRegex = /\d-\d/;
    it.each([
      ["when the regex is matched with the whole value", "1-2"],
      ["when the regex is matched with part of the string", "s1-2e"],
    ])("should return empty string %s", (_, value) => {
      const result = isRegex(sampleRegex)(value);
      expect(result).toBe("");
    });

    const nullableRegex = /.*/;
    it.each([
      ["even when the value is an empty string if the regex allows it", ""],
    ])("should return empty string %s", (_, value) => {
      const result = isRegex(nullableRegex)(value);
      expect(result).toBe("");
    });

    it.each([
      ["when the value is empty and doesn't match", ""],
      ["when the value is not empty but doesn't match anyway", "12"],
    ])("should return an error %s", (_, value) => {
      const result = isRegex(sampleRegex)(value);
      expect(result).not.toBe("");
    });
  });
  describe("isGreaterThanOrEqualTo", () => {
    it("should return an error when value is less than the minimum allowed value", () => {
      const result = isGreaterThanOrEqualTo(7)("6");
      expect(result).not.toBe("");
    });

    it("should return empty when value is equal to the minimum allowed value", () => {
      const result = isGreaterThanOrEqualTo(7)("7");
      expect(result).toBe("");
    });

    it("should return empty when value is greater than the minimum allowed value", () => {
      const result = isGreaterThanOrEqualTo(7)("8");
      expect(result).toBe("");
    });
  });
  describe("isLessThanOrEqualTo", () => {
    it("should return an error when value is greater than the maximum allowed value", () => {
      const result = isLessThanOrEqualTo(7)("8");
      expect(result).not.toBe("");
    });

    it("should return empty when value is equal to the maximum allowed value", () => {
      const result = isLessThanOrEqualTo(7)("7");
      expect(result).toBe("");
    });

    it("should return empty when value is less than the maximum allowed value", () => {
      const result = isLessThanOrEqualTo(7)("6");
      expect(result).toBe("");
    });
  });
  describe("validateSelection", () => {
    describe("when the provided selector is a function that extracts the string portion after the colon", () => {
      const extract = (value: string) => {
        const index = value.indexOf(":") + 1;
        return value.substring(index);
      };
      describe("and there is a validation on max length as 5", () => {
        const validator = isMaxSize()(5);

        it.each([
          ["when the extracted portion passes the validation", "prefix:1234"],
        ])("should return an empty string %s (%s)", (_, value) => {
          const result = validateSelection(extract)(validator)(value);
          expect(result).toBe("");
        });

        it.each([
          ["when the extracted portion doesn't pass the validation", "prefix:123456"],
        ])("should return an error %s (%s)", (_, value) => {
          const result = validateSelection(extract)(validator)(value);
          expect(result).not.toBe("");
        });
      });
      describe("and there is a validation on min length as 3", () => {
        const validator = isMinSize()(3);

        it.each([
          ["when the extracted portion passes the validation", "prefix:1234"],
        ])("should return an empty string %s (%s)", (_, value) => {
          const result = validateSelection(extract)(validator)(value);
          expect(result).toBe("");
        });

        it.each([
          ["when the extracted portion doesn't pass the validation", "prefix:12"],
        ])("should return an error %s (%s)", (_, value) => {
          const result = validateSelection(extract)(validator)(value);
          expect(result).not.toBe("");
        });
      });
    });
  });
  describe("when the provided selector throws an error", () => {
    const selector = (value: string) => {
      throw new Error;
    };

    const validator = isMaxSize()(20);

    describe.each([
      ["and a onSelectorErrorMessage was not provided", undefined, "the default selector error message"],
      ["and a onSelectorErrorMessage was provided", "Sorry, the extraction failed!", "the provided message"]
    ])("%s", (_, selectorMessage, explanation) => {
      it.each([
        ["regardless of the value", "anything"],
      ])(`should return ${explanation}`, (_, value) => {
        const result = validateSelection(selector, selectorMessage)(validator)(value);
        const defaultSelectorMessage = "Value could not be validated";
        const message = selectorMessage !== undefined ? selectorMessage : defaultSelectorMessage;
        expect(result).toBe(message);
      });
    });
  });
})
