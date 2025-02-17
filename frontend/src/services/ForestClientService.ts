import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type { ClientLocation, CodeDescrType, UserRole } from "@/dto/CommonTypesDto";
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

/**
 * Converts location data from ClientLocation format to Address format, as required by the
 * StaffLocationGroupComponent, first developed for the staff create form.
 *
 * @param location - ClientLocation formatted data
 * @returns Address data
 */
export const locationToCreateFormat = (location: ClientLocation): Address => {
  const address: Address = {
    streetAddress: location.addressOne,
    complementaryAddressOne: location.addressTwo,
    complementaryAddressTwo: location.addressThree,
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
    index: Number(location.clientLocnCode),
    locationName: location.clientLocnName,
  };
  return address;
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
  const location: ClientLocation = {
    ...baseLocation,
    clientLocnName: address.locationName,
    clientLocnCode: String(address.index).padStart(2, "0"),
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

  return location;
};
