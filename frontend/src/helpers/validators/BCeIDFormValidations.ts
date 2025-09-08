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
} from "@/helpers/validators/GlobalValidators";

const isMaxSizeMsg = (fieldName: string, maxSize: number) =>
  isMaxSize(`The ${fieldName} has a ${maxSize} character limit`)(maxSize);

// Step 1: Business Information
formFieldValidations["businessInformation.businessName"] = [
  isNotEmpty("Business name cannot be empty"),
  isMaxSizeMsg("business name", 60),
  isAscii("business name"),
];

formFieldValidations["businessInformation.birthdate"] = [
  isNotEmpty("You must enter a date of birth"),
  isDateInThePast("Date of birth must be in the past"),
  isMinimumYearsAgo(19, "You must be at least 19 years old to apply"),
];

formFieldValidations["businessInformation.birthdate.year"] = [
  isGreaterThan(1899, "Please check the birth year"),
];

formFieldValidations["businessInformation.district.text"] = [
  isNotEmpty("You must select a district."),
];

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
  isAscii("city name"),
];
formFieldValidations["location.addresses.*.streetAddress"] = [
  isNotEmpty("Please provide a valid address or PO Box"),
  isMinSize("The address must be between 4 and 40 characters")(4),
  isMaxSize("The address must be between 4 and 40 characters")(40),
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

export const getValidations = (key: string): ((value: any) => string)[] =>
  key ? formFieldValidations[key] || [] : [];
