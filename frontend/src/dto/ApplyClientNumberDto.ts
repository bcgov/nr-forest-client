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
    businessType: string;
    legalType: string;
    clientType: string;
    incorporationNumber: string;
    businessName: string;
    goodStandingInd: string;
    birthDate: string;
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

export const formDataDto: FormDataDto = {
  businessInformation: {
    businessType: "",
    legalType: "",
    clientType: "",
    incorporationNumber: "",
    businessName: "",
    goodStandingInd: "",
    birthDate: "",
  },
  location: {
    addresses: [
      {
        locationName: "Mailing address",
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
