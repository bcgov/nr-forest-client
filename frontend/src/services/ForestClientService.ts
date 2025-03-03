import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type {
  ClientDetails,
  CodeDescrType,
  CodeNameType,
  FieldAction,
  FieldUpdateReason,
  UserRole,
} from "@/dto/CommonTypesDto";
import { isNullOrUndefinedOrBlank } from "@/helpers/validators/GlobalValidators";
import * as jsonpatch from "fast-json-patch";
import { unref, type Ref } from "vue";

export const addNewAddress = (addresses: Address[]): number => {
  const blankAddress: Address = {
    locationName: "",
    streetAddress: "",
    country: { value: "", text: "" } as CodeDescrType,
    province: { value: "", text: "" } as CodeDescrType,
    city: "",
    postalCode: "",
    index: addresses.length,
  };

  const newAddresses = addresses.push(blankAddress);
  return newAddresses;
};

export const addNewContact = (contacts: Contact[]): number => {
  const blankContact: Contact = {
    locationNames: [{ value: "", text: "" }] as CodeDescrType[],
    contactType: { value: "", text: "" } as CodeDescrType,
    firstName: "",
    lastName: "",
    phoneNumber: "",
    email: "",
    index: contacts.length,
  };

  const newContacts = contacts.push(blankContact);
  return newContacts;
};

export const getAddressDescription = (
  address: Address,
  index: number,
  entityName = "Address",
): string => (address.locationName.length !== 0 ? address.locationName : `${entityName} #` + index);

export const getContactDescription = (contact: Contact, index: number): string =>
  !isNullOrUndefinedOrBlank(contact.firstName)
    ? `${contact.firstName} ${contact.lastName}`
    : "Contact #" + index;

export const toTitleCase = (inputString: string): string => {
  if (inputString === undefined) return "";

  const splitMapJoin = (currentString: string, separator: string) =>
    currentString
      .split(separator)
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(separator);

  let result = inputString.toLowerCase();
  result = splitMapJoin(result, " ");
  result = splitMapJoin(result, "(");
  result = splitMapJoin(result, ".");
  return result;
};

export const toSentenceCase = (inputString: string | null): string => {
  if (!inputString) return "";
  return inputString.charAt(0).toUpperCase() + inputString.slice(1).toLowerCase();
};

export const codeConversionFn = (code: any) => {
  return {
    value: code.code,
    text: code.name ?? "",
  } as CodeDescrType;
};

export const getEnumKeyByEnumValue = <T extends Record<string, any>>(enumObject: T, enumValue: any): string => {
  const key = Object.keys(enumObject).find((x) => enumObject[x] === enumValue);
  return key ? String(key) : "Unknown";
};

export const getObfuscatedEmail = email => {
  const obfuscatedEmail = email.replace('@', '&#64;');
  return obfuscatedEmail;
};

export const convertFieldNameToSentence = (input: string): string => {
  const lastPart = input.split('.').pop();

  const words = lastPart
                  .replace(/([a-z])([A-Z])/g, '$1 $2') // Insert space between camel case
                  .split(/\s+/);
  
  return words.map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
};

export const adminEmail = "forhvap.cliadmin@gov.bc.ca";

export const getObfuscatedEmailLink = email => {
  return `<a target="_blank" href="mailto:${email}">${getObfuscatedEmail(email)}</a>`;
};

export const getFormattedHtml = ((value: string) => {
  return value ? value.replace(/\n/g, '<br>') : '';
});

export const highlightMatch = (itemName: string, searchTerm: string): string => {
  const trimmedSearchTerm = searchTerm.trim();
  if (!trimmedSearchTerm) return itemName;

  // Escape special characters in the search term
  const escapedSearchTerm = trimmedSearchTerm.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
  const regex = new RegExp(`(${escapedSearchTerm})`, 'i');
  const parts = itemName.split(regex);

  return parts
    .map(part =>
      part.toLowerCase() === trimmedSearchTerm.toLowerCase()
        ? `<span>${part}</span>` 
        : `<strong>${part}</strong>`
    )
    .join('');
};

export const getTagColorByClientStatus = (status: string): string => {
  switch (status) {
    case "Active":
      return "green";
    case "Deactivated":
      return "purple";
    case "Receivership":
      return "magenta";
    case "Suspended":
      return "red";
    case "Deceased":
      return "gray";
    default:
      return "";
  }
};

export const goodStanding = (goodStanding: string): string => {
  if (goodStanding) return goodStanding === "Y" ? "Good standing" : "Not in good standing";
  return "Unknown";
};

export const formatPhoneNumber = (phoneNumber: string): string => {
  if (!phoneNumber) {
    return "";
  }

  const part1 = phoneNumber.slice(0, 3);
  const part2 = phoneNumber.slice(3, 6);
  const part3 = phoneNumber.slice(6);

  return `(${part1}) ${part2}-${part3}`;
};

/**
 * This function should be used only if the roles are considered to be a hierarchy.
 * @param authorities - the array of user roles
 * @returns the highest role the user has.
 */
export const getPrevailingRole = (authorities: string[]): UserRole => {
  const returnValueIfIncluded = (value: UserRole) => (authorities.includes(value) ? value : null);
  return (
    returnValueIfIncluded("CLIENT_ADMIN") ||
    returnValueIfIncluded("CLIENT_SUSPEND") ||
    returnValueIfIncluded("CLIENT_EDITOR") ||
    returnValueIfIncluded("CLIENT_VIEWER")
  );
};

export const includesAnyOf = (haystack: any[], needles: any[]): boolean =>
  !!haystack?.find((item) => needles?.includes(item));

//Fields that require a reason when changing
export const reasonRequiredFields = new Set<string>([
  'clientStatusCode',
  'clientIdentification',
  'clientName',
  'legalFirstName',
  'legalMiddleName',
  'addressOne',
  'addressTwo',
  'addressThree',
  'city',
  'provinceCode',
  'countryCode',
  'postalCode'
]);

// Map for general field actions
const fieldActionMap = new Map<string, string>([
  ['clientIdentification', 'ID'],
  ['clientName', 'NAME'],
  ['legalFirstName', 'NAME'],
  ['legalMiddleName', 'NAME'],
  ['addressOne', 'ADDR'],
  ['addressTwo', 'ADDR'],
  ['addressThree', 'ADDR'],
  ['city', 'ADDR'],
  ['provinceCode', 'ADDR'],
  ['countryCode', 'ADDR'],
  ['postalCode', 'ADDR']
]);

// Map for client status transitions
const statusTransitionMap = new Map<string, string>([
  ['ACT-DEC', 'ACDC'],  // Active → Deceased
  ['ACT-DAC', 'DAC'],   // Active → Deactivated
  ['DEC-ACT', 'RACT'],  // Deceased → Active
  ['REC-ACT', 'RACT'],  // Receivership → Active
  ['DAC-ACT', 'RACT'],  // Deactivated → Active
  ['DAC-REC', ''],      // Deactivated → Receivership (No reason needed)
  ['DEC-REC', ''],      // Deceased → Receivership (No reason needed)
  ['SPN-ACT', 'USPN'],  // Suspended → Active
  ['ACT-SPN', 'SPN'],   // Active → Suspended
  ['REC-SPN', 'SPN'],   // Receivership → Suspended
  ['ACT-REC', ''],      // Active → Receivership (No reason needed)
  ['REC-DAC', 'DAC'],   // Receivership → Deactivated
  ['SPN-DAC', 'DAC'],   // Suspended → Deactivated
  ['DAC-DEC', ''],      // Deactivated → Deceased (No reason needed)
  ['REC-DEC', ''],      // Receivership → Deceased (No reason needed)
  ['SPN-DEC', ''],      // Suspended → Deceased (No reason needed)
  ['DAC-SPN', 'SPN'],   // Deactivated → Suspended
  ['DEC-DAC', 'DAC'],   // Deceased → Deactivated
  ['DEC-SPN', 'SPN'],   // Deceased → Suspended
  ['SPN-REC', '']       // Suspended → Receivership (No reason needed)
]);

// Function to extract required reason fields from patch data
export const extractReasonFields = (
  patchData: jsonpatch.Operation[],
  originalData: ClientDetails,
): FieldAction[] => {
  return patchData
    .filter((patch) => reasonRequiredFields.has(patch.path.replace('/', '')))
    .map((patch) => {
      const field = patch.path.replace('/', '');
      let action = '';

      if (field === 'clientStatusCode') {
        const oldValue = originalData.clientStatusCode;
        const newValue = patch.value;
        const transitionKey = `${oldValue}-${newValue}`;
        action = statusTransitionMap.get(transitionKey) || '';
      } else {
        action = fieldActionMap.get(field) || '';
      }

      return action ? { field, action } : null; 
    })
    .filter(Boolean);
};

export const getAction = (path: string, oldValue?: string, newValue?: string) => {
  const fieldName = path.split('/').pop();

  if (fieldName === 'clientStatusCode' && oldValue && newValue) {
    const transitionKey = `${oldValue}-${newValue}`;
    const transitionAction = statusTransitionMap.get(transitionKey);
    return transitionAction || null;
  }

  return fieldActionMap.get(fieldName) || null;
};

const fieldLabelsByAction = new Map<string, string>([
  ["RACT", 'Reason for "reactivated" status'],
  ["ACDC", 'Reason for "deceased" status'],
  ["DAC", 'Reason for "deactivated" status'],
  ["USPN", 'Reason for "unsuspended" status'],
  ["SPN", 'Reason for "suspended" status'],
  ["ID", "Reason for ID change"],
  ["NAME", "Reason for name change"],
  ["ADDR", "Reason for address change"],
]);

export const getActionLabel = (action: string) => {
  return fieldLabelsByAction.get(action || "") || "Unknown";
};

export const getOldValue = (path: string, data: Ref<ClientDetails> | ClientDetails) => {
  if (!data) {
    console.warn("Old value was called with undefined data!", path);
    return "N/A";
  }

  const clientData = unref(data);
  const fieldName = path.split("/").pop() || "";

  if (fieldName in clientData) {
    return clientData[fieldName as keyof ClientDetails] || "N/A";
  }

  for (const [key, value] of Object.entries(clientData)) {
    if (Array.isArray(value)) {
      for (const item of value) {
        if (fieldName in item) {
          if (Array.isArray(item[fieldName])) {
            const arrayValue = item[fieldName as keyof typeof item];
            return arrayValue?.[0] || "N/A";
          }
          return item[fieldName as keyof typeof item] || "N/A";
        }
      }
    }
  }

  return "N/A";
};

export const updateSelectedReason = (
  selectedOption: CodeNameType,
  index: number,
  patch: jsonpatch.Operation,
  selectedReasons: FieldUpdateReason[],
): void => {
  if (selectedOption) {
    selectedReasons[index] = {
      field: patch.path.replace("/", ""),
      reason: selectedOption.code,
    };
  } else {
    selectedReasons[index] = { field: patch.path.replace("/", ""), reason: "" };
  }
};
