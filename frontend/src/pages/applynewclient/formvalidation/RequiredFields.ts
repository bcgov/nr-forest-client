import type { FormValidationRequiredField } from "../../../core/AppType";

/* --------------- Required Fields --------------------- */
export const commonRequiredFields: Array<FormValidationRequiredField> = [
  { containerId: "begin", fieldId: "client_type" },
  { containerId: "contact", fieldId: "address", subFieldId: "stree_address" },
  { containerId: "contact", fieldId: "address", subFieldId: "country" },
  { containerId: "contact", fieldId: "address", subFieldId: "province" },
  { containerId: "contact", fieldId: "address", subFieldId: "city" },
  { containerId: "contact", fieldId: "address", subFieldId: "postal_code" },
];

export const businessRequiredFields: Array<FormValidationRequiredField> = [
  { containerId: "information", fieldId: "business_name" },
  { containerId: "information", fieldId: "registration_number" },
];

export const individualRequiredFields: Array<FormValidationRequiredField> = [
  { containerId: "information", fieldId: "first_name" },
  { containerId: "information", fieldId: "birthdate" },
];
