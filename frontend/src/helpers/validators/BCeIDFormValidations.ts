import { useEventBus } from "@vueuse/core";

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
  isNot,
  formFieldValidations,
} from "@/helpers/validators/GlobalValidators";


// Step 1: Business Information
formFieldValidations["businessInformation.businessName"] = [
  isNotEmpty("Business Name cannot be empty"),
];
formFieldValidations["businessInformation.clientType"] = [
  isNot(
    "I",
    "Individuals cannot be selected. Please select a different client."
  ),
];

// Step 2: Addresses
formFieldValidations["location.addresses.*.locationName"] = [
  isNotEmpty("You must provide a name for this location"),
  isMinSize(
    "The location name must be between 3 and 50 characters and cannot contain special characters"
  )(3),
  isMaxSize(
    "The location name must be between 3 and 50 characters and cannot contain special characters"
  )(50),
  isNoSpecialCharacters(
    "The location name must be between 3 and 50 characters and cannot contain special characters"
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
  isMinSize("The city name must be between 3 and 50 characters")(3),
  isMaxSize("The city name must be between 3 and 50 characters")(50),
];
formFieldValidations["location.addresses.*.streetAddress"] = [
  isNotEmpty("Please provide a valid address or PO Box"),
  isMinSize("The address must be between 5 and 50 characters")(5),
  isMaxSize("The address must be between 5 and 50 characters")(50),
];
formFieldValidations[
  'location.addresses.*.postalCode(location.addresses.*.country.text === "CA")'
] = [isCanadianPostalCode];
formFieldValidations[
  'location.addresses.*.postalCode(location.addresses.*.country.text === "US")'
] = [isUsZipCode];
formFieldValidations[
  'location.addresses.*.postalCode(location.addresses.*.country.text !== "CA" && country.text !== "US")'
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
formFieldValidations["location.contacts.*.locationNames.*.text"] = [
  isNotEmpty("You must select at least one location"),
];
formFieldValidations["location.contacts.*.contactType.text"] = [
  isNotEmpty("You must select at least one contact type"),
];
formFieldValidations["location.contacts.*.firstName"] = [
  isMinSize("Enter a name")(1),
  isMaxSize("Enter a name")(25),
  isNoSpecialCharacters("Enter a name"),
];
formFieldValidations["location.contacts.*.lastName"] = [
  isMinSize("Enter a name")(1),
  isMaxSize("Enter a name")(25),
  isNoSpecialCharacters("Enter a name"),
];
formFieldValidations["location.contacts.*.email"] = [
  isEmail("Please provide a valid email address"),
  isMinSize("Please provide a valid email address")(6),
  isMaxSize("Please provide a valid email address")(50),
];
formFieldValidations["location.contacts.*.phoneNumber"] = [
  isPhoneNumber("Please provide a valid phone number"),
  isMaxSize("Please provide a valid phone number")(15),
  isMinSize("Please provide a valid phone number")(10),
];

export const addValidation = (
  key: string,
  validation: (value: string) => string
): void => {
  if (!formFieldValidations[key]) formFieldValidations[key] = [];
  formFieldValidations[key].push(validation);
};
