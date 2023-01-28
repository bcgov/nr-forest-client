import type { FormValidationRequiredFieldType } from "../../../core/FormType";

/* --------------- Required Fields --------------------- */
// todo: so far it supports at most 2 level in-depth array, if the depth increase, need to redesign the structure
export const commonRequiredFields: Array<FormValidationRequiredFieldType> = [
  { path: "begin.client_type" },
  { path: "location.address", subFieldId: "street_address" },
  { path: "location.address", subFieldId: "country" },
  { path: "location.address", subFieldId: "province" },
  { path: "location.address", subFieldId: "city" },
  { path: "location.address", subFieldId: "postal_code" },
];

export const businessRequiredFields: Array<FormValidationRequiredFieldType> = [
  { path: "information.business_name" },
  { path: "information.registration_number" },
];

export const individualRequiredFields: Array<FormValidationRequiredFieldType> =
  [{ path: "information.first_name" }, { path: "information.birthdate" }];
