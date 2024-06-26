import type { CodeDescrType } from "@/dto/CommonTypesDto";

export interface Address {
  locationName: string;
  streetAddress: string;
  country: CodeDescrType;
  province: CodeDescrType;
  city: string;
  postalCode: string;
}

export interface Contact {
  locationNames: CodeDescrType[];
  contactType: CodeDescrType;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  email: string;
}

export interface FormDataDto {
  businessInformation: {
    district: string;
    businessType: string;
    legalType: string;
    clientType: string;
    registrationNumber: string;
    businessName: string;
    goodStandingInd: string;
    birthdate: string;
    address: Address;
    firstName?: string;
    middleName?: string;
    lastName?: string;
    identificationType?: string;
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
    address: {
      locationName: "",
      streetAddress: "",
      country: { value: "", text: "" },
      province: { value: "", text: "" },
      city: "",
      postalCode: "",
    },
  },
  location: {
    addresses: [
      {
        locationName: locationName.text,
        streetAddress: "",
        country: { value: "CA", text: "Canada" },
        province: { value: "BC", text: "British Columbia" },
        city: "",
        postalCode: "",
      },
    ],
    contacts: [],
  },
};

export const emptyAddress = (): Address =>
  JSON.parse(
    JSON.stringify({
      locationName: "",
      streetAddress: "",
      country: { value: "CA", text: "Canada" },
      province: { value: "BC", text: "British Columbia" },
      city: "",
      postalCode: "",
    })
  );

export const emptyContact: Contact = {
  locationNames: [],
  contactType: { value: "", text: "" },
  firstName: "",
  lastName: "",
  phoneNumber: "",
  email: "",
};

export const newFormDataDto = (): FormDataDto =>
  JSON.parse(JSON.stringify(formDataDto));
