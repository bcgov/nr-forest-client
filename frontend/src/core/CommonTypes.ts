import { isRef } from "vue";

export interface CodeDescrType {
  value: string;
  text: string;
}

export interface CodeNameType {
  code: string;
  name: string;
}

export interface BusinessSearchResult {
  code: string;
  name: string;
  status: string;
  legalType: string;
}

export interface ValidationMessageType {
  fieldId: string;
  errorMsg: string;
}

export const isEmpty = (receivedValue: any): boolean => {
  const value = isRef(receivedValue) ? receivedValue.value : receivedValue;
  return value === undefined || value === null || value === "";
};

export enum ClientTypeEnum {
  Unknow,
  R,
  U
}

export interface ProgressData {
  kind: string;
  title: string;
  subtitle: string;
}

export interface Submitter {
  firstName: string;
  lastName: string;
  email: string;
  bceidBusinessName: string;
  userId: string | undefined;
}