export interface FormFieldTemplateType {
  label: string;
  required?: boolean;
  id: string;
  note?: string;
  tooltip?: string;
}

export interface FromSelectOptionType {
  value: { [key: string]: any };
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
