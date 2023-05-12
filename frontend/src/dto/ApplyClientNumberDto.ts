import type { CodeDescrType } from "@/core/CommonTypes";

export interface FormDataDto {
  businessInformation: {
    businessType: string,
    legalType: string,
    clientType: string;
    incorporationNumber: string;
    businessName: null | string;
    goodStandingInd: string;
  },
  location: {
    addresses: Address[];
    contacts: Contact[];
  },
}

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

export const formDataDto: FormDataDto = {
  businessInformation: {
    businessType: "",
    legalType: "",
    clientType: "",
    incorporationNumber: "",
    businessName: "",
    goodStandingInd: "",
  },
  location: {
    addresses: [],
    contacts: [],
  }
};