import type { CodeDescrType, IdentificationCodeDescrType } from "@/dto/CommonTypesDto";
import { toSentenceCase } from "@/services/ForestClientService";

export interface Address {
  streetAddress: string;
  complementaryAddressOne?: string;
  complementaryAddressTwo?: string;
  country: CodeDescrType;
  province: CodeDescrType;
  city: string;
  postalCode: string;
  businessPhoneNumber?: string;
  secondaryPhoneNumber?: string;
  tertiaryPhoneNumber?: string; // only for the Client View/Edit page
  faxNumber?: string;
  emailAddress?: string;
  notes?: string;
  index: number;
  locationName: string;
}

export interface Contact {
  contactType: CodeDescrType;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  phoneNumber: string;
  secondaryPhoneNumber?: string;
  faxNumber?: string;
  email: string;
  index: number;
  locationNames: CodeDescrType[];
}

export interface FormDataDto {
  businessInformation: {
    registrationNumber: string;
    businessName: string;
    businessType: string;
    clientType: string;
    goodStandingInd: string;
    legalType: string;
    birthdate: string;
    district: string;
    workSafeBcNumber?: string;
    doingBusinessAs?: string;
    clientAcronym?: string;
    firstName?: string;
    middleName?: string;
    lastName?: string;
    notes?: string;
    identificationType?: IdentificationCodeDescrType;
    clientIdentification?: string;
    identificationCountry?: string;
    identificationProvince?: string;
  };
  location: {
    addresses: Address[];
    contacts: Contact[];
  };
  notifyClientInd?: string;
}

export interface ForestClientDetailsDto {
  name: string;
  id: string;
  goodStanding: boolean;
  addresses: Address[];
  contacts: Contact[];
  isOwnedByPerson: boolean;
}

export interface FirstNationDetailsDto {
  id: string;
  name: string;
  goodStanding: boolean;
  clientType: string;
  addresses: Address[];
}

export const indexedEmptyAddress = (index: number): Address =>
  JSON.parse(
    JSON.stringify({
      locationName: "",
      complementaryAddressOne: "",
      complementaryAddressTwo: null, // hides the input field and displays the button to add it
      streetAddress: "",
      country: { value: "CA", text: "Canada" },
      province: { value: "BC", text: "British Columbia" },
      city: "",
      postalCode: "",
      businessPhoneNumber: "",
      secondaryPhoneNumber: "",
      faxNumber: "",
      emailAddress: "",
      notes: "",
      index,
    }),
  );

export const defaultLocation = { value: "0", text: "Mailing address" };

export const defaultContactType = {
  value: "BL",
  text: "Billing",
};

export const emptyAddress = (): Address => indexedEmptyAddress(0);

export const formatAddresses = (addresses: Address[]): Address[] => {
  if (addresses && addresses.length > 0) {
    return addresses.map(address => ({
      ...address,
      locationName: toSentenceCase(address.locationName),
    }));
  }
  return [{ ...emptyAddress(), locationName: defaultLocation.text }];
};

export const indexedEmptyContact = (index: number): Contact =>
  JSON.parse(
    JSON.stringify({
      locationNames: [],
      contactType: { value: "", text: "" },
      firstName: "",
      lastName: "",
      phoneNumber: "",
      secondaryPhoneNumber: "",
      faxNumber: "",
      email: "",
      index,
    }),
  );

export const emptyContact: Contact = indexedEmptyContact(0);

export const formDataDto: FormDataDto = {
  businessInformation: {
    district: "",
    businessType: "",
    legalType: "",
    clientType: "",
    registrationNumber: "",
    businessName: "",
    goodStandingInd: "",
    birthdate: "",
    notes: "",
    identificationType: {value: "", text: "", countryCode: ""},
    workSafeBcNumber: "",
    doingBusinessAs: "",
    clientAcronym: "",
    firstName: "",
    middleName: "",
    lastName: "",    
    clientIdentification: "",
    identificationCountry: "",
    identificationProvince: "",
  },
  location: {
    addresses: [emptyAddress()],
    contacts: [],
  },
  notifyClientInd: ""
};

export const newFormDataDto = (): FormDataDto => {
  const value = JSON.parse(JSON.stringify(formDataDto));
  value.location.addresses[0].locationName = defaultLocation.text;
  return value;
};
