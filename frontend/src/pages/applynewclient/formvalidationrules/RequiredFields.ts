import type { FormValidationRequiredFieldType } from "../../../core/FormType";

/* --------------- Required Fields --------------------- */
// todo: so far it supports at most 2 level in-depth array, if the depth increase, need to redesign the structure
export const commonRequiredFields: Array<FormValidationRequiredFieldType> = [
  { path: "begin.clientType" },
  { path: "location.address", subFieldModelName: "streetAddress" },
  { path: "location.address", subFieldModelName: "country" },
  { path: "location.address", subFieldModelName: "province" },
  { path: "location.address", subFieldModelName: "city" },
  { path: "location.address", subFieldModelName: "postalCode" },
];

export const businessRequiredFields: Array<FormValidationRequiredFieldType> = [
  { path: "information.businessName" },
  { path: "information.registrationNumber" },
];

export const individualRequiredFields: Array<FormValidationRequiredFieldType> =
  [{ path: "information.firstName" }, { path: "information.birthdate" }];
