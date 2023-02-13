/* -------------- form component type ----------------- */
export interface FormFieldTemplateType {
  label?: string;
  required?: boolean;
  id: string; // id is required when using tooltip, and need to be unique;
  modelName: string;
  note?: string;
  tooltip?: string;
  errorMsg?: string;
}

export interface FormFieldAutoCompleteTemplateType {
  label?: string;
  required?: boolean;
  id: string; // id is required when using tooltip, and need to be unique;
  dataListId?: string; // id is required when using datalist, and need to be unique, otherwise not needed;
  modelName: string;
  note?: string;
  tooltip?: string;
  errorMsg?: string;
}

export interface FormSelectOptionType {
  value: CommonObjectType | string;
  text: string;
}

export interface FormCheckBoxGroupOptionType {
  code: string;
  text: string;
}

export interface FormRadioGroupOptionType {
  code: string;
  text: string;
}

export interface CommonObjectType {
  [key: string]: any;
}

/* --------------- form schema type ----------------- */
export interface FormSectionContainerType {
  // properties for CollapseCard component
  title: string;
  id: string;
  defaultOpen?: boolean;
  nextId?: string;
  nextText?: string;
  alwaysOpen?: boolean;
}

export interface FormSectionSchemaType {
  container: FormSectionContainerType;
  content: Array<FormComponentSchemaType>;
}

export interface FormComponentSchemaType {
  fieldProps: FormFieldAutoCompleteTemplateType;
  type: string;
  state?: boolean;
  depend?: {
    fieldModelName: string;
    value: string | number | boolean;
  };
  options?:
  | Array<FormSelectOptionType>
  | Array<FormCheckBoxGroupOptionType>
  | Array<FormRadioGroupOptionType>; // for select, checkbox group, radio group
  addButtonText?: string; // for table
  deleteButtonText?: string; // for group
  subfields?: Array<FormComponentSchemaType>; // for table and group
}

/* ---------------- form validation type -------------- */
export interface FormValidationRequiredFieldType {
  path: string;
  subPath?: string;
  subFieldModelName?: string;
}

export interface FormFieldValidationResultType {
  path: string;
  errorMsg: string;
}

export interface FormValidationResultType {
  [key: string]: Array<FormFieldValidationResultType>;
}

export interface FormDisableType {
  [key: string]: Array<string>;
}
