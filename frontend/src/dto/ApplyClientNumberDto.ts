import type { CodeDescrType, IdentificationCodeDescrType } from "@/dto/CommonTypesDto";

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
  faxNumber?: string;
  emailAddress?: string;
  notes?: string;
  index: number;
  locationName: string;
}

export interface Contact {
  contactType: CodeDescrType;
  firstName: string;
  lastName: string;
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
}

export interface ForestClientDetailsDto {
  name: string;
  id: string;
  goodStanding: boolean;
  addresses: Address[];
  contacts: Contact[];
}

export const indexedEmptyAddress = (index: number): Address =>
  JSON.parse(
    JSON.stringify({
      locationName: "",
      complementaryAddressOne: "",
      complementaryAddressTwo: null,
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

export const emptyAddress = (): Address => indexedEmptyAddress(0);

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

export const locationName = { value: "0", text: "Mailing address" };

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
    identificationType: {value: "", text: "", countryCode: ""}
  },
  location: {
    addresses: [emptyAddress()],
    contacts: [],
  },
};

export const emptyContact: Contact = {
  locationNames: [],
  contactType: { value: "", text: "" },
  firstName: "",
  lastName: "",
  phoneNumber: "",
  secondaryPhoneNumber: "",
  faxNumber: "",
  email: "",
  index: 0,
};

export const newFormDataDto = (): FormDataDto => JSON.parse(JSON.stringify(formDataDto));

export const newFormDataDtoExternal = (): FormDataDto => {
  const value = newFormDataDto();
  value.location.addresses[0].locationName = locationName.text;
  return value;
};
