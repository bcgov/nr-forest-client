import type { CodeDescrType } from "@/core/CommonTypes";

export interface FormDataDto {
  businessInformation: {
    businessType: string,
    clientType: CodeDescrType;
    incorporationNumber: string;
    businessName: null | string;
    goodStanding: string;
    firstName: string;
    lastName: string;
    birthdate: string;
    doingBusinessAsName: string;
  };
  location: {
    addresses: Address[];
  };
  submitterInformation: {
    submitterFirstName: string;
    submitterLastName: string;
    submitterPhoneNumber: string;
    submitterEmail: string;
  };
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
    clientType: { value: "", text: "" },
    incorporationNumber: "",
    businessName: "",
    goodStanding: "",
    firstName: "",
    lastName: "",
    birthdate: "",
    doingBusinessAsName: "",
  },
  location: {
    addresses: [],
  },
  submitterInformation: {
    submitterFirstName: "",
    submitterLastName: "",
    submitterPhoneNumber: "",
    submitterEmail: "",
  },
};