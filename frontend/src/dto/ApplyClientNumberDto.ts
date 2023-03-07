import type { CodeDescrType } from "@/core/CommonTypes";

export const formDataDto = {
  businessType: {
    clientType: {value: "", text:""} as CodeDescrType,
  },
  businessInformation: {
    firstName: "",
    lastName: "",
    birthdate: "",
    incorporationNumber: "",
    doingBusinessAsName: "",
    businessName: null,
  },
  location: {
    addresses: [] as Address[],
  },
  submitterInformation: {
    firstName: "",
    lastName: "",
    phoneNumber: "",
    email: "",
  },
};

export interface Address {
  streetAddress: string,
  country: CodeDescrType,
  province: CodeDescrType,
  city: string,
  postalCode: string,
  contacts: Contact[],
};

export interface Contact {
  contactType: CodeDescrType,
  firstName: string,
  lastName: string,
  phoneNumber: string,
  emailAddress: string,
};