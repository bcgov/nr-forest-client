import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type { CodeDescrType } from "@/dto/CommonTypesDto";

export const addNewAddress = (addresses: Address[]): number => {
  const blankAddress: Address = {
    locationName: "",
    streetAddress: "",
    country: { value: "", text: "" } as CodeDescrType,
    province: { value: "", text: "" } as CodeDescrType,
    city: "",
    postalCode: "",
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
  };

  const newContacts = contacts.push(blankContact);
  return newContacts;
};

export const toTitleCase = (inputString: string): string => {
  if (inputString === undefined) return "";
  return inputString
    .toLowerCase()
    .split(" ")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
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

export const getMailtoLink = email => {
  const encodedEmail = encodeURIComponent(email);
  return 'mailto:' + encodedEmail;
};

export const openMailtoLink = (email) => {
  const encodedEmail = encodeURIComponent(email);
  const mailtoLink = 'mailto:' +  encodedEmail;
  location.assign(mailtoLink);
}

export const convertFieldNameToSentence = (input: string): string => {
  const lastPart = input.split('.').pop();

  const words = lastPart
                  .replace(/([a-z])([A-Z])/g, '$1 $2') // Insert space between camel case
                  .split(/\s+/);
  
  return words.map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
};
