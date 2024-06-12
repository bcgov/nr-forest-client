import {
  isNotEmpty,
  isEmail,
  isPhoneNumber,
  isCanadianPostalCode,
  isUsZipCode,
  isMaxSize,
  isMinSize,
  isOnlyNumbers,
  isNoSpecialCharacters,
  isNotEmptyArray,
  isMinimumYearsAgo,
  isDateInThePast,
  isGreaterThan,
  hasOnlyNamingCharacters,
  isAscii,
  isIdCharacters,
  isRegex,
  validateSelection,
} from "@/helpers/validators/GlobalValidators";

const fieldValidations: Record<string, ((value: any) => string)[]> = {};

// This function will return all validators for the field
export const getValidations = (key: string): ((value: any) => string)[] =>
  fieldValidations[key] || [];

const isMinSizeMsg = (fieldName: string, minSize: number) =>
  isMinSize(`The ${fieldName} must contain at least ${minSize} characters`)(minSize);

const isMaxSizeMsg = (fieldName: string, maxSize: number) =>
  isMaxSize(`The ${fieldName} has a ${maxSize} character limit`)(maxSize);

const isExactSizMsg = (fieldName: string, size: number) => {
  const msg = `The ${fieldName} must contain ${size} characters`;
  return [isMinSize(msg)(size), isMaxSize(msg)(size)];
};

// Step 1: Business Information
fieldValidations["businessInformation.businessName"] = [
  isNotEmpty("Business name cannot be empty"),
  isMaxSizeMsg("business name", 60),
  isAscii("business name"),
];

fieldValidations["businessInformation.birthdate"] = [
  isDateInThePast("Date of birth must be in the past"),
  isMinimumYearsAgo(19, "The applicant must be at least 19 years old to apply"),
];

fieldValidations["businessInformation.birthdate.year"] = [
  isGreaterThan(1899, "Please check the birth year"),
];

fieldValidations["businessInformation.middleName"] = [
  isMaxSizeMsg("middle name", 30),
  hasOnlyNamingCharacters("middle name"),
];

fieldValidations["businessInformation.idType.text"] = [
  isNotEmpty("You must select an ID type."),
];

fieldValidations["businessInformation.issuingProvince.text"] = [
  isNotEmpty("You must select a value."),
];

interface IdNumberValidation {
  maxSize: number;
  onlyNumbers?: boolean;
}

/*
Variable defined using an IIFE to allow an easy definition of type-checkable keys.
*/
export const idNumberMaskParams = (() => {
  const init = {
    BCDL: {
      maxSize: 8,
      onlyNumbers: true,
    },
    nonBCDL: {
      maxSize: 20,
    },
    BRTH: {
      maxSize: 13,
      onlyNumbers: true,
    },
    PASS: {
      maxSize: 8,
    },
    CITZ: {
      maxSize: 8,
    },
    FNID: {
      maxSize: 10,
      onlyNumbers: true,
    },
    OTHR: undefined,
  };
  return init as Record<keyof typeof init, IdNumberValidation | undefined>;
})();

type IdNumberFormFieldValidationKey =
  `businessInformation.idNumber-${keyof typeof idNumberMaskParams}`;

const createIdNumberfieldValidations = (
  validations: Record<IdNumberFormFieldValidationKey, ((value: string) => string)[]>,
) => validations;

// idNumber base validations - applied regardless of the ID type / province.
fieldValidations["businessInformation.idNumber"] = [
  isNotEmpty("You must provide an ID number"),
];

Object.assign(
  fieldValidations,
  createIdNumberfieldValidations({
    "businessInformation.idNumber-BCDL": [
      isOnlyNumbers("BC driver's licence should contain only numbers"),
      isMinSizeMsg("BC driver's licence", 7),
      isMaxSizeMsg("BC driver's licence", idNumberMaskParams.BCDL.maxSize),
    ],

    "businessInformation.idNumber-nonBCDL": [
      isIdCharacters(),
      isMinSizeMsg("driver's licence", 7),
      isMaxSizeMsg("driver's licence", idNumberMaskParams.nonBCDL.maxSize),
    ],

    "businessInformation.idNumber-BRTH": [
      isOnlyNumbers("Canadian birth certificate should contain only numbers"),
      isMinSizeMsg("Canadian birth certificate", 12),
      isMaxSizeMsg("Canadian birth certificate", idNumberMaskParams.BRTH.maxSize),
    ],

    "businessInformation.idNumber-PASS": [
      isIdCharacters(),
      ...isExactSizMsg("Canadian passport", idNumberMaskParams.PASS.maxSize),
    ],

    "businessInformation.idNumber-CITZ": [
      isIdCharacters(),
      ...isExactSizMsg("Canadian citizenship card", idNumberMaskParams.CITZ.maxSize),
    ],

    "businessInformation.idNumber-FNID": [
      isIdCharacters(),
      ...isExactSizMsg("First Nation status ID", idNumberMaskParams.FNID.maxSize),
    ],

    "businessInformation.idNumber-OTHR": [
      isMinSizeMsg("ID number", 3),
      isMaxSizeMsg("ID number", 40),
      isRegex(
        /^[^:]+:[^:]+$/,
        'Other identification must follow the pattern: [ID Type] : [ID Value] such as "USA Passport : 12345"',
      ),
      validateSelection((value) => value.split(":")[1]?.trim())(
        isIdCharacters("The value to right of the colon can only contain: A-Z or 0-9"),
      ),
    ],
  }),
);

export const getIdNumberValidations = (key: IdNumberFormFieldValidationKey) => getValidations(key);

// Step 2: Addresses
fieldValidations["location.addresses.*.locationName"] = [
  isNotEmpty("You must provide a name for this location"),
  isMinSize(
    "The location name must be between 3 and 40 characters and cannot contain special characters"
  )(3),
  isMaxSize(
    "The location name must be between 3 and 40 characters and cannot contain special characters"
  )(40),
  isNoSpecialCharacters(
    "The location name must be between 3 and 40 characters and cannot contain special characters"
  ),
];
fieldValidations["location.addresses.*.country.text"] = [
  isNotEmpty("You must select a country"),
];
fieldValidations["location.addresses.*.province.text"] = [
  isNotEmpty("You must select a value"),
];
fieldValidations["location.addresses.*.city"] = [
  isNotEmpty("You must provide a city"),
  isMinSize("The city name must be between 3 and 30 characters")(3),
  isMaxSize("The city name must be between 3 and 30 characters")(30),
  hasOnlyNamingCharacters("city name"),
];
fieldValidations["location.addresses.*.streetAddress"] = [
  isNotEmpty("Please provide a valid address or PO Box"),
  isMinSize("The address must be between 5 and 40 characters")(5),
  isMaxSize("The address must be between 5 and 40 characters")(40),
  isAscii("address"),
];
fieldValidations[
  'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")'
] = [isCanadianPostalCode];
fieldValidations[
  'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")'
] = [isUsZipCode];
fieldValidations[
  'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")'
] = [
  isOnlyNumbers(
    "Postal code should be composed of only numbers and should be between 5 and 10 characters"
  ),
  isMinSize(
    "Postal code should be composed of only numbers and should be between 5 and 10 characters"
  )(5),
  isMaxSize(
    "Postal code should be composed of only numbers and should be between 5 and 10 characters"
  )(10),
];

// Step 3: Contacts
fieldValidations["location.contacts.*.locationNames"] = [
  isNotEmptyArray("You must select at least one location"),
];
fieldValidations["location.contacts.*.contactType.text"] = [
  isNotEmpty("You must select a role."),
];

fieldValidations["location.contacts.*.firstName"] = [
  isMinSize("Please enter the first name")(1),
  isMaxSizeMsg("first name", 30),
  hasOnlyNamingCharacters("first name"),
];
fieldValidations["location.contacts.*.lastName"] = [
  isMinSize("Please enter the last name")(1),
  isMaxSizeMsg("last name", 30),
  hasOnlyNamingCharacters("last name"),
];
fieldValidations["location.contacts.*.email"] = [
  isEmail("Please provide a valid email address"),
  isMinSize("Please provide a valid email address")(6),
  isMaxSizeMsg("email address", 100),
];
fieldValidations["location.contacts.*.phoneNumber"] = [
  isPhoneNumber("Please provide a valid phone number"),
  isMinSize("Please provide a valid phone number")(10),
  isMaxSizeMsg("phone number", 14),
];

export const addValidation = (
  key: string,
  validation: (value: string) => string
): void => {
  if (!fieldValidations[key]) fieldValidations[key] = [];
  fieldValidations[key].push(validation);
};
