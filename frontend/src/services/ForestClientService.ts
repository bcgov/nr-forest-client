import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type { CodeDescrType } from "@/dto/CommonTypesDto";
import { isNullOrUndefinedOrBlank } from "@/helpers/validators/GlobalValidators";

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

export const toSentenceCase = (inputString: string): string => {
  if (inputString === undefined) return "";
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
