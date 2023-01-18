import type { FormValidationRequiredFieldType } from "../../../core/FormType";

/* --------------- Required Fields --------------------- */
// todo: so far it supports at most 2 level in-depth array, if the depth increase, need to redesign the structure
export const commonRequiredFields: Array<FormValidationRequiredFieldType> = [
  { path: "begin.client_type" },
  {
    path: "contact.address",
    subPath: "street_address",
    subFieldId: "address_line",
  },
  { path: "contact.address", subFieldId: "country" },
  { path: "contact.address", subFieldId: "province" },
  { path: "contact.address", subFieldId: "city" },
  { path: "contact.address", subFieldId: "postal_code" },
];

export const businessRequiredFields: Array<FormValidationRequiredFieldType> = [
  { path: "information.business_name" },
  { path: "information.registration_number" },
];

export const individualRequiredFields: Array<FormValidationRequiredFieldType> =
  [{ path: "information.first_name" }, { path: "information.birthdate" }];
