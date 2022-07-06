export const backendUrl =
  config.VITE_BACKEND_URL || import.meta.env.VITE_BACKEND_URL;

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

export interface FormGridColumnType {
  label: string;
  required?: boolean;
  id: string;
  tooltip?: string;
  type: string;
  options?: Array<{ value: string; label: string }>;
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
