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
  formFieldValidations,
  isNotEmptyArray,
  isMinimumYearsAgo,
  isDateInThePast,
  isGreaterThan,
  hasOnlyNamingCharacters,
  isAscii,
  isIdCharacters,
  getValidations,
} from "@/helpers/validators/GlobalValidators";

const isMinSizeMsg = (fieldName: string, minSize: number) =>
  isMinSize(`The ${fieldName} must contain at least ${minSize} characters`)(minSize);

const isMaxSizeMsg = (fieldName: string, maxSize: number) =>
  isMaxSize(`The ${fieldName} has a ${maxSize} character limit`)(maxSize);

// Step 1: Business Information
formFieldValidations["businessInformation.businessName"] = [
  isNotEmpty("Business name cannot be empty"),
  isMaxSizeMsg("business name", 60),
  isAscii("business name"),
];

formFieldValidations["businessInformation.birthdate"] = [
  isDateInThePast("Date of birth must be in the past"),
  isMinimumYearsAgo(19, "You must be at least 19 years old to apply"),
];

formFieldValidations["businessInformation.birthdate.year"] = [
  isGreaterThan(1899, "Please check the birth year"),
];

formFieldValidations["businessInformation.middleName"] = [
  isMaxSizeMsg("middle name", 30),
  hasOnlyNamingCharacters("middle name"),
];

formFieldValidations["businessInformation.idType.text"] = [
  isNotEmpty("You must select an ID type."),
];

formFieldValidations["businessInformation.issuingProvince.text"] = [
  isNotEmpty("You must select a value."),
];

interface IdNumberValidation {
  maxSize: number;
  onlyNumbers?: boolean;
}

/*
Variable defined using an IIFE to allow an easy definition of type-checkable keys.
*/
export const idNumberValidation = (() => {
  const init = {
    default: {
      maxSize: 20,
    },
    BCDL: {
      maxSize: 8,
      onlyNumbers: true,
    },
    nonBCDL: {
      maxSize: 10,
    },
    BRTH: {
      maxSize: 13,
      onlyNumbers: true,
    },
  };
  return init as Record<keyof typeof init, IdNumberValidation>;
})();

type IdNumberFormFieldValidationKey =
  `businessInformation.idNumber-${keyof typeof idNumberValidation}`;

const createIdNumberFormFieldValidations = (
  validations: Record<IdNumberFormFieldValidationKey, ((value: string) => string)[]>,
) => validations;

Object.assign(
  formFieldValidations,
  createIdNumberFormFieldValidations({
    "businessInformation.idNumber-default": [
      isNotEmpty("You must provide an ID number"),
      isIdCharacters(),
      isMinSizeMsg("ID number", 3),
      isMaxSizeMsg("ID number", idNumberValidation.default.maxSize),
    ],

    "businessInformation.idNumber-BCDL": [
      isMinSizeMsg("BC driver's licence", 7),
      isMaxSizeMsg("BC driver's licence", idNumberValidation.BCDL.maxSize),
      isOnlyNumbers("BC driver's licence should contain only numbers"),
    ],

    "businessInformation.idNumber-nonBCDL": [
      isMinSizeMsg("driver's licence", 7),
      isMaxSizeMsg("driver's licence", idNumberValidation.nonBCDL.maxSize),
    ],

    "businessInformation.idNumber-BRTH": [
      isMinSizeMsg("Canadian birth certificate", 12),
      isMaxSizeMsg("Canadian birth certificate", idNumberValidation.BRTH.maxSize),
    ],
  }),
);

export const getIdNumberValidations = (key: IdNumberFormFieldValidationKey) => getValidations(key);

// Make the default value also available at the plain field name as key.
formFieldValidations["businessInformation.idNumber"] =
  formFieldValidations["businessInformation.idNumber-default"];

// Step 2: Addresses
formFieldValidations["location.addresses.*.locationName"] = [
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
formFieldValidations["location.addresses.*.country.text"] = [
  isNotEmpty("You must select a country"),
];
formFieldValidations["location.addresses.*.province.text"] = [
  isNotEmpty("You must select a value"),
];
formFieldValidations["location.addresses.*.city"] = [
  isNotEmpty("You must provide a city"),
  isMinSize("The city name must be between 3 and 30 characters")(3),
  isMaxSize("The city name must be between 3 and 30 characters")(30),
  hasOnlyNamingCharacters("city name"),
];
formFieldValidations["location.addresses.*.streetAddress"] = [
  isNotEmpty("Please provide a valid address or PO Box"),
  isMinSize("The address must be between 5 and 40 characters")(5),
  isMaxSize("The address must be between 5 and 40 characters")(40),
  isAscii("address"),
];
formFieldValidations[
  'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")'
] = [isCanadianPostalCode];
formFieldValidations[
  'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")'
] = [isUsZipCode];
formFieldValidations[
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
formFieldValidations["location.contacts.*.locationNames"] = [
  isNotEmptyArray("You must select at least one location"),
];
formFieldValidations["location.contacts.*.contactType.text"] = [
  isNotEmpty("You must select a role."),
];

formFieldValidations["location.contacts.*.firstName"] = [
  isMinSize("Please enter the first name")(1),
  isMaxSizeMsg("first name", 30),
  hasOnlyNamingCharacters("first name"),
];
formFieldValidations["location.contacts.*.lastName"] = [
  isMinSize("Please enter the last name")(1),
  isMaxSizeMsg("last name", 30),
  hasOnlyNamingCharacters("last name"),
];
formFieldValidations["location.contacts.*.email"] = [
  isEmail("Please provide a valid email address"),
  isMinSize("Please provide a valid email address")(6),
  isMaxSizeMsg("email address", 100),
];
formFieldValidations["location.contacts.*.phoneNumber"] = [
  isPhoneNumber("Please provide a valid phone number"),
  isMinSize("Please provide a valid phone number")(10),
  isMaxSizeMsg("phone number", 14),
];

export const addValidation = (
  key: string,
  validation: (value: string) => string
): void => {
  if (!formFieldValidations[key]) formFieldValidations[key] = [];
  formFieldValidations[key].push(validation);
};
