/* -------------- form component type ----------------- */
export interface FormFieldTemplateType {
  label: string;
  required?: boolean;
  id: string; // id is required when using tooltip, and need to be unique
  note?: string;
  tooltip?: string;
  errorMsg?: string;
}

export interface FromSelectOptionType {
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
  fieldProps: FormFieldTemplateType;
  type: string;
  disabled?: boolean;
  state?: boolean;
  depend?: {
    fieldId: string;
    value: string | number | boolean;
  };
  options?:
    | Array<FromSelectOptionType>
    | Array<FormCheckBoxGroupOptionType>
    | Array<FormRadioGroupOptionType>; // for select, checkbox group, radio group
  addButtonText?: string; // for table
  deleteButtonText?: string; // for group
  columns?: Array<CommonObjectType>; // for table and group
}

export interface FormValidationResultType {
  [key: string]: Array<{
    fieldId: string;
    columnId?: string;
    errorMsg: string;
  }>;
} // {container_id: [{fieldId, columnId, errorMsg}]}
