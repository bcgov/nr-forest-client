import {
  isNotEmpty,
  isMaxSize,
  isMinSize,
  isOnlyNumbers,
  isMinimumYearsAgo,
  isDateInThePast,
  hasOnlyNamingCharacters,
  isIdCharacters,
  isRegex,
  validateSelection,
  formFieldValidations as externalFormFieldValidations,
  validate as globalValidate,
  runValidation as globalRunValidation
} from "@/helpers/validators/GlobalValidators";

/*
Start by grabbing the same validations we use on the external form.
And just change / add what's different.
*/
const fieldValidations: Record<string, ((value: any) => string)[]> = {
  ...externalFormFieldValidations,
};

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
fieldValidations["businessInformation.birthdate"] = [
  isDateInThePast("Date of birth must be in the past"),
  isMinimumYearsAgo(19, "The applicant must be at least 19 years old to apply"),
];

// use the same validations as firstName in contacts
fieldValidations["businessInformation.firstName"] = [
  ...fieldValidations["location.contacts.*.firstName"],
];

fieldValidations["businessInformation.middleName"] = [
  isMaxSizeMsg("middle name", 30),
  hasOnlyNamingCharacters("middle name"),
];

// use the same validations as lastName in contacts
fieldValidations["businessInformation.lastName"] = [
  ...fieldValidations["location.contacts.*.lastName"],
];

// For the input field.
fieldValidations["idType.text"] = [isNotEmpty("You must select an ID type.")];

// For the input field.
fieldValidations["issuingProvince.text"] = [isNotEmpty("You must select a value.")];

// For the form data.
fieldValidations["businessInformation.idType"] = [
  isNotEmpty("You must select an ID type (and related additional fields if any)."),
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
fieldValidations["businessInformation.idNumber"] = [isNotEmpty("You must provide an ID number")];

const extractOtherId = (value: string) => value.split(":")[1]?.trim();

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
      isOnlyNumbers("First Nation status ID should contain only numbers"),
      ...isExactSizMsg("First Nation status ID", idNumberMaskParams.FNID.maxSize),
    ],

    "businessInformation.idNumber-OTHR": [
      isMinSizeMsg("ID number", 3),
      isMaxSizeMsg("ID number", 40),
      isRegex(
        /^[^:]+:\s?[^\s:]+$/,
        'Other identification must follow the pattern: [ID Type] : [ID Value] such as "USA Passport : 12345"',
      ),
      validateSelection(extractOtherId)(
        isIdCharacters("The value to right of the colon can only contain: A-Z or 0-9"),
      ),
      validateSelection(extractOtherId)(isMinSizeMsg("value to right of the colon", 3)),
    ],
  }),
);

export const getIdNumberValidations = (
  key: IdNumberFormFieldValidationKey,
): ((value: any) => string)[] => getValidations(key);

// Step 2: Addresses

// Step 3: Contacts

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
