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

export interface FormUploadFileType {
  content: string;
  contentType: string;
  encoding: string;
  filename: string;
  filesize: number;
}

export interface CommonObjectType {
  [key: string]: any;
}

/* --------------- form schema type ----------------- */
export interface FormSectionSchemaType {
  container: {
    // properties for CollapseCard component
    title: String;
    id: String;
    defaultOpen?: Boolean;
    nextId?: String;
    nextText?: String;
    alwaysOpen?: boolean;
  };
  content: Array<FormComponentSchemaType>;
}

export interface FormComponentSchemaType {
  fieldProps: FormFieldTemplateType;
  type: String;
  disabled?: boolean;
  request?: String;
  options?: Array<CommonObjectType>; // for select, checkbox group, radio group
  addButtonText?: String; // for table
  columns?: Array<CommonObjectType>; // for table and group
}
