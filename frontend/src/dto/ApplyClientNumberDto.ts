import type { CodeDescrType } from "@/core/CommonTypes";

export interface FormDataDto {
  businessType: {
    clientType: CodeDescrType;
  };
  businessInformation: {
    firstName: string;
    lastName: string;
    birthdate: string;
    incorporationNumber: string;
    doingBusinessAsName: string;
    businessName: null | string;
  };
  location: {
    addresses: Address[];
  };
  submitterInformation: {
    firstName: string;
    lastName: string;
    phoneNumber: string;
    email: string;
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
  emailAddress: string;
}

export const formDataDto: FormDataDto = {
  businessType: {
    clientType: { value: "", text: "" },
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
    addresses: [],
  },
  submitterInformation: {
    firstName: "",
    lastName: "",
    phoneNumber: "",
    email: "",
  },
};