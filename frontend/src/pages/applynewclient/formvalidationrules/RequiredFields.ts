import type { FormValidationRequiredFieldType } from "../../../core/FormType";

/* --------------- Required Fields --------------------- */
export const commonRequiredFields: Array<FormValidationRequiredFieldType> = [
  { containerId: "begin", fieldId: "client_type" },
  { containerId: "contact", fieldId: "address", subFieldId: "stree_address" },
  { containerId: "contact", fieldId: "address", subFieldId: "country" },
  { containerId: "contact", fieldId: "address", subFieldId: "province" },
  { containerId: "contact", fieldId: "address", subFieldId: "city" },
  { containerId: "contact", fieldId: "address", subFieldId: "postal_code" },
];

export const businessRequiredFields: Array<FormValidationRequiredFieldType> = [
  { containerId: "information", fieldId: "business_name" },
  { containerId: "information", fieldId: "registration_number" },
];

export const individualRequiredFields: Array<FormValidationRequiredFieldType> =
  [
    { containerId: "information", fieldId: "first_name" },
    { containerId: "information", fieldId: "birthdate" },
  ];
