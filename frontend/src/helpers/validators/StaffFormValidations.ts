import {
  isNotEmpty,
  isMaxSize,
  isMinSize,
  isOnlyNumbers,
  isMinimumYearsAgo,
  isDateInThePast,
  hasOnlyNamingCharacters,
  isIdCharacters,
  isExactSize,
  formFieldValidations as externalFormFieldValidations,
  validate as globalValidate,
  runValidation as globalRunValidation,
  isAscii,
  isEmail,
  isPhoneNumber,
  optional,
  isAsciiLineBreak,
  isNotEmptyArray,
} from "@/helpers/validators/GlobalValidators";

// Allow externalFormFieldValidations to get populated
import "@/helpers/validators/BCeIDFormValidations";

/*
Start by grabbing the same validations we use on the external form.
And just change / add what's different.
*/
const fieldValidations: Record<string, ((value: any) => string)[]> = {
  ...externalFormFieldValidations,
};

// This function will return all validators for the field
export const getValidations = (key: string): ((value: any) => string)[] =>
  key ? fieldValidations[key] || [] : [];

const isMinSizeMsg = (fieldName: string, minSize: number) =>
  isMinSize(`The ${fieldName} must contain at least ${minSize} characters`)(minSize);

const isMaxSizeMsg = (fieldName: string, maxSize: number) =>
  isMaxSize(`The ${fieldName} has a ${maxSize} character limit`)(maxSize);

const isExactSizMsg = (fieldName: string, size: number) => 
  isExactSize(`The ${fieldName} must contain ${size} characters`)(size);

// Step 1: Business Information
fieldValidations["businessInformation.clientType"] = [
  isNotEmpty("You must select a client type."),
];

fieldValidations["businessInformation.birthdate"] = [
  isDateInThePast("Date of birth must be in the past"),
  isMinimumYearsAgo(19, "The applicant must be at least 19 years old to apply"),
];

// use the same validations as firstName in contacts
fieldValidations["businessInformation.firstName"] = [
  isMinSize("Please enter the first name")(1),
  isMaxSizeMsg("first name", 30),
  hasOnlyNamingCharacters("first name"),
];

fieldValidations["businessInformation.middleName"] = [
  isMaxSizeMsg("middle name", 30),
  hasOnlyNamingCharacters("middle name"),
];

// use the same validations as lastName in contacts
fieldValidations["businessInformation.lastName"] = [
  isMinSize("Please enter the last name")(1),
  isMaxSizeMsg("last name", 30),
  hasOnlyNamingCharacters("last name"),
];

fieldValidations["businessInformation.businessName"] = [
  isNotEmpty("Client name cannot be empty"),
  isMaxSizeMsg("client name", 60),
  isAscii("client name"),
];

fieldValidations["businessInformation.workSafeBcNumber"] = [
  optional(isOnlyNumbers("WorkSafeBC number should contain only numbers")),
  optional(isMaxSizeMsg("WorkSafeBC", 6))
];

fieldValidations["businessInformation.doingBusinessAs"] = [  
  optional(isMaxSizeMsg("doing business as", 120)),
  optional(isAscii("doing business as")),
];

fieldValidations["businessInformation.clientAcronym"] = [  
  optional(isMaxSizeMsg("acronym", 8)),
  optional(isAscii("acronym")),
];

// For the input field.
fieldValidations["identificationType.text"] = [isNotEmpty("You must select an ID type.")];

// For the input field.
fieldValidations["identificationProvince.text"] = [isNotEmpty("You must select a value.")];

// For the form data.
fieldValidations["businessInformation.identificationType"] = [
  isNotEmpty("You must select an ID type (and related additional fields if any)."),
];

interface ClientIdentificationValidation {
  maxSize: number;
  onlyNumbers?: boolean;
}

/*
Variable defined using an IIFE to allow an easy definition of type-checkable keys.
*/
export const clientIdentificationMaskParams = (() => {
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
  };
  return init as Record<keyof typeof init, ClientIdentificationValidation | undefined>;
})();

type ClientIdentificationFormFieldValidationKey =
  `businessInformation.clientIdentification-${keyof typeof clientIdentificationMaskParams}`;

// clientIdentification base validations - applied regardless of the ID type / province.
fieldValidations["businessInformation.clientIdentification"] = [
  isNotEmpty("You must provide an ID number"),
];

fieldValidations["businessInformation.clientTypeOfId"] = [
  isNotEmpty("You must provide a type of ID"),
  isMaxSizeMsg("Type of ID", 38), //40 - 1 colon - at least 1 from ID number
  hasOnlyNamingCharacters("Type of ID")
];

fieldValidations["businessInformation.clientIdNumber"] = [
  isNotEmpty("You must provide an ID number"),
  isMaxSizeMsg("ID number", 38), //40 - 1 colon - at least 1 from Type of ID
  hasOnlyNamingCharacters("ID number")
];

fieldValidations["businessInformation.clientIdentification-BCDL"] = [
  isOnlyNumbers("BC driver's licence should contain only numbers"),
  isMinSizeMsg("BC driver's licence", 7),
  isMaxSizeMsg("BC driver's licence", clientIdentificationMaskParams.BCDL.maxSize),
];

fieldValidations["businessInformation.clientIdentification-nonBCDL"] = [      
  isIdCharacters(),
  isMinSizeMsg("driver's licence", 7),
  isMaxSizeMsg("driver's licence", clientIdentificationMaskParams.nonBCDL.maxSize),
];

fieldValidations["businessInformation.clientIdentification-BRTH"] = [
  isOnlyNumbers("Canadian birth certificate should contain only numbers"),
  isMinSizeMsg("Canadian birth certificate", 12),
  isMaxSizeMsg("Canadian birth certificate", clientIdentificationMaskParams.BRTH.maxSize),
];

fieldValidations["businessInformation.clientIdentification-PASS"] = [
  isIdCharacters(),
  isExactSizMsg("Canadian passport", clientIdentificationMaskParams.PASS.maxSize),
];

fieldValidations["businessInformation.clientIdentification-CITZ"] = [
  isIdCharacters(),
  isExactSizMsg("Canadian citizenship card", clientIdentificationMaskParams.CITZ.maxSize),
];

fieldValidations["businessInformation.clientIdentification-FNID"] = [
  isOnlyNumbers("First Nation status ID should contain only numbers"),
  isExactSizMsg("First Nation status ID", clientIdentificationMaskParams.FNID.maxSize),
];

export const getClientIdentificationValidations = (
  key: ClientIdentificationFormFieldValidationKey,
): ((value: any) => string)[] => getValidations(key);

// Step 2: Locations

fieldValidations["location.addresses.*.complementaryAddressOne"] = [
  isMaxSizeMsg("delivery information", 40),
  isAscii("delivery information"),
];

fieldValidations["location.addresses.*.complementaryAddressTwo"] = [
  isMaxSizeMsg("additional delivery information", 40),
  isAscii("additional delivery information"),
];

fieldValidations["location.addresses.*.emailAddress"] = [
  optional(isEmail("Please provide a valid email address")),
  optional(isMinSize("Please provide a valid email address")(6)),
  isMaxSizeMsg("email address", 100),
];

const phoneValidations = [
  optional(isPhoneNumber("Please provide a valid phone number")),
  optional(isMinSize("Please provide a valid phone number")(10)),
  isMaxSizeMsg("phone number", 14),
];

fieldValidations["location.addresses.*.businessPhoneNumber"] = [...phoneValidations];
fieldValidations["location.addresses.*.secondaryPhoneNumber"] = [...phoneValidations];
fieldValidations["location.addresses.*.faxNumber"] = [...phoneValidations];

fieldValidations["location.addresses.*.notes"] = [
  isMaxSizeMsg("notes", 4000),
  isAsciiLineBreak("notes"),
];

// Step 3: Contacts
fieldValidations["location.contacts.*.locationNames.text"] = [
  isNotEmptyArray("You must select at least one location"),
];
fieldValidations["location.contacts.*.contactType.text"] = [
  isNotEmpty("You must select a contact type."),
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

fieldValidations["location.contacts.*.emailAddress"] = [
  optional(isEmail("Please provide a valid email address")),
  optional(isMinSize("Please provide a valid email address")(6)),
  isMaxSizeMsg("email address", 100),
];

fieldValidations["location.contacts.*.phoneNumber"] = [...phoneValidations];

fieldValidations["location.contacts.*.secondaryPhoneNumber"] = [...phoneValidations];

fieldValidations["location.contacts.*.faxNumber"] = [...phoneValidations];

// Step 4: Review
fieldValidations["businessInformation.notes"] = [
  isMaxSizeMsg("notes", 4000),
  isAsciiLineBreak("notes"),
];

// General information

export const addValidation = (key: string, validation: (value: string) => string): void => {
  if (!fieldValidations[key]) fieldValidations[key] = [];
  fieldValidations[key].push(validation);
};

const defaultGetValidations = getValidations;

export const validate = (
  ...args: Parameters<typeof globalValidate>
): ReturnType<typeof globalValidate> => {  
  const getValidations = args[3] || defaultGetValidations;
  args[3] = getValidations;
  return globalValidate.apply(this, args);
};

export const runValidation = globalRunValidation;
