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
  },
  submitterInformation: {
    userId: string,
    submitterFirstName: string;
    submitterLastName: string;
    submitterPhoneNumber: string;
    submitterEmail: string;
  }
}

export interface Address {
  streetAddress: string;
  country: CodeDescrType;
  province: CodeDescrType;
  city: string;
  postalCode: string;
  contacts: Contact[];
}

export interface Contact {
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
  },
  submitterInformation: {
    userId: "",
    submitterFirstName: "",
    submitterLastName: "",
    submitterPhoneNumber: "",
    submitterEmail: "",
  },
};