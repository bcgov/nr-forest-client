/* -------------- form component type ----------------- */
export interface FormFieldTemplateType {
  label: string;
  required?: boolean;
  id: string; // id is required when using tooltip, and need to be unique
  note?: string;
  tooltip?: string;
}

export interface FromSelectOptionType {
  value: CommonObjectType | string;
  text: string;
}

export interface FormCheckBoxGroupOptionType {
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
