import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type {
  ClientContact,
  ClientDetails,
  ClientLocation,
  ClientSearchResult,
  CodeDescrType,
  CodeNameType,
  FieldAction,
  FieldReason,
  RelatedClientEntry,
  UserRole,
} from "@/dto/CommonTypesDto";
import { isNullOrUndefinedOrBlank } from "@/helpers/validators/GlobalValidators";
import * as jsonpatch from "fast-json-patch";

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
): string => address.locationName || `${entityName} #` + index;

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
  const trimmedSearchTerm = searchTerm?.trim();
  if (!trimmedSearchTerm) return itemName;

  // Escape special characters in the search term
  const escapedSearchTerm = trimmedSearchTerm.replace(/[-/\\^$*+?.()|[\]{}]/g, "\\$&");
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

export const keepOnlyNumbersAndLetters = (input: string): string => {
  const result = input.replaceAll(/[^A-Za-z0-9]/g, "");
  return result;
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
  'clientIdTypeCode',
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

export const fieldActionMap: { [key: string]: string[] } = {
  'clientIdTypeCode': ['ID'],
  'clientIdTypeDesc': ['ID'],
  'clientIdentification': ['ID'],
  'clientName': ['NAME'],
  'legalFirstName': ['NAME'],
  'legalMiddleName': ['NAME'],
  'fullName': ['NAME'],
  'address': ['ADDR'],
  'addressOne': ['ADDR'],
  'addressTwo': ['ADDR'],
  'addressThree': ['ADDR'],
  'city': ['ADDR'],
  'provinceCode': ['ADDR'],
  'countryCode': ['ADDR'],
  'provinceDesc': ['ADDR'],
  'countryDesc': ['ADDR'],
  'postalCode': ['ADDR'],
  'zipCode': ['ADDR'],
  'clientStatusDesc': ['ACDC', 'DAC', 'RACT', 'USPN', 'SPN'],
};

// Map for client status transitions
const statusTransitionMap = new Map<string, string>([
  ['ACT-DEC', ''],      // Active → Deceased (No reason needed)
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

/**
 * Extracts the last segment of the path which corresponds to the specific field name.
 *
 * @param path
 * @returns string
 */
export const extractFieldName = (path: string): string => {
  const fieldName = path.split("/").slice(-1)[0];
  return fieldName;
};

/**
 * Extracts the field path to be sent in the reason patch.
 * It might be a specific field or the parent field where the change occurred.
 *
 * @param path
 * @returns string
 */
export const extractActionField = (path: string, action: string): string => {
  if (action === "ID") {
    // virtual field representing multiple fields
    return "/client/id";
  }

  if (action === "NAME") {
    // virtual field representing multiple fields
    return "/client/name";
  }

  if (path.startsWith("/addresses")) {
    const fieldName = extractFieldName(path);

    // Should return something like "/addresses/00"
    const fieldKey = path.split(`/${fieldName}`)[0];
    return fieldKey;
  }

  return path;
};

// Function to extract required reason fields from patch data
export const extractReasonFields = (
  patchData: jsonpatch.Operation[],
  originalData: ClientDetails,
): FieldAction[] => {
  const reasonActions: Record<string, FieldAction> = {};
  patchData
    .filter(
      (patch) => patch.op === "replace" && reasonRequiredFields.has(extractFieldName(patch.path)),
    )
    .forEach((patch) => {
      const fieldName = extractFieldName(patch.path);

      let action = '';

      if (fieldName === 'clientStatusCode') {
        const oldValue = originalData.client.clientStatusCode;
        const newValue = patch.value;
        const transitionKey = `${oldValue}-${newValue}`;
        action = statusTransitionMap.get(transitionKey) || '';
      } else {
        action = fieldActionMap[fieldName]?.[0] || '';
      }

      if (reasonActions[action]) {
        // If the field corresponds to a "group change", like an address, we only need it once.
        return;
      }

      if (action) {
        const actionField = extractActionField(patch.path, action);
        reasonActions[action] = { field: actionField, action };
      }
    });

  return Object.values(reasonActions);
};

export const getAction = (path: string, oldValue?: string, newValue?: string): string[] | string | null => {
  const fieldName = path.split('/').pop();

  if (fieldName === 'clientStatusCode' && oldValue && newValue) {
    const transitionKey = `${oldValue}-${newValue}`;
    const transitionAction = statusTransitionMap.get(transitionKey);
    return transitionAction || null;
  }

  return fieldActionMap[fieldName] || null;
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

export const updateSelectedReason = (
  selectedOption: CodeNameType,
  index: number,
  patch: jsonpatch.Operation,
  selectedReasons: FieldReason[],
): void => {
  if (selectedOption) {
    selectedReasons[index] = {
      field: extractFieldName(patch.path),
      reason: selectedOption.code,
    };
  } else {
    selectedReasons[index] = { field: extractFieldName(patch.path), reason: "" };
  }
};

export const removeNullText = (text: string | null): string | null => {
  if (!text || text === 'null') {
    return null;
  }
  return text;
};

export const extractAddressParts = (
  location: ClientLocation,
): {
  streetAddress: string
  complementaryAddressOne: string | null
  complementaryAddressTwo: string | null
} => {
  const { addressOne, addressTwo, addressThree } = location

  if (removeNullText(addressTwo)) {
    return {
      streetAddress: removeNullText(addressThree) ?? addressTwo,
      complementaryAddressOne: addressOne,
      complementaryAddressTwo: removeNullText(addressThree) ? addressTwo : null,
    }
  }
  return {
    streetAddress: addressOne,
    complementaryAddressOne: addressTwo,
    complementaryAddressTwo: addressThree,
  }
};


/**
 * Converts location data from ClientLocation format to Address format, as required by the
 * StaffLocationGroupComponent, first developed for the staff create form.
 *
 * @param location - ClientLocation formatted data
 * @returns Address data
 */
export const locationToCreateFormat = (location: ClientLocation): Address => {
  const index = location.clientLocnCode !== null ? Number(location.clientLocnCode) : null;
  const { streetAddress, complementaryAddressOne, complementaryAddressTwo } =
    extractAddressParts(location)

  const address: Address = {
    streetAddress,
    complementaryAddressOne,
    complementaryAddressTwo,
    country: {
      value: location.countryCode,
      text: location.countryDesc,
    },
    province: {
      value: location.provinceCode,
      text: location.provinceDesc,
    },
    city: location.city,
    postalCode: location.postalCode,
    businessPhoneNumber: formatPhoneNumber(location.businessPhone),
    secondaryPhoneNumber: formatPhoneNumber(location.cellPhone),
    tertiaryPhoneNumber: formatPhoneNumber(location.homePhone),
    faxNumber: formatPhoneNumber(location.faxNumber),
    emailAddress: location.emailAddress,
    notes: location.cliLocnComment,
    index,
    locationName: location.clientLocnName,
  };
  return address;
};

export const indexToLocationCode = (index: number): string => {
  return index !== null ? String(index).padStart(2, "0") : null;
};

const reorderAddresses = (location: ClientLocation): ClientLocation => {
  const { addressOne, addressTwo, addressThree, ...rest } = location;

  let newAddressOne = addressOne;
  let newAddressTwo: string | null = null;
  let newAddressThree: string | null = null;

  if (addressTwo) {
    // Move addressTwo to position 1
    newAddressOne = addressTwo;
    newAddressTwo = addressThree ?? addressOne;
    newAddressThree = addressThree ? addressOne : null;
  }

  return {
    addressOne: newAddressOne,
    addressTwo: newAddressTwo,
    addressThree: newAddressThree,
    ...rest,
  };
};

/**
 * Converts location data from Address format to ClientLocation format, as required by the Patch
 * API.
 * Note: data which don't exist in the Address format can be provided in the baseLocation
 * parameter.
 *
 * @param address - Address formatted data
 * @param baseLocation - ClientLocation data to fulfill data non-existent in the Address format
 * @returns ClientLocation data
 */
export const locationToEditFormat = (
  address: Address,
  baseLocation: ClientLocation,
): ClientLocation => {
  let location: ClientLocation = {
    ...baseLocation,
    clientLocnName: address.locationName,
    clientLocnCode: indexToLocationCode(address.index),
    addressOne: address.streetAddress,
    addressTwo: address.complementaryAddressOne,
    addressThree: address.complementaryAddressTwo,
    city: address.city,
    provinceCode: address.province.value,
    provinceDesc: address.province.text,
    postalCode: address.postalCode,
    countryCode: address.country.value,
    countryDesc: address.country.text,
    businessPhone: keepOnlyNumbersAndLetters(address.businessPhoneNumber),
    homePhone: keepOnlyNumbersAndLetters(address.tertiaryPhoneNumber),
    cellPhone: keepOnlyNumbersAndLetters(address.secondaryPhoneNumber),
    faxNumber: keepOnlyNumbersAndLetters(address.faxNumber),
    emailAddress: address.emailAddress,
    cliLocnComment: address.notes,
  };

  location = reorderAddresses(location);

  return location;
};

/**
 * Converts contact data from ClientContact format to Contact format, as required by the
 * StaffContactGroupComponent, first developed for the staff create form.
 *
 * @param clientContact - ClientContact formatted data
 * @param associatedLocations - list of locations associated to this contact
 * @returns Contact data
 */
export const contactToCreateFormat = (
  clientContact: ClientContact,
  allLocations: ClientLocation[],
): Contact => {
  const contact: Contact = {
    contactType: {
      value: clientContact.contactTypeCode,
      text: clientContact.contactTypeDesc,
    },
    fullName: clientContact.contactName,
    phoneNumber: formatPhoneNumber(clientContact.businessPhone),
    secondaryPhoneNumber: formatPhoneNumber(clientContact.secondaryPhone),
    faxNumber: formatPhoneNumber(clientContact.faxNumber),
    email: clientContact.emailAddress,
    index: clientContact.contactId,
    locationNames: allLocations
      .filter((location) => clientContact.locationCodes.includes(location.clientLocnCode))
      .map((location) => ({
        value: location.clientLocnCode,
        text: location.clientLocnName,
      })),
  };

  return contact;
};

/**
 * Converts contac data from Contact format to ClientContact format, as required by the Patch
 * API.
 * Note: data which don't exist in the Contact format can be provided in the baseContact
 * parameter.
 *
 * @param contact - Contact formatted data
 * @param baseContact - ClientContact data to fulfill data non-existent in the Contact format
 * @returns ClientContact data
 */
export const contactToEditFormat = (
  contact: Contact,
  baseContact: ClientContact,
): ClientContact => {
  const clientContact: ClientContact = {
    ...baseContact,
    contactId: contact.index,
    locationCodes: contact.locationNames.map((item) => item.value),
    contactName: contact.fullName,
    contactTypeCode: contact.contactType.value,
    contactTypeDesc: contact.contactType.text,
    businessPhone: keepOnlyNumbersAndLetters(contact.phoneNumber),
    secondaryPhone: keepOnlyNumbersAndLetters(contact.secondaryPhoneNumber),
    faxNumber: keepOnlyNumbersAndLetters(contact.faxNumber),
    emailAddress: contact.email,
  };

  return clientContact;
};

/**
 * Keeps the scrollbar at its current bottom position while the supplied promise resolves.
 *
 * What does this mean? This function should be used when the content on the viewport is about to
 * shrink, specially when the user just sees the bottom of such content.
 * The goal is to make the bottom of such content remain at the same Y position, kind of similarly
 * to what the overflow-anchor CSS property is able to do while the content grows, so as to avoid
 * scroll jumping in unexpected ways.
 *
 * @param uiUpdatePromise - The promise whose resolution should trigger the scroll fix.
 */
export const keepScrollBottomPosition = (uiUpdatePromise: Promise<void>): void => {
  const app = document.getElementById("app");
  const lastHeightFromBottom = app.scrollHeight - window.scrollY;

  uiUpdatePromise.then(() => {
    window.scrollTo({ top: app.scrollHeight - lastHeightFromBottom });
  });
};

export const formatLocation = (code: string, name: string): string => {
  if (!code && !name) {
    return "New location";
  }

  // keep only non-empty parts
  const parts = [code, name].filter(Boolean);

  const title = parts.join(" - ");

  return title;
};

const columnNameToLabelMap: Record<string, string> = {
  fullName: "Full name",
  clientTypeDesc: "Client type",
  clientAcronym: "Acronym",
  birthdate: "Date of birth",
  clientIdTypeDesc: "ID type",
  clientIdentification: "ID number",
  clientStatusDesc: "Client status",
  corpRegnNmbr: "Registration number",
  wcbFirmNumber: "WorkSafeBC number",
  doingBusinessAs: "Doing business as",
  locnExpiredInd: "Location status",
  clientComment: "Notes",
  locationName: "Location name",
  address: "Address",
  addressOne: "Street address or PO box",
  addressTwo: "Delivery information",
  addressThree: "Additional delivery information",
  cityProvinceDesc: "City and province, state or territory",
  city: "City",
  provinceDesc: "Province or territory",
  stateDesc: "State",
  countryDesc: "Country",
  postalCode: "Postal code",
  zipCode: "Zip code",
  trustLocationInd: "Trust location",
  contactTypeDesc: "Contact type",
  associatedLocation: "Associated location",
  contactName: "Full name",
  emailAddress: "Email address",
  businessPhone: "Primary phone number",
  secondaryPhone: "Secondary phone number",
  homePhone: "Tertiary phone number",
  cellPhone: "Secondary phone number",
  cliLocnComment: "Notes",
  faxNumber: "Fax",
  returnedMailDate: "Returned mail date",
  primaryClient: "Primary client",
  primaryClientLocation: "Primary client location",
  relationshipType: "Relationship type",
  relatedClient: "Related client",
  relatedClientLocation: "Related client location",
  signingAuthInd: "Signing auth",
  percentOwnership: "Percent ownership"
};

export function getLabelByColumnName(columnName: string): string {
  return columnNameToLabelMap[columnName] ?? columnName;
};

export function removePrefix(input?: string | null): string {
  if (!input) return '';
  return input.split('\\').pop() || '';
};

export const formatDatetime = (timestamp?: Date | string | number | null): string => {
  if (!timestamp) return '';

  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return '';

  const options: Intl.DateTimeFormatOptions = {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
  };

  const datePart = date.toLocaleDateString('en-US', options);
  const timePart = date.toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });

  return `${datePart}  ${timePart}`;
};

const dateFormatter = new Intl.DateTimeFormat("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    timeZone: "UTC",
    hour12: false,
});

export const formatDate = (timestamp?: Date | string | number | null): string => {
  if (!timestamp) return '';

  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return '';

  return dateFormatter.format(date);
};

/**
 * Adjusts the data based on the original values so as to prevent changing any field from null to
 * empty string (or vice-versa).
 *
 * Also replaces newly cleared values from empty string to null.
 */
export const preserveUnchangedData = <T>(data: T, original: T): T => {
  const dataClone = JSON.parse(JSON.stringify(data));
  for (const key in dataClone) {
    if ([null, ""].includes(dataClone[key] as any)) {
      dataClone[key] = null; // default empty value

      if (original && [null, ""].includes(original[key] as any)) {
        // copy original empty value
        dataClone[key] = original[key];
      }

      continue;
    }

    if (Array.isArray(dataClone[key])) {
      // skip - keep as is
      continue;
    }

    if (typeof dataClone[key] === "object") {
      // recursive
      dataClone[key] = preserveUnchangedData(dataClone[key], original[key]);
    }
  }

  return dataClone;
};

export const formatAddress = (location: ClientLocation): string => {
  const { city, provinceCode, countryDesc, postalCode } = location;

  const { streetAddress } = extractAddressParts(location);

  const list = [streetAddress, city, provinceCode, countryDesc, postalCode];
  return list.join(", ");
};

export const compareAny = <T>(a: T, b: T): number => {
  if (a < b) {
    return -1;
  }
  if (a > b) {
    return 1;
  }
  return 0;
};

export const booleanToYesNo = (
  value: boolean,
  options = { empty: "-", defaultToFalse: false },
): string => {
  if (value === true) {
    return "Yes";
  }
  if (value === false || options.defaultToFalse) {
    return "No";
  }
  return options.empty;
};

export const searchResultToText = (searchResult: ClientSearchResult): string => {
  const { clientNumber, clientFullName, clientType, city } = searchResult;
  const result = toTitleCase(`${clientNumber}, ${clientFullName}, ${clientType}, ${city}`);
  return result;
};

/**
 * Builds a value that works as an index for the related client, by joining the location code and
 * the index of the relationship within that location.
 *
 * @param locationCode - The location's code
 * @param relationshipIndex - The index of the relationship within that location
 * @returns an index for the related client.
 */
export const buildRelatedClientIndex = (
  locationCode: string,
  relationshipIndex: number | string,
): string => {
  const uniqueIndex = [locationCode, relationshipIndex].join(",");
  return uniqueIndex;
};

/**
 * Builds a string value that holds the combination of properties that must be unique in a client relationship.
 * @param entry - The client relationship
 * @returns a string value that can uniquely identify a relationship.
 */
export const buildRelatedClientCombination = (entry: RelatedClientEntry): string => {
  const value = [
    entry.client.client?.code,
    entry.client.location?.code,
    entry.relationship?.code,
    entry.relatedClient.client?.code,
    entry.relatedClient.location?.code,
  ].join(",");

  return value;
};

export const isLocationExpired = (location: ClientLocation): boolean =>
  location.locnExpiredInd === "Y";
